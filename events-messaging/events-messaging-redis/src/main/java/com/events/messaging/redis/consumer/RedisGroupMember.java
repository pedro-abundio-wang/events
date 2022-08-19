package com.events.messaging.redis.consumer;

import com.events.messaging.partition.management.GroupMember;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class RedisGroupMember implements GroupMember {

  private final RedisTemplate<String, String> redisTemplate;
  private final String memberId;
  private final long ttlInMilliseconds;
  private final String groupKey;
  private final String groupMemberKey;
  private final Timer timer = new Timer();

  public RedisGroupMember(
      RedisTemplate<String, String> redisTemplate,
      String groupId,
      String memberId,
      long ttlInMilliseconds) {
    this.redisTemplate = redisTemplate;
    this.memberId = memberId;
    this.ttlInMilliseconds = ttlInMilliseconds;
    this.groupKey = RedisKeyUtil.keyForMemberGroupSet(groupId);
    this.groupMemberKey = RedisKeyUtil.keyForGroupMember(groupId, memberId);
    this.createOrUpdateGroupMember();
    this.addMemberToGroup();
    this.scheduleGroupMemberTtlRefresh();
  }

  public void remove() {
    this.stopTtlRefreshing();
    this.redisTemplate.opsForSet().remove(this.groupKey, new Object[] {this.memberId});
    this.redisTemplate.delete(this.groupMemberKey);
  }

  void stopTtlRefreshing() {
    this.timer.cancel();
  }

  private void addMemberToGroup() {
    this.redisTemplate.opsForSet().add(this.groupKey, new String[] {this.memberId});
  }

  private void scheduleGroupMemberTtlRefresh() {
    this.timer.schedule(
        new TimerTask() {
          public void run() {
            RedisGroupMember.this.createOrUpdateGroupMember();
          }
        },
        0L,
        this.ttlInMilliseconds / 2L);
  }

  private void createOrUpdateGroupMember() {
    this.redisTemplate
        .opsForValue()
        .set(this.groupMemberKey, this.memberId, this.ttlInMilliseconds, TimeUnit.MILLISECONDS);
  }
}
