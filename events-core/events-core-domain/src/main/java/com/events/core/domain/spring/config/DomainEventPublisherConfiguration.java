package com.events.core.domain.spring.config;

import com.events.core.domain.common.DefaultDomainEventNameMapping;
import com.events.core.domain.common.DomainEventNameMapping;
import com.events.core.domain.publisher.DomainEventPublisher;
import com.events.core.domain.publisher.DomainEventPublisherImpl;
import com.events.core.messaging.publisher.MessagePublisher;
import com.events.core.messaging.config.MessagePublisherConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MessagePublisherConfiguration.class})
public class DomainEventPublisherConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public DomainEventNameMapping domainEventNameMapping() {
    return new DefaultDomainEventNameMapping();
  }

  @Bean
  public DomainEventPublisher domainEventPublisher(
      MessagePublisher messagePublisher, DomainEventNameMapping domainEventNameMapping) {
    return new DomainEventPublisherImpl(messagePublisher, domainEventNameMapping);
  }
}
