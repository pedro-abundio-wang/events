package com.events.cdc.service.publisher;

import com.events.cdc.service.config.others.EventsCdcProperties;
import com.events.cdc.connector.db.transaction.log.messaging.MessageWithDestination;
import com.events.cdc.service.reader.EventSourcing;
import com.events.cdc.service.reader.SourceTableNameSupplier;
import com.events.cdc.publisher.CdcPublisher;
import com.events.cdc.publisher.strategy.PublishingStrategy;
import com.events.common.id.IdGenerator;
import com.events.common.jdbc.schema.EventsSchema;
import com.events.messaging.kafka.properties.KafkaProperties;
import com.events.messaging.kafka.consumer.basic.KafkaConsumerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Collections;

public abstract class AbstractKafkaCdcPublisherTest {

  @Autowired protected IdGenerator idGenerator;

  @Autowired protected EventsSchema eventsSchema;

  @Autowired protected SourceTableNameSupplier sourceTableNameSupplier;

  @Autowired protected MeterRegistry meterRegistry;

  @Autowired protected PublishingStrategy<MessageWithDestination> publishingStrategy;

  @Autowired protected KafkaTestHelper testHelper;

  @Autowired protected KafkaProperties kafkaProperties;

  @Autowired protected EventsCdcProperties eventsCdcProperties;

  @Autowired protected KafkaConsumerFactory kafkaConsumerFactory;

  protected CdcPublisher<MessageWithDestination> cdcPublisher;

  @Before
  public void init() {
    cdcPublisher = createKafkaCdcPublisher();
    cdcPublisher.start();
  }

  @Test
  public void shouldSendEventSourcingToKafka() {

    EventSourcing eventSourcing = testHelper.saveRandomEvent();

    KafkaConsumer<String, byte[]> consumer =
        testHelper.createConsumer(kafkaProperties.getBootstrapServers());
    consumer.partitionsFor(testHelper.getEventTopicName());
    consumer.subscribe(Collections.singletonList(testHelper.getEventTopicName()));

    testHelper.waitForEventInKafka(
        consumer, eventSourcing.getEntityId(), LocalDateTime.now().plusSeconds(40));
    cdcPublisher.stop();
  }

  public abstract void clear();

  protected abstract CdcPublisher<MessageWithDestination> createKafkaCdcPublisher();
}
