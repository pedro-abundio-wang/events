package com.events.cdc.service.config.others;

import org.springframework.beans.factory.annotation.Value;

public class EventsCdcProperties {

  @Value("${events.cdc.db.user.name:#{null}}")
  private String dbUserName;

  @Value("${events.cdc.db.password:#{null}}")
  private String dbPassword;

  @Value("${events.cdc.offset.storage.topic.name:#{\"offset.storage.topic\"}}")
  private String offsetStorageTopicName;

  @Value("${events.cdc.reader.name:#{null}}")
  private String readerName;

  @Value("${events.cdc.source.table.name:#{null}}")
  private String sourceTableName;

  @Value("${events.cdc.polling.interval.in.milliseconds:#{500}}")
  private int pollingIntervalInMilliseconds;

  @Value("${events.cdc.max.events.per.polling:#{1000}}")
  private int maxEventsPerPolling;

  @Value("${events.cdc.max.attempts.for.polling:#{100}}")
  private int maxAttemptsForPolling;

  @Value("${events.cdc.polling.retry.interval.in.milleseconds:#{500}}")
  private int pollingRetryIntervalInMilliseconds;

  @Value("${events.cdc.leadership.lock.path:#{\"/events.local/cdc/leader\"}}")
  private String leadershipLockPath;

  @Value("${events.cdc.read.old.debezium.db.offset.storage.topic:#{null}}")
  private Boolean readOldDebeziumDbOffsetStorageTopic;

  @Value("${events.cdc.mysql.transaction.log.client.unique.id:#{null}}")
  private Long mySqlTransactionLogClientUniqueId;

  @Value("${events.cdc.transaction.log.connection.timeout.in.milliseconds:#{5000}}")
  private int transactionLogConnectionTimeoutInMilliseconds;

  @Value("${events.cdc.max.attempts.for.transaction.log.connection:#{100}}")
  private int maxAttemptsForTransactionLogConnection;

  @Value("${events.cdc.replication.lag.measuring.interval.in.milliseconds:#{10000}}")
  private Long replicationLagMeasuringIntervalInMilliseconds;

  @Value("${events.cdc.monitoring.retry.interval.in.milliseconds:#{500}}")
  private int monitoringRetryIntervalInMilliseconds;

  @Value("${events.cdc.monitoring.retry.attempts:#{1000}}")
  private int monitoringRetryAttempts;

  @Value("${events.cdc.additional.service.replication.slot.name:#{\"events_offset_control_slot\"}}")
  private String additionalServiceReplicationSlotName;

  @Value("${events.cdc.wait.for.offset.sync.timeout.in.milliseconds:#{60000}}")
  private long waitForOffsetSyncTimeoutInMilliseconds;

  @Value("${events.cdc.offset.store.key:#{null}}")
  private String offsetStoreKey;

  @Value("${events.monitoring.schema:#{\"events\"}}")
  private String monitoringSchema;

  @Value("${events.cdc.kafka.enable.batch.processing:#{false}}")
  private boolean enableBatchProcessing;

  @Value("${events.cdc.kafka.batch.processing.max.batch.size:#{1000000}}")
  private int maxBatchSize;

  @Value("${events.cdc.reader.outboxId:#{null}}")
  private Long outboxId;

  private int postgresWalIntervalInMilliseconds = 500;

  private int postgresReplicationStatusIntervalInMilliseconds = 1000;

  private String postgresReplicationSlotName = "events_slot";

  public String getDbUserName() {
    return dbUserName;
  }

  public String getDbPassword() {
    return dbPassword;
  }

  public String getOffsetStorageTopicName() {
    return offsetStorageTopicName;
  }

  public String getReaderName() {
    return readerName;
  }

  public Long getOutboxId() {
    return outboxId;
  }

  public String getSourceTableName() {
    return sourceTableName;
  }

  public int getPollingIntervalInMilliseconds() {
    return pollingIntervalInMilliseconds;
  }

  public int getMaxEventsPerPolling() {
    return maxEventsPerPolling;
  }

  public int getMaxAttemptsForPolling() {
    return maxAttemptsForPolling;
  }

  public int getPollingRetryIntervalInMilliseconds() {
    return pollingRetryIntervalInMilliseconds;
  }

  public String getLeadershipLockPath() {
    return leadershipLockPath;
  }

  public Boolean getReadOldDebeziumDbOffsetStorageTopic() {
    return readOldDebeziumDbOffsetStorageTopic;
  }

  public Long getMySqlTransactionLogClientUniqueId() {
    return mySqlTransactionLogClientUniqueId;
  }

  public int getTransactionLogConnectionTimeoutInMilliseconds() {
    return transactionLogConnectionTimeoutInMilliseconds;
  }

  public int getMaxAttemptsForTransactionLogConnection() {
    return maxAttemptsForTransactionLogConnection;
  }

  public void setMaxAttemptsForTransactionLogConnection(
      int maxAttemptsForTransactionLogConnection) {
    this.maxAttemptsForTransactionLogConnection = maxAttemptsForTransactionLogConnection;
  }

  public int getPostgresWalIntervalInMilliseconds() {
    return postgresWalIntervalInMilliseconds;
  }

  public void setPostgresWalIntervalInMilliseconds(int postgresWalIntervalInMilliseconds) {
    this.postgresWalIntervalInMilliseconds = postgresWalIntervalInMilliseconds;
  }

  public int getPostgresReplicationStatusIntervalInMilliseconds() {
    return postgresReplicationStatusIntervalInMilliseconds;
  }

  public void setPostgresReplicationStatusIntervalInMilliseconds(
      int postgresReplicationStatusIntervalInMilliseconds) {
    this.postgresReplicationStatusIntervalInMilliseconds =
            postgresReplicationStatusIntervalInMilliseconds;
  }

  public String getPostgresReplicationSlotName() {
    return postgresReplicationSlotName;
  }

  public void setPostgresReplicationSlotName(String postgresReplicationSlotName) {
    this.postgresReplicationSlotName = postgresReplicationSlotName;
  }

  public Long getReplicationLagMeasuringIntervalInMilliseconds() {
    return replicationLagMeasuringIntervalInMilliseconds;
  }

  public int getMonitoringRetryIntervalInMilliseconds() {
    return monitoringRetryIntervalInMilliseconds;
  }

  public int getMonitoringRetryAttempts() {
    return monitoringRetryAttempts;
  }

  public String getAdditionalServiceReplicationSlotName() {
    return additionalServiceReplicationSlotName;
  }

  public long getWaitForOffsetSyncTimeoutInMilliseconds() {
    return waitForOffsetSyncTimeoutInMilliseconds;
  }

  public String getOffsetStoreKey() {
    return offsetStoreKey == null ? readerName : offsetStoreKey;
  }

  public String getMonitoringSchema() {
    return monitoringSchema;
  }

  public boolean isEnableBatchProcessing() {
    return enableBatchProcessing;
  }

  public int getMaxBatchSize() {
    return maxBatchSize;
  }
}
