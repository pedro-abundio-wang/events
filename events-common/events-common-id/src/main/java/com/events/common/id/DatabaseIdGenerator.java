package com.events.common.id;

import java.util.Optional;

public class DatabaseIdGenerator implements IdGenerator {

  public static final long OUTBOX_ID_MAX_VALUE = 0x0000ffffffffffffL;
  public static final long COUNTER_MAX_VALUE = 0xffffL;

  private final long outboxId;

  @Override
  public boolean databaseIdRequired() {
    return true;
  }

  public DatabaseIdGenerator(long outboxId) {
    this.outboxId = outboxId;
    if (outboxId < 0 || outboxId > OUTBOX_ID_MAX_VALUE) {
      throw new IllegalArgumentException(
          String.format("service id should be between 0 and %s", OUTBOX_ID_MAX_VALUE));
    }
  }

  @Override
  public Int128 genId(Long databaseId) {
    if (databaseId == null) {
      throw new IllegalArgumentException("database id is required");
    }
    return new Int128(databaseId, outboxId);
  }

  @Override
  public Optional<Int128> incrementIdIfPossible(Int128 anchorId) {
    long counter = anchorId.getLow() >>> 48;
    if (counter == COUNTER_MAX_VALUE) {
      return Optional.empty();
    }
    counter = (++counter) << 48;
    long low = anchorId.getLow() & 0x0000ffffffffffffL | counter;
    return Optional.of(new Int128(anchorId.getHigh(), low));
  }
}
