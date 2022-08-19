package com.events.core.messaging.config;

import com.events.core.messaging.channel.MessageChannelMapping;
import com.events.core.messaging.channel.MessageChannelMappingBuilder;
import com.events.core.messaging.interceptor.MessageInterceptor;
import com.events.core.messaging.producer.MessageProducer;
import com.events.core.messaging.publisher.MessagePublisher;
import com.events.core.messaging.publisher.MessagePublisherImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MessageProducerConfiguration.class})
public class MessagePublisherConfiguration {

  @Autowired(required = false)
  private final MessageInterceptor[] messageInterceptors = new MessageInterceptor[0];

  @ConditionalOnMissingBean(MessageChannelMapping.class)
  @Bean
  public MessageChannelMapping messageChannelMapping() {
    return MessageChannelMappingBuilder.createInstance().withMapping("from", "to").build();
  }

  @Bean
  public MessagePublisher messagePublisher(
      MessageChannelMapping messageChannelMapping, MessageProducer messageProducer) {
    return new MessagePublisherImpl(messageInterceptors, messageChannelMapping, messageProducer);
  }
}
