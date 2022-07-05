package com.events.cdc.publisher.filter;

import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogFileOffset;

public interface PublishingFilter {
  boolean shouldBePublished(
      TransactionLogFileOffset sourceTransactionLogFileOffset, String destinationTopic);
}
