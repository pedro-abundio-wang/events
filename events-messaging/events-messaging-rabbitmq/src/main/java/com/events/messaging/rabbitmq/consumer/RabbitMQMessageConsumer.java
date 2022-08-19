package com.events.messaging.rabbitmq.consumer;

import com.events.messaging.partition.management.CoordinatorFactory;
import com.events.messaging.partition.management.SubscriptionLeaderHook;
import com.events.messaging.partition.management.SubscriptionLifecycleHook;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class RabbitMQMessageConsumer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public final String consumerId;
  private final Supplier<String> subscriptionIdSupplier;

  private final CoordinatorFactory coordinatorFactory;
  private Connection connection;
  private final int partitionCount;
  private final Address[] brokerAddresses;

  private final ConcurrentLinkedQueue<Subscription> subscriptions = new ConcurrentLinkedQueue<>();

  public RabbitMQMessageConsumer(
      CoordinatorFactory coordinatorFactory, Address[] brokerAddresses, int partitionCount) {
    this(
        () -> UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        coordinatorFactory,
        brokerAddresses,
        partitionCount);
  }

  public RabbitMQMessageConsumer(
      Supplier<String> subscriptionIdSupplier,
      String consumerId,
      CoordinatorFactory coordinatorFactory,
      Address[] brokerAddresses,
      int partitionCount) {

    this.subscriptionIdSupplier = subscriptionIdSupplier;
    this.consumerId = consumerId;
    this.coordinatorFactory = coordinatorFactory;
    this.partitionCount = partitionCount;
    this.brokerAddresses = brokerAddresses;

    prepareRabbitMQConnection();

    logger.info("consumer {} created and ready to subscribe", consumerId);
  }

  private void prepareRabbitMQConnection() {
    ConnectionFactory factory = new ConnectionFactory();
    try {
      logger.info("Creating connection");
      connection = factory.newConnection(brokerAddresses);
      logger.info("Created connection");
    } catch (IOException | TimeoutException e) {
      logger.error("Creating connection failed", e);
      throw new RuntimeException(e);
    }
  }

  public Subscription subscribe(
      String subscriberId, Set<String> channels, RabbitMQMessageHandler handler) {
    logger.info(
        "consumer {} with subscriberId {} is subscribing to channels {}",
        consumerId,
        subscriberId,
        channels);

    Subscription subscription =
        new Subscription(
            coordinatorFactory,
            consumerId,
            subscriptionIdSupplier.get(),
            connection,
            subscriberId,
            channels,
            partitionCount,
            (message, acknowledgeCallback) ->
                handleMessage(subscriberId, handler, message, acknowledgeCallback));

    subscriptions.add(subscription);

    subscription.setClosingCallback(() -> subscriptions.remove(subscription));

    logger.info(
        "consumer {} with subscriberId {} subscribed to channels {}",
        consumerId,
        subscriberId,
        channels);
    return subscription;
  }

  private void handleMessage(
      String subscriberId,
      RabbitMQMessageHandler handler,
      RabbitMQMessage message,
      Runnable acknowledgeCallback) {
    try {
      handler.accept(message);
      logger.info(
          "consumer {} with subscriberId {} handled message {}", consumerId, subscriberId, message);
    } catch (Throwable t) {
      logger.info(
          "consumer {} with subscriberId {} got exception when tried to handle message {}",
          consumerId,
          subscriberId,
          message);
      logger.info("Got exception ", t);
      throw new RuntimeException(t);
    }

    acknowledgeCallback.run();
  }

  public void setSubscriptionLifecycleHook(SubscriptionLifecycleHook subscriptionLifecycleHook) {
    subscriptions.forEach(
        subscription -> subscription.setSubscriptionLifecycleHook(subscriptionLifecycleHook));
  }

  public void setLeaderHook(SubscriptionLeaderHook leaderHook) {
    subscriptions.forEach(subscription -> subscription.setLeaderHook(leaderHook));
  }

  public void close() {
    logger.info("consumer {} is closing", consumerId);
    subscriptions.forEach(Subscription::close);
    subscriptions.clear();
    try {
      logger.info("Closing connection");
      connection.close();
      logger.info("Closed connection");
    } catch (IOException e) {
      logger.error("Closing connection failed", e);
    }
    logger.info("consumer {} is closed", consumerId);
  }

  public String getId() {
    return consumerId;
  }
}
