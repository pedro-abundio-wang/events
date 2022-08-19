package com.events.messaging.activemq.producer;

import com.events.messaging.activemq.common.ChannelType;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ActiveMQMessageProducer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final Connection connection;

  private final Session session;

  private final Map<String, ChannelType> messageModes;

  public ActiveMQMessageProducer(String url, Optional<String> user, Optional<String> password) {
    this(url, Collections.emptyMap(), user, password);
  }

  public ActiveMQMessageProducer(
      String url,
      Map<String, ChannelType> messageModes,
      Optional<String> user,
      Optional<String> password) {

    this.messageModes = messageModes;
    ActiveMQConnectionFactory connectionFactory =
        createActiveMQConnectionFactory(url, user, password);
    try {
      logger.info("Creating connection");
      connection = connectionFactory.createConnection();
      connection.setExceptionListener(e -> logger.error(e.getMessage(), e));
      logger.info("Starting connection");
      connection.start();
      logger.info("Creating session");
      session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      logger.info("Created session");
    } catch (JMSException e) {
      logger.error("Creating session/connection failed", e);
      throw new RuntimeException(e);
    }
  }

  public CompletableFuture<?> send(String channel, String key, String message) {
    MessageProducer producer = null;
    try {
      ChannelType mode = messageModes.getOrDefault(channel, ChannelType.TOPIC);
      Destination destination =
          mode == ChannelType.TOPIC
              ? session.createTopic("VirtualTopic." + channel)
              : session.createQueue(channel);

      producer = session.createProducer(destination);
      producer.setDeliveryMode(DeliveryMode.PERSISTENT);

      TextMessage textMessage = session.createTextMessage(message);
      // Message Grouping
      //
      // Message groups are sets of messages that have the following characteristics:
      //
      // 1). Messages in a message group share the same group id, i.e. they have same group
      // identifier property (JMSXGroupID for JMS).
      //
      // 2). Messages in a message group are always consumed by the same consumer, even if there are
      // many consumers on a queue. They pin all messages with the same group id to the same
      // consumer. If that consumer closes another consumer is chosen and will receive all messages
      // with the same group id.
      textMessage.setStringProperty("JMSXGroupID", key);
      logger.info("Sending message = {} with key = {} for channel = {}", message, key, channel);
      producer.send(textMessage);
      producer.close();
    } catch (JMSException e) {
      logger.error("Sending failed", e);
    } finally {
      if (producer != null) {
        try {
          producer.close();
        } catch (JMSException e) {
          logger.error("closing producer failed", e);
        }
      }
    }

    return CompletableFuture.completedFuture(null);
  }

  private ActiveMQConnectionFactory createActiveMQConnectionFactory(
      String url, Optional<String> user, Optional<String> password) {
    return user.flatMap(
            usr ->
                password.flatMap(
                    pass -> Optional.of(new ActiveMQConnectionFactory(usr, pass, url))))
        .orElseGet(() -> new ActiveMQConnectionFactory(url));
  }

  public void close() {
    try {
      logger.info("closing session/connection");
      session.close();
      connection.close();
      logger.info("closed session/connection");
    } catch (JMSException e) {
      logger.info("closing session/connection failed");
      logger.error(e.getMessage(), e);
    }
  }
}
