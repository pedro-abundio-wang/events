package com.events.messaging.kafka.common;

public class TopicCleaner {

  public static String clean(String topic) {
    return topic.replace("$", "_DLR_");
  }
}
