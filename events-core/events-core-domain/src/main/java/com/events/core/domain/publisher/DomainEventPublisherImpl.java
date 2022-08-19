package com.events.core.domain.publisher;

import com.events.common.json.mapper.JsonMapper;
import com.events.core.domain.common.DomainEvent;
import com.events.core.domain.common.DomainEventMessageHeaders;
import com.events.core.domain.common.DomainEventNameMapping;
import com.events.core.messaging.message.Message;
import com.events.core.messaging.message.MessageBuilder;
import com.events.core.messaging.publisher.MessagePublisher;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DomainEventPublisherImpl implements DomainEventPublisher {

  private final MessagePublisher messagePublisher;

  private final DomainEventNameMapping domainEventNameMapping;

  public DomainEventPublisherImpl(
      MessagePublisher messagePublisher, DomainEventNameMapping domainEventNameMapping) {
    this.messagePublisher = messagePublisher;
    this.domainEventNameMapping = domainEventNameMapping;
  }

  @Override
  public void publish(String aggregateType, Object aggregateId, List<DomainEvent> domainEvents) {
    publish(aggregateType, aggregateId, Collections.emptyMap(), domainEvents);
  }

  @Override
  public void publish(
      String aggregateType,
      Object aggregateId,
      Map<String, String> headers,
      List<DomainEvent> domainEvents) {
    for (DomainEvent event : domainEvents) {
      messagePublisher.publish(
          aggregateType,
          makeMessageForDomainEvent(
              aggregateType,
              aggregateId,
              headers,
              event,
              domainEventNameMapping.eventToExternalEventType(aggregateType, event)));
    }
  }

  private Message makeMessageForDomainEvent(
      String aggregateType,
      Object aggregateId,
      Map<String, String> headers,
      DomainEvent event,
      String eventType) {
    String aggregateIdAsString = aggregateId.toString();
    return MessageBuilder.createInstance()
        .withPayload(JsonMapper.toJson(event))
        .withExtraHeaders("extra-", headers)
        .withHeader(DomainEventMessageHeaders.PARTITION_ID, aggregateIdAsString)
        .withHeader(DomainEventMessageHeaders.AGGREGATE_ID, aggregateIdAsString)
        .withHeader(DomainEventMessageHeaders.AGGREGATE_TYPE, aggregateType)
        .withHeader(DomainEventMessageHeaders.DOMAIN_EVENT_TYPE, eventType)
        .build();
  }
}
