package io.eventuate.tram.sagas.orchestration;

import com.events.core.domain.common.DomainEvent;
import com.events.core.domain.subscriber.DomainEventEnvelope;

public interface SagaStateMachineEventHandler<Data, EventClass extends DomainEvent> {

  SagaActions<Data> apply(Data data, DomainEventEnvelope<EventClass> event);
}
