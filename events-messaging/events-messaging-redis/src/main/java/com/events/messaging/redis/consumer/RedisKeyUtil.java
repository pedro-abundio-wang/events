package com.events.messaging.redis.consumer;

public class RedisKeyUtil {
  public static String keyForMemberGroupSet(String groupId) {
    return String.format("transactional-messaging:group:members:%s", groupId);
  }

  public static String keyForGroupMember(String groupId, String memberId) {
    return String.format("transactional-messaging:group:member:%s:%s", groupId, memberId);
  }

  public static String keyForAssignment(String groupId, String memberId) {
    return String.format("transactional-messaging:group:member:assignment:%s:%s", groupId, memberId);
  }

  public static String keyForLeaderLock(String groupId) {
    return String.format("transactional-messaging:leader:lock:%s", groupId);
  }
}
