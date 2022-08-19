package com.events.cdc.service.config.others;

public interface DebeziumOffsetStoreFactory {
  DebeziumBinlogKafkaTransactionLogFileOffsetStore create();
}
