package com.events.core.messaging.subscriber;

import com.events.core.messaging.message.Message;
import com.events.core.messaging.interceptor.MessageInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class PrePostReceiveMessageHandlerDecorator implements MessageHandlerDecorator {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private MessageInterceptor[] messageInterceptors;

  public PrePostReceiveMessageHandlerDecorator(MessageInterceptor[] messageInterceptors) {
    this.messageInterceptors = messageInterceptors;
  }

  @Override
  public void accept(
      SubscriberIdAndMessage subscriberIdAndMessage,
      MessageHandlerDecoratorChain messageHandlerDecoratorChain) {
    Message message = subscriberIdAndMessage.getMessage();
    preReceive(message);
    try {
      messageHandlerDecoratorChain.invokeNext(subscriberIdAndMessage);
    } finally {
      postReceive(message);
    }
  }

  private void preReceive(Message message) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.preReceive(message));
  }

  private void postReceive(Message message) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.postReceive(message));
  }

  @Override
  public int getOrder() {
    return BuiltInMessageHandlerDecoratorOrder.PRE_POST_RECEIVE_MESSAGE_HANDLER_DECORATOR;
  }
}
