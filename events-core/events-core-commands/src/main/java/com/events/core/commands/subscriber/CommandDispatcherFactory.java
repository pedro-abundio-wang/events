package com.events.core.commands.subscriber;

import com.events.core.messaging.publisher.MessagePublisher;
import com.events.core.messaging.subscriber.MessageSubscriber;

public class CommandDispatcherFactory {

  private final MessageSubscriber messageSubscriber;
  private final MessagePublisher messagePublisher;

  public CommandDispatcherFactory(
      MessageSubscriber messageSubscriber, MessagePublisher messagePublisher) {
    this.messageSubscriber = messageSubscriber;
    this.messagePublisher = messagePublisher;
  }

  public CommandDispatcher make(String commandDispatcherId, CommandHandlers commandHandlers) {
    return new CommandDispatcher(
        commandDispatcherId, commandHandlers, messageSubscriber, messagePublisher);
  }
}
