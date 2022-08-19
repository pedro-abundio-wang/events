package com.events.messaging.kafka.consumer.basic;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.*;

public class DefaultKafkaConsumer implements KafkaConsumer {

  private final org.apache.kafka.clients.consumer.KafkaConsumer delegate;

  public static KafkaConsumer create(Properties properties) {
    return new DefaultKafkaConsumer(
        new org.apache.kafka.clients.consumer.KafkaConsumer(properties));
  }

  private DefaultKafkaConsumer(org.apache.kafka.clients.consumer.KafkaConsumer delegate) {
    this.delegate = delegate;
  }

  @Override
  public void assign(Collection<TopicPartition> topicPartitions) {
    delegate.assign(topicPartitions);
  }

  @Override
  public void seekToEnd(Collection<TopicPartition> topicPartitions) {
    delegate.seekToEnd(topicPartitions);
  }

  @Override
  public long position(TopicPartition topicPartition) {
    return delegate.position(topicPartition);
  }

  @Override
  public void seek(TopicPartition topicPartition, long position) {
    delegate.seek(topicPartition, position);
  }

  @Override
  public void subscribe(List<String> topics) {
    delegate.subscribe(new ArrayList<>(topics));
  }

  @Override
  public void commitOffsets(Map<TopicPartition, OffsetAndMetadata> offsets) {
    delegate.commitSync(offsets);
  }

  @Override
  public List<PartitionInfo> partitionsFor(String topic) {
    return delegate.partitionsFor(topic);
  }

  @Override
  public ConsumerRecords<String, byte[]> poll(Duration duration) {
    return delegate.poll(duration);
  }

  @Override
  public void pause(Set<TopicPartition> partitions) {
    delegate.pause(partitions);
  }

  @Override
  public void resume(Set<TopicPartition> partitions) {
    delegate.resume(partitions);
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public void close(Duration duration) {
    delegate.close(duration);
  }
}
