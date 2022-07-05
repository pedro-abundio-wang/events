package com.events.core.commands.common;

public class CommandMessageHeaders {

  public static final String COMMAND_HEADER_PREFIX = "command_";

  public static final String COMMAND_TYPE = COMMAND_HEADER_PREFIX + "type";

  public static final String RESOURCE = COMMAND_HEADER_PREFIX + "resource";

  public static final String DESTINATION = COMMAND_HEADER_PREFIX + "destination";

  public static final String REPLY_TO = COMMAND_HEADER_PREFIX + "reply_to";

  public static final String COMMAND_REPLY_PREFIX = "commandreply_";

  public static String inReply(String header) {
    assert header.startsWith(COMMAND_HEADER_PREFIX);
    return COMMAND_REPLY_PREFIX + header.substring(COMMAND_HEADER_PREFIX.length());
  }
}
