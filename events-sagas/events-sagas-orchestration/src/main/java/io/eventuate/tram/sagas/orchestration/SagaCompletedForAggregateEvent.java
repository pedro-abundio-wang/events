package io.eventuate.tram.sagas.orchestration;

import com.events.core.domain.common.DomainEvent;

public class SagaCompletedForAggregateEvent implements DomainEvent {
  public SagaCompletedForAggregateEvent(String sagaId) {}
}
