package com.events.messaging.redis.config;

import com.events.messaging.redis.common.RedisProperties;
import com.events.messaging.redis.producer.RedisMessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Profile("redis")
@Import({RedisConfiguration.class})
public class RedisMessageProducerConfiguration {
  @Bean
  public RedisMessageProducer redisMessageProducer(
      RedisTemplate<String, String> redisTemplate, RedisProperties redisProperties) {
    return new RedisMessageProducer(redisTemplate, redisProperties.getPartitions());
  }
}
