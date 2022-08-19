package com.events.core.domain.subscriber;

import com.events.core.domain.common.DomainEvent;
import com.events.core.messaging.message.Message;

public interface DomainEventEnvelope<E extends DomainEvent> {

  String getAggregateType();

  String getAggregateId();

  Message getMessage();

  String getDomainEventId();

  E getDomainEvent();
}
