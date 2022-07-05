package com.events.cdc.service.config.pipeline;

import com.events.cdc.connector.db.transaction.log.converter.TransactionLogEntryToEventWithSourcingConverter;
import com.events.cdc.connector.db.transaction.log.messaging.EventWithSourcing;
import com.events.cdc.pipeline.CdcPipelineFactory;
import com.events.cdc.pipeline.CdcPipelineType;
import com.events.cdc.publisher.CdcPublisher;
import com.events.cdc.reader.provider.CdcReaderProvider;
import com.events.cdc.service.config.publisher.EventSourcingCondition;
import com.events.common.id.DatabaseIdGenerator;
import com.events.common.jdbc.spring.config.EventsSqlDialectConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Conditional(EventSourcingCondition.class)
@Import(EventsSqlDialectConfiguration.class)
public class EventSourcingCdcPipelineFactoryConfiguration {

  @Bean
  public CdcPipelineFactory<EventWithSourcing> cdcPipelineFactory(
      CdcReaderProvider cdcReaderProvider, CdcPublisher<EventWithSourcing> cdcPublisher) {
    return new CdcPipelineFactory<>(
        CdcPipelineType.EVENT_SOURCING,
        cdcReaderProvider,
        cdcPublisher,
        outboxId ->
            new TransactionLogEntryToEventWithSourcingConverter(new DatabaseIdGenerator(outboxId)));
  }

  @Bean
  public SourceTableNameResolver eventSourcingSourceTableNameResolver() {
    return pipelineType -> {
      if (CdcPipelineType.TRANSACTIONAL_MESSAGING.equals(pipelineType))
        return SourceTableNameResolver.MESSAGE;
      if (CdcPipelineType.EVENT_SOURCING.equals(pipelineType)
          || SourceTableNameResolver.DEFAULT.equals(pipelineType))
        return SourceTableNameResolver.EVENT;
      throw new RuntimeException(String.format("Unknown pipeline type '%s'", pipelineType));
    };
  }
}
