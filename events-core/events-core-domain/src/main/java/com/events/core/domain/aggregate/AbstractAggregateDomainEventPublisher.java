package com.events.core.domain.aggregate;

import com.events.core.domain.common.DomainEvent;
import com.events.core.domain.publisher.DomainEventPublisher;

import java.util.List;
import java.util.function.Function;

public abstract class AbstractAggregateDomainEventPublisher<A, E extends DomainEvent> {

  private final Function<A, Object> idSupplier;
  private final DomainEventPublisher domainEventPublisher;
  private final Class<A> aggregateType;

  protected AbstractAggregateDomainEventPublisher(
      DomainEventPublisher domainEventPublisher, Class<A> aggregateType, Function<A, Object> idSupplier) {
    this.domainEventPublisher = domainEventPublisher;
    this.aggregateType = aggregateType;
    this.idSupplier = idSupplier;
  }

  public Class<A> getAggregateType() {
    return aggregateType;
  }

  public void publish(A aggregate, List<E> events) {
    domainEventPublisher.publish(aggregateType, idSupplier.apply(aggregate), (List<DomainEvent>) events);
  }
}
