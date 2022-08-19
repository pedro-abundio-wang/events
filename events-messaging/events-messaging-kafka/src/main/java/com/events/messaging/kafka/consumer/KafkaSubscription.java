package com.events.messaging.kafka.consumer;

public class KafkaSubscription {

  private final Runnable closingCallback;

  public KafkaSubscription(Runnable closingCallback) {
    this.closingCallback = closingCallback;
  }

  public void close() {
    closingCallback.run();
  }
}
