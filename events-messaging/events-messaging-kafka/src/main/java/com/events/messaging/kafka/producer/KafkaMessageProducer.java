package com.events.messaging.kafka.producer;

import com.events.messaging.kafka.common.BinaryMessageEncoding;
import com.events.messaging.kafka.properties.KafkaMessageProducerProperties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class KafkaMessageProducer {

  private final Producer<String, byte[]> producer;

  private final StringSerializer stringSerializer = new StringSerializer();

  private final KafkaPartitioner kafkaPartitioner = new KafkaPartitioner();

  public KafkaMessageProducer(
      String bootstrapServers, KafkaMessageProducerProperties kafkaMessageProducerProperties) {

    Properties producerProps = new Properties();
    producerProps.put("bootstrap.servers", bootstrapServers);
    producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    producerProps.put(
        "value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

    producerProps.putAll(kafkaMessageProducerProperties.getProperties());

    producerProps.put("acks", "all");
    producerProps.put("retries", 2147483647);

    producerProps.put("max.in.flight.requests.per.connection", 5);

    // buffer.memory = 32M
    producerProps.put("buffer.memory", 33554432);
    // max.block.ms = 1min
    producerProps.put("max.block.ms", 60000);

    producerProps.put("compression.type", "none");

    producerProps.put("batch.size", 16384);
    producerProps.put("linger.ms", 1);

    producerProps.put("request.timeout.ms", 300);
    producerProps.put("delivery.timeout.ms", 500);

    producer = new KafkaProducer<>(producerProps);
  }

  public CompletableFuture<?> send(String topic, String key, String message) {
    return send(topic, key, BinaryMessageEncoding.stringToBytes(message));
  }

  public CompletableFuture<?> send(String topic, String key, byte[] bytes) {
    return send(new ProducerRecord<>(topic, key, bytes));
  }

  public CompletableFuture<?> send(String topic, int partition, String key, String message) {
    return send(topic, partition, key, BinaryMessageEncoding.stringToBytes(message));
  }

  public CompletableFuture<?> send(String topic, int partition, String key, byte[] bytes) {
    return send(new ProducerRecord<>(topic, partition, key, bytes));
  }

  private CompletableFuture<?> send(ProducerRecord<String, byte[]> producerRecord) {
    CompletableFuture<Object> result = new CompletableFuture<>();
    producer.send(
        producerRecord,
        (metadata, exception) -> {
          if (exception == null) result.complete(metadata);
          else result.completeExceptionally(exception);
        });

    return result;
  }

  public int partitionFor(String channel, String key) {
    return kafkaPartitioner.partition(
        channel, stringSerializer.serialize(channel, key), partitionsFor(channel));
  }

  public List<PartitionInfo> partitionsFor(String channel) {
    return producer.partitionsFor(channel);
  }

  public void close() {
    producer.close(Duration.ofSeconds(1));
  }
}
