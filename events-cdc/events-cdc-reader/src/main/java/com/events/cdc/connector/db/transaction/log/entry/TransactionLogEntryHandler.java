package com.events.cdc.connector.db.transaction.log.entry;

import com.events.cdc.connector.db.transaction.log.converter.TransactionLogEntryConverter;
import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogMessage;
import com.events.common.jdbc.schema.SchemaAndTable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class TransactionLogEntryHandler<TLM extends TransactionLogMessage> {

  protected SchemaAndTable schemaAndTable;
  protected TransactionLogEntryConverter<TLM> transactionLogEntryConverter;
  protected Function<TLM, CompletableFuture<?>> cdcPublisher;

  public TransactionLogEntryHandler(
      SchemaAndTable schemaAndTable,
      TransactionLogEntryConverter<TLM> transactionLogEntryConverter,
      Function<TLM, CompletableFuture<?>> cdcPublisher) {
    this.schemaAndTable = schemaAndTable;
    this.transactionLogEntryConverter = transactionLogEntryConverter;
    this.cdcPublisher = cdcPublisher;
  }

  public String getQualifiedTable() {
    return String.format("%s.%s", schemaAndTable.getSchema(), schemaAndTable.getTableName());
  }

  public SchemaAndTable getSchemaAndTable() {
    return schemaAndTable;
  }

  public boolean isFor(SchemaAndTable schemaAndTable) {
    return this.schemaAndTable.equals(schemaAndTable);
  }

  public CompletableFuture<?> publish(TransactionLogEntry transactionLogEntry) {
    return transactionLogEntryConverter
        .convert(transactionLogEntry)
        .map(cdcPublisher::apply)
        .orElse(CompletableFuture.completedFuture(null));
  }
}
