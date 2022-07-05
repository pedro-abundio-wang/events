package com.events.cdc.connector.db.transaction.log.messaging;

import com.events.common.json.mapper.JsonMapper;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MessageWithDestination implements TransactionLogMessage {

  private final String destination;
  private String payload;
  private Map<String, String> headers;
  private TransactionLogFileOffset transactionLogFileOffset;

  public MessageWithDestination(
      String destination,
      String payload,
      Map<String, String> headers,
      TransactionLogFileOffset transactionLogFileOffset) {

    this.destination = destination;
    this.payload = payload;
    this.headers = headers;
    this.transactionLogFileOffset = transactionLogFileOffset;
  }

  public String getDestination() {
    return destination;
  }

  public String getPayload() {
    return payload;
  }

  public Optional<String> getPartitionId() {
    return getHeader("PARTITION_ID");
  }

  public Optional<String> getHeader(String name) {
    return Optional.ofNullable(headers.get(name));
  }

  public String getRequiredHeader(String name) {
    String s = headers.get(name);
    if (s == null)
      throw new RuntimeException("No such header: " + name + " in this message " + this);
    else return s;
  }

  public boolean hasHeader(String name) {
    return headers.containsKey(name);
  }

  public String getId() {
    return getRequiredHeader("id");
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public void setHeader(String name, String value) {
    if (headers == null) headers = new HashMap<>();
    headers.put(name, value);
  }

  public void removeHeader(String key) {
    headers.remove(key);
  }

  public String toJson() {
    return JsonMapper.toJson(ImmutableMap.of("payload", getPayload(), "headers", getHeaders()));
  }

  @Override
  public Optional<TransactionLogFileOffset> getTransactionLogFileOffset() {
    return Optional.ofNullable(transactionLogFileOffset);
  }
}
