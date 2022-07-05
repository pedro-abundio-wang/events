package com.events.core.domain.subscriber;

import com.events.core.domain.common.DomainEventNameMapping;
import com.events.core.messaging.subscriber.MessageSubscriber;

public class DomainEventDispatcherFactory {

  protected MessageSubscriber messageSubscriber;
  protected DomainEventNameMapping domainEventNameMapping;

  public DomainEventDispatcherFactory(
      MessageSubscriber messageSubscriber, DomainEventNameMapping domainEventNameMapping) {
    this.messageSubscriber = messageSubscriber;
    this.domainEventNameMapping = domainEventNameMapping;
  }

  public DomainEventDispatcher make(
      String eventDispatcherId, DomainEventHandlers domainEventHandlers) {
    return new DomainEventDispatcher(
        eventDispatcherId, domainEventHandlers, messageSubscriber, domainEventNameMapping);
  }
}
