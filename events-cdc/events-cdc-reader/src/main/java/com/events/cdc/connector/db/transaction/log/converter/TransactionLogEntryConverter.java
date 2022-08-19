package com.events.cdc.connector.db.transaction.log.converter;

import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogMessage;
import com.events.cdc.connector.db.transaction.log.entry.TransactionLogEntry;

import java.util.Optional;

public interface TransactionLogEntryConverter<TLM extends TransactionLogMessage> {
  Optional<TLM> convert(TransactionLogEntry transactionLogEntry);
}
