package com.events.cdc.reader;

import com.events.cdc.connector.db.transaction.log.converter.TransactionLogEntryConverter;
import com.events.cdc.connector.db.transaction.log.entry.TransactionLogEntryHandler;
import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogMessage;

import com.events.cdc.reader.metrics.CdcReaderMetrics;
import com.events.cdc.reader.status.CdcReaderProcessingStatusService;
import com.events.common.jdbc.schema.EventsSchema;
import com.events.common.jdbc.schema.SchemaAndTable;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public abstract class CdcReader {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  protected CdcReaderMetrics cdcReaderMetrics;
  protected MeterRegistry meterRegistry;

  protected List<TransactionLogEntryHandler<?>> transactionLogEntryHandlers =
      new CopyOnWriteArrayList<>();

  protected AtomicBoolean running = new AtomicBoolean(false);
  protected CountDownLatch stopCountDownLatch;

  protected String readerName;
  protected Long outboxId;

  private volatile long lastEventTime = 0;

  protected volatile Optional<String> processingError = Optional.empty();

  // TODO: need to design when to use exceptionCallback to relinquish leadership
  protected Optional<Runnable> exceptionCallback = Optional.empty();

  public CdcReader(MeterRegistry meterRegistry, String readerName, Long outboxId) {
    this.readerName = readerName;
    this.outboxId = outboxId;
    this.meterRegistry = meterRegistry;
    cdcReaderMetrics = new CdcReaderMetrics(meterRegistry, readerName);
  }

  public abstract CdcReaderProcessingStatusService getCdcProcessingStatusService();

  public <TLM extends TransactionLogMessage> TransactionLogEntryHandler<TLM> addCdcPipelineHandler(
      EventsSchema eventsSchema,
      String sourceTableName,
      TransactionLogEntryConverter<TLM> transactionLogEntryConverter,
      Function<TLM, CompletableFuture<?>> cdcPublisher) {

    logger.info(
        "Adding transaction log entry handler for schema = {}, table = {}",
        eventsSchema.getEventsDatabaseSchema(),
        sourceTableName);

    SchemaAndTable schemaAndTable =
        new SchemaAndTable(eventsSchema.getEventsDatabaseSchema(), sourceTableName);

    TransactionLogEntryHandler transactionLogEntryHandler =
        new TransactionLogEntryHandler(schemaAndTable, transactionLogEntryConverter, cdcPublisher);

    transactionLogEntryHandlers.add(transactionLogEntryHandler);

    logger.info(
        "Added transaction log entry handler for schema = {}, table = {}",
        eventsSchema.getEventsDatabaseSchema(),
        sourceTableName);

    return transactionLogEntryHandler;
  }

  public void start() {
    stopCountDownLatch = new CountDownLatch(1);
    running.set(true);
    cdcReaderMetrics.setLeader(true);
  }

  public void stop() {
    stop(true);
  }

  public void stop(boolean removeHandlers) {

    if (!running.compareAndSet(true, false)) {
      return;
    }

    try {
      // waiting current processing finished
      stopCountDownLatch.await();
    } catch (InterruptedException e) {
      logger.error(e.getMessage(), e);
    }

    if (removeHandlers) {
      transactionLogEntryHandlers.clear();
    }

    stopMetrics();
  }

  protected void stopMetrics() {
    cdcReaderMetrics.setLeader(false);
  }

  protected void onMessageReceived() {
    cdcReaderMetrics.onMessageProcessed();
    onActivity();
  }

  protected void onActivity() {
    lastEventTime = System.currentTimeMillis();
  }

  protected void handleProcessingFailException(Throwable e) {
    logger.error("Stopping due to exception", e);
    processingError = Optional.of(e.getMessage());
    stopCountDownLatch.countDown();
    throw new RuntimeException(e);
  }

  public Optional<String> getProcessingError() {
    return processingError;
  }

  public String getReaderName() {
    return readerName;
  }

  public Long getOutboxId() {
    return outboxId;
  }

  public long getLastEventTime() {
    return lastEventTime;
  }

  public void setExceptionCallback(Runnable exceptionCallback) {
    this.exceptionCallback = Optional.of(exceptionCallback);
  }
}
