package com.events.core.domain.publisher;

import com.events.core.domain.spring.config.DomainEventPublisherConfiguration;
import com.events.core.messaging.config.MessagePublisherConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({DomainEventPublisherConfiguration.class, MessagePublisherConfiguration.class})
public class DomainEventPublisherTestConfiguration {}
