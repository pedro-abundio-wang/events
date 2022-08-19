package com.events.cdc.service.health;

import com.events.messaging.kafka.consumer.basic.ConsumerPropertiesFactory;
import com.events.messaging.kafka.properties.KafkaMessageConsumerProperties;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Properties;
import java.util.UUID;

public class KafkaHealthCheck extends AbstractHealthCheck {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Value("${events.cdc.kafka.bootstrap.servers}")
  private String kafkaServers;

  @Autowired private KafkaMessageConsumerProperties kafkaMessageConsumerProperties;

  private volatile KafkaConsumer<String, String> consumer;

  @Override
  protected void determineHealth(HealthBuilder builder) {
    if (consumer == null) {
      synchronized (this) {
        if (consumer == null) {
          Properties consumerProperties =
              ConsumerPropertiesFactory.makeDefaultConsumerProperties(
                  kafkaServers, UUID.randomUUID().toString());
          consumerProperties.put("session.timeout.ms", "500");
          consumerProperties.put("request.timeout.ms", "1000");
          consumerProperties.put("heartbeat.interval.ms", "100");
          consumerProperties.putAll(kafkaMessageConsumerProperties.getProperties());
          consumer = new KafkaConsumer<>(consumerProperties);
        }
      }
    }

    try {
      synchronized (this) {
        consumer.partitionsFor("__consumer_offsets");
      }
      builder.addDetail("Connected to Kafka");
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      builder.addError("Connection to kafka failed");
    }
  }
}
