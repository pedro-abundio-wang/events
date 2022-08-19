package com.events.core.domain.aggregate;

import com.events.core.domain.common.DomainEvent;

import java.util.Arrays;
import java.util.List;

public class DomainModelWithDomainEvents<A, E extends DomainEvent> {

  public final A model;
  public final List<E> events;

  public DomainModelWithDomainEvents(A model, List<E> events) {
    this.model = model;
    this.events = events;
  }

  public DomainModelWithDomainEvents(A model, E... events) {
    this.model = model;
    this.events = Arrays.asList(events);
  }
}
