package com.events.cdc.service.config.producer;

import com.events.cdc.publisher.filter.PublishingFilter;
import com.events.cdc.publisher.producer.CdcProducerFactory;
import com.events.cdc.publisher.producer.wrappers.redis.RedisCdcProducer;
import com.events.messaging.redis.config.RedisMessageProducerConfiguration;
import com.events.messaging.redis.producer.RedisMessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Import(RedisMessageProducerConfiguration.class)
@Profile("redis")
public class RedisCdcProducerConfiguration {
  @Bean
  public PublishingFilter redisDuplicatePublishingDetector() {
    return (fileOffset, topic) -> true;
  }

  @Bean
  public CdcProducerFactory redisCdcProducerFactory(RedisMessageProducer redisMessageProducer) {
    return () -> new RedisCdcProducer(redisMessageProducer);
  }
}
