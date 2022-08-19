package com.events.messaging.kafka.consumer.basic;

public enum KafkaConsumerState {
  MESSAGE_HANDLING_FAILED,
  STARTED,
  FAILED_TO_START,
  STOPPED,
  FAILED,
  CREATED
}
