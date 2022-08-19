package com.events.core.messaging.config;

import com.events.core.messaging.consumer.MessageConsumer;
import com.events.core.messaging.consumer.activemq.ActiveMQMessageConsumerWrapper;
import com.events.core.messaging.consumer.kafka.KafkaMessageConsumerWrapper;
import com.events.core.messaging.consumer.rabbitmq.RabbitMQMessageConsumerWrapper;
import com.events.core.messaging.consumer.redis.RedisMessageConsumerWrapper;
import com.events.messaging.activemq.config.ActiveMQMessageConsumerConfiguration;
import com.events.messaging.activemq.consumer.ActiveMQMessageConsumer;
import com.events.messaging.kafka.config.KafkaMessageConsumerConfiguration;
import com.events.messaging.kafka.consumer.KafkaMessageConsumer;
import com.events.messaging.rabbitmq.config.RabbitMQMessageConsumerConfiguration;
import com.events.messaging.rabbitmq.consumer.RabbitMQMessageConsumer;
import com.events.messaging.redis.config.RedisMessageConsumerConfiguration;
import com.events.messaging.redis.consumer.RedisMessageConsumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  KafkaMessageConsumerConfiguration.class,
  RabbitMQMessageConsumerConfiguration.class,
  ActiveMQMessageConsumerConfiguration.class,
  RedisMessageConsumerConfiguration.class,
})
public class MessageConsumerConfiguration {

  @Bean
  @ConditionalOnBean(KafkaMessageConsumer.class)
  public MessageConsumer kafkaMessageConsumer(KafkaMessageConsumer kafkaMessageConsumer) {
    return new KafkaMessageConsumerWrapper(kafkaMessageConsumer);
  }

  @Bean
  @ConditionalOnBean(RabbitMQMessageConsumer.class)
  public MessageConsumer rabbitMQMessageConsumer(RabbitMQMessageConsumer rabbitMQMessageConsumer) {
    return new RabbitMQMessageConsumerWrapper(rabbitMQMessageConsumer);
  }

  @Bean
  @ConditionalOnBean(RedisMessageConsumer.class)
  public MessageConsumer redisMessageConsumer(RedisMessageConsumer redisMessageConsumer) {
    return new RedisMessageConsumerWrapper(redisMessageConsumer);
  }

  @Bean
  @ConditionalOnBean(ActiveMQMessageConsumer.class)
  public MessageConsumer activeMQMessageConsumer(ActiveMQMessageConsumer activeMQMessageConsumer) {
    return new ActiveMQMessageConsumerWrapper(activeMQMessageConsumer);
  }
}
