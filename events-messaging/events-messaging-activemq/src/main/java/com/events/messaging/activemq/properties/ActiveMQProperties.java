package com.events.messaging.activemq.properties;

import org.springframework.beans.factory.annotation.Value;

public class ActiveMQProperties {

  @Value("${events.cdc.activemq.url}")
  private String url;

  @Value("${events.cdc.activemq.user:#{null}}")
  private String user;

  @Value("${events.cdc.activemq.password:#{null}}")
  private String password;

  public String getUrl() {
    return url;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }
}
