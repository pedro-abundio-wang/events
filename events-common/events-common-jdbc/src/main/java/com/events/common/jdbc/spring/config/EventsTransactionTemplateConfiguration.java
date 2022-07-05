package com.events.common.jdbc.spring.config;

import com.events.common.jdbc.transaction.EventsSpringTransactionTemplate;
import com.events.common.jdbc.transaction.EventsTransactionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class EventsTransactionTemplateConfiguration {
  @Bean
  public EventsTransactionTemplate eventsTransactionTemplate(
      TransactionTemplate transactionTemplate) {
    return new EventsSpringTransactionTemplate(transactionTemplate);
  }
}
