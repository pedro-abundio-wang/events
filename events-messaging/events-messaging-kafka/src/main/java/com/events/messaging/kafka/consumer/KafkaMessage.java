package com.events.messaging.kafka.consumer;

public class KafkaMessage {

  private String payload;

  public KafkaMessage(String payload) {
    this.payload = payload;
  }

  public String getPayload() {
    return payload;
  }
}
