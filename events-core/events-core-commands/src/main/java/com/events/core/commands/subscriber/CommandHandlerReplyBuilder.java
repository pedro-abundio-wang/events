package com.events.core.commands.subscriber;

import com.events.common.json.mapper.JsonMapper;
import com.events.core.commands.common.outcome.CommandReplyOutcome;
import com.events.core.commands.common.outcome.Failure;
import com.events.core.commands.common.CommandReplyMessageHeaders;
import com.events.core.commands.common.outcome.Success;
import com.events.core.messaging.message.Message;
import com.events.core.messaging.message.MessageBuilder;

public class CommandHandlerReplyBuilder {

  private static <T> Message with(T reply, CommandReplyOutcome outcome) {
    MessageBuilder messageBuilder =
        MessageBuilder.createInstance()
            .withPayload(JsonMapper.toJson(reply))
            .withHeader(CommandReplyMessageHeaders.REPLY_OUTCOME, outcome.name())
            .withHeader(CommandReplyMessageHeaders.REPLY_TYPE, reply.getClass().getName());
    return messageBuilder.build();
  }

  public static Message withSuccess(Object reply) {
    return with(reply, CommandReplyOutcome.SUCCESS);
  }

  public static Message withSuccess() {
    return withSuccess(new Success());
  }

  public static Message withFailure() {
    return withFailure(new Failure());
  }

  public static Message withFailure(Object reply) {
    return with(reply, CommandReplyOutcome.FAILURE);
  }
}
