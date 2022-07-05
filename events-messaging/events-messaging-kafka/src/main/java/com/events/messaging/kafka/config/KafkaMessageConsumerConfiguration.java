package com.events.messaging.kafka.config;

import com.events.messaging.kafka.consumer.KafkaMessageConsumer;
import com.events.messaging.kafka.consumer.basic.KafkaConsumerFactory;
import com.events.messaging.kafka.properties.KafkaMessageConsumerProperties;
import com.events.messaging.kafka.properties.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Import({
  KafkaConfiguration.class,
  KafkaMessageConsumerFactoryConfiguration.class,
})
@EnableConfigurationProperties(KafkaMessageConsumerProperties.class)
@Profile("kafka")
public class KafkaMessageConsumerConfiguration {

  @Bean
  public KafkaMessageConsumerProperties kafkaMessageConsumerProperties() {
    return new KafkaMessageConsumerProperties();
  }

  @Bean
  public KafkaMessageConsumer messageConsumerKafka(
      KafkaProperties props,
      KafkaMessageConsumerProperties kafkaMessageConsumerProperties,
      KafkaConsumerFactory kafkaConsumerFactory) {
    return new KafkaMessageConsumer(
        props.getBootstrapServers(), kafkaMessageConsumerProperties, kafkaConsumerFactory);
  }
}
