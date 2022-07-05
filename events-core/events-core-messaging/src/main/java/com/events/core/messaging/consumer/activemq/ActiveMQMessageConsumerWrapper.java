package com.events.core.messaging.consumer.activemq;

import com.events.common.json.mapper.JsonMapper;
import com.events.core.messaging.consumer.MessageConsumer;
import com.events.core.messaging.message.MessageImpl;
import com.events.core.messaging.subscriber.MessageHandler;
import com.events.core.messaging.subscriber.MessageSubscription;
import com.events.messaging.activemq.consumer.ActiveMQSubscription;
import com.events.messaging.activemq.consumer.ActiveMQMessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ActiveMQMessageConsumerWrapper implements MessageConsumer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final ActiveMQMessageConsumer messageConsumerActiveMQ;

  public ActiveMQMessageConsumerWrapper(ActiveMQMessageConsumer messageConsumerActiveMQ) {
    this.messageConsumerActiveMQ = messageConsumerActiveMQ;
  }

  @Override
  public MessageSubscription subscribe(
      String subscriberId, Set<String> channels, MessageHandler handler) {

    logger.info("Subscribing: subscriberId = {}, channels = {}", subscriberId, channels);

    ActiveMQSubscription subscription =
        messageConsumerActiveMQ.subscribe(
            subscriberId,
            channels,
            message ->
                handler.accept(JsonMapper.fromJson(message.getPayload(), MessageImpl.class)));

    logger.info("Subscribed: subscriberId = {}, channels = {}", subscriberId, channels);

    return subscription::close;
  }

  @Override
  public String getId() {
    return messageConsumerActiveMQ.getId();
  }

  @Override
  public void close() {
    logger.info("Closing consumer");
    messageConsumerActiveMQ.close();
    logger.info("Closed consumer");
  }
}
