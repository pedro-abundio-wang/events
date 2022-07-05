package com.events.cdc.connector.db.transaction.log.offset;

public interface OffsetStore<OFFSET> {
  void save(OFFSET offset);
}
