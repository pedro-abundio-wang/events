package com.events.common.jdbc.transaction;

import java.util.function.Supplier;

public interface EventsTransactionTemplate {
  <T> T executeInTransaction(Supplier<T> callback);
}
