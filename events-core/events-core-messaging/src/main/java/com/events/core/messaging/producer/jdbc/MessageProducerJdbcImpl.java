package com.events.core.messaging.producer.jdbc;

import com.events.common.id.IdGenerator;
import com.events.common.jdbc.operation.EventsJdbcOperations;
import com.events.core.messaging.message.Message;
import com.events.core.messaging.producer.MessageProducer;

public class MessageProducerJdbcImpl implements MessageProducer {

  private final EventsJdbcOperations eventsJdbcOperations;

  private final IdGenerator idGenerator;

  public MessageProducerJdbcImpl(
      EventsJdbcOperations eventsJdbcOperations, IdGenerator idGenerator) {
    this.eventsJdbcOperations = eventsJdbcOperations;
    this.idGenerator = idGenerator;
  }

  @Override
  public String sendInternal(Message message) {
    return eventsJdbcOperations.insertIntoMessageTable(
        idGenerator,
        message.getPayload(),
        message.getRequiredHeader(Message.CHANNEL),
        message.getHeaders());
  }
}
