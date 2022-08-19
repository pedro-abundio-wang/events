package com.events.cdc.reader.factory;

import com.events.cdc.reader.CdcReader;
import com.events.cdc.reader.connection.pool.ConnectionPoolProperties;
import com.events.cdc.reader.connection.pool.DataSourceFactory;
import com.events.cdc.reader.properties.CdcReaderProperties;
import io.micrometer.core.instrument.MeterRegistry;

import javax.sql.DataSource;

public abstract class AbstractCdcReaderFactory<
        PROPERTIES extends CdcReaderProperties, READER extends CdcReader>
    implements CdcReaderFactory<PROPERTIES, READER> {

  protected MeterRegistry meterRegistry;

  // TODO: https://github.com/brettwooldridge/HikariCP
  protected ConnectionPoolProperties connectionPoolProperties;

  public AbstractCdcReaderFactory(
      MeterRegistry meterRegistry, ConnectionPoolProperties connectionPoolProperties) {
    this.meterRegistry = meterRegistry;
    this.connectionPoolProperties = connectionPoolProperties;
  }

  public abstract READER create(PROPERTIES cdcReaderProperties);

  protected DataSource createDataSource(PROPERTIES cdcReaderProperties) {
    return DataSourceFactory.createDataSource(
        cdcReaderProperties.getDataSourceUrl(),
        cdcReaderProperties.getDataSourceDriverClassName(),
        cdcReaderProperties.getDataSourceUserName(),
        cdcReaderProperties.getDataSourcePassword(),
        connectionPoolProperties);
  }
}
