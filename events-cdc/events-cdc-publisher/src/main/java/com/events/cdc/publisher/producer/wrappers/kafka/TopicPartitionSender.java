package com.events.cdc.publisher.producer.wrappers.kafka;

import com.events.messaging.kafka.common.BinaryMessageEncoding;
import com.events.messaging.kafka.common.KafkaMultiMessage;
import com.events.messaging.kafka.common.KafkaMultiMessageConverter;
import com.events.messaging.kafka.producer.KafkaMessageProducer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class TopicPartitionSender {

  private final KafkaMessageProducer kafkaMessageProducer;
  private final ConcurrentLinkedQueue<TopicPartitionMessage> messages =
      new ConcurrentLinkedQueue<>();
  private final AtomicReference<TopicPartitionSenderState> state =
      new AtomicReference<>(TopicPartitionSenderState.IDLE);
  private final boolean enableBatchProcessing;
  private final int batchSize;
  private final MeterRegistry meterRegistry;
  private final Executor executor = Executors.newCachedThreadPool();
  private final Timer sendDurationTimer;
  private AtomicLong timeOfLastProcessedMessage;

  public TopicPartitionSender(
      KafkaMessageProducer kafkaMessageProducer,
      boolean enableBatchProcessing,
      int batchSize,
      MeterRegistry meterRegistry) {
    this.kafkaMessageProducer = kafkaMessageProducer;
    this.enableBatchProcessing = enableBatchProcessing;
    this.batchSize = batchSize;
    this.meterRegistry = meterRegistry;
    this.sendDurationTimer = meterRegistry.timer("events.cdc.kafka.send.duration");
  }

  public CompletableFuture<?> sendMessage(String topic, String key, String body) {

    TopicPartitionMessage topicPartitionMessage = new TopicPartitionMessage(topic, key, body);

    if (state.get() == TopicPartitionSenderState.ERROR) {
      throw new RuntimeException("Sender is in error state, publishing is not possible.");
    } else {
      messages.add(topicPartitionMessage);

      if (state.compareAndSet(TopicPartitionSenderState.IDLE, TopicPartitionSenderState.SENDING)) {
        sendMessage();
      }
    }

    return topicPartitionMessage.getFuture();
  }

  private void sendMessage() {
    if (!messages.isEmpty()) {
      sendNextMessage();
    } else {
      state.set(TopicPartitionSenderState.IDLE);

      /*
        Additional check is necessary because,

        there can be the case when we got null message and not yet turned state to IDLE.
        But other thread added message to queue (just after we got null message) and tried to turn state to SENDING.
        Because state is not yet IDLE, 'sendMessage' will not be invoked.
        Then state will be turned to IDLE.
        So message will stay in queue until next 'sendMessage' invocation.
      */

      if (!messages.isEmpty()
          && state.compareAndSet(
              TopicPartitionSenderState.IDLE, TopicPartitionSenderState.SENDING)) {
        sendMessage();
      }
    }
  }

  private void sendNextMessage() {
    if (enableBatchProcessing) {
      sendMessageBatch();
    } else {
      sendSingleMessage();
    }
  }

  private void sendSingleMessage() {
    TopicPartitionMessage topicPartitionMessage;

    while ((topicPartitionMessage = messages.poll()) != null) {
      TopicPartitionMessage message = topicPartitionMessage;

      kafkaMessageProducer
          .send(
              message.getTopic(),
              message.getKey(),
              BinaryMessageEncoding.stringToBytes(message.getBody()))
          .whenComplete(
              (o, throwable) -> {
                updateMetrics(1);
                if (throwable != null) {
                  state.set(TopicPartitionSenderState.ERROR);
                  message.completeExceptionally(throwable);
                } else {
                  message.complete(o);
                  sendMessage();
                }
              });
    }
  }

  private void sendMessageBatch() {
    List<TopicPartitionMessage> batch = new ArrayList<>();
    KafkaMultiMessageConverter.MessageBuilder messageBuilder =
        new KafkaMultiMessageConverter.MessageBuilder(batchSize);

    String key = null;
    String topic = null;

    while (true) {
      TopicPartitionMessage messageForBatch = messages.peek();

      if (messageForBatch != null
          && messageBuilder.addMessage(
              new KafkaMultiMessage(messageForBatch.getKey(), messageForBatch.getBody()))) {

        messageForBatch = messages.poll();

        // key of the first message is a kafka record key
        if (key == null) {
          key = messageForBatch.getKey();
          topic = messageForBatch.getTopic();
        }

        batch.add(messageForBatch);
      } else {
        break;
      }
    }

    if (batch.isEmpty()) {
      state.set(TopicPartitionSenderState.ERROR);
      throw new RuntimeException("Message is too big to send.");
    }

    long startTime = System.currentTimeMillis();

    kafkaMessageProducer
        .send(topic, key, messageBuilder.toBinaryArray())
        .whenCompleteAsync(
            (o, throwable) -> {
              long endTime = System.currentTimeMillis();
              sendDurationTimer.record(endTime - startTime, TimeUnit.MILLISECONDS);
              updateMetrics(batch.size());
              if (throwable != null) {
                state.set(TopicPartitionSenderState.ERROR);
                batch.forEach(m -> m.completeExceptionally(throwable));
              } else {
                batch.forEach(m -> m.complete(o));
                sendMessage();
              }
            },
            executor);
  }

  private void updateMetrics(int processedEvents) {
    meterRegistry.summary("events.cdc.kafka.batch.size").record(processedEvents);
    meterRegistry.counter("events.cdc.processed.messages").increment(processedEvents);
    if (timeOfLastProcessedMessage == null) {
      timeOfLastProcessedMessage = new AtomicLong(System.nanoTime());
      meterRegistry.gauge("events.cdc.time.of.last.processed.message", timeOfLastProcessedMessage);
    } else {
      timeOfLastProcessedMessage.set(System.nanoTime());
    }
  }
}
