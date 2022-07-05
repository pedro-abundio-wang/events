package com.events.messaging.activemq.consumer;

public class ActiveMQSubscription {

  private final Runnable closingCallback;

  public ActiveMQSubscription(Runnable closingCallback) {
    this.closingCallback = closingCallback;
  }

  public void close() {
    closingCallback.run();
  }
}
