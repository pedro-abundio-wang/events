package com.events.core.domain.spring.config;

import com.events.core.domain.common.DefaultDomainEventNameMapping;
import com.events.core.domain.common.DomainEventNameMapping;
import com.events.core.domain.subscriber.DomainEventDispatcherFactory;
import com.events.core.messaging.config.MessageSubscriberConfiguration;
import com.events.core.messaging.subscriber.MessageSubscriber;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MessageSubscriberConfiguration.class})
public class DomainEventSubscriberConfiguration {

  @Bean
  public DomainEventDispatcherFactory domainEventDispatcherFactory(
      MessageSubscriber messageSubscriber, DomainEventNameMapping domainEventNameMapping) {
    return new DomainEventDispatcherFactory(messageSubscriber, domainEventNameMapping);
  }

  @Bean
  @ConditionalOnMissingBean
  public DomainEventNameMapping domainEventNameMapping() {
    return new DefaultDomainEventNameMapping();
  }
}
