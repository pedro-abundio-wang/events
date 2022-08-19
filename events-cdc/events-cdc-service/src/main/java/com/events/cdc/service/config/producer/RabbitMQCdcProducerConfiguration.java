package com.events.cdc.service.config.producer;

import com.events.cdc.publisher.filter.PublishingFilter;
import com.events.cdc.publisher.producer.CdcProducerFactory;
import com.events.cdc.publisher.producer.wrappers.rabbitmq.RabbitMQCdcProducer;
import com.events.messaging.rabbitmq.config.RabbitMQMessageProducerConfiguration;
import com.events.messaging.rabbitmq.producer.RabbitMQMessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("rabbitmq")
@Import(RabbitMQMessageProducerConfiguration.class)
public class RabbitMQCdcProducerConfiguration {
  @Bean
  public PublishingFilter rabbitMQDuplicatePublishingDetector() {
    return (fileOffset, topic) -> true;
  }

  @Bean
  public CdcProducerFactory rabbitMQCdcProducerFactory(
      RabbitMQMessageProducer rabbitMQMessageProducer) {
    return () -> new RabbitMQCdcProducer(rabbitMQMessageProducer);
  }
}
