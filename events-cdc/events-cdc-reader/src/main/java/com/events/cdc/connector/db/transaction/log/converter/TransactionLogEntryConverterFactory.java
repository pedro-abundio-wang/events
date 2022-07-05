package com.events.cdc.connector.db.transaction.log.converter;

import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogMessage;
import com.events.cdc.connector.db.transaction.log.converter.TransactionLogEntryConverter;

import java.util.function.Function;

public interface TransactionLogEntryConverterFactory<TLM extends TransactionLogMessage>
    extends Function<Long, TransactionLogEntryConverter<TLM>> {}
