package com.events.core.domain.testing.support;

import com.events.core.domain.common.DefaultDomainEventNameMapping;
import com.events.core.domain.common.DomainEvent;
import com.events.core.domain.publisher.DomainEventPublisher;
import com.events.core.domain.publisher.DomainEventPublisherImpl;
import com.events.core.domain.subscriber.DomainEventDispatcher;
import com.events.core.domain.subscriber.DomainEventEnvelope;
import com.events.core.domain.subscriber.DomainEventHandlers;
import com.events.core.messaging.message.Message;
import com.events.core.messaging.subscriber.MessageHandler;
import com.events.core.messaging.subscriber.MessageSubscriber;
import com.events.core.messaging.subscriber.MessageSubscription;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.util.SimpleIdGenerator;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DomainEventHandlerUnitTestSupport {

  private MessageHandler handler;
  private String aggregateType;
  private Object aggregateId;
  private DomainEventDispatcher dispatcher;
  private final SimpleIdGenerator idGenerator = new SimpleIdGenerator();

  public static DomainEventHandlerUnitTestSupport given() {
    return new DomainEventHandlerUnitTestSupport();
  }

  public DomainEventHandlerUnitTestSupport eventHandlers(DomainEventHandlers domainEventHandlers) {

    this.dispatcher =
        new DomainEventDispatcher(
            "mockDomainEventDispatcher-" + System.currentTimeMillis(),
            domainEventHandlers,
            new MessageSubscriber() {
              @Override
              public MessageSubscription subscribe(
                  String subscriberId, Set<String> channels, MessageHandler handler) {
                DomainEventHandlerUnitTestSupport.this.handler = handler;
                return () -> {};
              }

              @Override
              public String getId() {
                return null;
              }

              @Override
              public void close() {}
            },
            new DefaultDomainEventNameMapping());

    dispatcher.initialize();
    return this;
  }

  public DomainEventHandlerUnitTestSupport when() {
    return this;
  }

  public DomainEventHandlerUnitTestSupport then() {
    return this;
  }

  public DomainEventHandlerUnitTestSupport aggregate(String aggregateType, Object aggregateId) {
    this.aggregateType = aggregateType;
    this.aggregateId = aggregateId;
    return this;
  }

  public DomainEventHandlerUnitTestSupport publishes(DomainEvent event) {
    DomainEventPublisher publisher =
        new DomainEventPublisherImpl(
            (destination, message) -> {
              String id = idGenerator.generateId().toString();
              message.getHeaders().put(Message.ID, id);
              handler.accept(message);
            },
            new DefaultDomainEventNameMapping());

    publisher.publish(aggregateType, aggregateId, Collections.singletonList(event));
    return this;
  }

  public DomainEventHandlerUnitTestSupport verify(Runnable r) {
    r.run();
    return this;
  }

  public <EH, EV extends DomainEvent> DomainEventHandlerUnitTestSupport expectEventHandlerInvoked(
      EH eventHandlers,
      BiConsumer<EH, DomainEventEnvelope<EV>> c,
      Consumer<DomainEventEnvelope<EV>> consumer) {
    ArgumentCaptor<DomainEventEnvelope<EV>> arg =
        ArgumentCaptor.forClass(DomainEventEnvelope.class);
    c.accept(Mockito.verify(eventHandlers), arg.capture());
    consumer.accept(arg.getValue());
    return this;
  }
}
