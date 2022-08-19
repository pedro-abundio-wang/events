package com.events.cdc.connector.db.transaction.log.entry;

import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogFileOffset;

public interface TransactionLogEntry {

  Object getColumn(String name);

  TransactionLogFileOffset getTransactionLogFileOffset();

  default boolean getBooleanColumn(String name) {
    Object columnValue = getColumn(name);

    if (columnValue instanceof Number)
      return ((Number) getColumn(name)).intValue() != 0; // Integer - mysql, Short - mssql
    if (columnValue instanceof String)
      return Integer.parseInt((String) getColumn(name)) != 0; // String - postgresql

    throw new IllegalArgumentException(
        String.format(
            "Unexpected type %s of column %s, should be int or stringified int",
            columnValue.getClass(), name));
  }

  default Long getLongColumn(String name) {
    Object columnValue = getColumn(name);

    if (columnValue == null) {
      return null;
    }

    if (columnValue instanceof Long) return (Long) columnValue; // mysql
    if (columnValue instanceof String) return Long.parseLong((String) columnValue); // postgresql

    throw new IllegalArgumentException(
        String.format(
            "Unexpected type %s of column %s, should be bigint or stringified bigint",
            columnValue.getClass(), name));
  }

  default String getStringColumn(String name) {
    Object columnValue = getColumn(name);

    if (columnValue == null) {
      return null;
    }

    if (columnValue instanceof String) return (String) columnValue;

    throw new IllegalArgumentException(
        String.format(
            "Unexpected type %s of column %s, should be String", columnValue.getClass(), name));
  }
}
