package com.events.cdc.service.publisher;

import com.events.cdc.connector.db.transaction.log.messaging.MessageWithDestination;
import com.events.cdc.publisher.CdcPublisher;
import com.events.cdc.publisher.filter.KafkaDuplicatePublishingDetector;
import com.events.cdc.publisher.producer.CdcProducerFactory;
import com.events.cdc.publisher.producer.wrappers.kafka.KafkaCdcProducer;
import com.events.messaging.kafka.producer.KafkaMessageProducer;
import com.events.messaging.kafka.properties.KafkaMessageConsumerProperties;
import com.events.messaging.kafka.properties.KafkaMessageProducerProperties;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;

public abstract class AbstractTransactionLogCdcKafkaTest extends AbstractKafkaCdcPublisherTest {

  @Override
  protected CdcPublisher<MessageWithDestination> createKafkaCdcPublisher() {
    CdcProducerFactory cdcProducerFactory =
        () ->
            new KafkaCdcProducer(
                createEventsKafkaProducer(),
                eventsCdcProperties.isEnableBatchProcessing(),
                eventsCdcProperties.getMaxBatchSize(),
                new LoggingMeterRegistry());

    KafkaDuplicatePublishingDetector duplicatePublishingDetector =
        new KafkaDuplicatePublishingDetector(
            kafkaProperties.getBootstrapServers(),
            KafkaMessageConsumerProperties.empty(),
            kafkaConsumerFactory);

    return new CdcPublisher<>(
        cdcProducerFactory, duplicatePublishingDetector, publishingStrategy, meterRegistry);
  }

  private KafkaMessageProducer createEventsKafkaProducer() {
    return new KafkaMessageProducer(
        kafkaProperties.getBootstrapServers(), KafkaMessageProducerProperties.empty());
  }
}
