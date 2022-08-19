package com.events.cdc.service.config.reader.postgresql.wal;

import com.events.cdc.connector.postgres.wal.PostgresWalCdcReaderFactory;
import com.events.cdc.reader.factory.CdcReaderFactory;
import com.events.cdc.reader.connection.pool.ConnectionPoolProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PostgresWalCdcReaderFactoryConfiguration
//    extends DbLogCdcReaderPropertiesConfiguration
{

  @Bean
  public CdcReaderFactory postgresWalCdcReaderFactory(
      MeterRegistry meterRegistry, ConnectionPoolProperties connectionPoolProperties) {

    return new PostgresWalCdcReaderFactory(meterRegistry, connectionPoolProperties);
  }

  //  @Bean
  //  public CdcReaderProperties postgresWalCdcReaderProperties() {
  //
  //    PostgresWalCdcReaderProperties postgresWalCdcPipelineReaderProperties =
  //        createPostgresWalCdcPipelineReaderProperties();
  //
  //    postgresWalCdcPipelineReaderProperties.setType(PostgresWalCdcReaderFactory.TYPE);
  //
  //    initCdcReaderProperties(postgresWalCdcPipelineReaderProperties);
  //    initDbLogCdcReaderProperties(postgresWalCdcPipelineReaderProperties);
  //
  //    return postgresWalCdcPipelineReaderProperties;
  //  }
  //
  //  private PostgresWalCdcReaderProperties createPostgresWalCdcPipelineReaderProperties() {
  //    PostgresWalCdcReaderProperties postgresWalCdcPipelineReaderProperties =
  //        new PostgresWalCdcReaderProperties();
  //
  //    postgresWalCdcPipelineReaderProperties.setPostgresReplicationStatusIntervalInMilliseconds(
  //        eventsCdcConfigurationProperties.getPostgresReplicationStatusIntervalInMilliseconds());
  //    postgresWalCdcPipelineReaderProperties.setPostgresReplicationSlotName(
  //        eventsCdcConfigurationProperties.getPostgresReplicationSlotName());
  //    postgresWalCdcPipelineReaderProperties.setPostgresWalIntervalInMilliseconds(
  //        eventsCdcConfigurationProperties.getPostgresWalIntervalInMilliseconds());
  //
  //    return postgresWalCdcPipelineReaderProperties;
  //  }
}
