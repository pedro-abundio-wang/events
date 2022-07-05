package com.events.messaging.kafka.others;

import com.events.messaging.kafka.properties.KafkaMessageProducerProperties;
import com.events.messaging.kafka.others.KafkaMessageProducerSpringProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(KafkaMessageProducerSpringProperties.class)
public class EventsKafkaProducerSpringPropertiesConfiguration {

  @Bean
  public KafkaMessageProducerProperties eventsKafkaProducerProperties(
      KafkaMessageProducerSpringProperties kafkaMessageProducerSpringProperties) {
    return new KafkaMessageProducerProperties(kafkaMessageProducerSpringProperties.getProperties());
  }
}
