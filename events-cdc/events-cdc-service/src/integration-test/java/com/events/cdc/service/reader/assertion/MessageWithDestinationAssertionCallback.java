package com.events.cdc.service.reader.assertion;

import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogMessage;

public interface MessageWithDestinationAssertionCallback<TLM extends TransactionLogMessage> {

    void onMessageSent(TLM transactionLogMessage);

    default int waitIterations() {
        return 20;
    }

    default int iterationTimeoutMilliseconds() {
        return 500;
    }

    default boolean shouldAddToQueue(TLM transactionLogMessage) {
        return true;
    }
}
