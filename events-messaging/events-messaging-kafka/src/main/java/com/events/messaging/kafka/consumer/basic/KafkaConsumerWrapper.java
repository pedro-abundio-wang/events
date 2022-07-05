package com.events.messaging.kafka.consumer.basic;

import com.events.messaging.kafka.consumer.KafkaMessageConsumerHandler;
import com.events.messaging.kafka.consumer.back.pressure.BackPressureActions;
import com.events.messaging.kafka.consumer.back.pressure.BackPressureConfig;
import com.events.messaging.kafka.consumer.back.pressure.BackPressureManager;
import com.events.messaging.kafka.properties.KafkaMessageConsumerProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaConsumerWrapper {

  private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerWrapper.class);

  private final String subscriberId;
  private final KafkaMessageConsumerHandler handler;
  private final List<String> topics;
  private final KafkaConsumerFactory kafkaConsumerFactory;
  private final BackPressureConfig backPressureConfig;
  private final AtomicBoolean stopFlag = new AtomicBoolean(false);
  private final Properties consumerProperties;
  private volatile KafkaConsumerState state = KafkaConsumerState.CREATED;

  private volatile boolean closeConsumerOnStop = true;

  private Optional<ConsumerCallbacks> consumerCallbacks = Optional.empty();

  private final long pollTimeout;

  public KafkaConsumerWrapper(
      String subscriberId,
      KafkaMessageConsumerHandler handler,
      List<String> topics,
      String bootstrapServers,
      KafkaMessageConsumerProperties kafkaMessageConsumerProperties,
      KafkaConsumerFactory kafkaConsumerFactory) {
    this.subscriberId = subscriberId;
    this.handler = handler;
    this.topics = topics;
    this.kafkaConsumerFactory = kafkaConsumerFactory;

    this.consumerProperties =
        ConsumerPropertiesFactory.makeDefaultConsumerProperties(bootstrapServers, subscriberId);
    this.consumerProperties.putAll(kafkaMessageConsumerProperties.getProperties());
    this.backPressureConfig = kafkaMessageConsumerProperties.getBackPressure();
    this.pollTimeout = kafkaMessageConsumerProperties.getPollTimeout();
  }

  public void setConsumerCallbacks(Optional<ConsumerCallbacks> consumerCallbacks) {
    this.consumerCallbacks = consumerCallbacks;
  }

  public boolean isCloseConsumerOnStop() {
    return closeConsumerOnStop;
  }

  public void setCloseConsumerOnStop(boolean closeConsumerOnStop) {
    this.closeConsumerOnStop = closeConsumerOnStop;
  }

  public static List<PartitionInfo> verifyTopicExistsBeforeSubscribing(
      KafkaConsumer consumer, String topic) {
    try {
      logger.debug("Verifying Topic {}", topic);
      List<PartitionInfo> partitions = consumer.partitionsFor(topic);
      logger.debug("Got these partitions {} for Topic {}", partitions, topic);
      return partitions;
    } catch (Throwable e) {
      logger.error("Got exception: ", e);
      throw new RuntimeException(e);
    }
  }

  private void maybeCommitOffsets(KafkaConsumer consumer, KafkaMessageProcessor processor) {
    Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = processor.offsetsToCommit();
    if (!offsetsToCommit.isEmpty()) {
      consumerCallbacks.ifPresent(ConsumerCallbacks::onTryCommitCallback);
      logger.debug("Committing offsets {} {}", subscriberId, offsetsToCommit);
      consumer.commitOffsets(offsetsToCommit);
      logger.debug("Committed offsets {}", subscriberId);
      processor.noteOffsetsCommitted(offsetsToCommit);
      consumerCallbacks.ifPresent(ConsumerCallbacks::onCommitedCallback);
    }
  }

  public void start() {
    try {
      KafkaConsumer consumer = kafkaConsumerFactory.makeConsumer(subscriberId, consumerProperties);

      KafkaMessageProcessor processor = new KafkaMessageProcessor(subscriberId, handler);

      BackPressureManager backpressureManager = new BackPressureManager(backPressureConfig);

      for (String topic : topics) {
        verifyTopicExistsBeforeSubscribing(consumer, topic);
      }

      subscribe(consumer);

      new Thread(
              () -> {
                try {
                  runPollingLoop(consumer, processor, backpressureManager);

                  maybeCommitOffsets(consumer, processor);

                  state = KafkaConsumerState.STOPPED;

                  if (closeConsumerOnStop) {
                    consumer.close();
                  }

                } catch (KafkaMessageProcessorFailedException e) {
                  // We are done
                  logger.trace("Terminating since KafkaMessageProcessorFailedException");
                  state = KafkaConsumerState.MESSAGE_HANDLING_FAILED;
                  consumer.close(Duration.of(200, ChronoUnit.MILLIS));
                } catch (Throwable e) {
                  logger.error("Got exception: ", e);
                  state = KafkaConsumerState.FAILED;
                  consumer.close(Duration.of(200, ChronoUnit.MILLIS));
                  throw new RuntimeException(e);
                }
                logger.trace("Stopped in state {}", state);
              },
              "Events-subscriber-" + subscriberId)
          .start();

      state = KafkaConsumerState.STARTED;

    } catch (Exception e) {
      logger.error("Error subscribing", e);
      state = KafkaConsumerState.FAILED_TO_START;
      throw new RuntimeException(e);
    }
  }

  private void subscribe(KafkaConsumer consumer) {
    logger.debug("Subscribing to {} {}", subscriberId, topics);
    consumer.subscribe(topics);
    logger.debug("Subscribed to {} {}", subscriberId, topics);
  }

  private void runPollingLoop(
      KafkaConsumer consumer,
      KafkaMessageProcessor processor,
      BackPressureManager backPressureManager) {
    while (!stopFlag.get()) {
      ConsumerRecords<String, byte[]> records = consumer.poll(Duration.of(100, ChronoUnit.MILLIS));
      if (!records.isEmpty()) logger.debug("Got {} {} records", subscriberId, records.count());
      if (records.isEmpty()) processor.throwFailureException();
      else
        for (ConsumerRecord<String, byte[]> record : records) {
          logger.debug(
              "processing record {} {} {} {} {} {}",
              subscriberId,
              record.topic(),
              record.partition(),
              record.offset(),
              record.key(),
              record.value());
          if (logger.isDebugEnabled())
            logger.debug(
                String.format(
                    "EventsKafkaAggregateSubscriptions subscriber = %s, topic = %s, partition = %d, offset = %d, key = %s, value = %s",
                    subscriberId,
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    record.value()));
          processor.process(record);
        }
      if (!records.isEmpty())
        logger.debug("Processed {} {} records", subscriberId, records.count());

      try {
        maybeCommitOffsets(consumer, processor);
      } catch (Exception e) {
        logger.error("Cannot commit offsets", e);
        consumerCallbacks.ifPresent(ConsumerCallbacks::onCommitFailedCallback);
      }

      if (!records.isEmpty()) logger.debug("To commit {} {}", subscriberId, processor.getPending());

      int backlog = processor.backlog();

      Set<TopicPartition> topicPartitions = new HashSet<>();
      for (ConsumerRecord<String, byte[]> record : records) {
        topicPartitions.add(new TopicPartition(record.topic(), record.partition()));
      }
      BackPressureActions actions = backPressureManager.update(topicPartitions, backlog);

      if (!actions.pause.isEmpty()) {
        logger.info(
            "Subscriber {} pausing {} due to backlog {} > {}",
            subscriberId,
            actions.pause,
            backlog,
            backPressureConfig.getHigh());
        consumer.pause(actions.pause);
      }

      if (!actions.resume.isEmpty()) {
        logger.info(
            "Subscriber {} resuming {} due to backlog {} <= {}",
            subscriberId,
            actions.resume,
            backlog,
            backPressureConfig.getLow());
        consumer.resume(actions.resume);
      }
    }
  }

  public void stop() {
    stopFlag.set(true);
    //    can't call consumer.close(), it is not thread safe,
    //    it can produce java.util.ConcurrentModificationException: KafkaConsumer is not safe for
    // multi-threaded access
  }

  public KafkaConsumerState getState() {
    return state;
  }
}
