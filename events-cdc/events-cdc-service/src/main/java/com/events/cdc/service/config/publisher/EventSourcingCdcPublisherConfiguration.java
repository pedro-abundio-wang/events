package com.events.cdc.service.config.publisher;

import com.events.cdc.connector.db.transaction.log.messaging.EventWithSourcing;
import com.events.cdc.publisher.CdcPublisher;
import com.events.cdc.publisher.filter.PublishingFilter;
import com.events.cdc.publisher.producer.CdcProducerFactory;
import com.events.cdc.publisher.strategy.EventWithSourcingPublishingStrategy;
import com.events.cdc.publisher.strategy.PublishingStrategy;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(EventSourcingCondition.class)
public class EventSourcingCdcPublisherConfiguration {

  @Bean
  public CdcPublisher<EventWithSourcing> cdcPublisher(
      CdcProducerFactory cdcProducerFactory,
      PublishingFilter publishingFilter,
      MeterRegistry meterRegistry) {

    return new CdcPublisher<>(
        cdcProducerFactory,
        publishingFilter,
        new EventWithSourcingPublishingStrategy(),
        meterRegistry);
  }

  @Bean
  public PublishingStrategy<EventWithSourcing> publishingStrategy() {
    return new EventWithSourcingPublishingStrategy();
  }
}
