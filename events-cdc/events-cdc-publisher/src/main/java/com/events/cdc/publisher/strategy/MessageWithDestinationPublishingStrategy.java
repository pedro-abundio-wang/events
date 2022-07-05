package com.events.cdc.publisher.strategy;

import com.events.cdc.connector.db.transaction.log.messaging.MessageWithDestination;
import com.events.cdc.publisher.strategy.PublishingStrategy;

import java.util.Optional;

public class MessageWithDestinationPublishingStrategy
    implements PublishingStrategy<MessageWithDestination> {

  @Override
  public String partitionKeyFor(MessageWithDestination messageWithDestination) {
    return messageWithDestination.getPartitionId().orElseGet(messageWithDestination::getId);
  }

  @Override
  public String channelFor(MessageWithDestination messageWithDestination) {
    return messageWithDestination.getDestination();
  }

  @Override
  public String toJson(MessageWithDestination messageWithDestination) {
    return messageWithDestination.toJson();
  }

  @Override
  public Optional<Long> getCreateTime(MessageWithDestination messageWithDestination) {
    return Optional.empty(); // TODO
  }
}
