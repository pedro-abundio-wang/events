package com.events.messaging.leadership.coordination;

public interface LeaderSelectorFactory {
  LeaderSelector create(
      String lockId,
      String leaderId,
      LeaderSelectedCallback leaderSelectedCallback,
      LeaderRemovedCallback leaderRemovedCallback);
}
