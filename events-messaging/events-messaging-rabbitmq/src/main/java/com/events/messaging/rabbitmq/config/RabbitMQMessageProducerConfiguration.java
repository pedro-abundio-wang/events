package com.events.messaging.rabbitmq.config;

import com.events.messaging.rabbitmq.producer.RabbitMQMessageProducer;
import com.events.messaging.rabbitmq.properties.RabbitMQMessageProducerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("rabbitmq")
@Import({RabbitMQConfiguration.class})
public class RabbitMQMessageProducerConfiguration {
  @Bean
  public RabbitMQMessageProducer rabbitMQMessageProducer(
      RabbitMQMessageProducerProperties rabbitMQMessageProducerProperties) {
    return new RabbitMQMessageProducer(rabbitMQMessageProducerProperties.getBrokerAddresses());
  }
}
