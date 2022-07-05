package com.events.cdc.connector.postgres.wal;

import com.events.cdc.reader.status.CdcReaderProcessingStatus;
import com.events.cdc.reader.status.CdcReaderProcessingStatusService;
import com.events.common.util.WaitUtil;
import org.postgresql.replication.LogSequenceNumber;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.UUID;

public class PostgresWalCdcReaderProcessingStatusService
    implements CdcReaderProcessingStatusService {

  private final JdbcTemplate jdbcTemplate;
  private volatile long endingOffsetOfLastProcessedEvent;
  private long currentWalPosition;
  private final String additionalSlotName;
  private final WaitUtil waitUtil;

  public PostgresWalCdcReaderProcessingStatusService(
      DataSource dataSource,
      String additionalSlotName,
      long waitForOffsetSyncTimeoutInMilliseconds) {
    jdbcTemplate = new JdbcTemplate(dataSource);
    this.additionalSlotName = additionalSlotName;
    waitUtil = new WaitUtil(waitForOffsetSyncTimeoutInMilliseconds);
  }

  @Override
  public CdcReaderProcessingStatus getCurrentStatus() {
    checkCurrentWalOffsetAndWaitForSyncWithOffsetOfLastProcessedEvent();
    return new CdcReaderProcessingStatus(endingOffsetOfLastProcessedEvent, currentWalPosition);
  }

  @Override
  public void saveEndingOffsetOfLastProcessedEvent(long endingOffsetOfLastProcessedEvent) {
    this.endingOffsetOfLastProcessedEvent = endingOffsetOfLastProcessedEvent;
  }

  private synchronized void checkCurrentWalOffsetAndWaitForSyncWithOffsetOfLastProcessedEvent() {
    if (waitUtil.start()) {
      currentWalPosition = getCurrentWalPosition();
    } else {
      if (currentWalPosition == endingOffsetOfLastProcessedEvent) {
        waitUtil.stop();
      } else {
        waitUtil.tick();
      }
    }
  }

  private long getCurrentWalPosition() {
    try {
      jdbcTemplate.queryForList(
          "SELECT * FROM pg_create_logical_replication_slot(?, 'test_decoding')",
          additionalSlotName);
      jdbcTemplate.execute(
          "CREATE TABLE if not exists events.replication_test (id VARCHAR(64) PRIMARY KEY)");
      jdbcTemplate.execute("DELETE FROM events.replication_test");
      jdbcTemplate.update(
          "INSERT INTO events.replication_test values (?)", UUID.randomUUID().toString());

      String position =
          jdbcTemplate.queryForObject(
              "SELECT lsn FROM pg_logical_slot_get_changes(?, NULL, NULL) ORDER BY lsn DESC limit 1",
              String.class,
              additionalSlotName);

      return LogSequenceNumber.valueOf(position).asLong();
    } finally {
      jdbcTemplate.queryForList("SELECT * FROM pg_drop_replication_slot(?)", additionalSlotName);
    }
  }
}
