package com.events.cdc.connector.db.transaction.log.converter;

import com.events.cdc.connector.db.transaction.log.messaging.MessageWithDestination;
import com.events.cdc.connector.db.transaction.log.entry.TransactionLogEntry;
import com.events.common.id.IdGenerator;
import com.events.common.jdbc.operation.EventsJdbcOperationsUtils;
import com.events.common.json.mapper.JsonMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TransactionLogEntryToMessageWithDestinationConverter
    implements TransactionLogEntryConverter<MessageWithDestination> {

  public IdGenerator idGenerator;

  public TransactionLogEntryToMessageWithDestinationConverter(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  @Override
  public Optional<MessageWithDestination> convert(TransactionLogEntry transactionLogEntry) {

    if (transactionLogEntry.getBooleanColumn("published")) {
      return Optional.empty();
    }

    Map<String, String> headers =
        JsonMapper.fromJson(transactionLogEntry.getStringColumn("headers"), Map.class);

    if (!headers.containsKey("id")) {
      headers = new HashMap<>(headers);

      String generatedId =
          idGenerator
              .genId(
                  transactionLogEntry.getLongColumn(
                      EventsJdbcOperationsUtils.MESSAGE_AUTO_GENERATED_ID_COLUMN))
              .asString();

      headers.put("id", generatedId);
    }

    MessageWithDestination message =
        new MessageWithDestination(
            transactionLogEntry.getStringColumn("destination"),
            transactionLogEntry.getStringColumn("payload"),
            headers,
            transactionLogEntry.getTransactionLogFileOffset());

    return Optional.of(message);
  }
}
