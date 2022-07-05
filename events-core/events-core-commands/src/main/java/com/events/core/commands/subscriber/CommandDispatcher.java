package com.events.core.commands.subscriber;

import com.events.common.json.mapper.JsonMapper;
import com.events.core.commands.common.CommandMessageHeaders;
import com.events.core.commands.common.outcome.Failure;
import com.events.core.commands.common.CommandReplyMessageHeaders;
import com.events.core.commands.common.path.ResourcePath;
import com.events.core.commands.common.path.ResourcePathPattern;
import com.events.core.messaging.message.Message;
import com.events.core.messaging.message.MessageBuilder;
import com.events.core.messaging.publisher.MessagePublisher;
import com.events.core.messaging.subscriber.MessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.EMPTY_MAP;
import static java.util.Collections.singletonList;

public class CommandDispatcher {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final String commandDispatcherId;
  private final CommandHandlers commandHandlers;
  private final MessageSubscriber messageSubscriber;
  private final MessagePublisher messagePublisher;

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

  @PostConstruct
  public void initialize() {
    logger.info("Initializing command dispatcher");
    messageSubscriber.subscribe(
        commandDispatcherId, commandHandlers.getChannels(), this::messageHandler);
    logger.info("Initialized command dispatcher");
  }

  public void messageHandler(Message message) {

    logger.trace("Received message {} {}", commandDispatcherId, message);

    Optional<CommandHandler> handler = commandHandlers.findTargetMethod(message);

    if (!handler.isPresent()) {
      throw new RuntimeException("No handler for " + message);
    }

    CommandHandler m = handler.get();

    Object param = convertPayload(m, message.getPayload());

    Map<String, String> correlationHeaders = correlationHeaders(message.getHeaders());

    Map<String, String> pathVars = getPathVars(message, m);

    Optional<String> defaultReplyChannel = message.getHeader(CommandMessageHeaders.REPLY_TO);

    List<Message> replies;
    try {
      CommandMessage cm = new CommandMessage(message.getId(), param, correlationHeaders, message);
      replies = invoke(m, cm, pathVars);
      logger.trace("Generated replies {} {} {}", commandDispatcherId, message, replies);
    } catch (Exception e) {
      logger.error(
          "Generated error {} {} {}", commandDispatcherId, message, e.getClass().getName());
      logger.error("Generated error", e);
      handleException(message, param, m, e, pathVars, defaultReplyChannel);
      return;
    }

    if (replies != null) {
      sendReplies(correlationHeaders, replies, defaultReplyChannel);
    } else {
      logger.trace("Null replies - not publishling");
    }
  }

  protected List<Message> invoke(
      CommandHandler commandHandler, CommandMessage cm, Map<String, String> pathVars) {
    return commandHandler.invokeMethod(cm, pathVars);
  }

  protected Object convertPayload(CommandHandler m, String payload) {
    Class<?> paramType = findCommandParameterType(m);
    return JsonMapper.fromJson(payload, paramType);
  }

  private Map<String, String> getPathVars(Message message, CommandHandler handler) {
    return handler
        .getResource()
        .flatMap(
            res -> {
              ResourcePathPattern r = ResourcePathPattern.parse(res);
              return message
                  .getHeader(CommandMessageHeaders.RESOURCE)
                  .map(
                      h -> {
                        ResourcePath mr = ResourcePath.parse(h);
                        return r.getPathVariableValues(mr);
                      });
            })
        .orElse(EMPTY_MAP);
  }

  private void sendReplies(
      Map<String, String> correlationHeaders,
      List<Message> replies,
      Optional<String> defaultReplyChannel) {
    for (Message reply : replies)
      messagePublisher.publish(
          destination(defaultReplyChannel),
          MessageBuilder.createInstance()
              .withMessage(reply)
              .withExtraHeaders("", correlationHeaders)
              .build());
  }

  private String destination(Optional<String> defaultReplyChannel) {
    return defaultReplyChannel.orElseGet(
        () -> {
          throw new RuntimeException();
        });
  }

  private Map<String, String> correlationHeaders(Map<String, String> headers) {
    Map<String, String> m =
        headers.entrySet().stream()
            .filter(e -> e.getKey().startsWith(CommandMessageHeaders.COMMAND_HEADER_PREFIX))
            .collect(
                Collectors.toMap(
                    e -> CommandMessageHeaders.inReply(e.getKey()), Map.Entry::getValue));
    m.put(CommandReplyMessageHeaders.IN_REPLY_TO, headers.get(Message.ID));
    return m;
  }

  private void handleException(
      Message message,
      Object param,
      CommandHandler commandHandler,
      Throwable cause,
      Map<String, String> pathVars,
      Optional<String> defaultReplyChannel) {
    Optional<CommandExceptionHandler> m =
        commandHandlers.findExceptionHandler(commandHandler, cause);

    logger.info("Handler for {} is {}", cause.getClass(), m);

    if (m.isPresent()) {
      List<Message> replies = m.get().invoke(cause);
      sendReplies(correlationHeaders(message.getHeaders()), replies, defaultReplyChannel);
    } else {
      List<Message> replies =
          singletonList(
              MessageBuilder.createInstance()
                  .withPayload(JsonMapper.toJson(new Failure()))
                  .build());
      sendReplies(correlationHeaders(message.getHeaders()), replies, defaultReplyChannel);
    }
  }

  private Class findCommandParameterType(CommandHandler m) {
    return m.getCommandClass();
  }
}
