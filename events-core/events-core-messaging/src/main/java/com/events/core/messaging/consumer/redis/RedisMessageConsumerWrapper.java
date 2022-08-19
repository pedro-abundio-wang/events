package com.events.core.messaging.consumer.redis;

import com.events.common.json.mapper.JsonMapper;
import com.events.core.messaging.consumer.MessageConsumer;
import com.events.core.messaging.message.MessageImpl;
import com.events.core.messaging.subscriber.MessageHandler;
import com.events.core.messaging.subscriber.MessageSubscription;
import com.events.messaging.redis.consumer.RedisMessageConsumer;
import com.events.messaging.redis.consumer.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class RedisMessageConsumerWrapper implements MessageConsumer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final RedisMessageConsumer messageConsumerRedis;

  public RedisMessageConsumerWrapper(RedisMessageConsumer messageConsumerRedis) {
    this.messageConsumerRedis = messageConsumerRedis;
  }

  @Override
  public MessageSubscription subscribe(
      String subscriberId, Set<String> channels, MessageHandler handler) {
    logger.info("Subscribing: subscriberId = {}, channels = {}", subscriberId, channels);

    Subscription subscription =
        messageConsumerRedis.subscribe(
            subscriberId,
            channels,
            message ->
                handler.accept(JsonMapper.fromJson(message.getPayload(), MessageImpl.class)));

    logger.info("Subscribed: subscriberId = {}, channels = {}", subscriberId, channels);

    return subscription::close;
  }

  @Override
  public String getId() {
    return messageConsumerRedis.getId();
  }

  @Override
  public void close() {
    logger.info("Closing consumer");

    messageConsumerRedis.close();

    logger.info("Closed consumer");
  }
}
