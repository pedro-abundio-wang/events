package com.events.messaging.rabbitmq.producer;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public class RabbitMQMessageProducer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final Connection connection;

  private final Channel channel;

  public RabbitMQMessageProducer(Address[] brokerAddresses) {

    ConnectionFactory factory = new ConnectionFactory();
    try {
      logger.info("Creating channel/connection");
      connection = factory.newConnection(brokerAddresses);
      channel = connection.createChannel();
      logger.info("Created channel/connection");
    } catch (IOException | TimeoutException e) {
      logger.error("Creating channel/connection failed", e);
      throw new RuntimeException(e);
    }
  }

  public CompletableFuture<?> send(String channel, String key, String message) {
    try {
      AMQP.BasicProperties bp =
          new AMQP.BasicProperties.Builder().headers(Collections.singletonMap("key", key)).build();

      this.channel.exchangeDeclare(channel, BuiltinExchangeType.FANOUT);
      this.channel.basicPublish(channel, key, bp, message.getBytes(StandardCharsets.UTF_8));

    } catch (IOException e) {
      logger.error("sending message failed", e);
      throw new RuntimeException(e);
    }

    return CompletableFuture.completedFuture(null);
  }

  public void close() {
    try {
      logger.info("Closing channel/connection");
      channel.close();
      connection.close();
      logger.info("Closed channel/connection");
    } catch (IOException | TimeoutException e) {
      logger.error("Closing channel/connection failed", e);
    }
  }
}
