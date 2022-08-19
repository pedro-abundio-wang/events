package com.events.messaging.redis.config;

import com.events.messaging.redis.common.RedisProperties;
import com.events.messaging.redis.common.RedisServers;
import com.events.messaging.redis.common.RedissonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Profile("redis")
public class RedisConfiguration {

  @Bean
  public RedisProperties redisProperties() {
    return new RedisProperties();
  }

  @Bean
  public RedisServers redisServers(RedisProperties redisProperties) {
    return new RedisServers(redisProperties.getServers());
  }

  @Bean
  public RedissonClients redissonClients(RedisServers redisServers) {
    return new RedissonClients(redisServers);
  }

  @Bean
  public LettuceConnectionFactory lettuceConnectionFactory(RedisServers redisServers) {
    RedisServers.HostAndPort mainServer = redisServers.getHostsAndPorts().get(0);
    return new LettuceConnectionFactory(mainServer.getHost(), mainServer.getPort());
  }

  @Bean
  public RedisTemplate<String, String> redisTemplate(
      LettuceConnectionFactory lettuceConnectionFactory) {
    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(lettuceConnectionFactory);
    template.setDefaultSerializer(stringRedisSerializer);
    template.setKeySerializer(stringRedisSerializer);
    template.setValueSerializer(stringRedisSerializer);
    template.setHashKeySerializer(stringRedisSerializer);
    return template;
  }
}
