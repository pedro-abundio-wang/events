package com.events.cdc.publisher.producer.wrappers.kafka;

import com.events.cdc.publisher.producer.CdcProducer;
import com.events.messaging.kafka.producer.KafkaMessageProducer;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class KafkaCdcProducer implements CdcProducer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final KafkaMessageProducer kafkaMessageProducer;
  private final ConcurrentHashMap<TopicPartition, TopicPartitionSender> topicPartitionSenders =
      new ConcurrentHashMap<>();
  private final boolean enableBatchProcessing;
  private final int batchSize;
  private final MeterRegistry meterRegistry;

  public KafkaCdcProducer(
      KafkaMessageProducer kafkaMessageProducer,
      boolean enableBatchProcessing,
      int batchSize,
      MeterRegistry meterRegistry) {
    this.kafkaMessageProducer = kafkaMessageProducer;
    this.enableBatchProcessing = enableBatchProcessing;
    this.batchSize = batchSize;
    this.meterRegistry = meterRegistry;

    logger.info("enableBatchProcessing={}", enableBatchProcessing);
    logger.info("batchSize={}", batchSize);
  }

  @Override
  public CompletableFuture<?> send(String channel, String key, String message) {
    return getOrCreateTopicPartitionSender(channel, key, meterRegistry)
        .sendMessage(channel, key, message);
  }

  @Override
  public void close() {
    logger.info("closing EventsKafkaCdcProducerWrapper");
    kafkaMessageProducer.close();
    logger.info("closed EventsKafkaCdcProducerWrapper");
  }

  private TopicPartitionSender getOrCreateTopicPartitionSender(
      String channel, String key, MeterRegistry meterRegistry) {
    TopicPartition topicPartition =
        new TopicPartition(channel, kafkaMessageProducer.partitionFor(channel, key));
    return topicPartitionSenders.computeIfAbsent(
        topicPartition,
        tp ->
            new TopicPartitionSender(
                    kafkaMessageProducer, enableBatchProcessing, batchSize, meterRegistry));
  }
}
