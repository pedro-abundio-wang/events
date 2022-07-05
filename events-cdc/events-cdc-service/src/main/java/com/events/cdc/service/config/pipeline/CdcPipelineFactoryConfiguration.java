package com.events.cdc.service.config.pipeline;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  TransactionalMessagingCdcPipelineFactoryConfiguration.class,
  EventSourcingCdcPipelineFactoryConfiguration.class,
})
public class CdcPipelineFactoryConfiguration {}
