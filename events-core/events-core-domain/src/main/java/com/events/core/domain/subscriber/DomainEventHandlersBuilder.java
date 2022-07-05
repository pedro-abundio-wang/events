package com.events.core.domain.subscriber;

import com.events.core.domain.common.DomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DomainEventHandlersBuilder {

  private String aggregateType;

  private final List<DomainEventHandler> handlers = new ArrayList<>();

  public DomainEventHandlersBuilder(String aggregateType) {
    this.aggregateType = aggregateType;
  }

  public static DomainEventHandlersBuilder forAggregateType(String aggregateType) {
    return new DomainEventHandlersBuilder(aggregateType);
  }

  public <E extends DomainEvent> DomainEventHandlersBuilder onEvent(
      Class<E> domainEventClass, Consumer<DomainEventEnvelope<E>> handler) {
    handlers.add(
        new DomainEventHandler(
            aggregateType,
            ((Class<DomainEvent>) domainEventClass),
            (dee) -> handler.accept((DomainEventEnvelope<E>) dee)));
    return this;
  }

  public DomainEventHandlersBuilder andForAggregateType(String aggregateType) {
    this.aggregateType = aggregateType;
    return this;
  }

  public DomainEventHandlers build() {
    return new DomainEventHandlers(handlers);
  }
}
