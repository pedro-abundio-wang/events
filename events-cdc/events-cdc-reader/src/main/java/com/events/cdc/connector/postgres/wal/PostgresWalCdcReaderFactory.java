package com.events.cdc.connector.postgres.wal;

import com.events.cdc.reader.factory.AbstractCdcReaderFactory;
import com.events.cdc.reader.connection.pool.ConnectionPoolProperties;
import com.events.common.jdbc.schema.EventsSchema;
import io.micrometer.core.instrument.MeterRegistry;

import javax.sql.DataSource;

public class PostgresWalCdcReaderFactory
    extends AbstractCdcReaderFactory<PostgresWalCdcReaderProperties, PostgresWalClient> {

  public static final String TYPE = "postgres-wal";

  public PostgresWalCdcReaderFactory(
      MeterRegistry meterRegistry, ConnectionPoolProperties connectionPoolProperties) {
    super(meterRegistry, connectionPoolProperties);
  }

  @Override
  public boolean supports(String type) {
    return TYPE.equals(type);
  }

  @Override
  public Class<PostgresWalCdcReaderProperties> propertyClass() {
    return PostgresWalCdcReaderProperties.class;
  }

  @Override
  public PostgresWalClient create(PostgresWalCdcReaderProperties cdcReaderProperties) {

    DataSource dataSource = createDataSource(cdcReaderProperties);

    return new PostgresWalClient(
        meterRegistry,
        cdcReaderProperties.getDataSourceUrl(),
        cdcReaderProperties.getDataSourceUserName(),
        cdcReaderProperties.getDataSourcePassword(),
        cdcReaderProperties.getPostgresWalIntervalInMilliseconds(),
        cdcReaderProperties.getTransactionLogConnectionTimeoutInMilliseconds(),
        cdcReaderProperties.getTransactionLogMaxAttemptsForConnection(),
        cdcReaderProperties.getPostgresReplicationStatusIntervalInMilliseconds(),
        cdcReaderProperties.getPostgresReplicationSlotName(),
        dataSource,
        cdcReaderProperties.getReaderName(),
        cdcReaderProperties.getReplicationLagMeasuringIntervalInMilliseconds(),
        cdcReaderProperties.getMonitoringRetryIntervalInMilliseconds(),
        cdcReaderProperties.getMonitoringRetryAttempts(),
        cdcReaderProperties.getAdditionalServiceReplicationSlotName(),
        cdcReaderProperties.getWaitForOffsetSyncTimeoutInMilliseconds(),
        new EventsSchema(cdcReaderProperties.getMonitoringSchema()),
        cdcReaderProperties.getOutboxId());
  }
}
