package com.events.core.messaging.message;

import java.util.Map;
import java.util.Optional;

public interface Message {

  String ID = "id";

  String CHANNEL = "channel";

  String DATE = "date";

  String getId();

  Map<String, String> getHeaders();

  Optional<String> getHeader(String name);

  String getRequiredHeader(String name);

  boolean hasHeader(String name);

  void setHeaders(Map<String, String> headers);

  void setHeader(String name, String value);

  void removeHeader(String key);

  String getPayload();

  void setPayload(String payload);
}
