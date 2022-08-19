package com.events.cdc.reader.metrics;

import com.events.common.jdbc.schema.EventsSchema;
import com.events.common.jdbc.schema.SchemaAndTable;
import com.events.common.util.DaoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class CdcReaderMonitoringDao {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  private final JdbcTemplate jdbcTemplate;
  private final EventsSchema eventsSchema;
  private final int monitoringRetryIntervalInMilliseconds;
  private final int monitoringRetryAttempts;

  public CdcReaderMonitoringDao(
      DataSource dataSource,
      EventsSchema eventsSchema,
      int monitoringRetryIntervalInMilliseconds,
      int monitoringRetryAttempts) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.eventsSchema = eventsSchema;
    this.monitoringRetryIntervalInMilliseconds = monitoringRetryIntervalInMilliseconds;
    this.monitoringRetryAttempts = monitoringRetryAttempts;
  }

  public void update(String readerName) {

    DaoUtils.handleConnectionLost(
        monitoringRetryAttempts,
        monitoringRetryIntervalInMilliseconds,
        () -> {
          int rows =
              jdbcTemplate.update(
                  String.format(
                      "update %s set last_time = ? where reader_id = ?",
                      eventsSchema.qualifyTable("cdc_monitoring")),
                  System.currentTimeMillis(),
                  readerName);

          if (rows == 0) {
            jdbcTemplate.update(
                String.format(
                    "insert into %s (reader_id, last_time) values (?, ?)",
                    eventsSchema.qualifyTable("cdc_monitoring")),
                readerName,
                System.currentTimeMillis());
          }

          return null;
        },
        () -> {});
  }

  public SchemaAndTable getMonitoringSchemaAndTable() {
    return new SchemaAndTable(eventsSchema.getEventsDatabaseSchema(), "cdc_monitoring");
  }

  public boolean isMonitoringTableChange(String changeSchema, String changeTable) {
    SchemaAndTable expectedSchemaAndTable =
        new SchemaAndTable(eventsSchema.getEventsDatabaseSchema(), "cdc_monitoring");

    return expectedSchemaAndTable.equals(new SchemaAndTable(changeSchema, changeTable));
  }
}
