package com.events.cdc.service.config.others;

import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogFileOffset;
import com.events.cdc.connector.db.transaction.log.offset.OffsetStore;

import java.util.Optional;

public interface TransactionLogFileOffsetStore extends OffsetStore<TransactionLogFileOffset> {

  Optional<TransactionLogFileOffset> getLastTransactionlogFileOffset();

  void save(TransactionLogFileOffset transactionLogFileOffset);
}
