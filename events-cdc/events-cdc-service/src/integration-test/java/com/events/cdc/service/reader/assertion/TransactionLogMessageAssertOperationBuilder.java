package com.events.cdc.service.reader.assertion;

import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogMessage;

public interface TransactionLogMessageAssertOperationBuilder<TLM extends TransactionLogMessage> {
  TransactionLogMessageAssertOperation<TLM> build();
}
