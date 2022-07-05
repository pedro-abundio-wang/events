package com.events.cdc.service.config.others;

import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogFileOffset;
import com.events.common.json.mapper.JsonMapper;
import com.events.messaging.kafka.others.KafkaMessageConsumerSpringProperties;
import com.events.messaging.kafka.properties.KafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class DebeziumBinlogKafkaTransactionLogFileOffsetStore extends KafkaTransactionLogFileOffsetStore {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public DebeziumBinlogKafkaTransactionLogFileOffsetStore(
      KafkaProperties kafkaProperties,
      KafkaMessageConsumerSpringProperties kafkaMessageConsumerSpringProperties) {

    super(
        "events.cdc.my-sql-connector.offset.storage",
            kafkaProperties,
            kafkaMessageConsumerSpringProperties);
  }

  @Override
  protected TransactionLogFileOffset handleRecord(ConsumerRecord<String, String> record) {
    try {
      Map<String, Object> keyMap = JsonMapper.fromJson(record.key(), Map.class);

      List<Object> payload = (List<Object>) keyMap.get("payload");
      String connector = (String) payload.get(0);
      String server = ((Map<String, String>) payload.get(1)).get("server");

      if (!"my-sql-connector".equals(connector) || !"my-app-connector".equals(server)) {
        return null;
      }

      Map<String, Object> valueMap = JsonMapper.fromJson(record.value(), Map.class);

      String file = (String) valueMap.get("file");
      long position = ((Number) valueMap.get("pos")).longValue();
      int rowsToSkip = valueMap.containsKey("row") ? ((Number) valueMap.get("row")).intValue() : 0;

      return new TransactionLogFileOffset(file, position, rowsToSkip);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public void save(TransactionLogFileOffset transactionLogFileOffset) {
    ;
  }
}
