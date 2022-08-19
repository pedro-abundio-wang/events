package com.events.messaging.redis.common;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class RedissonClients {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final List<RedissonClient> redissonClients;

  public RedissonClients(RedisServers redisServers) {
    redissonClients =
        redisServers.getHostsAndPorts().stream()
            .map(this::createRedissonClient)
            .collect(Collectors.toList());
  }

  public List<RedissonClient> getRedissonClients() {
    return redissonClients;
  }

  private RedissonClient createRedissonClient(RedisServers.HostAndPort hostAndPort) {
    logger.info("Creating redisson client");
    Config config = new Config();
    config.useSingleServer().setRetryAttempts(Integer.MAX_VALUE);
    config.useSingleServer().setRetryInterval(100);
    config
        .useSingleServer()
        .setAddress(String.format("redis://%s:%s", hostAndPort.getHost(), hostAndPort.getPort()));
    logger.info("Created redisson client");
    RedissonClient redissonClient = Redisson.create(config);
    return redissonClient;
  }
}
