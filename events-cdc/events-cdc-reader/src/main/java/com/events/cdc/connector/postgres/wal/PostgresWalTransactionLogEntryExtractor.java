package com.events.cdc.connector.postgres.wal;

import com.events.cdc.connector.db.transaction.log.entry.TransactionLogEntry;
import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogFileOffset;

import java.util.Arrays;
import java.util.List;

public class PostgresWalTransactionLogEntryExtractor {

  public TransactionLogEntry extract(PostgresWalChange postgresWalChange) {
    List<String> columns = Arrays.asList(postgresWalChange.getColumnnames());
    List<String> values = Arrays.asList(postgresWalChange.getColumnvalues());
    return new TransactionLogEntry() {
      @Override
      public Object getColumn(String name) {
        return values.get(columns.indexOf(name));
      }

      @Override
      public TransactionLogFileOffset getTransactionLogFileOffset() {
        return null;
      }
    };
  }
}
