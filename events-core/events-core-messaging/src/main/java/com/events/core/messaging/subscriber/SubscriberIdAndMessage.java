package com.events.core.messaging.subscriber;

import com.events.core.messaging.message.Message;

public class SubscriberIdAndMessage {

  private final String subscriberId;
  private final Message message;

  public SubscriberIdAndMessage(String subscriberId, Message message) {
    this.subscriberId = subscriberId;
    this.message = message;
  }

  public String getSubscriberId() {
    return subscriberId;
  }

  public Message getMessage() {
    return message;
  }
}
