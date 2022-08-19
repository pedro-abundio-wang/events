package com.events.core.commands.subscriber;

import com.events.core.messaging.message.Message;

import java.util.List;

public class CommandExceptionHandler {
  public List<Message> invoke(Throwable cause) {
    throw new UnsupportedOperationException();
  }
}
