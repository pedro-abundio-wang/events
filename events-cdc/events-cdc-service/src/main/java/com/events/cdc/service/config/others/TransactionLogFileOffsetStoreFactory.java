package com.events.cdc.service.config.others;

import com.events.cdc.reader.properties.DbLogCdcReaderProperties;
import com.events.common.jdbc.schema.EventsSchema;

import javax.sql.DataSource;

public interface TransactionLogFileOffsetStoreFactory {
  TransactionLogFileOffsetStore create(
      DbLogCdcReaderProperties properties,
      DataSource dataSource,
      EventsSchema eventsSchema,
      String clientName);
}
