package com.events.messaging.partition.management;

import com.events.messaging.leadership.coordination.LeaderRemovedCallback;
import com.events.messaging.leadership.coordination.LeaderSelectedCallback;

import java.util.Set;
import java.util.function.Consumer;

public interface CoordinatorFactory {
  Coordinator makeCoordinator(
      String subscriberId,
      Set<String> channels,
      String subscriptionId,
      Consumer<Assignment> assignmentUpdatedCallback,
      String lockId,
      LeaderSelectedCallback leaderSelectedCallback,
      LeaderRemovedCallback leaderRemovedCallback);
}
