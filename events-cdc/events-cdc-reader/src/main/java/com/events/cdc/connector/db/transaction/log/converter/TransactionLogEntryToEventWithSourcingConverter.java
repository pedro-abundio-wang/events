package com.events.cdc.connector.db.transaction.log.converter;

import com.events.cdc.connector.db.transaction.log.entry.TransactionLogEntry;
import com.events.cdc.connector.db.transaction.log.messaging.EventWithSourcing;
import com.events.common.id.IdGenerator;
import com.events.common.jdbc.operation.EventsJdbcOperationsUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class TransactionLogEntryToEventWithSourcingConverter
    implements TransactionLogEntryConverter<EventWithSourcing> {

  public IdGenerator idGenerator;

  public TransactionLogEntryToEventWithSourcingConverter(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  @Override
  public Optional<EventWithSourcing> convert(TransactionLogEntry transactionLogEntry) {

    if (transactionLogEntry.getBooleanColumn("published")) {
      return Optional.empty();
    }

    String eventId = transactionLogEntry.getStringColumn("event_id");

    if (StringUtils.isEmpty(eventId)) {
      Long dbId =
          transactionLogEntry.getLongColumn(
              EventsJdbcOperationsUtils.EVENT_AUTO_GENERATED_ID_COLUMN);
      eventId = idGenerator.genId(dbId).asString();
    }

    EventWithSourcing eventWithSourcing =
        new EventWithSourcing(
            eventId,
            transactionLogEntry.getStringColumn("entity_id"),
            transactionLogEntry.getStringColumn("entity_type"),
            transactionLogEntry.getStringColumn("event_data"),
            transactionLogEntry.getStringColumn("event_type"),
            transactionLogEntry.getTransactionLogFileOffset(),
            Optional.ofNullable(transactionLogEntry.getStringColumn("metadata")));

    return Optional.of(eventWithSourcing);
  }
}
