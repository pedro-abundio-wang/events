package com.events.core.commands.publisher;

import com.events.core.commands.common.Command;

import java.util.Map;

public interface CommandPublisher {

  String publish(String channel, Command command, String replyTo, Map<String, String> headers);

  String publish(
      String channel,
      String resource,
      Command command,
      String replyTo,
      Map<String, String> headers);
}
