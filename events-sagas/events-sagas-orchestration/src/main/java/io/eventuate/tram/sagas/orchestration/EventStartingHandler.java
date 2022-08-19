package io.eventuate.tram.sagas.orchestration;

import com.events.core.domain.common.DomainEvent;
import com.events.core.domain.subscriber.DomainEventEnvelope;

public interface EventStartingHandler<Data, EventClass extends DomainEvent> {
  void apply(Data data, DomainEventEnvelope<EventClass> event);
}
