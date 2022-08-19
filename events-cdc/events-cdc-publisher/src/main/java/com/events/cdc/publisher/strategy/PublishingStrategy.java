package com.events.cdc.publisher.strategy;

import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogMessage;

import java.util.Optional;

public interface PublishingStrategy<TLM extends TransactionLogMessage> {

  String partitionKeyFor(TLM transactionLogMessage);

  String channelFor(TLM transactionLogMessage);

  String toJson(TLM transactionLogMessage);

  Optional<Long> getCreateTime(TLM transactionLogMessage);
}
