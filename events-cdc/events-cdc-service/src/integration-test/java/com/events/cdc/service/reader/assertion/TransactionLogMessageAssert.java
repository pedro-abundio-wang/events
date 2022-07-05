package com.events.cdc.service.reader.assertion;

import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogMessage;
import com.events.common.util.Eventually;
import org.junit.Assert;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TransactionLogMessageAssert<TLM extends TransactionLogMessage> {

  private final int waitIterations;
  private final int iterationTimeoutMilliseconds;
  private final ConcurrentLinkedQueue<TLM> transactionLogMessages = new ConcurrentLinkedQueue<>();

  public TransactionLogMessageAssert(int waitIterations, int iterationTimeoutMilliseconds) {
    this.waitIterations = waitIterations;
    this.iterationTimeoutMilliseconds = iterationTimeoutMilliseconds;
  }

  public void assertMessaageReceived(TransactionLogMessageAssertOperation<TLM> assertOperation) {

    AtomicReference<Throwable> throwable = new AtomicReference<>();

    Eventually.run(
        waitIterations,
        iterationTimeoutMilliseconds,
        TimeUnit.MILLISECONDS,
        () -> {
          TLM transactionLog = transactionLogMessages.poll();
          Assert.assertNotNull(transactionLog);
          try {
            assertOperation.applyOnlyOnce(transactionLog);
          } catch (Throwable t) {
            throwable.set(t);
          }
          if (throwable.get() == null) {
            assertOperation.apply(transactionLog);
          }
        });

    if (throwable.get() != null) {
      throw new RuntimeException(throwable.get());
    }
  }

  public void addMessage(TLM transactionLog) {
    transactionLogMessages.add(transactionLog);
  }
}
