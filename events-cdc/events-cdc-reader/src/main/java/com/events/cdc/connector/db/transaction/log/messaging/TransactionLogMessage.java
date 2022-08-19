package com.events.cdc.connector.db.transaction.log.messaging;

import java.util.Optional;

public interface TransactionLogMessage {
  Optional<TransactionLogFileOffset> getTransactionLogFileOffset();
}
