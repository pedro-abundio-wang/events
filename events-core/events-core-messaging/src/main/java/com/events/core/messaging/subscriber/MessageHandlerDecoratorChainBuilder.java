package com.events.core.messaging.subscriber;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class MessageHandlerDecoratorChainBuilder {

  private final List<MessageHandlerDecorator> handlers = new LinkedList<>();

  public static MessageHandlerDecoratorChainBuilder startingWith(MessageHandlerDecorator mhd) {
    MessageHandlerDecoratorChainBuilder builder = new MessageHandlerDecoratorChainBuilder();
    builder.add(mhd);
    return builder;
  }

  private void add(MessageHandlerDecorator mhd) {
    this.handlers.add(mhd);
  }

  public MessageHandlerDecoratorChainBuilder andThen(MessageHandlerDecorator mhd) {
    this.add(mhd);
    return this;
  }

  public MessageHandlerDecoratorChain andFinally(Consumer<SubscriberIdAndMessage> consumer) {
    return buildChain(handlers, consumer);
  }

  private MessageHandlerDecoratorChain buildChain(
      List<MessageHandlerDecorator> decorators, Consumer<SubscriberIdAndMessage> consumer) {
    if (decorators.isEmpty()) return consumer::accept;
    else {
      MessageHandlerDecorator head = decorators.get(0);
      List<MessageHandlerDecorator> tail = decorators.subList(1, decorators.size());
      return subscriberIdAndMessage ->
          head.accept(subscriberIdAndMessage, buildChain(tail, consumer));
    }
  }
}
