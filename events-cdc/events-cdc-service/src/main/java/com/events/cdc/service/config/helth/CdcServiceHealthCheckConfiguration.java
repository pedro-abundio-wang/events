package com.events.cdc.service.config.helth;

import com.events.cdc.publisher.CdcPublisher;
import com.events.cdc.reader.provider.CdcReaderProvider;
import com.events.cdc.service.health.CdcPublisherHealthCheck;
import com.events.cdc.service.health.CdcReaderHealthCheck;
import com.events.cdc.service.health.KafkaHealthCheck;
import com.events.cdc.service.health.ZookeeperHealthCheck;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CdcServiceHealthCheckConfiguration {

  @Bean
  public ZookeeperHealthCheck zookeeperHealthCheck() {
    return new ZookeeperHealthCheck();
  }

  @Bean
  public CdcReaderHealthCheck cdcReaderHealthCheck(CdcReaderProvider cdcReaderProvider) {
    return new CdcReaderHealthCheck(cdcReaderProvider);
  }

  @Bean
  public CdcPublisherHealthCheck cdcDataPublisherHealthCheck(CdcPublisher cdcPublisher) {
    return new CdcPublisherHealthCheck(cdcPublisher);
  }

  @Bean
  public KafkaHealthCheck kafkaHealthCheck() {
    return new KafkaHealthCheck();
  }
}
