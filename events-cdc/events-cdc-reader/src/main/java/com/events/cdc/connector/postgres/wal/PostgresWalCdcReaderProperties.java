package com.events.cdc.connector.postgres.wal;

import com.events.cdc.reader.properties.DbLogCdcReaderProperties;

public class PostgresWalCdcReaderProperties extends DbLogCdcReaderProperties {

  private Integer postgresWalIntervalInMilliseconds = 500;
  private Integer postgresReplicationStatusIntervalInMilliseconds = 1000;
  private String postgresReplicationSlotName = "events_slot";
  private String additionalServiceReplicationSlotName = "events_offset_control_slot";
  private long waitForOffsetSyncTimeoutInMilliseconds = 60000;

  public Integer getPostgresWalIntervalInMilliseconds() {
    return postgresWalIntervalInMilliseconds;
  }

  public void setPostgresWalIntervalInMilliseconds(Integer postgresWalIntervalInMilliseconds) {
    this.postgresWalIntervalInMilliseconds = postgresWalIntervalInMilliseconds;
  }

  public Integer getPostgresReplicationStatusIntervalInMilliseconds() {
    return postgresReplicationStatusIntervalInMilliseconds;
  }

  public void setPostgresReplicationStatusIntervalInMilliseconds(
      Integer postgresReplicationStatusIntervalInMilliseconds) {
    this.postgresReplicationStatusIntervalInMilliseconds =
        postgresReplicationStatusIntervalInMilliseconds;
  }

  public String getPostgresReplicationSlotName() {
    return postgresReplicationSlotName;
  }

  public void setPostgresReplicationSlotName(String postgresReplicationSlotName) {
    this.postgresReplicationSlotName = postgresReplicationSlotName;
  }

  public String getAdditionalServiceReplicationSlotName() {
    return additionalServiceReplicationSlotName;
  }

  public void setAdditionalServiceReplicationSlotName(String additionalServiceReplicationSlotName) {
    this.additionalServiceReplicationSlotName = additionalServiceReplicationSlotName;
  }

  public long getWaitForOffsetSyncTimeoutInMilliseconds() {
    return waitForOffsetSyncTimeoutInMilliseconds;
  }

  public void setWaitForOffsetSyncTimeoutInMilliseconds(
      long waitForOffsetSyncTimeoutInMilliseconds) {
    this.waitForOffsetSyncTimeoutInMilliseconds = waitForOffsetSyncTimeoutInMilliseconds;
  }
}
