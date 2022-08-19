package com.events.cdc.service.config.others;

import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogFileOffset;

import com.events.messaging.kafka.others.KafkaMessageConsumerSpringProperties;
import com.events.messaging.kafka.properties.KafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class KafkaTransactionLogFileOffsetStore implements TransactionLogFileOffsetStore {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  protected final String dbHistoryTopicName;
  protected KafkaProperties kafkaProperties;
  private KafkaMessageConsumerSpringProperties kafkaMessageConsumerSpringProperties;

  private static final int N = 20;

  public KafkaTransactionLogFileOffsetStore(
      String offsetStorageName,
      KafkaProperties kafkaProperties,
      KafkaMessageConsumerSpringProperties kafkaMessageConsumerSpringProperties) {
    this.kafkaProperties = kafkaProperties;
    this.dbHistoryTopicName = offsetStorageName;
    this.kafkaMessageConsumerSpringProperties =
            kafkaMessageConsumerSpringProperties;
  }

  @Override
  public Optional<TransactionLogFileOffset> getLastTransactionlogFileOffset() {
    try (KafkaConsumer<String, String> consumer = createConsumer()) {
      getPartitionsForTopicRetryOnFail(consumer, 10);
      consumer.subscribe(Arrays.asList(dbHistoryTopicName));

      int count = N;
      TransactionLogFileOffset result = null;
      boolean lastRecordFound = false;
      while (!lastRecordFound) {
        ConsumerRecords<String, String> records = consumer.poll(100);
        if (records.isEmpty()) {
          count--;
          if (count == 0) lastRecordFound = true;
        } else {
          count = N;
          for (ConsumerRecord<String, String> record : records) {
            TransactionLogFileOffset current = handleRecord(record);
            if (current != null) {
              result = current;
            }
          }
        }
      }
      return Optional.ofNullable(result);
    }
  }

  public List<PartitionInfo> getPartitionsForTopicRetryOnFail(
      KafkaConsumer<String, String> consumer, int attempts) {
    try {
      return consumer.partitionsFor(dbHistoryTopicName);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      if (attempts > 0) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException ie) {
          throw new RuntimeException(e);
        }
        return getPartitionsForTopicRetryOnFail(consumer, attempts - 1);
      } else throw new RuntimeException(e);
    }
  }

  private KafkaConsumer<String, String> createConsumer() {
    Properties props = new Properties();
    props.put("bootstrap.servers", kafkaProperties.getBootstrapServers());
    props.put("auto.offset.reset", "earliest");
    props.put("group.id", UUID.randomUUID().toString());
    props.put("enable.auto.commit", "false");
    props.put("auto.commit.interval.ms", "1000");
    props.put("session.timeout.ms", "30000");
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

    props.putAll(kafkaMessageConsumerSpringProperties.getProperties());

    return new KafkaConsumer<>(props);
  }

  protected abstract TransactionLogFileOffset handleRecord(ConsumerRecord<String, String> record);
}
