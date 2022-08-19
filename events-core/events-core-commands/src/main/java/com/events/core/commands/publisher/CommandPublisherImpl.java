package com.events.core.commands.publisher;

import com.events.common.json.mapper.JsonMapper;
import com.events.core.commands.common.Command;
import com.events.core.commands.common.CommandMessageHeaders;
import com.events.core.messaging.message.Message;
import com.events.core.messaging.message.MessageBuilder;
import com.events.core.messaging.publisher.MessagePublisher;

import java.util.Map;

public class CommandPublisherImpl implements CommandPublisher {

  private final MessagePublisher messagePublisher;

  public CommandPublisherImpl(MessagePublisher messagePublisher) {
    this.messagePublisher = messagePublisher;
  }

  @Override
  public String publish(
      String channel, Command command, String replyTo, Map<String, String> headers) {
    return publish(channel, null, command, replyTo, headers);
  }

  @Override
  public String publish(
      String channel,
      String resource,
      Command command,
      String replyTo,
      Map<String, String> headers) {
    Message message = makeMessageForCommand(channel, resource, command, replyTo, headers);
    messagePublisher.publish(channel, message);
    return message.getId();
  }

  private Message makeMessageForCommand(
      String channel,
      String resource,
      Command command,
      String replyTo,
      Map<String, String> headers) {
    MessageBuilder builder =
        MessageBuilder.createInstance()
            .withPayload(JsonMapper.toJson(command))
            .withExtraHeaders("extra-", headers)
            .withHeader(CommandMessageHeaders.DESTINATION, channel)
            .withHeader(CommandMessageHeaders.COMMAND_TYPE, command.getClass().getName())
            .withHeader(CommandMessageHeaders.REPLY_TO, replyTo);

    if (resource != null) builder.withHeader(CommandMessageHeaders.RESOURCE, resource);

    return builder.build();
  }
}
