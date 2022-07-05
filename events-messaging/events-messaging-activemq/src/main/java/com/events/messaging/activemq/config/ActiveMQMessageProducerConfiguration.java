package com.events.messaging.activemq.config;

import com.events.messaging.activemq.properties.ActiveMQProperties;
import com.events.messaging.activemq.producer.ActiveMQMessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import java.util.Optional;

@Configuration
@Import({ActiveMQConfiguration.class})
@Profile("activemq")
public class ActiveMQMessageProducerConfiguration {
  @Bean
  public ActiveMQMessageProducer activeMQMessageProducer(ActiveMQProperties activeMQProperties) {
    return new ActiveMQMessageProducer(
        activeMQProperties.getUrl(),
        Optional.ofNullable(activeMQProperties.getUser()),
        Optional.ofNullable(activeMQProperties.getPassword()));
  }
}
