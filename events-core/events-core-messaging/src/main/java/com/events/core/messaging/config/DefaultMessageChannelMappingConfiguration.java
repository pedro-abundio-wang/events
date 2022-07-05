package com.events.core.messaging.config;

import com.events.core.messaging.channel.DefaultMessageChannelMapping;
import com.events.core.messaging.channel.MessageChannelMapping;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultMessageChannelMappingConfiguration {

  @ConditionalOnMissingBean(MessageChannelMapping.class)
  @Bean
  public MessageChannelMapping channelMapping() {
    return new DefaultMessageChannelMapping.DefaultMessageChannelMappingBuilder().build();
  }
}
