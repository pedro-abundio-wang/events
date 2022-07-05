package com.events.messaging.rabbitmq.consumer;

import java.nio.charset.StandardCharsets;

public class ZkUtil {

  public ZkUtil() {}

  public static String pathForAssignment(String groupId, String memberId) {
    return String.format(
        "/transactional-messaging/rabbitmq/consumer-assignments/%s/%s", groupId, memberId);
  }

  public static String pathForMemberGroup(String groupId) {
    return String.format("/transactional-messaging/rabbitmq/consumer-groups/%s", groupId);
  }

  public static String pathForGroupMember(String groupId, String memberId) {
    return String.format(
        "/transactional-messaging/rabbitmq/consumer-groups/%s/%s", groupId, memberId);
  }

  public static String pathForLeader(String groupId) {
    return String.format("/transactional-messaging/rabbitmq/consumer-leaders/%s", groupId);
  }

  public static String byteArrayToString(byte[] bytes) {
    return new String(bytes, StandardCharsets.UTF_8);
  }

  public static byte[] stringToByteArray(String string) {
    return string.getBytes(StandardCharsets.UTF_8);
  }
}
