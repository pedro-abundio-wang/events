package com.events.messaging.kafka.properties;

import org.springframework.beans.factory.annotation.Value;

public class KafkaProperties {

  @Value("${events.cdc.kafka.bootstrap.servers}")
  private String bootstrapServers;

  @Value("${events.cdc.kafka.connection.validation.timeout:#{1000}}")
  private long connectionValidationTimeout;

  public String getBootstrapServers() {
    return bootstrapServers;
  }

  public long getConnectionValidationTimeout() {
    return connectionValidationTimeout;
  }
}
