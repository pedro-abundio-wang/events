package com.events.core.commands.spring.config;

import com.events.core.commands.publisher.CommandPublisher;
import com.events.core.commands.publisher.CommandPublisherImpl;
import com.events.core.messaging.channel.MessageChannelMapping;
import com.events.core.messaging.publisher.MessagePublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandPublisherConfiguration {

  @Bean
  public CommandPublisher commandPublisher(MessagePublisher messagePublisher) {
    return new CommandPublisherImpl(messagePublisher);
  }
}
