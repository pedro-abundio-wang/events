package com.events.messaging.kafka.config;

import com.events.messaging.kafka.consumer.basic.DefaultKafkaConsumerFactory;
import com.events.messaging.kafka.consumer.basic.KafkaConsumerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaMessageConsumerFactoryConfiguration {
  // spring autoconfigure KafkaConsumerFactory same name
  @Bean("defaultKafkaConsumerFactory")
  @ConditionalOnMissingBean
  public KafkaConsumerFactory kafkaConsumerFactory() {
    return new DefaultKafkaConsumerFactory();
  }
}
