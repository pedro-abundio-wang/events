package com.events.core.messaging.channel;

import java.util.HashMap;
import java.util.Map;

public class DefaultMessageChannelMapping implements MessageChannelMapping {

  private final Map<String, String> mappings;

  public DefaultMessageChannelMapping(Map<String, String> mappings) {
    this.mappings = mappings;
  }

  public static class DefaultMessageChannelMappingBuilder {

    private Map<String, String> mappings = new HashMap<>();

    public DefaultMessageChannelMappingBuilder with(String from, String to) {
      mappings.put(from, to);
      return this;
    }

    public MessageChannelMapping build() {
      return new DefaultMessageChannelMapping(mappings);
    }
  }

  public static DefaultMessageChannelMappingBuilder builder() {
    return new DefaultMessageChannelMappingBuilder();
  }

  @Override
  public String transform(String destination) {
    return mappings.getOrDefault(destination, destination);
  }
}
