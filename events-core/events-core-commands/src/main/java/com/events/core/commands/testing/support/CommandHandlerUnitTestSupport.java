package com.events.core.commands.testing.support;

import com.events.common.json.mapper.JsonMapper;
import com.events.core.commands.common.Command;
import com.events.core.commands.common.CommandReplyMessageHeaders;
import com.events.core.commands.publisher.CommandPublisher;
import com.events.core.commands.publisher.CommandPublisherImpl;
import com.events.core.commands.subscriber.CommandDispatcher;
import com.events.core.commands.subscriber.CommandHandlers;
import com.events.core.commands.subscriber.CommandMessage;
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

public class CommandHandlerUnitTestSupport {

  private MessageHandler handler;
  private CommandDispatcher dispatcher;
  private SimpleIdGenerator idGenerator = new SimpleIdGenerator();

  private String replyDestination;
  private Message replyMessage;
  private CommandPublisher commandPublisher;

  public static CommandHandlerUnitTestSupport given() {
    return new CommandHandlerUnitTestSupport();
  }

  public CommandHandlerUnitTestSupport commandHandlers(CommandHandlers commandHandlers) {
    this.dispatcher =
        new CommandDispatcher(
            "mockCommandDispatcher-" + System.currentTimeMillis(),
            commandHandlers,
            new MessageSubscriber() {
              @Override
              public MessageSubscription subscribe(
                  String subscriberId, Set<String> channels, MessageHandler handler) {
                CommandHandlerUnitTestSupport.this.handler = handler;
                return () -> {};
              }

              @Override
              public String getId() {
                return null;
              }

              @Override
              public void close() {}
            },
            (destination, message) -> {
              CommandHandlerUnitTestSupport.this.replyDestination = destination;
              CommandHandlerUnitTestSupport.this.replyMessage = message;
            });

    dispatcher.initialize();
    commandPublisher =
        new CommandPublisherImpl(
            (destination, message) -> {
              String id = idGenerator.generateId().toString();
              message.getHeaders().put(Message.ID, id);
              dispatcher.messageHandler(message);
            });

    return this;
  }

  public CommandHandlerUnitTestSupport when() {
    return this;
  }

  public CommandHandlerUnitTestSupport then() {
    return this;
  }

  public CommandHandlerUnitTestSupport receives(Command command) {
    commandPublisher.publish("don't care", command, "reply_to", Collections.emptyMap());
    return this;
  }

  public CommandHandlerUnitTestSupport verify(Consumer<Message> c) {
    return verifyReply(c);
  }

  public CommandHandlerUnitTestSupport verifyReply(Consumer<Message> c) {
    c.accept(replyMessage);
    return this;
  }

  public <CH, CT extends Command, RT> CommandHandlerUnitTestSupport expectCommandHandlerInvoked(
      CH commandHandlers,
      BiConsumer<CH, CommandMessage<CT>> c,
      BiConsumer<CommandMessage<CT>, CommandHandlerReply<RT>> consumer) {
    ArgumentCaptor<CommandMessage<CT>> arg = ArgumentCaptor.forClass(CommandMessage.class);
    c.accept(Mockito.verify(commandHandlers), arg.capture());
    consumer.accept(arg.getValue(), makeCommandHandlerReply(replyMessage));
    return this;
  }

  private <RT> CommandHandlerReply<RT> makeCommandHandlerReply(Message replyMessage) {
    return new CommandHandlerReply<>(
        JsonMapper.fromJsonByName(
            replyMessage.getPayload(),
            replyMessage.getRequiredHeader(CommandReplyMessageHeaders.REPLY_TYPE)),
        replyMessage);
  }
}
