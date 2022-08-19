### Domain Events

Publish domain events using `DomainEventPublisher`:

```java
public interface DomainEventPublisher {
    void publish(String aggregateType, Object aggregateId, List<DomainEvent> domainEvents);
}
```

Subscribe domain events using `DomainEventDispatcher`:

```java
public class DomainEventDispatcher {
    public DomainEventDispatcher(
            String eventDispatcherId,
            DomainEventHandlers domainEventHandlers,
            MessageSubscriber messageSubscriber,
            DomainEventNameMapping domainEventNameMapping) {
        this.eventDispatcherId = eventDispatcherId;
        this.domainEventHandlers = domainEventHandlers;
        this.messageSubscriber = messageSubscriber;
        this.domainEventNameMapping = domainEventNameMapping;
    }
}
```

Handle domain events using `DomainEventHandlers`:

```java
public class OrderEventSubscriber {

    public DomainEventHandlers domainEventHandlers() {
        return DomainEventHandlersBuilder.forAggregateType(OrderServiceChannels.EVENT_CHANNEL)
                .onEvent(OrderCreatedEvent.class, this::handleOrderCreatedEvent)
                .onEvent(OrderAuthorizedEvent.class, this::handleOrderAuthorizedEvent)
                .onEvent(OrderCancelledEvent.class, this::handleOrderCancelledEvent)
                .onEvent(OrderRejectedEvent.class, this::handleOrderRejectedEvent)
                .build();
    }

    private void handleOrderCreatedEvent(DomainEventEnvelope<RestaurantCreated> dee) {
    }

    public void handleOrderAuthorizedEvent(DomainEventEnvelope<RestaurantMenuRevised> dee) {
    }
}
```

### Commands

Publish command using `CommandPublisher`:

```java
public interface CommandPublisher {
    String publish(String channel, Command command, String replyTo, Map<String, String> headers);
}
```

Subscribe commands using `CommandDispatcher`:

```java
public class CommandDispatcher {
    public CommandDispatcher(
            String commandDispatcherId,
            CommandHandlers commandHandlers,
            MessageSubscriber messageSubscriber,
            MessagePublisher messagePublisher) {
        this.commandDispatcherId = commandDispatcherId;
        this.commandHandlers = commandHandlers;
        this.messageSubscriber = messageSubscriber;
        this.messagePublisher = messagePublisher;
    }
}

```

Handle commands and send a reply using `CommandHandlers`:

```java
public class OrderCommandHandlers {

    public CommandHandlers commandHandlers() {
        return CommandHandlersBuilder.fromChannel("orderService")
                .onMessage(ApproveOrderCommand.class, this::approveOrder)
                .onMessage(RejectOrderCommand.class, this::rejectOrder)
                .onMessage(BeginCancelCommand.class, this::beginCancel)
                .onMessage(UndoBeginCancelCommand.class, this::undoCancel)
                .onMessage(ConfirmCancelOrderCommand.class, this::confirmCancel)
                .onMessage(BeginReviseOrderCommand.class, this::beginReviseOrder)
                .onMessage(UndoBeginReviseOrderCommand.class, this::undoPendingRevision)
                .onMessage(ConfirmReviseOrderCommand.class, this::confirmRevision)
                .build();
    }

    public Message approveOrder(CommandMessage<ApproveOrderCommand> cm) {
        long orderId = cm.getCommand().getOrderId();
        orderService.approveOrder(orderId);
        return withSuccess();
    }
}
```

### Transactional messaging

Publish a message using `MessagePublisher`:

```java
public interface MessagePublisher {
    void publish(String destination, Message message);
}
```

Subscribe messages using `MessageSubscriber`:

```java
public interface MessageSubscriber {
    MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler);
}
```