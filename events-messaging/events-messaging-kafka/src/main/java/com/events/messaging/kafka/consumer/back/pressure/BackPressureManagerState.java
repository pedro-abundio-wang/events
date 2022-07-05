package com.events.messaging.kafka.consumer.back.pressure;

import org.apache.kafka.common.TopicPartition;

import java.util.Set;

public interface BackPressureManagerState {
  BackPressureManagerStateAndActions update(
      Set<TopicPartition> allTopicPartitions, int backlog, BackPressureConfig backPressureConfig);
}
