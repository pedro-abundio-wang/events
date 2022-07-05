package com.events.core.messaging.producer;

import com.events.core.messaging.message.Message;

public interface MessageProducer {

  default void send(Message message) {
    String messageId = sendInternal(message);
    setMessageId(message, messageId);
  }

  String sendInternal(Message message);

  default void setMessageId(Message message, String messageId) {
    message.setHeader(Message.ID, messageId);
  }

  default void withContext(Runnable runnable) {
    runnable.run();
  }
}
