package com.events.cdc.connector.postgres.wal;

import com.events.cdc.connector.db.transaction.log.entry.TransactionLogEntry;
import com.events.cdc.connector.db.transaction.log.entry.TransactionLogEntryHandler;
import com.events.cdc.connector.db.transaction.log.offset.OffsetProcessor;
import com.events.cdc.reader.DbLogCdcReader;
import com.events.cdc.reader.status.CdcReaderProcessingStatusService;
import com.events.common.jdbc.schema.EventsSchema;
import com.events.common.jdbc.schema.SchemaAndTable;
import com.events.common.json.mapper.JsonMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.postgresql.PGConnection;
import org.postgresql.PGProperty;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;

import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PostgresWalClient extends DbLogCdcReader {

  private final String replicationSlotName;

  private final int maxAttemptsForTransactionLogConnection;
  private final int connectionTimeoutInMilliseconds;

  private final int walIntervalInMilliseconds;
  private final int replicationStatusIntervalInMilliseconds;

  private final PostgresWalTransactionLogEntryExtractor postgresWalTransactionLogEntryExtractor;
  private final PostgresWalCdcReaderProcessingStatusService postgresWalCdcProcessingStatusService;

  private Connection connection;
  private PGReplicationStream stream;
  private OffsetProcessor<LogSequenceNumber> offsetProcessor;

  public PostgresWalClient(
      MeterRegistry meterRegistry,
      String url,
      String user,
      String password,
      int walIntervalInMilliseconds,
      int connectionTimeoutInMilliseconds,
      int maxAttemptsForTransactionLogConnection,
      int replicationStatusIntervalInMilliseconds,
      String replicationSlotName,
      DataSource dataSource,
      String readerName,
      long replicationLagMeasuringIntervalInMilliseconds,
      int monitoringRetryIntervalInMilliseconds,
      int monitoringRetryAttempts,
      String additionalServiceReplicationSlotName,
      long waitForOffsetSyncTimeoutInMilliseconds,
      EventsSchema monitoringSchema,
      Long outboxId) {

    super(
        meterRegistry,
        url,
        user,
        password,
        dataSource,
        readerName,
        replicationLagMeasuringIntervalInMilliseconds,
        monitoringRetryIntervalInMilliseconds,
        monitoringRetryAttempts,
        monitoringSchema,
        outboxId);

    this.walIntervalInMilliseconds = walIntervalInMilliseconds;
    this.connectionTimeoutInMilliseconds = connectionTimeoutInMilliseconds;
    this.maxAttemptsForTransactionLogConnection = maxAttemptsForTransactionLogConnection;
    this.replicationStatusIntervalInMilliseconds = replicationStatusIntervalInMilliseconds;
    this.replicationSlotName = replicationSlotName;
    this.postgresWalTransactionLogEntryExtractor = new PostgresWalTransactionLogEntryExtractor();

    postgresWalCdcProcessingStatusService =
        new PostgresWalCdcReaderProcessingStatusService(
            dataSource,
            additionalServiceReplicationSlotName,
            waitForOffsetSyncTimeoutInMilliseconds);
  }

  @Override
  public CdcReaderProcessingStatusService getCdcProcessingStatusService() {
    return postgresWalCdcProcessingStatusService;
  }

  @Override
  public void start() {
    logger.info("Starting PostgresWalClient");
    super.start();
    connectWithRetriesOnFail();
    logger.info("PostgresWalClient finished processing");
  }

  private void connectWithRetriesOnFail() {
    for (int i = 1; running.get(); i++) {
      try {
        logger.info("trying to connect to postgres wal");
        connectAndRun();
        break;
      } catch (SQLException e) {
        onDisconnected();
        logger.error("connection to postgres wal failed: {}", e.getMessage());
        if (i == maxAttemptsForTransactionLogConnection) {
          handleProcessingFailException(e);
        }
        try {
          Thread.sleep(connectionTimeoutInMilliseconds);
        } catch (InterruptedException ex) {
          handleProcessingFailException(e);
        }
      } catch (Exception e) {
        handleProcessingFailException(e);
      }
    }
    stopCountDownLatch.countDown();
  }

  private void connectAndRun() throws SQLException {

    Properties props = new Properties();
    PGProperty.USER.set(props, dbUserName);
    PGProperty.PASSWORD.set(props, dbPassword);
    PGProperty.ASSUME_MIN_SERVER_VERSION.set(props, "9.4");
    PGProperty.REPLICATION.set(props, "database");
    PGProperty.PREFER_QUERY_MODE.set(props, "simple");

    connection = DriverManager.getConnection(dataSourceUrl, props);

    PGConnection replConnection = connection.unwrap(PGConnection.class);

    stream =
        replConnection
            .getReplicationAPI()
            .replicationStream()
            .logical()
            .withSlotName(replicationSlotName)
            .withSlotOption("include-xids", false)
            .withSlotOption("write-in-chunks", true)
            .withStatusInterval(replicationStatusIntervalInMilliseconds, TimeUnit.MILLISECONDS)
            .start();

    offsetProcessor =
        new OffsetProcessor<>(
            // lsn offset store
            logSequenceNumber -> {
              stream.setAppliedLSN(stream.getLastReceiveLSN());
              stream.setFlushedLSN(stream.getLastReceiveLSN());
              try {
                stream.forceUpdateStatus();
              } catch (SQLException e) {
                throw new RuntimeException(e);
              }
            },
            this::handleProcessingFailException);

    onConnected();

    logger.info("connection to postgres wal succeed");

    StringBuilder messageBuilder = new StringBuilder();

    while (running.get()) {

      ByteBuffer messageBuffer = stream.readPending();

      if (messageBuffer == null) {
        saveOffsetOfLastProcessedEvent();
        logger.debug("Got empty message, sleeping");
        try {
          TimeUnit.MILLISECONDS.sleep(walIntervalInMilliseconds);
        } catch (InterruptedException e) {
          handleProcessingFailException(e);
        }
        continue;
      }

      String messagePart = extractStringFromBuffer(messageBuffer);

      messageBuilder.append(messagePart);

      if (!"]}".equals(messagePart)) {
        continue;
      }

      dbLogCdcReaderMetrics.onTransactionLogEntryProcessed();

      String messageString = messageBuilder.toString();
      messageBuilder.setLength(0);

      logger.info("Got message: {}", messageString);

      PostgresWalMessage postgresWalMessage =
          JsonMapper.fromJson(messageString, PostgresWalMessage.class);

      checkMonitoringChange(postgresWalMessage);

      LogSequenceNumber lastReceivedLSN = stream.getLastReceiveLSN();

      logger.info("received offset: {} == {}", lastReceivedLSN, lastReceivedLSN.asLong());

      List<TransactionLogEntryWithSchemaAndTable> inserts =
          Arrays.stream(postgresWalMessage.getChange())
              .filter(change -> change.getKind().equals("insert"))
              .map(
                  change ->
                      TransactionLogEntryWithSchemaAndTable.make(
                          postgresWalTransactionLogEntryExtractor, change))
              .collect(Collectors.toList());

      transactionLogEntryHandlers.forEach(
          handler ->
              inserts.stream()
                  .filter(entry -> handler.isFor(entry.getSchemaAndTable()))
                  .map(TransactionLogEntryWithSchemaAndTable::getTransactionLogEntry)
                  .forEach(e -> handleTransactionLogEntry(e, handler)));

      saveOffsetOfLastProcessedEvent();
    }

    stopCountDownLatch.countDown();
  }

  private void handleTransactionLogEntry(
      TransactionLogEntry entry, TransactionLogEntryHandler handler) {

    LogSequenceNumber logSequenceNumber = stream.getLastReceiveLSN();
    CompletableFuture<Optional<LogSequenceNumber>> futureOffset = new CompletableFuture<>();
    CompletableFuture<?> future = null;

    try {
      future = handler.publish(entry);
    } catch (Exception e) {
      handleProcessingFailException(e);
    }

    future.whenComplete(
        (o, throwable) -> {
          if (throwable == null) {
            futureOffset.complete(Optional.of(logSequenceNumber));
          } else {
            futureOffset.completeExceptionally(throwable);
            handleProcessingFailException(throwable);
          }
        });

    offsetProcessor.saveOffset(futureOffset);

    onMessageReceived();
  }

  @Override
  public void stop(boolean removeHandlers) {
    logger.info("Stopping PostgresWalClient");
    super.stop(removeHandlers);

    try {
      stream.close();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }

    try {
      connection.close();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    logger.info("Stopped PostgresWalClient");
  }

  private void checkMonitoringChange(PostgresWalMessage postgresWalMessage) {
    Optional<PostgresWalChange> monitoringChange =
        Arrays.stream(postgresWalMessage.getChange())
            .filter(
                change -> {
                  String changeSchema = change.getSchema();
                  String changeTable = change.getTable();
                  return cdcReaderMonitoringDao.isMonitoringTableChange(changeSchema, changeTable);
                })
            .findAny();

    monitoringChange.ifPresent(
        change -> {
          int index = Arrays.asList(change.getColumnnames()).indexOf("last_time");
          dbLogCdcReaderMetrics.onLagMeasurementEventReceived(
              Long.parseLong(change.getColumnvalues()[index]));
          onMessageReceived();
        });
  }

  private String extractStringFromBuffer(ByteBuffer byteBuffer) {
    int offset = byteBuffer.arrayOffset();
    byte[] source = byteBuffer.array();
    int length = source.length - offset;

    return new String(source, offset, length);
  }

  private void saveOffsetOfLastProcessedEvent() {
    if (postgresWalCdcProcessingStatusService != null) {
      postgresWalCdcProcessingStatusService.saveEndingOffsetOfLastProcessedEvent(
          stream.getLastReceiveLSN().asLong());
    }
  }

  private static class TransactionLogEntryWithSchemaAndTable {

    private TransactionLogEntry transactionLogEntry;
    private SchemaAndTable schemaAndTable;

    public TransactionLogEntryWithSchemaAndTable(
        TransactionLogEntry transactionLogEntry, SchemaAndTable schemaAndTable) {
      this.transactionLogEntry = transactionLogEntry;
      this.schemaAndTable = schemaAndTable;
    }

    public TransactionLogEntry getTransactionLogEntry() {
      return transactionLogEntry;
    }

    public SchemaAndTable getSchemaAndTable() {
      return schemaAndTable;
    }

    public static TransactionLogEntryWithSchemaAndTable make(
        PostgresWalTransactionLogEntryExtractor extractor, PostgresWalChange change) {
      TransactionLogEntry transactionLogEntry = extractor.extract(change);
      SchemaAndTable schemaAndTable = new SchemaAndTable(change.getSchema(), change.getTable());
      return new TransactionLogEntryWithSchemaAndTable(transactionLogEntry, schemaAndTable);
    }
  }
}
