package com.events.messaging.redis.config;

import com.events.messaging.leadership.coordination.LeaderSelectorFactory;
import com.events.messaging.partition.management.*;
import com.events.messaging.redis.common.RedisProperties;
import com.events.messaging.redis.common.RedissonClients;
import com.events.messaging.redis.consumer.*;
import com.events.messaging.redis.leadership.RedisLeaderSelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Profile("redis")
@Import({RedisConfiguration.class})
public class RedisMessageConsumerConfiguration {
  @Bean
  public AssignmentManager assignmentManager(
      RedisTemplate<String, String> redisTemplate, RedisProperties redisProperties) {
    return new RedisAssignmentManager(
        redisTemplate, redisProperties.getAssignmentTtlInMilliseconds());
  }

  @Bean
  public AssignmentListenerFactory assignmentListenerFactory(
      RedisTemplate<String, String> redisTemplate, RedisProperties redisProperties) {
    return (groupId, memberId, assignmentUpdatedCallback) ->
        new RedisAssignmentListener(
            redisTemplate,
            groupId,
            memberId,
            redisProperties.getListenerIntervalInMilliseconds(),
            assignmentUpdatedCallback);
  }

  @Bean
  public MemberGroupManagerFactory memberGroupManagerFactory(
      RedisTemplate<String, String> redisTemplate, RedisProperties redisProperties) {
    return (groupId, memberId, groupMembersUpdatedCallback) ->
        new RedisMemberGroupManager(
            redisTemplate,
            groupId,
            memberId,
            redisProperties.getListenerIntervalInMilliseconds(),
            groupMembersUpdatedCallback);
  }

  @Bean
  public LeaderSelectorFactory leaderSelectorFactory(
      RedissonClients redissonClients, RedisProperties redisProperties) {
    return (lockId, leaderId, leaderSelectedCallback, leaderRemovedCallback) ->
        new RedisLeaderSelector(
            redissonClients,
            lockId,
            leaderId,
            redisProperties.getLeadershipTtlInMilliseconds(),
            leaderSelectedCallback,
            leaderRemovedCallback);
  }

  @Bean
  public GroupMemberFactory groupMemberFactory(
      RedisTemplate<String, String> redisTemplate, RedisProperties redisProperties) {
    return (groupId, memberId) ->
        new RedisGroupMember(
            redisTemplate, groupId, memberId, redisProperties.getGroupMemberTtlInMilliseconds());
  }

  @Bean
  public CoordinatorFactory redisCoordinatorFactory(
      AssignmentManager assignmentManager,
      AssignmentListenerFactory assignmentListenerFactory,
      MemberGroupManagerFactory memberGroupManagerFactory,
      LeaderSelectorFactory leaderSelectorFactory,
      GroupMemberFactory groupMemberFactory,
      RedisProperties redisProperties) {
    return new CoordinatorFactoryImpl(
        assignmentManager,
        assignmentListenerFactory,
        memberGroupManagerFactory,
        leaderSelectorFactory,
        groupMemberFactory,
        redisProperties.getPartitions());
  }

  @Bean
  public RedisMessageConsumer redisMessageConsumer(
      RedisTemplate<String, String> redisTemplate,
      CoordinatorFactory coordinatorFactory,
      RedisProperties redisProperties) {
    return new RedisMessageConsumer(
        redisTemplate,
        coordinatorFactory,
        redisProperties.getTimeInMillisecondsToSleepWhenKeyDoesNotExist(),
        redisProperties.getBlockStreamTimeInMilliseconds());
  }
}
