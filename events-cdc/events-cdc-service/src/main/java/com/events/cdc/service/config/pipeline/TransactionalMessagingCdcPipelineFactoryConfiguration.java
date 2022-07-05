package com.events.cdc.service.config.pipeline;

import com.events.cdc.connector.db.transaction.log.converter.TransactionLogEntryToMessageWithDestinationConverter;
import com.events.cdc.connector.db.transaction.log.messaging.MessageWithDestination;
import com.events.cdc.pipeline.CdcPipelineFactory;
import com.events.cdc.pipeline.CdcPipelineType;
import com.events.cdc.publisher.CdcPublisher;
import com.events.cdc.reader.provider.CdcReaderProvider;
import com.events.cdc.service.config.publisher.TransactionalMessagingCondition;
import com.events.common.id.DatabaseIdGenerator;
import com.events.common.jdbc.spring.config.EventsSqlDialectConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Conditional(TransactionalMessagingCondition.class)
@Import(EventsSqlDialectConfiguration.class)
public class TransactionalMessagingCdcPipelineFactoryConfiguration {

  @Bean
  public CdcPipelineFactory<MessageWithDestination> cdcPipelineFactory(
      CdcReaderProvider cdcReaderProvider, CdcPublisher<MessageWithDestination> cdcPublisher) {
    return new CdcPipelineFactory<>(
        CdcPipelineType.TRANSACTIONAL_MESSAGING,
        cdcReaderProvider,
        cdcPublisher,
        outboxId ->
            new TransactionLogEntryToMessageWithDestinationConverter(
                new DatabaseIdGenerator(outboxId)));
  }

  @Bean
  public SourceTableNameResolver transactionalMessagingSourceTableNameResolver() {
    return pipelineType -> {
      if (CdcPipelineType.TRANSACTIONAL_MESSAGING.equals(pipelineType)
          || SourceTableNameResolver.DEFAULT.equals(pipelineType))
        return SourceTableNameResolver.MESSAGE;
      if (CdcPipelineType.EVENT_SOURCING.equals(pipelineType)) return SourceTableNameResolver.EVENT;
      throw new RuntimeException(String.format("Unknown pipeline type '%s'", pipelineType));
    };
  }
}
