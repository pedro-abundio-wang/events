package com.events.messaging.rabbitmq.properties;

import com.rabbitmq.client.Address;
import org.springframework.beans.factory.annotation.Value;

public class RabbitMQProperties {

  @Value("${events.cdc.rabbitmq.broker.addresses:#{null}}")
  private String brokerAddresses;

  @Value("${events.cdc.rabbitmq.url:#{null}}")
  private String url;

  public Address[] getBrokerAddresses() {
    if ((url == null && brokerAddresses == null) || (url != null && brokerAddresses != null)) {
      throw new IllegalArgumentException(
          "One of rabbitmq.broker.addresses or rabbitmq.url should be specified");
    }

    return Address.parseAddresses(url == null ? brokerAddresses : url);
  }
}
