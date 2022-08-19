package com.events.cdc.reader.properties;

public abstract class DbLogCdcReaderProperties extends CdcReaderProperties {

  private String offsetStorageTopicName = "offset.storage.topic";
  private Integer transactionLogConnectionTimeoutInMilliseconds = 5000;
  private Integer transactionLogMaxAttemptsForConnection = 100;
  private Long replicationLagMeasuringIntervalInMilliseconds = 10000L;

  public String getOffsetStorageTopicName() {
    return offsetStorageTopicName;
  }

  public void setOffsetStorageTopicName(String offsetStorageTopicName) {
    this.offsetStorageTopicName = offsetStorageTopicName;
  }

  public Integer getTransactionLogConnectionTimeoutInMilliseconds() {
    return transactionLogConnectionTimeoutInMilliseconds;
  }

  public void setTransactionLogConnectionTimeoutInMilliseconds(
      Integer transactionLogConnectionTimeoutInMilliseconds) {
    this.transactionLogConnectionTimeoutInMilliseconds =
        transactionLogConnectionTimeoutInMilliseconds;
  }

  public Integer getTransactionLogMaxAttemptsForConnection() {
    return transactionLogMaxAttemptsForConnection;
  }

  public void setTransactionLogMaxAttemptsForConnection(
      Integer transactionLogMaxAttemptsForConnection) {
    this.transactionLogMaxAttemptsForConnection = transactionLogMaxAttemptsForConnection;
  }

  public Long getReplicationLagMeasuringIntervalInMilliseconds() {
    return replicationLagMeasuringIntervalInMilliseconds;
  }

  public void setReplicationLagMeasuringIntervalInMilliseconds(
      Long replicationLagMeasuringIntervalInMilliseconds) {
    this.replicationLagMeasuringIntervalInMilliseconds =
        replicationLagMeasuringIntervalInMilliseconds;
  }
}
