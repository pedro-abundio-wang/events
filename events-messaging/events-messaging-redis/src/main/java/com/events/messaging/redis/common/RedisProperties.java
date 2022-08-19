package com.events.messaging.redis.common;

import org.springframework.beans.factory.annotation.Value;

public class RedisProperties {

  @Value("${events.cdc.redis.servers:#{\"\"}}")
  private String servers;

  @Value("${events.cdc.redis.partitions}")
  private int partitions;

  @Value("${events.cdc.redis.group.member.ttl.in.milliseconds:#{10000}}")
  private long groupMemberTtlInMilliseconds;

  @Value("${events.cdc.redis.listener.interval.in.milliseconds:#{1000}}")
  private long listenerIntervalInMilliseconds;

  @Value("${events.cdc.redis.assignment.ttl.in.milliseconds:#{36000000}}")
  private long assignmentTtlInMilliseconds;

  @Value("${events.cdc.redis.leadership.ttl.in.milliseconds:#{10000}}")
  private long leadershipTtlInMilliseconds;

  @Value("${events.cdc.redis.consumer.time.to.sleep.in.milliseconds.when.key.does.not.exist:#{500}}")
  private long timeInMillisecondsToSleepWhenKeyDoesNotExist;

  @Value("${events.cdc.redis.block.stream.time.in.milliseconds:#{10000}}")
  private long blockStreamTimeInMilliseconds;

  public String getServers() {
    return servers;
  }

  public int getPartitions() {
    return partitions;
  }

  public long getGroupMemberTtlInMilliseconds() {
    return groupMemberTtlInMilliseconds;
  }

  public long getListenerIntervalInMilliseconds() {
    return listenerIntervalInMilliseconds;
  }

  public long getAssignmentTtlInMilliseconds() {
    return assignmentTtlInMilliseconds;
  }

  public long getLeadershipTtlInMilliseconds() {
    return leadershipTtlInMilliseconds;
  }

  public long getTimeInMillisecondsToSleepWhenKeyDoesNotExist() {
    return timeInMillisecondsToSleepWhenKeyDoesNotExist;
  }

  public long getBlockStreamTimeInMilliseconds() {
    return blockStreamTimeInMilliseconds;
  }
}
