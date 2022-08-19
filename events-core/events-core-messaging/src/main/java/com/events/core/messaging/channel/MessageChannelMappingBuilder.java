package com.events.core.messaging.channel;

import java.util.HashMap;
import java.util.Map;

public class MessageChannelMappingBuilder {

  private Map<String, String> mappings = new HashMap<>();

  public static MessageChannelMappingBuilder createInstance() {
    return new MessageChannelMappingBuilder();
  }

  public MessageChannelMappingBuilder withMapping(String from, String to) {
    this.mappings.put(from, to);
    return this;
  }

  public MessageChannelMapping build() {
    return new DefaultMessageChannelMapping(mappings);
  }
}
