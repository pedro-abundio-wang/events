package com.events.common.jdbc.schema;

import org.apache.commons.lang3.StringUtils;

public class EventsSchema {

  public static final String DEFAULT_SCHEMA = "events";
  public static final String EMPTY_SCHEMA = "none";

  private final String eventsDatabaseSchema;

  public EventsSchema() {
    eventsDatabaseSchema = DEFAULT_SCHEMA;
  }

  public EventsSchema(String eventsDatabaseSchema) {
    this.eventsDatabaseSchema =
        StringUtils.isEmpty(eventsDatabaseSchema) ? DEFAULT_SCHEMA : eventsDatabaseSchema;
  }

  public String getEventsDatabaseSchema() {
    return eventsDatabaseSchema;
  }

  public boolean isDefault() {
    return DEFAULT_SCHEMA.equals(eventsDatabaseSchema);
  }

  public String qualifyTable(String table) {
    String schema = isDefault() ? DEFAULT_SCHEMA : eventsDatabaseSchema;
    return String.format("%s.%s", schema, table);
  }
}
