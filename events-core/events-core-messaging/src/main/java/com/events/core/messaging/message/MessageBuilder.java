package com.events.core.messaging.message;

import java.util.HashMap;
import java.util.Map;

public class MessageBuilder {

  protected String payload;

  protected Map<String, String> headers = new HashMap<>();

  public static MessageBuilder createInstance() {
    return new MessageBuilder();
  }

  public MessageBuilder withPayload(String payload) {
    this.payload = payload;
    return this;
  }

  public MessageBuilder withHeader(String name, String value) {
    this.headers.put(name, value);
    return this;
  }

  public MessageBuilder withExtraHeaders(String prefix, Map<String, String> headers) {
    for (Map.Entry<String, String> entry : headers.entrySet())
      this.headers.put(prefix + entry.getKey(), entry.getValue());
    return this;
  }

  public MessageBuilder withMessage(Message message) {
    this.payload = message.getPayload();
    this.headers = message.getHeaders();
    return this;
  }

  public Message build() {
    return new MessageImpl(payload, headers);
  }
}
