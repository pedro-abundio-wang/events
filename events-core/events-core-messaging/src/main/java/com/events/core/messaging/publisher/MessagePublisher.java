package com.events.core.messaging.publisher;

import com.events.core.messaging.message.Message;

public interface MessagePublisher {
  void publish(String destination, Message message);
}
