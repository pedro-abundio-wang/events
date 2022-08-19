package com.events.cdc.service.config.others;

import com.events.messaging.activemq.common.ChannelType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("events.channel")
public class EventsChannelProperties {

  Map<String, ChannelType> channelTypes;

  public Map<String, ChannelType> getChannelTypes() {
    return channelTypes;
  }

  public void setChannelTypes(Map<String, ChannelType> channelTypes) {
    this.channelTypes = channelTypes;
  }
}
