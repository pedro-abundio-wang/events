package com.events.messaging.rabbitmq.config;

import com.events.messaging.rabbitmq.properties.RabbitMQMessageConsumerProperties;
import com.events.messaging.rabbitmq.properties.RabbitMQMessageProducerProperties;
import com.events.messaging.rabbitmq.properties.RabbitMQProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("rabbitmq")
public class RabbitMQConfiguration {

  @Bean
  public RabbitMQProperties rabbitMQConfigurationProperties() {
    return new RabbitMQProperties();
  }

  @Bean
  public RabbitMQMessageProducerProperties rabbitMQMessageProducerProperties() {
    return new RabbitMQMessageProducerProperties();
  }

  @Bean
  public RabbitMQMessageConsumerProperties rabbitMQMessageConsumerProperties() {
    return new RabbitMQMessageConsumerProperties();
  }
}
