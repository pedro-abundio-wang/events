package com.events.core.domain.subscriber;

import com.events.core.domain.common.DomainEvent;
import com.events.core.domain.common.DomainEventMessageHeaders;
import com.events.core.messaging.message.Message;

import java.util.function.Consumer;

public class DomainEventHandler {

  private final String aggregateType;
  private final Class<DomainEvent> domainEventClass;
  private final Consumer<DomainEventEnvelope<DomainEvent>> handler;

  public DomainEventHandler(
      String aggregateType,
      Class<DomainEvent> domainEventClass,
      Consumer<DomainEventEnvelope<DomainEvent>> handler) {
    this.aggregateType = aggregateType;
    this.domainEventClass = domainEventClass;
    this.handler = handler;
  }

  public boolean handles(Message message) {
    return aggregateType.equals(message.getRequiredHeader(DomainEventMessageHeaders.AGGREGATE_TYPE))
        && domainEventClass
            .getName()
            .equals(message.getRequiredHeader(DomainEventMessageHeaders.DOMAIN_EVENT_TYPE));
  }

  public void invoke(DomainEventEnvelope<DomainEvent> dee) {
    handler.accept(dee);
  }

  public String getAggregateType() {
    return aggregateType;
  }

  public Class<DomainEvent> getDomainEventClass() {
    return domainEventClass;
  }
}
