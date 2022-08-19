package com.events.cdc.service.performance;

import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogFileOffset;
import com.events.cdc.service.config.others.TransactionLogFileOffsetStore;

import java.util.Optional;

public class TransactionLogFileOffsetStoreMock implements TransactionLogFileOffsetStore {

  Optional<TransactionLogFileOffset> transactionLogFileOffset = Optional.empty();
  boolean throwExceptionOnSave = false;
  boolean throwExceptionOnLoad = false;

  @Override
  public synchronized Optional<TransactionLogFileOffset> getLastTransactionlogFileOffset() {
    if (throwExceptionOnLoad) {
      throw new IllegalStateException("Loading offset is disabled");
    }
    return transactionLogFileOffset;
  }

  @Override
  public synchronized void save(TransactionLogFileOffset transactionLogFileOffset) {
    if (throwExceptionOnSave) {
      throw new IllegalStateException("Saving offset is disabled");
    }
    this.transactionLogFileOffset = Optional.ofNullable(transactionLogFileOffset);
  }
}
