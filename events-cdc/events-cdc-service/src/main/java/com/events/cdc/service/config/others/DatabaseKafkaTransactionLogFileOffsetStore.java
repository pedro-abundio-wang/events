package com.events.cdc.service.config.others;

import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogFileOffset;
import com.events.common.json.mapper.JsonMapper;
import com.events.common.util.CompletableFutureUtil;
import com.events.messaging.kafka.producer.KafkaMessageProducer;
import com.events.messaging.kafka.others.KafkaMessageConsumerSpringProperties;
import com.events.messaging.kafka.properties.KafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class DatabaseKafkaTransactionLogFileOffsetStore extends KafkaTransactionLogFileOffsetStore {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  private final String offsetStoreKey;

  private KafkaMessageProducer kafkaMessageProducer;

  public DatabaseKafkaTransactionLogFileOffsetStore(
      String dbHistoryTopicName,
      String offsetStoreKey,
      KafkaMessageProducer kafkaMessageProducer,
      KafkaProperties kafkaProperties,
      KafkaMessageConsumerSpringProperties kafkaMessageConsumerSpringProperties) {

    super(dbHistoryTopicName, kafkaProperties, kafkaMessageConsumerSpringProperties);

    this.offsetStoreKey = offsetStoreKey;
    this.kafkaMessageProducer = kafkaMessageProducer;
  }

  @Override
  public synchronized void save(TransactionLogFileOffset transactionLogFileOffset) {
    CompletableFuture<?> future =
        kafkaMessageProducer.send(
            dbHistoryTopicName, offsetStoreKey, JsonMapper.toJson(transactionLogFileOffset));

    CompletableFutureUtil.get(future);

    logger.debug("Offset is saved: {}", transactionLogFileOffset);
  }

  @Override
  protected TransactionLogFileOffset handleRecord(ConsumerRecord<String, String> record) {
    if (record.key().equals(offsetStoreKey)) {
      return JsonMapper.fromJson(record.value(), TransactionLogFileOffset.class);
    }
    return null;
  }
}
