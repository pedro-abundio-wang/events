package com.events.cdc.connector.db.transaction.log.messaging;

import java.util.Optional;

public class EventWithSourcing implements TransactionLogMessage {

  private String id;
  private String entityId;
  private String entityType;
  private String eventData;
  private String eventType;
  private TransactionLogFileOffset transactionLogFileOffset;
  private Optional<String> metadata;

  public EventWithSourcing() {}

  public EventWithSourcing(
      String id,
      String entityId,
      String entityType,
      String eventData,
      String eventType,
      TransactionLogFileOffset transactionLogFileOffset,
      Optional<String> metadata) {
    this.id = id;
    this.entityId = entityId;
    this.entityType = entityType;
    this.eventData = eventData;
    this.eventType = eventType;
    this.transactionLogFileOffset = transactionLogFileOffset;
    this.metadata = metadata;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public void setEventData(String eventData) {
    this.eventData = eventData;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getEntityId() {
    return entityId;
  }

  public String getEntityType() {
    return entityType;
  }

  public String getEventData() {
    return eventData;
  }

  public String getEventType() {
    return eventType;
  }

  public Optional<String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Optional<String> metadata) {
    this.metadata = metadata;
  }

  @Override
  public Optional<TransactionLogFileOffset> getTransactionLogFileOffset() {
    return Optional.ofNullable(transactionLogFileOffset);
  }

  public void setTransactionLogFileOffset(TransactionLogFileOffset transactionLogFileOffset) {
    this.transactionLogFileOffset = transactionLogFileOffset;
  }
}
