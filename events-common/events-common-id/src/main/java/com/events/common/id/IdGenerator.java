package com.events.common.id;

import java.util.Optional;

public interface IdGenerator {

  boolean databaseIdRequired();

  Int128 genId(Long baseId);

  Optional<Int128> incrementIdIfPossible(Int128 anchorId);
}
