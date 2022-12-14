package com.events.cdc.publisher.filter;

import com.events.cdc.connector.db.transaction.log.messaging.EventWithSourcing;
import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogFileOffset;
import com.events.common.json.mapper.JsonMapper;
import com.events.messaging.kafka.common.KafkaMultiMessageConverter;
import com.events.messaging.kafka.consumer.basic.ConsumerPropertiesFactory;
import com.events.messaging.kafka.consumer.basic.KafkaConsumer;
import com.events.messaging.kafka.consumer.basic.KafkaConsumerFactory;
import com.events.messaging.kafka.consumer.basic.KafkaConsumerWrapper;
import com.events.messaging.kafka.properties.KafkaMessageConsumerProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class KafkaDuplicatePublishingDetector implements PublishingFilter {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final Map<String, Optional<TransactionLogFileOffset>> maxOffsetsForTopics =
      new HashMap<>();
  private boolean okToProcess = false;
  private final String kafkaBootstrapServers;
  private final KafkaMessageConsumerProperties kafkaMessageConsumerProperties;
  private final KafkaMultiMessageConverter kafkaMultiMessageConverter =
      new KafkaMultiMessageConverter();
  private final KafkaConsumerFactory kafkaConsumerFactory;

  public KafkaDuplicatePublishingDetector(
      String kafkaBootstrapServers,
      KafkaMessageConsumerProperties kafkaMessageConsumerProperties,
      KafkaConsumerFactory kafkaConsumerFactory) {
    this.kafkaBootstrapServers = kafkaBootstrapServers;
    this.kafkaMessageConsumerProperties = kafkaMessageConsumerProperties;
    this.kafkaConsumerFactory = kafkaConsumerFactory;
  }

  @Override
  public boolean shouldBePublished(
      TransactionLogFileOffset sourceTransactionLogFileOffset, String destinationTopic) {
    if (okToProcess) return true;

    Optional<TransactionLogFileOffset> max =
        maxOffsetsForTopics.computeIfAbsent(destinationTopic, this::fetchMaxBinlogFileOffsetFor);
    logger.info("For topic {} max is {}", destinationTopic, max);

    okToProcess = max.map(sourceTransactionLogFileOffset::isSameOrAfter).orElse(true);

    logger.info(
        "max = {}, sourceBinlogFileOffset = {} okToProcess = {}",
        max,
        sourceTransactionLogFileOffset,
        okToProcess);
    return okToProcess;
  }

  private Optional<TransactionLogFileOffset> fetchMaxBinlogFileOffsetFor(String destinationTopic) {
    String subscriberId =
        "duplicate-checker-" + destinationTopic + "-" + System.currentTimeMillis();
    Properties consumerProperties =
        ConsumerPropertiesFactory.makeDefaultConsumerProperties(
            kafkaBootstrapServers, subscriberId);
    consumerProperties.putAll(kafkaMessageConsumerProperties.getProperties());

    KafkaConsumer consumer = kafkaConsumerFactory.makeConsumer(null, consumerProperties);

    logger.info("fetching maxOffsetFor {}", subscriberId);

    List<PartitionInfo> partitions =
        KafkaConsumerWrapper.verifyTopicExistsBeforeSubscribing(consumer, destinationTopic);

    List<TopicPartition> topicPartitionList =
        partitions.stream()
            .map(p -> new TopicPartition(destinationTopic, p.partition()))
            .collect(toList());
    consumer.assign(topicPartitionList);
    consumer.poll(Duration.ZERO);

    List<ConsumerRecord<String, byte[]>> records = getLastRecords(consumer, topicPartitionList);
    consumer.close();

    return getMaxBinlogFileOffsetFromRecords(records);
  }

  public List<ConsumerRecord<String, byte[]>> getLastRecords(
      KafkaConsumer consumer, List<TopicPartition> topicPartitionList) {

    List<PartitionOffset> offsetsOfLastRecords =
        getOffsetsOfLastRecords(consumer, topicPartitionList);

    logger.info("Seeking to offsetsOfLastRecords={}, {}", topicPartitionList, offsetsOfLastRecords);

    offsetsOfLastRecords.forEach(
        po -> {
          consumer.seek(
              new TopicPartition(topicPartitionList.get(0).topic(), po.partition), po.offset);
        });

    int expectedRecordCount = offsetsOfLastRecords.size();
    return getLastRecords(consumer, topicPartitionList, expectedRecordCount);
  }

  private List<PartitionOffset> getOffsetsOfLastRecords(
      KafkaConsumer consumer, List<TopicPartition> topicPartitionList) {

    Map<TopicPartition, Long> beginningOffsets;
    Map<TopicPartition, Long> endOffsets;

    try {
      org.apache.kafka.clients.consumer.KafkaConsumer kc = getActualKafkaConsumerHack(consumer);
      beginningOffsets = kc.beginningOffsets(topicPartitionList);
      endOffsets = kc.endOffsets(topicPartitionList);
      logger.info(
          "for {} beginning {}, ending {}", topicPartitionList, beginningOffsets, endOffsets);
    } catch (IllegalStateException e) {
      throw new RuntimeException("Error getting offsets " + topicPartitionList, e);
    }

    // Ignore partitions where the beginning == end since that means they are empty

    return endOffsets.entrySet().stream()
        .filter(
            endOffset ->
                !isEmptyPartition(beginningOffsets.get(endOffset.getKey()), endOffset.getValue()))
        .map(
            endOffset ->
                new PartitionOffset(endOffset.getKey().partition(), endOffset.getValue() - 1))
        .collect(toList());
  }

  private boolean isEmptyPartition(Long beginningOffset, Long endOffset) {
    return endOffset.equals(beginningOffset);
  }

  private org.apache.kafka.clients.consumer.KafkaConsumer getActualKafkaConsumerHack(
      KafkaConsumer consumer) {
    Field delegateField = ReflectionUtils.findField(consumer.getClass(), "delegate");
    delegateField.setAccessible(true);
    return (org.apache.kafka.clients.consumer.KafkaConsumer)
        ReflectionUtils.getField(delegateField, consumer);
  }

  private List<ConsumerRecord<String, byte[]>> getLastRecords(
      KafkaConsumer consumer, List<TopicPartition> topicPartitionList, int expectedRecordCount) {
    logger.info("Getting last records: {}", topicPartitionList);

    List<ConsumerRecord<String, byte[]>> records = new ArrayList<>();
    while (records.size() < expectedRecordCount) {
      ConsumerRecords<String, byte[]> consumerRecords =
          consumer.poll(Duration.of(1000, ChronoUnit.MILLIS));
      consumerRecords.forEach(records::add);

      logger.info("Got some last records: {} {}", topicPartitionList, consumerRecords.count());
    }

    logger.info("Got all last records: {} {}", topicPartitionList, records.size());

    return records;
  }

  private Optional<TransactionLogFileOffset> getMaxBinlogFileOffsetFromRecords(
      List<ConsumerRecord<String, byte[]>> records) {
    return records.stream()
        .flatMap(
            record -> {
              logger.info(
                  "Got record: {}, {}, {}", record.topic(), record.partition(), record.offset());

              return kafkaMultiMessageConverter.convertBytesToValues(record.value()).stream()
                  .map(
                      value ->
                          JsonMapper.fromJson(value, EventWithSourcing.class)
                              .getTransactionLogFileOffset());
            })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .max((tlfo1, tlfo2) -> tlfo1.isSameOrAfter(tlfo2) ? 1 : -1);
  }

  static class PartitionOffset {

    public final int partition;
    public final long offset;

    @Override
    public String toString() {
      return "PartitionOffset{" + "partition=" + partition + ", offset=" + offset + '}';
    }

    public PartitionOffset(int partition, long offset) {

      this.partition = partition;
      this.offset = offset;
    }
  }
}
