package com.events.cdc.service.publisher;

import com.events.cdc.connector.db.transaction.log.converter.TransactionLogEntryToMessageWithDestinationConverter;
import com.events.cdc.connector.db.transaction.log.messaging.MessageWithDestination;
import com.events.cdc.connector.postgres.wal.PostgresWalClient;
import com.events.cdc.publisher.CdcPublisher;
import com.events.cdc.publisher.filter.KafkaDuplicatePublishingDetector;
import com.events.cdc.publisher.producer.CdcProducerFactory;
import com.events.cdc.publisher.producer.wrappers.kafka.KafkaCdcProducer;
import com.events.cdc.publisher.strategy.MessageWithDestinationPublishingStrategy;
import com.events.cdc.publisher.strategy.PublishingStrategy;
import com.events.cdc.reader.SourceTableNameSupplier;
import com.events.cdc.service.config.others.EventsCdcProperties;
import com.events.cdc.service.helper.KafkaTestHelper;
import com.events.common.id.spring.config.IdGeneratorConfiguration;
import com.events.common.jdbc.schema.EventsSchema;
import com.events.common.jdbc.spring.config.EventsJdbcOperationsConfiguration;
import com.events.messaging.kafka.config.KafkaMessageConsumerConfiguration;
import com.events.messaging.kafka.config.KafkaMessageConsumerFactoryConfiguration;
import com.events.messaging.kafka.config.KafkaMessageProducerConfiguration;
import com.events.messaging.kafka.consumer.basic.KafkaConsumerFactory;
import com.events.messaging.kafka.producer.KafkaMessageProducer;
import com.events.messaging.kafka.properties.KafkaMessageConsumerProperties;
import com.events.messaging.kafka.properties.KafkaMessageProducerProperties;
import com.events.messaging.kafka.properties.KafkaProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PostgreSqlWalKafkaCdcPublisherTest.TestConfiguration.class)
@ActiveProfiles("kafka")
public class PostgreSqlWalKafkaCdcPublisherTest extends AbstractKafkaCdcPublisherTest {

  @Autowired private PostgresWalClient postgresWalClient;

  @Override
  public void init() {

    super.init();

    postgresWalClient.addCdcPipelineHandler(
        eventsSchema,
        sourceTableNameSupplier.getSourceTableName(),
        new TransactionLogEntryToMessageWithDestinationConverter(idGenerator),
        cdcPublisher::publishMessage);

    testHelper.runInSeparateThread(postgresWalClient::start);
  }

  @Override
  public void clear() {
    postgresWalClient.stop();
  }

  @Configuration
  @EnableAutoConfiguration
  @Import({
    KafkaMessageProducerConfiguration.class,
    KafkaMessageConsumerConfiguration.class,
    KafkaMessageConsumerFactoryConfiguration.class,
    EventsJdbcOperationsConfiguration.class,
    IdGeneratorConfiguration.class
  })
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
    public KafkaTestHelper testHelper() {
      return new KafkaTestHelper();
    }

    @Bean
    public CdcPublisher<MessageWithDestination> transactionLogCdcKafkaPublisher(
        CdcProducerFactory cdcProducerFactory,
        KafkaProperties kafkaProperties,
        KafkaMessageConsumerProperties kafkaMessageConsumerProperties,
        PublishingStrategy<MessageWithDestination> publishingStrategy,
        MeterRegistry meterRegistry,
        KafkaConsumerFactory kafkaConsumerFactory) {

      return new CdcPublisher<>(
          cdcProducerFactory,
          new KafkaDuplicatePublishingDetector(
              kafkaProperties.getBootstrapServers(),
              kafkaMessageConsumerProperties,
              kafkaConsumerFactory),
          publishingStrategy,
          meterRegistry);
    }

    @Bean
    public KafkaMessageProducer eventsKafkaProducer(
        KafkaProperties kafkaProperties,
        KafkaMessageProducerProperties kafkaMessageProducerProperties) {
      return new KafkaMessageProducer(
          kafkaProperties.getBootstrapServers(), kafkaMessageProducerProperties);
    }

    @Bean
    public CdcProducerFactory cdcProducerFactory(
        KafkaProperties kafkaProperties,
        KafkaMessageProducerProperties kafkaMessageProducerProperties,
        EventsCdcProperties eventsCdcProperties) {
      return () ->
          new KafkaCdcProducer(
              new KafkaMessageProducer(
                  kafkaProperties.getBootstrapServers(), kafkaMessageProducerProperties),
              eventsCdcProperties.isEnableBatchProcessing(),
              eventsCdcProperties.getMaxBatchSize(),
              new LoggingMeterRegistry());
    }

    @Bean
    public PublishingStrategy<MessageWithDestination> publishingStrategy() {
      return new MessageWithDestinationPublishingStrategy();
    }
  }
}
