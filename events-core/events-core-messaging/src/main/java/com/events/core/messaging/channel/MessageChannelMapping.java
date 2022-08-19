package com.events.core.messaging.channel;

public interface MessageChannelMapping {
  String transform(String destination);
}
