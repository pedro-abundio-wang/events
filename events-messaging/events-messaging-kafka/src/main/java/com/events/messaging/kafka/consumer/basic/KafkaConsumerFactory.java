package com.events.messaging.kafka.consumer.basic;

import java.util.Properties;

public interface KafkaConsumerFactory {

  KafkaConsumer makeConsumer(String subscriptionId, Properties consumerProperties);
}
