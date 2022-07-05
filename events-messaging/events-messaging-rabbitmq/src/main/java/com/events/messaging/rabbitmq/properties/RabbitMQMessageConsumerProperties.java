package com.events.messaging.rabbitmq.properties;

import org.springframework.beans.factory.annotation.Value;

public class RabbitMQMessageConsumerProperties extends RabbitMQProperties {

  @Value("${events.cdc.zookeeper.connection.string}")
  private String zkUrl;

  @Value("${events.cdc.rabbitmq.partition.count:#{2}}")
  private int partitionCount;

  public RabbitMQMessageConsumerProperties() {}

  public String getZkUrl() {
    return this.zkUrl;
  }

  public int getPartitionCount() {
    return this.partitionCount;
  }
}
