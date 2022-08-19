package com.events.core.commands.spring.config;

import com.events.core.commands.subscriber.CommandDispatcherFactory;
import com.events.core.messaging.publisher.MessagePublisher;
import com.events.core.messaging.subscriber.MessageSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandSubscriberConfiguration {
  @Bean
  public CommandDispatcherFactory commandDispatcherFactory(
      MessageSubscriber messageSubscriber, MessagePublisher messagePublisher) {
    return new CommandDispatcherFactory(messageSubscriber, messagePublisher);
  }
}
