package com.events.cdc.connector.db.transaction.log.messaging;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class TransactionLogFileOffset {

  // only used in mysql-binlog

  private String transactionLogFilename;
  private long offset;
  private int rowsToSkip;

  public TransactionLogFileOffset() {}

  public TransactionLogFileOffset(String transactionLogFilename, long offset) {
    this(transactionLogFilename, offset, 0);
  }

  public TransactionLogFileOffset(String transactionLogFilename, long offset, int rowsToSkip) {
    this.transactionLogFilename = transactionLogFilename;
    this.offset = offset;
    this.rowsToSkip = rowsToSkip;
  }

  public String getTransactionLogFilename() {
    return transactionLogFilename;
  }

  public void setTransactionLogFilename(String transactionLogFilename) {
    this.transactionLogFilename = transactionLogFilename;
  }

  public long getOffset() {
    return offset;
  }

  public void setOffset(long offset) {
    this.offset = offset;
  }

  public int getRowsToSkip() {
    return rowsToSkip;
  }

  public void setRowsToSkip(int rowsToSkip) {
    this.rowsToSkip = rowsToSkip;
  }

  public boolean isSameOrAfter(TransactionLogFileOffset transactionLogFileOffset) {
    if (this.equals(transactionLogFileOffset)) return true;
    if (this.getTransactionLogFilename().equals(transactionLogFileOffset.getTransactionLogFilename())) {
      if (this.getOffset() > transactionLogFileOffset.getOffset()) {
        return true;
      }
    } else {
      if (this.getTransactionLogFilename().compareTo(transactionLogFileOffset.getTransactionLogFilename())
          > 0) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
