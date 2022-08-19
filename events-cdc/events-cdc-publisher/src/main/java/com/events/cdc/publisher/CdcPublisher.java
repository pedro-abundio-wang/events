package com.events.cdc.publisher;

import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogMessage;
import com.events.cdc.publisher.exception.CdcPublishingException;
import com.events.cdc.publisher.filter.PublishingFilter;
import com.events.cdc.publisher.producer.CdcProducer;
import com.events.cdc.publisher.producer.CdcProducerFactory;
import com.events.cdc.publisher.strategy.PublishingStrategy;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CdcPublisher<TLM extends TransactionLogMessage> {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final CdcProducerFactory cdcProducerFactory;
  private final PublishingFilter publishingFilter;
  private final PublishingStrategy<TLM> publishingStrategy;
  private final MeterRegistry meterRegistry;

  private CdcProducer cdcProducer;

  private Counter meterMessagesPublished;
  private Counter meterMessagesDuplicates;
  private DistributionSummary distributionSummaryMessageAge;
  private long publishTimeAccumulator = 0;

  private final AtomicInteger totallyProcessedMessages = new AtomicInteger(0);
  private final AtomicLong timeOfLastProcessedMessage = new AtomicLong(0);

  private volatile boolean lastMessagePublishingFailed;

  public CdcPublisher(
      CdcProducerFactory cdcProducerFactory,
      PublishingFilter publishingFilter,
      PublishingStrategy<TLM> publishingStrategy,
      MeterRegistry meterRegistry) {
    this.cdcProducerFactory = cdcProducerFactory;
    this.publishingFilter = publishingFilter;
    this.publishingStrategy = publishingStrategy;
    this.meterRegistry = meterRegistry;
    initMetrics();
  }

  public boolean isLastMessagePublishingFailed() {
    return lastMessagePublishingFailed;
  }

  private void initMetrics() {
    if (meterRegistry != null) {
      distributionSummaryMessageAge = meterRegistry.summary("cdc.publisher.message.age");
      meterMessagesPublished = meterRegistry.counter("cdc.publisher.messages.sent");
      meterMessagesDuplicates = meterRegistry.counter("cdc.publisher.messages.duplicates");
    }
  }

  public int getTotallyProcessedEventCount() {
    return totallyProcessedMessages.get();
  }

  public long getTimeOfLastProcessedMessage() {
    return timeOfLastProcessedMessage.get();
  }

  public long getPublishTimeAccumulator() {
    return publishTimeAccumulator;
  }

  public void start() {
    logger.info("Starting CdcDataPublisher");
    cdcProducer = cdcProducerFactory.create();
    logger.info("Started CdcDataPublisher");
  }

  public void stop() {
    logger.info("Stopping CdcDataPublisher");
    if (cdcProducer != null) cdcProducer.close();
    logger.info("Stopped CdcDataPublisher");
  }

  public CompletableFuture<?> publishMessage(TLM transactionLogMessage)
      throws CdcPublishingException {

    Objects.requireNonNull(transactionLogMessage);

    String message = publishingStrategy.toJson(transactionLogMessage);

    logger.info("Got record: {}", message);

    String channel = publishingStrategy.channelFor(transactionLogMessage);

    CompletableFuture<Object> result = new CompletableFuture<>();

    if (transactionLogMessage
        .getTransactionLogFileOffset()
        .map(o -> publishingFilter.shouldBePublished(o, channel))
        .orElse(true)) {
      logger.info("sending record: {}", message);
      long t = System.nanoTime();
      publish(transactionLogMessage, channel, message, result);
      meterMessagesPublished.increment();
      publishingStrategy
          .getCreateTime(transactionLogMessage)
          .ifPresent(
              time -> distributionSummaryMessageAge.record(System.currentTimeMillis() - time));
      publishTimeAccumulator += System.nanoTime() - t;
    } else {
      logger.debug("Duplicate event {}", transactionLogMessage);
      meterMessagesDuplicates.increment();
      result.complete(null);
    }
    return result;
  }

  private void publish(
      TLM transactionLogMessage, String channel, String message, CompletableFuture<Object> result) {
    cdcProducer
        .send(channel, publishingStrategy.partitionKeyFor(transactionLogMessage), message)
        .whenComplete(
            (o, throwable) -> {
              if (throwable != null) {
                result.completeExceptionally(throwable);
              } else {
                result.complete(o);
                timeOfLastProcessedMessage.set(System.nanoTime());
                totallyProcessedMessages.incrementAndGet();
              }
            });
  }
}
