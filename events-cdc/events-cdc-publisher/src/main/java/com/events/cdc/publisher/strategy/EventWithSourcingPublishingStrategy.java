package com.events.cdc.publisher.strategy;

import com.events.cdc.connector.db.transaction.log.messaging.EventWithSourcing;
import com.events.common.json.mapper.JsonMapper;
import com.events.messaging.kafka.common.AggregateTopicMapping;

import java.util.Optional;

public class EventWithSourcingPublishingStrategy implements PublishingStrategy<EventWithSourcing> {

  @Override
  public String partitionKeyFor(EventWithSourcing eventWithSourcing) {
    return eventWithSourcing.getEntityId();
  }

  @Override
  public String channelFor(EventWithSourcing eventWithSourcing) {
    return AggregateTopicMapping.aggregateTypeToTopic(eventWithSourcing.getEntityType());
  }

  @Override
  public String toJson(EventWithSourcing eventWithSourcing) {
    return JsonMapper.toJson(eventWithSourcing);
  }

  @Override
  public Optional<Long> getCreateTime(EventWithSourcing eventWithSourcing) {
    // TODO: Original implementation is based in Int128 id, which should not be required.
    //       return Optional.of(Int128.fromString(publishedEvent.getId()).getHi());
    return Optional.empty();
  }
}
