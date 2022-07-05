package com.events.messaging.partition.management;

public interface GroupMemberFactory {
  GroupMember create(String groupId, String memberId);
}
