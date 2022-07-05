package com.events.messaging.kafka.others;

import com.events.messaging.kafka.properties.KafkaMessageConsumerProperties;
import com.events.messaging.kafka.others.KafkaMessageConsumerSpringProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(KafkaMessageConsumerSpringProperties.class)
public class EventsKafkaConsumerSpringPropertiesConfiguration {

  @Bean
  public KafkaMessageConsumerProperties eventsKafkaConsumerProperties(
      KafkaMessageConsumerSpringProperties kafkaMessageConsumerSpringProperties) {
    KafkaMessageConsumerProperties kafkaMessageConsumerProperties =
        new KafkaMessageConsumerProperties(kafkaMessageConsumerSpringProperties.getProperties());
    kafkaMessageConsumerProperties.setBackPressure(
        kafkaMessageConsumerSpringProperties.getBackPressure());
    kafkaMessageConsumerProperties.setPollTimeout(
        kafkaMessageConsumerSpringProperties.getPollTimeout());
    return kafkaMessageConsumerProperties;
  }
}
