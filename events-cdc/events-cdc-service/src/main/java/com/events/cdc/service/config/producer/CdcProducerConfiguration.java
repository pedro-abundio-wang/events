package com.events.cdc.service.config.producer;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  KafkaCdcProducerConfiguration.class,
  ActiveMQCdcProducerConfiguration.class,
  RabbitMQCdcProducerConfiguration.class,
  RedisCdcProducerConfiguration.class
})
public class CdcProducerConfiguration {}
