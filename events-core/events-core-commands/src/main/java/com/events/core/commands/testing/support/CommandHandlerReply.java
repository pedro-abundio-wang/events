package com.events.core.commands.testing.support;

import com.events.core.messaging.message.Message;

public class CommandHandlerReply<T> {

  private final T reply;
  private final Message replyMessage;

  public CommandHandlerReply(T reply, Message replyMessage) {
    this.reply = reply;
    this.replyMessage = replyMessage;
  }

  public T getReply() {
    return reply;
  }

  public Message getReplyMessage() {
    return replyMessage;
  }
}
