package com.events.core.messaging.subscriber;

import com.events.common.jdbc.transaction.EventsTransactionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionalNoopDuplicateMessageDetector implements DuplicateMessageDetector {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final EventsTransactionTemplate transactionTemplate;

  public TransactionalNoopDuplicateMessageDetector(EventsTransactionTemplate transactionTemplate) {
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public boolean isDuplicate(String consumerId, String messageId) {
    return false;
  }

  @Override
  public void doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Runnable callback) {
    transactionTemplate.executeInTransaction(
        () -> {
          try {
            callback.run();
            return null;
          } catch (Throwable e) {
            logger.error("Got exception - marking for rollback only", e);
            throw e;
          }
        });
  }
}
