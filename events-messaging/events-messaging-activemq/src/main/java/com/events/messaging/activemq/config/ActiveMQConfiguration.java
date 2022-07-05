package com.events.messaging.activemq.config;

import com.events.messaging.activemq.properties.ActiveMQProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("activemq")
public class ActiveMQConfiguration {
  @Bean
  public ActiveMQProperties activeMQProperties() {
    return new ActiveMQProperties();
  }
}
