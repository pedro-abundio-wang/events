package com.events.messaging.redis.producer;

import com.events.messaging.redis.common.RedisChannelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class RedisMessageProducer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final RedisTemplate<String, String> redisTemplate;

  private final int partitions;

  public RedisMessageProducer(RedisTemplate<String, String> redisTemplate, int partitions) {
    this.redisTemplate = redisTemplate;
    this.partitions = partitions;
  }

  public CompletableFuture<?> send(String channel, String key, String message) {

    int partition = Math.abs(key.hashCode()) % partitions;

    logger.info(
        "Sending message = {} with key = {} for channel = {}, partition = {}",
        message,
        key,
        channel,
        partition);

    redisTemplate
        .opsForStream()
        .add(
            StreamRecords.string(Collections.singletonMap(key, message))
                .withStreamKey(RedisChannelUtil.channelToRedisStream(channel, partition)));

    logger.info(
        "message sent = {} with key = {} for channel = {}, partition = {}",
        message,
        key,
        channel,
        partition);

    return CompletableFuture.completedFuture(null);
  }

  public void close() {}
}
