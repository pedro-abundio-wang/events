package com.events.core.messaging.subscriber;

import java.util.Set;

public interface MessageSubscriber {

  MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler);

  String getId();

  void close();
}
