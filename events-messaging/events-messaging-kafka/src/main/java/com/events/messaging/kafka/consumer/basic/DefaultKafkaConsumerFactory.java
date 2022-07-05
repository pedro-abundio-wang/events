package com.events.messaging.kafka.consumer.basic;

import java.util.Properties;

public class DefaultKafkaConsumerFactory implements KafkaConsumerFactory {

  @Override
  public KafkaConsumer makeConsumer(String subscriptionId, Properties consumerProperties) {
    return DefaultKafkaConsumer.create(consumerProperties);
  }
}
