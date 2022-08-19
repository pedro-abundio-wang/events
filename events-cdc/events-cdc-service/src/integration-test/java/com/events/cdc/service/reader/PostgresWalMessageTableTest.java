package com.events.cdc.service.reader;

import com.events.cdc.connector.postgres.wal.PostgresWalClient;
import com.events.cdc.reader.SourceTableNameSupplier;
import com.events.cdc.service.config.others.EventsCdcProperties;
import com.events.cdc.service.helper.TestHelper;
import com.events.common.id.spring.config.IdGeneratorConfiguration;
import com.events.common.jdbc.schema.EventsSchema;
import com.events.common.jdbc.spring.config.EventsJdbcOperationsConfiguration;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PostgresWalMessageTableTest.TestConfiguration.class)
public class PostgresWalMessageTableTest extends AbstractCdcReaderTest {

  @Configuration
  @EnableAutoConfiguration
  @Import({EventsJdbcOperationsConfiguration.class, IdGeneratorConfiguration.class})
  public static class TestConfiguration {

    @Bean
    public EventsCdcProperties eventsCdcProperties() {
      return new EventsCdcProperties();
    }

    @Bean
    public SourceTableNameSupplier sourceTableNameSupplier(
        EventsCdcProperties eventsCdcProperties) {
      return new SourceTableNameSupplier(
          eventsCdcProperties.getSourceTableName() == null
              ? "message"
              : eventsCdcProperties.getSourceTableName());
    }

    @Bean
    public PostgresWalClient postgreSqlWalClient(
        MeterRegistry meterRegistry,
        @Value("${spring.datasource.url}") String dbUrl,
        @Value("${spring.datasource.username}") String dbUserName,
        @Value("${spring.datasource.password}") String dbPassword,
        DataSource dataSource,
        EventsCdcProperties eventsCdcProperties) {

      return new PostgresWalClient(
          meterRegistry,
          dbUrl,
          dbUserName,
          dbPassword,
          eventsCdcProperties.getPostgresWalIntervalInMilliseconds(),
          eventsCdcProperties.getTransactionLogConnectionTimeoutInMilliseconds(),
          eventsCdcProperties.getMaxAttemptsForTransactionLogConnection(),
          eventsCdcProperties.getPostgresReplicationStatusIntervalInMilliseconds(),
          eventsCdcProperties.getPostgresReplicationSlotName(),
          dataSource,
          eventsCdcProperties.getReaderName(),
          eventsCdcProperties.getReplicationLagMeasuringIntervalInMilliseconds(),
          eventsCdcProperties.getMonitoringRetryIntervalInMilliseconds(),
          eventsCdcProperties.getMonitoringRetryAttempts(),
          eventsCdcProperties.getAdditionalServiceReplicationSlotName(),
          eventsCdcProperties.getWaitForOffsetSyncTimeoutInMilliseconds(),
          new EventsSchema(EventsSchema.DEFAULT_SCHEMA),
          eventsCdcProperties.getOutboxId());
    }

    @Bean
    public TestHelper testHelper() {
      return new TestHelper();
    }
  }
}
