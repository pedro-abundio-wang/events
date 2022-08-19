package com.events.core.messaging.config;

import com.events.core.messaging.interceptor.MessageInterceptor;
import com.events.core.messaging.subscriber.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MessageHandlerDecoratorConfiguration {

  @Autowired(required = false)
  private MessageInterceptor[] messageInterceptors = new MessageInterceptor[0];

  @Bean
  public MessageHandlerDecoratorFactory subscribedMessageHandlerChainFactory(
      List<MessageHandlerDecorator> decorators) {
    return new MessageHandlerDecoratorFactory(decorators);
  }

  @Bean
  public PrePostReceiveMessageHandlerDecorator prePostReceiveMessageHandlerDecoratorDecorator() {
    return new PrePostReceiveMessageHandlerDecorator(messageInterceptors);
  }

  @Bean
  public DuplicateDetectingMessageHandlerDecorator duplicateDetectingMessageHandlerDecorator(
      DuplicateMessageDetector duplicateMessageDetector) {
    return new DuplicateDetectingMessageHandlerDecorator(duplicateMessageDetector);
  }

  @Bean
  public PrePostHandlerMessageHandlerDecorator prePostHandlerMessageHandlerDecorator() {
    return new PrePostHandlerMessageHandlerDecorator(messageInterceptors);
  }
}
