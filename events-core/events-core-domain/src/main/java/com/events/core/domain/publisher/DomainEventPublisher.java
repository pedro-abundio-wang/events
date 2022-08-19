package com.events.core.domain.publisher;

import com.events.core.domain.common.DomainEvent;

import java.util.List;
import java.util.Map;

public interface DomainEventPublisher {

  default void publish(Class<?> aggregateType, Object aggregateId, List<DomainEvent> domainEvents) {
    publish(aggregateType.getName(), aggregateId, domainEvents);
  }

  void publish(String aggregateType, Object aggregateId, List<DomainEvent> domainEvents);

  void publish(
      String aggregateType,
      Object aggregateId,
      Map<String, String> headers,
      List<DomainEvent> domainEvents);
}
