package com.events.cdc.service.reader;

public class EventSourcing {

  private String eventData;

  private String eventId;

  private String entityId;

  public EventSourcing(String eventData, String eventId, String entityId) {
    this.eventData = eventData;
    this.eventId = eventId;
    this.entityId = entityId;
  }

  public String getEventData() {
    return eventData;
  }

  public void setEventData(String eventData) {
    this.eventData = eventData;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }
}
