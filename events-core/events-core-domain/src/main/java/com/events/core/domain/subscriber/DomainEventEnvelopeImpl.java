package com.events.core.domain.subscriber;

import com.events.core.domain.common.DomainEvent;
import com.events.core.messaging.message.Message;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class DomainEventEnvelopeImpl<E extends DomainEvent> implements DomainEventEnvelope<E> {

  private final Message message;
  private final String aggregateType;
  private final String aggregateId;
  private final String domainEventId;
  private final E domainEvent;

  public DomainEventEnvelopeImpl(
      Message message,
      String aggregateType,
      String aggregateId,
      String domainEventId,
      E domainEvent) {
    this.message = message;
    this.aggregateType = aggregateType;
    this.aggregateId = aggregateId;
    this.domainEventId = domainEventId;
    this.domainEvent = domainEvent;
  }

  @Override
  public String getAggregateId() {
    return aggregateId;
  }

  @Override
  public Message getMessage() {
    return message;
  }

  @Override
  public E getDomainEvent() {
    return domainEvent;
  }

  @Override
  public String getAggregateType() {
    return aggregateType;
  }

  @Override
  public String getDomainEventId() {
    return domainEventId;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
