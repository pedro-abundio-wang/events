package com.events.core.messaging.subscriber;

public interface MessageHandlerDecoratorChain {
  void invokeNext(SubscriberIdAndMessage subscriberIdAndMessage);
}
