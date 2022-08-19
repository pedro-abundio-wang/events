package com.events.core.domain.subscriber;

import com.events.core.messaging.message.Message;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DomainEventHandlers {

  private final List<DomainEventHandler> handlers;

  public DomainEventHandlers(List<DomainEventHandler> handlers) {
    this.handlers = handlers;
  }

  public Set<String> getAggregateTypes() {
    return handlers.stream().map(DomainEventHandler::getAggregateType).collect(Collectors.toSet());
  }

  public Optional<DomainEventHandler> findTargetMethod(Message message) {
    return handlers.stream().filter(h -> h.handles(message)).findFirst();
  }
}
