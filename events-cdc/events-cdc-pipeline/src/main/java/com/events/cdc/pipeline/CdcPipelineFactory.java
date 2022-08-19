package com.events.cdc.pipeline;

import com.events.cdc.connector.db.transaction.log.converter.TransactionLogEntryConverterFactory;
import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogMessage;
import com.events.cdc.publisher.CdcPublisher;
import com.events.cdc.reader.CdcReader;
import com.events.cdc.reader.provider.CdcReaderProvider;
import com.events.common.jdbc.schema.EventsSchema;

public class CdcPipelineFactory<TLM extends TransactionLogMessage> {

  private String type;
  private CdcReaderProvider cdcReaderProvider;
  private CdcPublisher<TLM> cdcPublisher;
  private TransactionLogEntryConverterFactory<TLM> transactionLogEntryConverterFactory;

  public CdcPipelineFactory(
      String type,
      CdcReaderProvider cdcReaderProvider,
      CdcPublisher<TLM> cdcPublisher,
      TransactionLogEntryConverterFactory<TLM> transactionLogEntryConverterFactory) {
    this.type = type;
    this.cdcReaderProvider = cdcReaderProvider;
    this.cdcPublisher = cdcPublisher;
    this.transactionLogEntryConverterFactory = transactionLogEntryConverterFactory;
  }

  public boolean supports(String type) {
    return this.type.equals(type);
  }

  public CdcPipeline<TLM> create(CdcPipelineProperties cdcPipelineProperties) {

    CdcReader cdcReader = cdcReaderProvider.get(cdcPipelineProperties.getReader()).getCdcReader();

    EventsSchema eventsSchema = new EventsSchema(cdcPipelineProperties.getEventsDatabaseSchema());

    cdcReader.addCdcPipelineHandler(
        eventsSchema,
        cdcPipelineProperties.getSourceTableName(),
        transactionLogEntryConverterFactory.apply(cdcReader.getOutboxId()),
        cdcPublisher::publishMessage);

    return new CdcPipeline<>(cdcPublisher);
  }
}
