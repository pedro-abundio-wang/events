package com.events.messaging.redis.common;

public class RedisChannelUtil {
  public static String channelToRedisStream(String channel, int partition) {
    return String.format("channels:%s-%s", channel, partition);
  }
}
