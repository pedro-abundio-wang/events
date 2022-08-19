package com.events.messaging.rabbitmq.consumer;

import com.events.messaging.leadership.coordination.LeaderRemovedCallback;
import com.events.messaging.leadership.coordination.LeaderSelectedCallback;
import com.events.messaging.leadership.coordination.LeadershipController;
import com.events.messaging.partition.management.*;
import com.rabbitmq.client.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Subscription {

  private static final String CONSISTENT_HASH_EXCHANGE_TYPE = "x-consistent-hash";

  // When a queue is bound to a Consistent Hash exchange, the binding key is a number-as-a-string
  // which indicates the binding weight: the number of buckets (sections of the range) that will be
  // associated with the target queue.
  private static final String BUCKET_NUMS = "10";

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final CoordinatorFactory coordinatorFactory;
  private final Coordinator coordinator;

  private final String subscriptionId;
  private final String consumerId;
  private final String subscriberId;

  private final Set<String> channels;
  private final int partitionCount;

  private final BiConsumer<RabbitMQMessage, Runnable> handleMessageCallback;

  private final Map<String, String> consumerTagByQueue = new HashMap<>();

  private final Map<String, Set<Integer>> currentPartitionsByChannel = new HashMap<>();

  private final Connection connection;
  private final Channel consumerChannel;

  private Optional<SubscriptionLifecycleHook> subscriptionLifecycleHook = Optional.empty();
  private Optional<SubscriptionLeaderHook> leaderHook = Optional.empty();
  private Optional<Runnable> closingCallback = Optional.empty();

  public Subscription(
      CoordinatorFactory coordinatorFactory,
      String consumerId,
      String subscriptionId,
      Connection connection,
      String subscriberId,
      Set<String> channels,
      int partitionCount,
      BiConsumer<RabbitMQMessage, Runnable> handleMessageCallback) {
    this.logger.info(
        "Creating subscription for channels {} and partition count {}. {}",
        channels,
        partitionCount,
        this.identificationInformation());
    this.coordinatorFactory = coordinatorFactory;
    this.consumerId = consumerId;
    this.subscriptionId = subscriptionId;
    this.connection = connection;
    this.subscriberId = subscriberId;
    this.channels = channels;
    this.partitionCount = partitionCount;
    this.handleMessageCallback = handleMessageCallback;
    channels.forEach(
        (channelName) -> this.currentPartitionsByChannel.put(channelName, new HashSet<>()));
    this.logger.info("Creating channel");
    this.consumerChannel = this.createRabbitMQChannel();
    this.logger.info("Created channel");
    this.coordinator =
        this.createCoordinator(
            subscriptionId,
            subscriberId,
            channels,
            this::leaderSelected,
            this::leaderRemoved,
            this::assignmentUpdated);
    this.logger.info(
        "Created subscription for channels {} and partition count {}. {}",
        channels,
        partitionCount,
        this.identificationInformation());
  }

  public void setSubscriptionLifecycleHook(SubscriptionLifecycleHook subscriptionLifecycleHook) {
    this.subscriptionLifecycleHook = Optional.ofNullable(subscriptionLifecycleHook);
  }

  public void setLeaderHook(SubscriptionLeaderHook leaderHook) {
    this.leaderHook = Optional.ofNullable(leaderHook);
  }

  public void setClosingCallback(Runnable closingCallback) {
    this.closingCallback = Optional.of(closingCallback);
  }

  protected Coordinator createCoordinator(
      String subscriptionId,
      String subscriberId,
      Set<String> channels,
      LeaderSelectedCallback leaderSelectedCallback,
      LeaderRemovedCallback leaderRemovedCallback,
      Consumer<Assignment> assignmentUpdatedCallback) {
    return this.coordinatorFactory.makeCoordinator(
        subscriberId,
        channels,
        subscriptionId,
        assignmentUpdatedCallback,
        ZkUtil.pathForLeader(subscriberId),
        leaderSelectedCallback,
        leaderRemovedCallback);
  }

  public synchronized void close() {
    if (this.consumerChannel.isOpen()) {
      this.logger.info("Closing coordinator: subscription = {}", this.identificationInformation());
      this.coordinator.close();

      try {
        this.logger.info(
            "Closing consumer channel: subscription = {}", this.identificationInformation());
        this.consumerChannel.close();
      } catch (TimeoutException | IOException e) {
        this.logger.error(
            "Closing consumer failed: subscription = {}", this.identificationInformation());
        this.logger.error("Closing consumer failed", e);
      }

      this.closingCallback.ifPresent(Runnable::run);
      this.logger.info("Subscription is closed. {}", this.identificationInformation());
    }
  }

  private Channel createRabbitMQChannel() {
    try {
      this.logger.info("Creating channel");
      Channel channel = this.connection.createChannel();
      this.logger.info("Created channel");
      return channel;
    } catch (IOException e) {
      this.logger.error("Creating channel failed", e);
      throw new RuntimeException(e);
    }
  }

  private void leaderSelected(LeadershipController leadershipController) {
    this.leaderHook.ifPresent((hook) -> hook.leaderUpdated(true, this.subscriptionId));
    this.logger.info("Subscription selected as leader. {}", this.identificationInformation());
    Channel subscriberGroupChannel = this.createRabbitMQChannel();

    for (String channelName : this.channels) {
      try {
        this.logger.info(
            "Leading subscription is creating exchanges and queues for channel {}. {}",
            channelName,
            this.identificationInformation());
        subscriberGroupChannel.exchangeDeclare(
            this.makeConsistentHashExchangeName(channelName, this.subscriberId),
            CONSISTENT_HASH_EXCHANGE_TYPE);

        for (int i = 0; i < this.partitionCount; ++i) {
          subscriberGroupChannel.queueDeclare(
              this.makeConsistentHashQueueName(channelName, this.subscriberId, i),
              true,
              false,
              false,
              null);
          subscriberGroupChannel.queueBind(
              this.makeConsistentHashQueueName(channelName, this.subscriberId, i),
              this.makeConsistentHashExchangeName(channelName, this.subscriberId),
              BUCKET_NUMS);
        }

        subscriberGroupChannel.exchangeDeclare(channelName, BuiltinExchangeType.FANOUT);
        subscriberGroupChannel.exchangeBind(
            this.makeConsistentHashExchangeName(channelName, this.subscriberId),
            channelName,
            StringUtils.EMPTY);
        this.logger.info(
            "Leading subscription created exchanges and queues for channel {}. {}",
            channelName,
            this.identificationInformation());
      } catch (IOException e) {
        this.logger.error(
            "Failed creating exchanges and queues for channel {}. {}",
            channelName,
            this.identificationInformation());
        this.logger.error("Failed creating exchanges and queues", e);
        throw new RuntimeException(e);
      }
    }

    try {
      this.logger.info("Closing subscriber group channel. {}", this.identificationInformation());
      subscriberGroupChannel.close();
      this.logger.info("Closed subscriber group channel. {}", this.identificationInformation());
    } catch (Exception e) {
      this.logger.error(
          "Closing subscriber group channel failed. {}", this.identificationInformation());
      this.logger.error(e.getMessage(), e);
    }
  }

  private void leaderRemoved() {
    this.logger.info("Revoking leadership from subscription. {}", this.identificationInformation());
    this.leaderHook.ifPresent((hook) -> hook.leaderUpdated(false, this.subscriptionId));
    this.logger.info(
        "Leadership is revoked from subscription. {}", this.identificationInformation());
  }

  private void assignmentUpdated(Assignment assignment) {
    this.logger.info("Updating assignment {}. {} ", assignment, this.identificationInformation());

    for (String channelName : this.currentPartitionsByChannel.keySet()) {
      Set<Integer> currentPartitions = this.currentPartitionsByChannel.get(channelName);
      this.logger.info(
          "Current partitions {} for channel {}. {}",
          currentPartitions,
          channelName,
          this.identificationInformation());
      Set<Integer> expectedPartitions =
          assignment.getPartitionAssignmentsByChannel().get(channelName);
      this.logger.info(
          "Expected partitions {} for channel {}. {}",
          expectedPartitions,
          channelName,
          this.identificationInformation());
      Set<Integer> resignedPartitions =
          (Set)
              currentPartitions.stream()
                  .filter(
                      (currentPartition) -> {
                        return !expectedPartitions.contains(currentPartition);
                      })
                  .collect(Collectors.toSet());
      this.logger.info(
          "Resigned partitions {} for channel {}. {}",
          resignedPartitions,
          channelName,
          this.identificationInformation());
      Set<Integer> assignedPartitions =
          (Set)
              expectedPartitions.stream()
                  .filter(
                      (expectedPartition) -> {
                        return !currentPartitions.contains(expectedPartition);
                      })
                  .collect(Collectors.toSet());
      this.logger.info(
          "Assigned partitions {} for channel {}. {}",
          assignedPartitions,
          channelName,
          this.identificationInformation());
      resignedPartitions.forEach(
          (resignedPartition) -> {
            try {
              this.logger.info(
                  "Removing partition {} for channel {}. {}",
                  resignedPartition,
                  channelName,
                  this.identificationInformation());
              String queue =
                  this.makeConsistentHashQueueName(
                      channelName, this.subscriberId, resignedPartition);
              this.consumerChannel.basicCancel(this.consumerTagByQueue.remove(queue));
              this.logger.info(
                  "Partition {} is removed for channel {}. {}",
                  resignedPartition,
                  channelName,
                  this.identificationInformation());
            } catch (Exception e) {
              this.logger.error(
                  "Removing partition {} for channel {} failed. {}",
                  resignedPartition,
                  channelName,
                  this.identificationInformation());
              this.logger.error("Removing partition failed", e);
              throw new RuntimeException(e);
            }
          });
      assignedPartitions.forEach(
          (assignedPartition) -> {
            try {
              this.logger.info(
                  "Assigning partition {} for channel {}. {}",
                  assignedPartition,
                  channelName,
                  this.identificationInformation());
              String queue =
                  this.makeConsistentHashQueueName(
                      channelName, this.subscriberId, assignedPartition);
              String exchange = this.makeConsistentHashExchangeName(channelName, this.subscriberId);
              this.consumerChannel.exchangeDeclare(exchange, "x-consistent-hash");
              this.consumerChannel.queueDeclare(queue, true, false, false, null);
              this.consumerChannel.queueBind(queue, exchange, "10");
              String tag = this.consumerChannel.basicConsume(queue, false, this.createConsumer());
              this.consumerTagByQueue.put(queue, tag);
              this.logger.info(
                  "Partition {} is assigned for channel {}. {}",
                  assignedPartition,
                  channelName,
                  this.identificationInformation());
            } catch (IOException e) {
              this.logger.error(
                  "Partition assignment failed. {}", this.identificationInformation());
              this.logger.error("Partition assignment failed", e);
              throw new RuntimeException(e);
            }
          });
      this.currentPartitionsByChannel.put(channelName, expectedPartitions);
      this.subscriptionLifecycleHook.ifPresent(
          (sh) -> sh.partitionsUpdated(channelName, this.subscriptionId, expectedPartitions));
    }

    this.logger.info("assignment {} is updated. {}", assignment, this.identificationInformation());
  }

  private com.rabbitmq.client.Consumer createConsumer() {
    return new DefaultConsumer(this.consumerChannel) {
      public void handleDelivery(
          String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        RabbitMQMessage message = new RabbitMQMessage(new String(body, StandardCharsets.UTF_8));

        try {
          if (Subscription.this.consumerChannel.isOpen()) {
            Subscription.this.handleMessageCallback.accept(
                message,
                () -> Subscription.this.acknowledge(envelope, Subscription.this.consumerChannel));
          }
        } catch (Exception e) {
          Subscription.this.close();
        }
      }
    };
  }

  private void acknowledge(Envelope envelope, Channel channel) {
    try {
      channel.basicAck(envelope.getDeliveryTag(), false);
    } catch (IOException e) {
      this.logger.error(e.getMessage(), e);
    }
  }

  private String makeConsistentHashExchangeName(String channelName, String subscriberId) {
    return String.format("%s-%s", channelName, subscriberId);
  }

  private String makeConsistentHashQueueName(
      String channelName, String subscriberId, int partition) {
    return String.format("%s-%s-%s", channelName, subscriberId, partition);
  }

  private String identificationInformation() {
    return String.format(
        "(consumerId = [%s], subscriptionId = [%s], subscriberId = [%s])",
        this.consumerId, this.subscriptionId, this.subscriberId);
  }
}
