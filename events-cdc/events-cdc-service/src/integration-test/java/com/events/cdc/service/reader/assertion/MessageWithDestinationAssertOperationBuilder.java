package com.events.cdc.service.reader.assertion;

import com.events.cdc.connector.db.transaction.log.messaging.MessageWithDestination;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class MessageWithDestinationAssertOperationBuilder
    implements TransactionLogMessageAssertOperationBuilder<MessageWithDestination> {

  private String id;
  private String destination;
  private String payload;
  private Map<String, String> headers;

  public static MessageWithDestinationAssertOperationBuilder assertion() {
    return new MessageWithDestinationAssertOperationBuilder();
  }

  @Override
  public TransactionLogMessageAssertOperation<MessageWithDestination> build() {
    return message -> {
      if (id != null) {
        Assert.assertEquals(id, message.getId());
      }

      if (destination != null) {
        Assert.assertEquals(destination, message.getDestination());
      }

      if (payload != null) {
        Assert.assertEquals(payload, message.getPayload());
      }

      if (headers != null) {
        headers.put("id", message.getId());
        Assert.assertEquals(headers, message.getHeaders());
      }
    };
  }

  public MessageWithDestinationAssertOperationBuilder withId(String id) {
    this.id = id;
    return this;
  }

  public MessageWithDestinationAssertOperationBuilder withDestination(String destination) {
    this.destination = destination;
    return this;
  }

  public MessageWithDestinationAssertOperationBuilder withPayload(String payload) {
    this.payload = payload;
    return this;
  }

  public MessageWithDestinationAssertOperationBuilder withHeaders(Map<String, String> headers) {
    this.headers = new HashMap<>(headers);
    return this;
  }
}
