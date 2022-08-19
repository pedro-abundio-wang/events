package com.events.cdc.service.config.publisher;

import com.events.cdc.service.config.producer.CdcProducerConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  // Cdc Publisher
  EventSourcingCdcPublisherConfiguration.class,
  TransactionalMessagingCdcPublisherConfiguration.class,
  // Cdc Producer
  CdcProducerConfiguration.class,
})
public class CdcPublisherConfiguration {}
