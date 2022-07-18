package com.events.cdc.service.helper;

import com.events.messaging.kafka.common.KafkaMultiMessage;
import com.events.messaging.kafka.common.KafkaMultiMessageConverter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public class KafkaTestHelper extends TestHelper {

  public Producer<String, String> createProducer(String bootstrapServers) {
    Properties props = new Properties();
    props.put("bootstrap.servers", bootstrapServers);
    props.put("acks", "all");
    props.put("retries", 0);
    props.put("batch.size", 16384);
    props.put("linger.ms", 1);
    props.put("buffer.memory", 33554432);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

    return new KafkaProducer<>(props);
  }

  public KafkaConsumer<String, byte[]> createConsumer(String bootstrapServers) {
    Properties props = new Properties();
    props.put("bootstrap.servers", bootstrapServers);
    props.put("auto.offset.reset", "earliest");
    props.put("group.id", UUID.randomUUID().toString());
    props.put("enable.auto.commit", "false");
    props.put("auto.commit.interval.ms", "1000");
    props.put("session.timeout.ms", "30000");
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

    return new KafkaConsumer<>(props);
  }

  public void waitForEventInKafka(
      KafkaConsumer<String, byte[]> consumer, String entityId, LocalDateTime deadline) {
    KafkaMultiMessageConverter kafkaMultiMessageConverter = new KafkaMultiMessageConverter();
    while (LocalDateTime.now().isBefore(deadline)) {
      long millis = ChronoUnit.MILLIS.between(LocalDateTime.now(), deadline);
      ConsumerRecords<String, byte[]> records = consumer.poll(millis);
      if (!records.isEmpty()) {
        for (ConsumerRecord<String, byte[]> record : records) {

          Optional<String> entity;
          if (kafkaMultiMessageConverter.isMultiMessage(record.value())) {
            entity =
                kafkaMultiMessageConverter
                    .convertBytesToMessages(record.value())
                    .getMessages()
                    .stream()
                    .map(KafkaMultiMessage::getKey)
                    .filter(entityId::equals)
                    .findAny();
          } else {
            entity = Optional.of(record.key());
          }

          if (entity.isPresent()) {
            return;
          }
        }
      }
    }
    throw new RuntimeException("entity not found: " + entityId);
  }
}
