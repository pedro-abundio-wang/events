package com.events.core.messaging.subscriber;

import java.util.function.BiConsumer;

public interface MessageHandlerDecorator
    extends BiConsumer<SubscriberIdAndMessage, MessageHandlerDecoratorChain> {
  int getOrder();
}
