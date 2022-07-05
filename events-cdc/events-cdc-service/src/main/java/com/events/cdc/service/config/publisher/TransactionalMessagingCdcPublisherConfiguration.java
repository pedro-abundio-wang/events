package com.events.cdc.service.config.publisher;

import com.events.cdc.connector.db.transaction.log.messaging.MessageWithDestination;
import com.events.cdc.publisher.CdcPublisher;
import com.events.cdc.publisher.strategy.MessageWithDestinationPublishingStrategy;
import com.events.cdc.publisher.filter.PublishingFilter;
import com.events.cdc.publisher.producer.CdcProducerFactory;
import com.events.cdc.publisher.strategy.PublishingStrategy;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(TransactionalMessagingCondition.class)
public class TransactionalMessagingCdcPublisherConfiguration {

  @Bean
  public CdcPublisher<MessageWithDestination> cdcPublisher(
      CdcProducerFactory cdcProducerFactory,
      PublishingFilter publishingFilter,
      MeterRegistry meterRegistry) {

    return new CdcPublisher<>(
        cdcProducerFactory,
        publishingFilter,
        new MessageWithDestinationPublishingStrategy(),
        meterRegistry);
  }

  @Bean
  public PublishingStrategy<MessageWithDestination> publishingStrategy() {
    return new MessageWithDestinationPublishingStrategy();
  }
}
