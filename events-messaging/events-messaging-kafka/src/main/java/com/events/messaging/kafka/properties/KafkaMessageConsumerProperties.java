package com.events.messaging.kafka.properties;

import com.events.messaging.kafka.consumer.back.pressure.BackPressureConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("events.cdc.kafka.consumer")
public class KafkaMessageConsumerProperties {

  private Map<String, String> properties = new HashMap<>();

  private BackPressureConfig backPressure = new BackPressureConfig();

  private long pollTimeout = 100;

  public KafkaMessageConsumerProperties() {}

  public KafkaMessageConsumerProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public BackPressureConfig getBackPressure() {
    return backPressure;
  }

  public void setBackPressure(BackPressureConfig backPressure) {
    this.backPressure = backPressure;
  }

  public long getPollTimeout() {
    return pollTimeout;
  }

  public void setPollTimeout(long pollTimeout) {
    this.pollTimeout = pollTimeout;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public static KafkaMessageConsumerProperties empty() {
    return new KafkaMessageConsumerProperties();
  }
}
