package com.events.cdc.service.helper;

import com.events.cdc.connector.db.transaction.log.converter.TransactionLogEntryConverter;
import com.events.cdc.connector.db.transaction.log.converter.TransactionLogEntryToMessageWithDestinationConverter;
import com.events.cdc.connector.db.transaction.log.messaging.EventWithSourcing;
import com.events.cdc.connector.db.transaction.log.messaging.MessageWithDestination;
import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogMessage;
import com.events.cdc.reader.CdcReader;
import com.events.cdc.reader.SourceTableNameSupplier;
import com.events.cdc.service.reader.assertion.MessageWithDestinationAssertionCallback;
import com.events.cdc.service.reader.assertion.TransactionLogMessageAssert;
import com.events.common.id.IdGenerator;
import com.events.common.jdbc.operation.EventsJdbcOperations;
import com.events.common.jdbc.schema.EventsSchema;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TestHelper {

  @Autowired private EventsSchema eventsSchema;

  @Autowired private IdGenerator idGenerator;

  @Autowired private EventsJdbcOperations eventsJdbcOperations;

  @Autowired(required = false)
  private SourceTableNameSupplier sourceTableNameSupplier;

  public void runInSeparateThread(Runnable callback) {
    new Thread(callback).start();
  }

  // -------------------------------------------------- //
  // Event Sourcing ----------------------------------- //
  // -------------------------------------------------- //

  public EventWithSourcing saveRandomEvent() {
    return saveEvent(getTestEntityType(), getTestCreatedEventType(), generateId());
  }

  public EventWithSourcing saveEvent(String entityType, String eventType, String eventData) {
    String entityId = generateId();
    return saveEvent(entityType, eventType, eventData, entityId);
  }

  public EventWithSourcing saveEvent(
      String entityType, String eventType, String eventData, String entityId) {
    String id =
        eventsJdbcOperations.insertIntoEventsTable(
            idGenerator,
            entityId,
            eventData,
            eventType,
            entityType,
            Optional.empty(),
            Optional.empty());

    return new EventWithSourcing(
        id, entityId, entityType, eventData, eventType, null, Optional.empty());
  }

  public String getEventTopicName() {
    return "TestEntity";
  }

  public String getTestEntityType() {
    return "TestEntity";
  }

  public String getTestCreatedEventType() {
    return "TestCreatedEvent";
  }

  public String getTestUpdatedEventType() {
    return "TestUpdatedEvent";
  }

  // -------------------------------------------------- //
  // Transactional Message ---------------------------- //
  // -------------------------------------------------- //

  public String saveMessage(
      IdGenerator idGenerator, String payload, String destination, Map<String, String> headers) {
    return eventsJdbcOperations.insertIntoMessageTable(idGenerator, payload, destination, headers);
  }

  public String generateRandomPayload() {
    return "\"" + "payload-" + generateId() + "\"";
  }

  public String generateRandomDestination() {
    return "destination-" + generateId();
  }

  public String generateId() {
    return StringUtils.rightPad(
        String.valueOf(System.nanoTime()), String.valueOf(Long.MAX_VALUE).length(), "0");
  }

  public TransactionLogMessageAssert<MessageWithDestination> prepareTransactionLogMessageAssertion(
      CdcReader cdcReader) {
    return prepareTransactionLogMessageAssertion(cdcReader, transactionLogMessage -> {});
  }

  public TransactionLogMessageAssert<MessageWithDestination> prepareTransactionLogMessageAssertion(
      CdcReader cdcReader,
      MessageWithDestinationAssertionCallback<MessageWithDestination> onMessageSentCallback) {
    return prepareTransactionLogEntryHandlerAssertion(
        cdcReader,
        new TransactionLogEntryToMessageWithDestinationConverter(idGenerator),
        onMessageSentCallback);
  }

  private <TLM extends TransactionLogMessage>
      TransactionLogMessageAssert<TLM> prepareTransactionLogEntryHandlerAssertion(
          CdcReader cdcReader,
          TransactionLogEntryConverter<TLM> converter,
          MessageWithDestinationAssertionCallback<TLM> messageWithDestinationAssertionCallback) {

    TransactionLogMessageAssert<TLM> transactionLogMessageAssert =
        new TransactionLogMessageAssert<>(
            messageWithDestinationAssertionCallback.waitIterations(),
            messageWithDestinationAssertionCallback.iterationTimeoutMilliseconds());

    cdcReader.addCdcPipelineHandler(
        eventsSchema,
        sourceTableNameSupplier.getSourceTableName(),
        converter,
        transactionLogMessage -> {
          if (messageWithDestinationAssertionCallback.shouldAddToQueue(transactionLogMessage)) {
            transactionLogMessageAssert.addMessage(transactionLogMessage);
          }

          messageWithDestinationAssertionCallback.onMessageSent(transactionLogMessage);

          return CompletableFuture.completedFuture(null);
        });

    return transactionLogMessageAssert;
  }
}
