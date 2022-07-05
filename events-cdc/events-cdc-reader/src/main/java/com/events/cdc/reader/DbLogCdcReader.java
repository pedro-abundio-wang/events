package com.events.cdc.reader;

import com.events.cdc.reader.metrics.CdcReaderMonitoringDao;
import com.events.cdc.reader.metrics.DbLogCdcReaderMetrics;
import com.events.common.jdbc.schema.EventsSchema;
import io.micrometer.core.instrument.MeterRegistry;

import javax.sql.DataSource;

public abstract class DbLogCdcReader extends CdcReader {

  protected volatile boolean connected;
  protected DbLogCdcReaderMetrics dbLogCdcReaderMetrics;
  protected CdcReaderMonitoringDao cdcReaderMonitoringDao;

  protected String dataSourceUrl;
  protected String dbUserName;
  protected String dbPassword;

  public DbLogCdcReader(
      MeterRegistry meterRegistry,
      String dataSourceUrl,
      String dbUserName,
      String dbPassword,
      DataSource dataSource,
      String readerName,
      long replicationLagMeasuringIntervalInMilliseconds,
      int monitoringRetryIntervalInMilliseconds,
      int monitoringRetryAttempts,
      EventsSchema monitoringSchema,
      Long outboxId) {

    super(meterRegistry, readerName, outboxId);

    this.dataSourceUrl = dataSourceUrl;
    this.dbUserName = dbUserName;
    this.dbPassword = dbPassword;

    cdcReaderMonitoringDao =
        new CdcReaderMonitoringDao(
            dataSource,
            monitoringSchema,
            monitoringRetryIntervalInMilliseconds,
            monitoringRetryAttempts);

    dbLogCdcReaderMetrics =
        new DbLogCdcReaderMetrics(
            meterRegistry,
            cdcReaderMonitoringDao,
            readerName,
            replicationLagMeasuringIntervalInMilliseconds);
  }

  public boolean isConnected() {
    return connected;
  }

  @Override
  public void start() {
    super.start();
    dbLogCdcReaderMetrics.start();
  }

  @Override
  protected void stopMetrics() {
    super.stopMetrics();
    dbLogCdcReaderMetrics.stop();
  }

  protected void onConnected() {
    dbLogCdcReaderMetrics.onConnected();
    connected = true;
  }

  protected void onDisconnected() {
    dbLogCdcReaderMetrics.onDisconnected();
    connected = false;
  }
}
