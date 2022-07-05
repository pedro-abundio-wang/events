package com.events.messaging.redis.consumer;

import com.events.messaging.partition.management.MemberGroupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class RedisMemberGroupManager implements MemberGroupManager {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private RedisTemplate<String, String> redisTemplate;
  private String groupId;
  private String memberId;
  private long refreshPeriodInMilliseconds;
  private Consumer<Set<String>> groupMembersUpdatedCallback;
  private Timer timer = new Timer();
  private String groupKey;
  private Set<String> checkedMembers;

  public RedisMemberGroupManager(
      RedisTemplate<String, String> redisTemplate,
      String groupId,
      String memberId,
      long checkIntervalInMilliseconds,
      Consumer<Set<String>> groupMembersUpdatedCallback) {
    this.redisTemplate = redisTemplate;
    this.groupId = groupId;
    this.memberId = memberId;
    this.refreshPeriodInMilliseconds = checkIntervalInMilliseconds;
    this.groupMembersUpdatedCallback = groupMembersUpdatedCallback;
    this.groupKey = RedisKeyUtil.keyForMemberGroupSet(groupId);
    this.checkedMembers = this.getCurrentGroupMembers();
    this.logger.info(
        "Calling groupMembersUpdatedCallback.accept, members : {}, group: {}, member: {}",
        new Object[] {this.checkedMembers, groupId, memberId});
    groupMembersUpdatedCallback.accept(this.checkedMembers);
    this.logger.info(
        "Calling groupMembersUpdatedCallback.accept, members : {}, group: {}, member: {}",
        new Object[] {this.checkedMembers, groupId, memberId});
    this.scheduleCheckForChangesInMemberGroup();
  }

  public void stop() {
    this.timer.cancel();
  }

  private void scheduleCheckForChangesInMemberGroup() {
    this.timer.schedule(
        new TimerTask() {
          public void run() {
            RedisMemberGroupManager.this.checkChangesInMemberGroup();
          }
        },
        0L,
        this.refreshPeriodInMilliseconds);
  }

  private void checkChangesInMemberGroup() {
    Set<String> currentMembers = this.getCurrentGroupMembers();
    currentMembers.stream()
        .filter(this::isGroupMemberExpired)
        .forEach(
            (expiredMemberId) -> {
              this.removeExpiredGroupMember(currentMembers, expiredMemberId);
            });
    if (!this.checkedMembers.equals(currentMembers)) {
      this.logger.info(
          "Calling groupMembersUpdatedCallback.accept, members : {}, group: {}, member: {}",
          new Object[] {currentMembers, this.groupId, this.memberId});
      this.groupMembersUpdatedCallback.accept(currentMembers);
      this.logger.info(
          "Calling groupMembersUpdatedCallback.accept, members : {}, group: {}, member: {}",
          new Object[] {currentMembers, this.groupId, this.memberId});
      this.checkedMembers = currentMembers;
    }
  }

  private Set<String> getCurrentGroupMembers() {
    return new HashSet(this.redisTemplate.opsForSet().members(this.groupKey));
  }

  private boolean isGroupMemberExpired(String memberId) {
    return !this.redisTemplate.hasKey(RedisKeyUtil.keyForGroupMember(this.groupId, memberId));
  }

  private void removeExpiredGroupMember(Set<String> currentGroupMembers, String expiredMemberId) {
    this.redisTemplate.opsForSet().remove(this.groupKey, new Object[] {expiredMemberId});
    currentGroupMembers.remove(expiredMemberId);
  }
}
