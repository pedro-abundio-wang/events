package com.events.common.jdbc.operation;

import com.events.common.jdbc.schema.EventsSchema;
import com.events.common.jdbc.sql.dialect.EventsSqlDialect;

public class EventsJdbcOperationsUtils {

  public static final String MESSAGE_AUTO_GENERATED_ID_COLUMN = "db_id";
  public static final String EVENT_AUTO_GENERATED_ID_COLUMN = "db_id";

  private final EventsSqlDialect eventsSqlDialect;

  public EventsJdbcOperationsUtils(EventsSqlDialect eventsSqlDialect) {
    this.eventsSqlDialect = eventsSqlDialect;
  }

  public String insertIntoEventsTableApplicationIdSql(EventsSchema eventsSchema) {
    return String.format(
        "INSERT INTO %s (event_id, event_type, event_data, entity_type, entity_id, triggering_event, metadata, published)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
        eventsSchema.qualifyTable("events"));
  }

  public String insertIntoEventsTableDatabaseIdSql(EventsSchema eventsSchema) {
    return String.format(
        "INSERT INTO %s (event_id, event_type, event_data, entity_type, entity_id, triggering_event, metadata, published)"
            + " VALUES ('', ?, ?, ?, ?, ?, ?, ?)",
        eventsSchema.qualifyTable("events"));
  }

  public String insertIntoMessageTableApplicationIdSql(EventsSchema eventsSchema) {
    return String.format(
        "INSERT INTO %s (id, destination, headers, payload, creation_time, published)"
            + " VALUES (?, ?, ?, ?, %s, ?)",
        eventsSchema.qualifyTable("message"),
        eventsSqlDialect.getCurrentTimeInMillisecondsExpression());
  }

  public String insertIntoMessageTableDatabaseIdSql(
      EventsSchema eventsSchema) {
    return String.format(
        "INSERT INTO %s (id, destination, headers, payload, creation_time, published)"
            + "VALUES ('', ?, ?, ?, %s, ?)",
        eventsSchema.qualifyTable("message"),
        eventsSqlDialect.getCurrentTimeInMillisecondsExpression());
  }

  public int booleanToInt(boolean bool) {
    return bool ? 1 : 0;
  }
}
