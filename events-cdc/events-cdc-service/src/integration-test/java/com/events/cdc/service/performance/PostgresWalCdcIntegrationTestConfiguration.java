package com.events.cdc.service.performance;

import com.events.cdc.connector.db.transaction.log.messaging.EventWithSourcing;
import com.events.cdc.connector.postgres.wal.PostgresWalClient;
import com.events.cdc.publisher.CdcPublisher;
import com.events.cdc.publisher.filter.KafkaDuplicatePublishingDetector;
import com.events.cdc.publisher.producer.CdcProducerFactory;
import com.events.cdc.publisher.producer.wrappers.kafka.KafkaCdcProducer;
import com.events.cdc.publisher.strategy.EventWithSourcingPublishingStrategy;
import com.events.cdc.publisher.strategy.PublishingStrategy;
import com.events.cdc.service.config.others.EventsCdcProperties;
import com.events.cdc.service.properties.ZookeeperProperties;
import com.events.cdc.service.reader.SourceTableNameSupplier;
import com.events.cdc.service.reader.TestHelper;
import com.events.common.id.spring.config.IdGeneratorConfiguration;
import com.events.common.jdbc.schema.EventsSchema;
import com.events.common.jdbc.spring.config.EventsJdbcOperationsConfiguration;
import com.events.messaging.kafka.config.KafkaConfiguration;
import com.events.messaging.kafka.config.KafkaMessageConsumerConfiguration;
import com.events.messaging.kafka.config.KafkaMessageProducerConfiguration;
import com.events.messaging.kafka.consumer.basic.KafkaConsumerFactory;
import com.events.messaging.kafka.producer.KafkaMessageProducer;
import com.events.messaging.kafka.properties.KafkaMessageConsumerProperties;
import com.events.messaging.kafka.properties.KafkaMessageProducerProperties;
import com.events.messaging.kafka.properties.KafkaProperties;
import com.events.messaging.leadership.coordination.LeaderSelectorFactory;
import com.events.messaging.rabbitmq.leadership.ZkLeaderSelector;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

@Configuration
@EnableAutoConfiguration
@Import({
  KafkaConfiguration.class,
  KafkaMessageProducerConfiguration.class,
  KafkaMessageConsumerConfiguration.class,
  EventsJdbcOperationsConfiguration.class,
  IdGeneratorConfiguration.class
})
public class PostgresWalCdcIntegrationTestConfiguration {

  @Bean
  public SourceTableNameSupplier sourceTableNameSupplier(EventsCdcProperties eventsCdcProperties) {
    return new SourceTableNameSupplier(
        eventsCdcProperties.getSourceTableName() == null
            ? "message"
            : eventsCdcProperties.getSourceTableName());
  }

  @Bean
  public EventsCdcProperties eventsCdcConfigurationProperties() {
    return new EventsCdcProperties();
  }

  @Bean
  public LeaderSelectorFactory leaderSelectorFactory(CuratorFramework curatorFramework) {
    return (lockId, leaderId, leaderSelectedCallback, leaderRemovedCallback) ->
        new ZkLeaderSelector(
            curatorFramework, lockId, leaderId, leaderSelectedCallback, leaderRemovedCallback);
  }

  @Bean
  public PostgresWalClient postgresWalClient(
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
  public CdcPublisher<EventWithSourcing> transactionLogBasedCdcKafkaPublisher(
      CdcProducerFactory cdcProducerFactory,
      KafkaProperties kafkaProperties,
      KafkaMessageConsumerProperties kafkaMessageConsumerProperties,
      PublishingStrategy<EventWithSourcing> publishingStrategy,
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
  public PublishingStrategy<EventWithSourcing> publishingStrategy() {
    return new EventWithSourcingPublishingStrategy();
  }

  @Bean
  public ZookeeperProperties eventsZookeeperProperties() {
    return new ZookeeperProperties();
  }

  @Bean
  public CuratorFramework curatorFramework(ZookeeperProperties zookeeperProperties) {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework client =
        CuratorFrameworkFactory.builder()
            .retryPolicy(retryPolicy)
            .connectString(zookeeperProperties.getConnectionString())
            .build();
    client.start();
    return client;
  }

  @Bean
  public TestHelper testHelper() {
    return new TestHelper();
  }
}
