package com.events.messaging.kafka.config;

import com.events.messaging.kafka.producer.KafkaMessageProducer;
import com.events.messaging.kafka.properties.KafkaMessageProducerProperties;
import com.events.messaging.kafka.properties.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Import({
  KafkaConfiguration.class,
})
@EnableConfigurationProperties(KafkaMessageProducerProperties.class)
@Profile("kafka")
public class KafkaMessageProducerConfiguration {

  @Bean
  public KafkaMessageProducerProperties kafkaMessageProducerProperties() {
    return new KafkaMessageProducerProperties();
  }

  @Bean
  public KafkaMessageProducer kafkaMessageProducer(
      KafkaProperties kafkaProperties,
      KafkaMessageProducerProperties kafkaMessageProducerProperties) {
    return new KafkaMessageProducer(
        kafkaProperties.getBootstrapServers(), kafkaMessageProducerProperties);
  }
}
