package com.events.messaging.redis.consumer;

import com.events.messaging.partition.management.CoordinatorFactory;
import com.events.messaging.partition.management.SubscriptionLeaderHook;
import com.events.messaging.partition.management.SubscriptionLifecycleHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class RedisMessageConsumer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public String consumerId;
  private final Supplier<String> subscriptionIdSupplier;
  private final RedisTemplate<String, String> redisTemplate;
  private final ConcurrentLinkedQueue<Subscription> subscriptions = new ConcurrentLinkedQueue<>();
  private final CoordinatorFactory coordinatorFactory;
  private final long timeInMillisecondsToSleepWhenKeyDoesNotExist;
  private final long blockStreamTimeInMilliseconds;

  public RedisMessageConsumer(
      RedisTemplate<String, String> redisTemplate,
      CoordinatorFactory coordinatorFactory,
      long timeInMillisecondsToSleepWhenKeyDoesNotExist,
      long blockStreamTimeInMilliseconds) {
    this(
        () -> UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        redisTemplate,
        coordinatorFactory,
        timeInMillisecondsToSleepWhenKeyDoesNotExist,
        blockStreamTimeInMilliseconds);
  }

  public RedisMessageConsumer(
      Supplier<String> subscriptionIdSupplier,
      String consumerId,
      RedisTemplate<String, String> redisTemplate,
      CoordinatorFactory coordinatorFactory,
      long timeInMillisecondsToSleepWhenKeyDoesNotExist,
      long blockStreamTimeInMilliseconds) {
    this.subscriptionIdSupplier = subscriptionIdSupplier;
    this.consumerId = consumerId;
    this.redisTemplate = redisTemplate;
    this.coordinatorFactory = coordinatorFactory;
    this.timeInMillisecondsToSleepWhenKeyDoesNotExist =
        timeInMillisecondsToSleepWhenKeyDoesNotExist;
    this.blockStreamTimeInMilliseconds = blockStreamTimeInMilliseconds;
    this.logger.info("Consumer created (consumer id = {})", consumerId);
  }

  public Subscription subscribe(
      String subscriberId, Set<String> channels, RedisMessageHandler handler) {
    logger.info(
        "Consumer subscribes to channels (consumer id = {}, subscriber id {}, channels = {})",
        consumerId,
        subscriberId,
        channels);
    Subscription subscription =
        new Subscription(
            subscriptionIdSupplier.get(),
            consumerId,
            redisTemplate,
            subscriberId,
            channels,
            handler,
            coordinatorFactory,
            timeInMillisecondsToSleepWhenKeyDoesNotExist,
            blockStreamTimeInMilliseconds);
    subscriptions.add(subscription);
    subscription.setClosingCallback(() -> subscriptions.remove(subscription));
    logger.info(
        "Consumer subscribed to channels (consumer id = {}, subscriber id {}, channels = {})",
        consumerId,
        subscriberId,
        channels);
    return subscription;
  }

  public void setSubscriptionLifecycleHook(SubscriptionLifecycleHook subscriptionLifecycleHook) {
    subscriptions.forEach(
        subscription -> subscription.setSubscriptionLifecycleHook(subscriptionLifecycleHook));
  }

  public void setLeaderHook(SubscriptionLeaderHook leaderHook) {
    subscriptions.forEach(subscription -> subscription.setLeaderHook(leaderHook));
  }

  public void close() {
    logger.info("Closing consumer (consumer id = {})", consumerId);
    subscriptions.forEach(Subscription::close);
    subscriptions.clear();
    logger.info("Closed consumer (consumer id = {})", consumerId);
  }

  public String getId() {
    return consumerId;
  }
}
