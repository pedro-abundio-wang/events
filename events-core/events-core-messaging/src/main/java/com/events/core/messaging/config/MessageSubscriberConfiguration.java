package com.events.core.messaging.config;

import com.events.core.messaging.channel.MessageChannelMapping;
import com.events.core.messaging.consumer.MessageConsumer;
import com.events.core.messaging.subscriber.MessageHandlerDecoratorFactory;
import com.events.core.messaging.subscriber.MessageSubscriber;
import com.events.core.messaging.subscriber.MessageSubscriberImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  MessageConsumerConfiguration.class,
  MessageHandlerDecoratorConfiguration.class,
  DefaultMessageChannelMappingConfiguration.class
})
public class MessageSubscriberConfiguration {

  @Bean
  public MessageSubscriber messageSubscriber(
      MessageConsumer messageConsumer,
      MessageChannelMapping messageChannelMapping,
      MessageHandlerDecoratorFactory messageHandlerDecoratorFactory) {
    return new MessageSubscriberImpl(
        messageChannelMapping, messageConsumer, messageHandlerDecoratorFactory);
  }
}
