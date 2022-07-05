package com.events.messaging.kafka.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("events.cdc.kafka.producer")
public class KafkaMessageProducerProperties {

  Map<String, String> properties = new HashMap<>();

  public KafkaMessageProducerProperties() {}

  public KafkaMessageProducerProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public static KafkaMessageProducerProperties empty() {
    return new KafkaMessageProducerProperties();
  }
}
