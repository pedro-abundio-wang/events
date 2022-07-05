package com.events.core.commands.subscriber;

import com.events.core.commands.common.CommandMessageHeaders;
import com.events.core.commands.common.path.ResourcePath;
import com.events.core.commands.common.path.ResourcePathPattern;
import com.events.core.messaging.message.Message;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class CommandHandler {

  private final String channel;
  private final Optional<String> resource;
  private final Class commandClass;
  private final BiFunction<CommandMessage<Object>, PathVariables, List<Message>> handler;

  public <C> CommandHandler(
      String channel,
      Optional<String> resource,
      Class<C> commandClass,
      BiFunction<CommandMessage<C>, PathVariables, List<Message>> handler) {
    this.channel = channel;
    this.resource = resource;
    this.commandClass = commandClass;
    this.handler = (cm, pv) -> handler.apply((CommandMessage<C>) cm, pv);
  }

  public String getChannel() {
    return channel;
  }

  public boolean handles(Message message) {
    return commandTypeMatches(message) && resourceMatches(message);
  }

  private boolean resourceMatches(Message message) {
    return !resource.isPresent()
        || message
            .getHeader(CommandMessageHeaders.RESOURCE)
            .map(m -> resourceMatches(m, resource.get()))
            .orElse(false);
  }

  private boolean commandTypeMatches(Message message) {
    return commandClass
        .getName()
        .equals(message.getRequiredHeader(CommandMessageHeaders.COMMAND_TYPE));
  }

  private boolean resourceMatches(String messageResource, String methodPath) {
    ResourcePathPattern r = ResourcePathPattern.parse(methodPath);
    ResourcePath mr = ResourcePath.parse(messageResource);
    return r.isSatisfiedBy(mr);
  }

  public Class getCommandClass() {
    return commandClass;
  }

  public Optional<String> getResource() {
    return resource;
  }

  public List<Message> invokeMethod(CommandMessage commandMessage, Map<String, String> pathVars) {
    return handler.apply(commandMessage, new PathVariables(pathVars));
  }
}
