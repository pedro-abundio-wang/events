package com.events.common.jdbc.operation;

import com.events.common.id.IdGenerator;
import com.events.common.jdbc.executor.EventsJdbcStatementExecutor;
import com.events.common.jdbc.schema.EventsSchema;
import com.events.common.json.mapper.JsonMapper;

import java.util.Map;
import java.util.Optional;

public class EventsJdbcOperations {

  private final EventsJdbcOperationsUtils eventsJdbcOperationsUtils;

  private final EventsJdbcStatementExecutor eventsJdbcStatementExecutor;

  private final EventsSchema eventsSchema;

  public EventsJdbcOperations(
      EventsJdbcOperationsUtils eventsJdbcOperationsUtils,
      EventsJdbcStatementExecutor eventsJdbcStatementExecutor,
      EventsSchema eventsSchema) {
    this.eventsJdbcOperationsUtils = eventsJdbcOperationsUtils;
    this.eventsJdbcStatementExecutor = eventsJdbcStatementExecutor;
    this.eventsSchema = eventsSchema;
  }

  // -------------------------------------------------- //
  // Event Sourcing ----------------------------------- //
  // -------------------------------------------------- //
  public String insertIntoEventsTable(
      IdGenerator idGenerator,
      String entityId,
      String eventData,
      String eventType,
      String entityType,
      Optional<String> triggeringEvent,
      Optional<String> metadata) {
    return insertIntoEventsTable(
        idGenerator, entityId, eventData, eventType, entityType, triggeringEvent, metadata, false);
  }

  private String insertIntoEventsTable(
      IdGenerator idGenerator,
      String entityId,
      String eventData,
      String eventType,
      String entityType,
      Optional<String> triggeringEvent,
      Optional<String> metadata,
      boolean published) {

    if (idGenerator.databaseIdRequired()) {
      return insertIntoEventsTableDatabaseId(
          idGenerator,
          entityId,
          eventData,
          eventType,
          entityType,
          triggeringEvent,
          metadata,
          published);
    } else {
      return insertIntoEventsTableApplicationId(
          idGenerator,
          entityId,
          eventData,
          eventType,
          entityType,
          triggeringEvent,
          metadata,
          published);
    }
  }

  private String insertIntoEventsTableApplicationId(
      IdGenerator idGenerator,
      String entityId,
      String eventData,
      String eventType,
      String entityType,
      Optional<String> triggeringEvent,
      Optional<String> metadata,
      boolean published) {
    String eventId = idGenerator.genId(null).asString();
    eventsJdbcStatementExecutor.update(
        eventsJdbcOperationsUtils.insertIntoEventsTableApplicationIdSql(eventsSchema),
        eventId,
        eventType,
        eventData,
        entityType,
        entityId,
        triggeringEvent.orElse(null),
        metadata.orElse(null),
        eventsJdbcOperationsUtils.booleanToInt(published));
    return eventId;
  }

  private String insertIntoEventsTableDatabaseId(
      IdGenerator idGenerator,
      String entityId,
      String eventData,
      String eventType,
      String entityType,
      Optional<String> triggeringEvent,
      Optional<String> metadata,
      boolean published) {
    Long databaseId =
        eventsJdbcStatementExecutor.insertAndReturnAutoGeneratedId(
            eventsJdbcOperationsUtils.insertIntoEventsTableDatabaseIdSql(eventsSchema),
            EventsJdbcOperationsUtils.EVENT_AUTO_GENERATED_ID_COLUMN,
            eventType,
            eventData,
            entityType,
            entityId,
            triggeringEvent.orElse(null),
            metadata.orElse(null),
            eventsJdbcOperationsUtils.booleanToInt(published));
    return idGenerator.genId(databaseId).asString();
  }

  // -------------------------------------------------- //
  // Transactional Message ---------------------------- //
  // -------------------------------------------------- //
  public String insertIntoMessageTable(
      IdGenerator idGenerator, String payload, String destination, Map<String, String> headers) {
    return insertIntoMessageTable(idGenerator, payload, destination, headers, false);
  }

  private String insertIntoMessageTable(
      IdGenerator idGenerator,
      String payload,
      String destination,
      Map<String, String> headers,
      boolean published) {
    if (idGenerator.databaseIdRequired()) {
      return insertIntoMessageTableDatabaseId(
          idGenerator, payload, destination, headers, published);
    } else {
      return insertIntoMessageTableApplicationId(
          idGenerator, payload, destination, headers, published);
    }
  }

  private String insertIntoMessageTableDatabaseId(
      IdGenerator idGenerator,
      String payload,
      String destination,
      Map<String, String> headers,
      boolean published) {
    long databaseId =
        eventsJdbcStatementExecutor.insertAndReturnAutoGeneratedId(
            eventsJdbcOperationsUtils.insertIntoMessageTableDatabaseIdSql(eventsSchema),
            EventsJdbcOperationsUtils.MESSAGE_AUTO_GENERATED_ID_COLUMN,
            destination,
            JsonMapper.toJson(headers),
            payload,
            eventsJdbcOperationsUtils.booleanToInt(published));
    return idGenerator.genId(databaseId).asString();
  }

  private String insertIntoMessageTableApplicationId(
      IdGenerator idGenerator,
      String payload,
      String destination,
      Map<String, String> headers,
      boolean published) {
    String messageId = idGenerator.genId(null).asString();
    headers.put("id", messageId);
    eventsJdbcStatementExecutor.update(
        eventsJdbcOperationsUtils.insertIntoMessageTableApplicationIdSql(eventsSchema),
        messageId,
        destination,
        JsonMapper.toJson(headers),
        payload,
        eventsJdbcOperationsUtils.booleanToInt(published));
    return messageId;
  }
}