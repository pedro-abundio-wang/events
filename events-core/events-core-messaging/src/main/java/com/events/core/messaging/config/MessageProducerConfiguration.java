package com.events.core.messaging.config;

import com.events.common.id.IdGenerator;
import com.events.common.id.spring.config.IdGeneratorConfiguration;
import com.events.common.jdbc.operation.EventsJdbcOperations;
import com.events.common.jdbc.spring.config.EventsJdbcOperationsConfiguration;
import com.events.core.messaging.producer.MessageProducer;
import com.events.core.messaging.producer.jdbc.MessageProducerJdbcImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({EventsJdbcOperationsConfiguration.class, IdGeneratorConfiguration.class})
public class MessageProducerConfiguration {
  @Bean
  @ConditionalOnMissingBean(MessageProducer.class)
  public MessageProducer messageProducer(
      EventsJdbcOperations eventsJdbcOperations, IdGenerator idGenerator) {
    return new MessageProducerJdbcImpl(eventsJdbcOperations, idGenerator);
  }
}
