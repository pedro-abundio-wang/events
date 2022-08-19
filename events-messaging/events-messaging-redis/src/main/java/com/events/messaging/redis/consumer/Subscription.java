package com.events.messaging.redis.consumer;

import com.events.messaging.leadership.coordination.LeaderRemovedCallback;
import com.events.messaging.leadership.coordination.LeaderSelectedCallback;
import com.events.messaging.leadership.coordination.LeadershipController;
import com.events.messaging.partition.management.*;
import com.events.messaging.redis.common.RedisChannelUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Subscription {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final CoordinatorFactory coordinatorFactory;
  private final Coordinator coordinator;

  private final String subscriptionId;
  private final String consumerId;
  private final String subscriberId;

  private final RedisMessageHandler handler;
  private final long timeInMillisecondsToSleepWhenKeyDoesNotExist;
  private final long blockStreamTimeInMilliseconds;
  private final ExecutorService executorService = Executors.newCachedThreadPool();

  private final Map<String, Set<Integer>> currentPartitionsByChannel = new HashMap<>();
  private final ConcurrentHashMap<ChannelPartition, ChannelProcessor>
      channelProcessorsByChannelAndPartition = new ConcurrentHashMap<>();

  private final RedisTemplate<String, String> redisTemplate;

  private Optional<SubscriptionLifecycleHook> subscriptionLifecycleHook = Optional.empty();
  private Optional<SubscriptionLeaderHook> leaderHook = Optional.empty();
  private Optional<Runnable> closingCallback = Optional.empty();

  public Subscription(
      String subscriptionId,
      String consumerId,
      RedisTemplate<String, String> redisTemplate,
      String subscriberId,
      Set<String> channels,
      RedisMessageHandler handler,
      CoordinatorFactory coordinatorFactory,
      long timeInMillisecondsToSleepWhenKeyDoesNotExist,
      long blockStreamTimeInMilliseconds) {
    this.coordinatorFactory = coordinatorFactory;
    this.subscriptionId = subscriptionId;
    this.consumerId = consumerId;
    this.redisTemplate = redisTemplate;
    this.subscriberId = subscriberId;
    this.handler = handler;
    this.timeInMillisecondsToSleepWhenKeyDoesNotExist =
        timeInMillisecondsToSleepWhenKeyDoesNotExist;
    this.blockStreamTimeInMilliseconds = blockStreamTimeInMilliseconds;
    channels.forEach(
        (channelName) -> this.currentPartitionsByChannel.put(channelName, new HashSet<>()));
    this.coordinator =
        this.createCoordinator(
            subscriptionId,
            subscriberId,
            channels,
            this::leaderSelected,
            this::leaderRemoved,
            this::assignmentUpdated);
    this.logger.info(
        "subscription created (channels = {}, {})", channels, this.identificationInformation());
  }

  private Coordinator createCoordinator(
      String subscriptionId,
      String subscriberId,
      Set<String> channels,
      LeaderSelectedCallback leaderSelectedCallback,
      LeaderRemovedCallback leaderRemovedCallback,
      Consumer<Assignment> assignmentUpdatedCallback) {
    return coordinatorFactory.makeCoordinator(
        subscriberId,
        channels,
        subscriptionId,
        assignmentUpdatedCallback,
        RedisKeyUtil.keyForLeaderLock(subscriberId),
        leaderSelectedCallback,
        leaderRemovedCallback);
  }

  private void leaderSelected(LeadershipController leadershipController) {
    this.leaderHook.ifPresent((hook) -> hook.leaderUpdated(true, this.subscriptionId));
  }

  private void leaderRemoved() {
    this.logger.info("Revoking leadership from subscription. {}", this.identificationInformation());
    this.leaderHook.ifPresent((hook) -> hook.leaderUpdated(false, this.subscriptionId));
    this.logger.info(
        "Leadership is revoked from subscription. {}", this.identificationInformation());
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

  private void assignmentUpdated(Assignment assignment) {
    this.logger.info(
        "assignment is updated (assignment = {}, {})",
        assignment,
        this.identificationInformation());

    for (String channelName : this.currentPartitionsByChannel.keySet()) {
      Set<Integer> currentPartitions = this.currentPartitionsByChannel.get(channelName);
      Set<Integer> expectedPartitions =
          assignment.getPartitionAssignmentsByChannel().get(channelName);
      Set<Integer> resignedPartitions =
          (Set)
              currentPartitions.stream()
                  .filter(
                      (currentPartition) -> {
                        return !expectedPartitions.contains(currentPartition);
                      })
                  .collect(Collectors.toSet());
      this.logger.info(
          "partitions resigned (resigned partitions = {}, {})",
          resignedPartitions,
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
          "partitions asigned (resigned partitions = {}, {})",
          assignment,
          this.identificationInformation());
      resignedPartitions.forEach(
          (resignedPartition) -> {
            ((ChannelProcessor)
                    this.channelProcessorsByChannelAndPartition.remove(
                        new ChannelPartition(channelName, resignedPartition)))
                .stop();
          });
      assignedPartitions.forEach(
          (assignedPartition) -> {
            ChannelProcessor channelProcessor =
                new ChannelProcessor(
                    this.redisTemplate,
                    this.subscriberId,
                    RedisChannelUtil.channelToRedisStream(channelName, assignedPartition),
                    this.handler,
                    this.identificationInformation(),
                    this.timeInMillisecondsToSleepWhenKeyDoesNotExist,
                    this.blockStreamTimeInMilliseconds);
            this.executorService.submit(channelProcessor::process);
            this.channelProcessorsByChannelAndPartition.put(
                new ChannelPartition(channelName, assignedPartition), channelProcessor);
          });
      this.currentPartitionsByChannel.put(channelName, expectedPartitions);
      this.subscriptionLifecycleHook.ifPresent(
          (sh) -> sh.partitionsUpdated(channelName, this.subscriptionId, expectedPartitions));
    }
  }

  public void close() {
    this.coordinator.close();
    this.channelProcessorsByChannelAndPartition.values().forEach(ChannelProcessor::stop);
  }

  private String identificationInformation() {
    return String.format(
        "(consumerId = %s, subscriptionId = %s, subscriberId = %s)",
        this.consumerId, this.subscriptionId, this.subscriberId);
  }

  private static class ChannelPartition {
    private String channel;
    private int partition;

    public ChannelPartition() {}

    public ChannelPartition(String channel, int partition) {
      this.channel = channel;
      this.partition = partition;
    }

    public String getChannel() {
      return this.channel;
    }

    public void setChannel(String channel) {
      this.channel = channel;
    }

    public int getPartition() {
      return this.partition;
    }

    public void setPartition(int partition) {
      this.partition = partition;
    }

    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }

    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }
  }
}
