package com.events.cdc.reader.connection.pool;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "events.cdc.connection")
public class ConnectionPoolProperties {

  public static final int DEFAULT_MINIMUM_IDLE_CONNECTIONS = 1;

  private Map<String, String> properties = new HashMap<>();

  public Map<String, String> getProperties() {
    properties.putIfAbsent("minimumIdle", String.valueOf(DEFAULT_MINIMUM_IDLE_CONNECTIONS));
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
}
