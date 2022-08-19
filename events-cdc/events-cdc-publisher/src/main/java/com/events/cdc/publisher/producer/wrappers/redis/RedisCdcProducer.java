package com.events.cdc.publisher.producer.wrappers.redis;

import com.events.cdc.publisher.producer.CdcProducer;
import com.events.messaging.redis.producer.RedisMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class RedisCdcProducer implements CdcProducer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final RedisMessageProducer redisMessageProducer;

  public RedisCdcProducer(RedisMessageProducer redisMessageProducer) {
    this.redisMessageProducer = redisMessageProducer;
  }

  @Override
  public CompletableFuture<?> send(String channel, String key, String message) {
    return redisMessageProducer.send(channel, key, message);
  }

  @Override
  public void close() {
    logger.info("closing EventsRedisCdcProducerWrapper");
    redisMessageProducer.close();
    logger.info("closed EventsRedisCdcProducerWrapper");
  }
}
