package com.events.common.jdbc.transaction;

import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

public class EventsSpringTransactionTemplate implements EventsTransactionTemplate {

  private final TransactionTemplate transactionTemplate;

  public EventsSpringTransactionTemplate(TransactionTemplate transactionTemplate) {
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public <T> T executeInTransaction(Supplier<T> callback) {
    return transactionTemplate.execute(status -> callback.get());
  }
}
