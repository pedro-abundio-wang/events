package com.events.messaging.kafka.others;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("events.cdc.kafka.producer")
public class KafkaMessageProducerSpringProperties {

  Map<String, String> properties = new HashMap<>();

  public KafkaMessageProducerSpringProperties() {}

  public KafkaMessageProducerSpringProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public static KafkaMessageProducerSpringProperties empty() {
    return new KafkaMessageProducerSpringProperties();
  }
}
