package com.events.cdc.service.config;

import com.events.cdc.reader.connection.pool.ConnectionPoolProperties;
import com.events.cdc.service.config.helth.CdcServiceHealthCheckConfiguration;
import com.events.cdc.service.config.offset.OffsetStoreFactoryConfiguration;
import com.events.cdc.service.config.pipeline.CdcPipelineFactoryConfiguration;
import com.events.cdc.service.config.publisher.CdcPublisherConfiguration;
import com.events.cdc.service.config.reader.CdcReaderFactoryConfiguration;
import com.events.cdc.service.config.zookeeper.ZookeeperConfiguration;
import com.events.cdc.service.main.EventsCdcService;
import com.events.cdc.service.properties.EventsCdcServiceProperties;
import com.events.common.jdbc.spring.config.EventsSqlDialectConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  EventsSqlDialectConfiguration.class,
  ZookeeperConfiguration.class,
  // Cdc Reader Factory
  CdcReaderFactoryConfiguration.class,
  // Cdc Pipeline Factory
  CdcPipelineFactoryConfiguration.class,
  // Cdc Publisher
  CdcPublisherConfiguration.class,
  // Offset Store Factory
  OffsetStoreFactoryConfiguration.class,
  // Helth Check
  CdcServiceHealthCheckConfiguration.class,
  // Cdc Message Cleaners
  // CdcPipelinePropertiesConfiguration.class,
})
@EnableConfigurationProperties({EventsCdcServiceProperties.class, ConnectionPoolProperties.class})
public class EventsCdcServiceConfiguration {

//  @Bean
//  public EventsCdcProperties eventsCdcProperties() {
//    return new EventsCdcProperties();
//  }

  @Bean
  public EventsCdcService eventsCdcService() {
    return new EventsCdcService();
  }

  //  @Bean
  //  public CdcMessageCleaners cdcMessageCleaners() {
  //    return new CdcMessageCleaners();
  //  }
}
