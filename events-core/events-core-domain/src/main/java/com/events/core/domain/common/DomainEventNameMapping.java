package com.events.core.domain.common;

public interface DomainEventNameMapping {

  String eventToExternalEventType(String aggregateType, DomainEvent event);

  String externalEventTypeToEventClassName(String aggregateType, String eventTypeHeader);

}
