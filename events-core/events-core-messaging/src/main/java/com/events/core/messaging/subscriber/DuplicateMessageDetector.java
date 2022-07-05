package com.events.core.messaging.subscriber;

public interface DuplicateMessageDetector {
  boolean isDuplicate(String consumerId, String messageId);
  void doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Runnable callback);
}
