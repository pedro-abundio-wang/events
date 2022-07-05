package com.events.cdc.service.config.producer;

import com.events.cdc.publisher.filter.KafkaDuplicatePublishingDetector;
import com.events.cdc.publisher.filter.PublishingFilter;
import com.events.cdc.publisher.producer.CdcProducerFactory;
import com.events.cdc.publisher.producer.wrappers.kafka.KafkaCdcProducer;
import com.events.cdc.service.properties.EventsCdcServiceProperties;
import com.events.messaging.kafka.config.KafkaMessageConsumerConfiguration;
import com.events.messaging.kafka.config.KafkaMessageProducerConfiguration;
import com.events.messaging.kafka.consumer.basic.KafkaConsumerFactory;
import com.events.messaging.kafka.producer.KafkaMessageProducer;
import com.events.messaging.kafka.properties.KafkaMessageConsumerProperties;
import com.events.messaging.kafka.properties.KafkaProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Import({KafkaMessageProducerConfiguration.class, KafkaMessageConsumerConfiguration.class})
@Profile("kafka")
public class KafkaCdcProducerConfiguration {
  @Bean
  public PublishingFilter kafkaDuplicatePublishingDetector(
      KafkaProperties kafkaProperties,
      KafkaMessageConsumerProperties kafkaMessageConsumerProperties,
      KafkaConsumerFactory kafkaConsumerFactory) {
    return new KafkaDuplicatePublishingDetector(
        kafkaProperties.getBootstrapServers(),
        kafkaMessageConsumerProperties,
        kafkaConsumerFactory);
  }

  @Bean
  public CdcProducerFactory kafkaCdcProducerFactory(
      // EventsCdcProperties eventsCdcProperties,
      EventsCdcServiceProperties eventsCdcServiceProperties,
      MeterRegistry meterRegistry,
      KafkaMessageProducer kafkaMessageProducer) {
    return () ->
        new KafkaCdcProducer(
            kafkaMessageProducer,
            eventsCdcServiceProperties.isEnableBatchProcessing(),
            eventsCdcServiceProperties.getMaxBatchSize(),
            meterRegistry);
  }

  //  @Bean
  //  public DebeziumOffsetStoreFactory debeziumOffsetStoreFactory(
  //      EventsKafkaProperties eventsKafkaProperties,
  //      EventsKafkaConsumerSpringProperties eventsKafkaConsumerSpringProperties) {
  //
  //    return () ->
  //        new DebeziumBinlogOffsetKafkaStore(
  //            eventsKafkaProperties, eventsKafkaConsumerSpringProperties);
  //  }

  //  @Bean
  //  public OffsetStoreFactory postgresWalKafkaOffsetStoreFactory(
  //      EventsKafkaProperties eventsKafkaProperties,
  //      EventsKafkaProducer eventsKafkaProducer,
  //      EventsKafkaConsumerSpringProperties eventsKafkaConsumerSpringProperties) {
  //
  //    return (properties, dataSource, eventsSchema, clientName) ->
  //        new DatabaseKafkaTransactionLogFileOffsetStore(
  //            properties.getOffsetStorageTopicName(),
  //            clientName,
  //            eventsKafkaProducer,
  //            eventsKafkaProperties,
  //            eventsKafkaConsumerSpringProperties);
  //  }
}
