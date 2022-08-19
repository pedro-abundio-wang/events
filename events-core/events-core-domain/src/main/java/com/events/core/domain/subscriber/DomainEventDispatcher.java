package com.events.core.domain.subscriber;

import com.events.common.json.mapper.JsonMapper;
import com.events.core.domain.common.DomainEvent;
import com.events.core.domain.common.DomainEventMessageHeaders;
import com.events.core.domain.common.DomainEventNameMapping;
import com.events.core.messaging.message.Message;
import com.events.core.messaging.subscriber.MessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Optional;

public class DomainEventDispatcher {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final String domainEventDispatcherId;
  private final DomainEventHandlers domainEventHandlers;
  private final MessageSubscriber messageSubscriber;
  private final DomainEventNameMapping domainEventNameMapping;

  public DomainEventDispatcher(
      String domainEventDispatcherId,
      DomainEventHandlers domainEventHandlers,
      MessageSubscriber messageSubscriber,
      DomainEventNameMapping domainEventNameMapping) {
    this.domainEventDispatcherId = domainEventDispatcherId;
    this.domainEventHandlers = domainEventHandlers;
    this.messageSubscriber = messageSubscriber;
    this.domainEventNameMapping = domainEventNameMapping;
  }

  @PostConstruct
  public void initialize() {
    logger.info("Initializing domain event dispatcher");
    messageSubscriber.subscribe(
        domainEventDispatcherId, domainEventHandlers.getAggregateTypes(), this::messageHandler);
    logger.info("Initialized domain event dispatcher");
  }

  public void messageHandler(Message message) {

    logger.trace("Received message {} {}", domainEventDispatcherId, message);

    Optional<DomainEventHandler> handler = domainEventHandlers.findTargetMethod(message);

    if (!handler.isPresent()) {
      throw new RuntimeException("No handler for " + message);
    }

    DomainEvent domainEvent =
        JsonMapper.fromJson(message.getPayload(), handler.get().getDomainEventClass());

    handler
        .get()
        .invoke(
            new DomainEventEnvelopeImpl<>(
                message,
                message.getRequiredHeader(DomainEventMessageHeaders.AGGREGATE_TYPE),
                message.getRequiredHeader(DomainEventMessageHeaders.AGGREGATE_ID),
                message.getRequiredHeader(Message.ID),
                domainEvent));
  }
}
