package com.events.core.messaging.consumer.rabbitmq;

import com.events.common.json.mapper.JsonMapper;
import com.events.core.messaging.consumer.MessageConsumer;
import com.events.core.messaging.message.MessageImpl;
import com.events.core.messaging.subscriber.MessageHandler;
import com.events.core.messaging.subscriber.MessageSubscription;
import com.events.messaging.rabbitmq.consumer.RabbitMQMessageConsumer;
import com.events.messaging.rabbitmq.consumer.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class RabbitMQMessageConsumerWrapper implements MessageConsumer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final RabbitMQMessageConsumer messageConsumerRabbitMQ;

  public RabbitMQMessageConsumerWrapper(RabbitMQMessageConsumer messageConsumerRabbitMQ) {
    this.messageConsumerRabbitMQ = messageConsumerRabbitMQ;
  }

  @Override
  public MessageSubscription subscribe(
      String subscriberId, Set<String> channels, MessageHandler handler) {

    logger.info("Subscribing: subscriberId = {}, channels = {}", subscriberId, channels);

    Subscription subscription =
        messageConsumerRabbitMQ.subscribe(
            subscriberId,
            channels,
            message ->
                handler.accept(JsonMapper.fromJson(message.getPayload(), MessageImpl.class)));

    logger.info("Subscribed: subscriberId = {}, channels = {}", subscriberId, channels);

    return subscription::close;
  }

  @Override
  public String getId() {
    return messageConsumerRabbitMQ.getId();
  }

  @Override
  public void close() {
    logger.info("Closing consumer");
    messageConsumerRabbitMQ.close();
    logger.info("Closed consumer");
  }
}
