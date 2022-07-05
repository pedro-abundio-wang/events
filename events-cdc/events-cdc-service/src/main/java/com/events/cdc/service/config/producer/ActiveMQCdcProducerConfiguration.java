package com.events.cdc.service.config.producer;

import com.events.cdc.publisher.filter.PublishingFilter;
import com.events.cdc.publisher.producer.CdcProducerFactory;
import com.events.cdc.publisher.producer.wrappers.activemq.ActiveMQCdcProducer;
import com.events.messaging.activemq.config.ActiveMQMessageProducerConfiguration;
import com.events.messaging.activemq.producer.ActiveMQMessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("activemq")
@Import(ActiveMQMessageProducerConfiguration.class)
public class ActiveMQCdcProducerConfiguration {
  @Bean
  public PublishingFilter activeMQDuplicatePublishingDetector() {
    return (fileOffset, topic) -> true;
  }

  @Bean
  public CdcProducerFactory activeMQCdcProducerFactory(
      ActiveMQMessageProducer activeMQMessageProducer) {
    return () -> new ActiveMQCdcProducer(activeMQMessageProducer);
  }
}
