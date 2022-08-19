package com.events.cdc.service.properties;

import org.springframework.beans.factory.annotation.Value;

public class ZookeeperProperties {

  @Value("${events.cdc.zookeeper.connection.string}")
  private String connectionString;

  public String getConnectionString() {
    return connectionString;
  }
}
