package com.events.common.jdbc.schema;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class JdbcUrl {

  private final String host;

  private final int port;

  private final String database;

  public JdbcUrl(String host, int port, String database) {
    this.host = host;
    this.port = port;
    this.database = database;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getDatabase() {
    return database;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
