package com.events.cdc.service.reader.assertion;

import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogMessage;

public interface TransactionLogMessageAssertOperation<TLM extends TransactionLogMessage> {

  void apply(TLM transactionLog);

  default void applyOnlyOnce(TLM transactionLog) {}
  ;
}
