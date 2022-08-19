package com.events.messaging.kafka.config;

import com.events.messaging.kafka.properties.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfiguration {

  @Bean
  public KafkaProperties kafkaProperties() {
    return new KafkaProperties();
  }
}
