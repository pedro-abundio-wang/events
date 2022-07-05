package com.events.messaging.kafka.consumer.swimlane;

import com.events.messaging.kafka.consumer.basic.MessageConsumerBacklog;

import java.util.concurrent.LinkedBlockingQueue;

public class SwimlaneDispatcherBacklog implements MessageConsumerBacklog {

  private final LinkedBlockingQueue<?> queue;

  public SwimlaneDispatcherBacklog(LinkedBlockingQueue<?> queue) {
    this.queue = queue;
  }

  @Override
  public int size() {
    return queue.size();
  }
}
