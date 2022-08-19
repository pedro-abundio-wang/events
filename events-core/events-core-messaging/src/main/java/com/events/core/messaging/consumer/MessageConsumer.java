package com.events.core.messaging.consumer;

import com.events.core.messaging.subscriber.MessageHandler;
import com.events.core.messaging.subscriber.MessageSubscription;

import java.util.Set;

public interface MessageConsumer {

  MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler);

  String getId();

  void close();
}
