package com.events.messaging.kafka.consumer.basic;

public interface ConsumerCallbacks {

  void onTryCommitCallback();

  void onCommitedCallback();

  void onCommitFailedCallback();
}
