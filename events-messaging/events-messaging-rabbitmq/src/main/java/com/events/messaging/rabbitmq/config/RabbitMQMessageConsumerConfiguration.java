package com.events.messaging.rabbitmq.config;

import com.events.messaging.leadership.coordination.LeaderSelectorFactory;
import com.events.messaging.partition.management.*;
import com.events.messaging.rabbitmq.consumer.*;
import com.events.messaging.rabbitmq.leadership.ZkLeaderSelector;
import com.events.messaging.rabbitmq.properties.RabbitMQMessageConsumerProperties;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("rabbitmq")
@Import({RabbitMQConfiguration.class})
public class RabbitMQMessageConsumerConfiguration {
  @Bean
  public CuratorFramework curatorFramework(RabbitMQMessageConsumerProperties properties) {
    CuratorFramework framework =
        CuratorFrameworkFactory.newClient(
            properties.getZkUrl(), new ExponentialBackoffRetry(1000, 5));
    framework.start();
    return framework;
  }

  @Bean
  public AssignmentManager assignmentManager(CuratorFramework curatorFramework) {
    return new ZkAssignmentManager(curatorFramework);
  }

  @Bean
  public AssignmentListenerFactory assignmentListenerFactory(CuratorFramework curatorFramework) {
    return (groupId, memberId, assignmentUpdatedCallback) ->
        new ZkAssignmentListener(curatorFramework, groupId, memberId, assignmentUpdatedCallback);
  }

  @Bean
  public MemberGroupManagerFactory memberGroupManagerFactory(CuratorFramework curatorFramework) {
    return (groupId, memberId, groupMembersUpdatedCallback) ->
        new ZkMemberGroupManager(curatorFramework, groupId, memberId, groupMembersUpdatedCallback);
  }

  @Bean
  public LeaderSelectorFactory leaderSelectorFactory(CuratorFramework curatorFramework) {
    return (lockId, leaderId, leaderSelectedCallback, leaderRemovedCallback) ->
        new ZkLeaderSelector(
            curatorFramework, lockId, leaderId, leaderSelectedCallback, leaderRemovedCallback);
  }

  @Bean
  public GroupMemberFactory groupMemberFactory(CuratorFramework curatorFramework) {
    return (groupId, memberId) -> new ZkGroupMember(curatorFramework, groupId, memberId);
  }

  @Bean
  public CoordinatorFactory coordinatorFactory(
      RabbitMQMessageConsumerProperties properties,
      AssignmentManager assignmentManager,
      AssignmentListenerFactory assignmentListenerFactory,
      MemberGroupManagerFactory memberGroupManagerFactory,
      LeaderSelectorFactory leaderSelectorFactory,
      GroupMemberFactory groupMemberFactory) {
    return new CoordinatorFactoryImpl(
        assignmentManager,
        assignmentListenerFactory,
        memberGroupManagerFactory,
        leaderSelectorFactory,
        groupMemberFactory,
        properties.getPartitionCount());
  }

  @Bean
  public RabbitMQMessageConsumer rabbitMQMessageConsumer(
      CoordinatorFactory coordinatorFactory, RabbitMQMessageConsumerProperties properties) {
    return new RabbitMQMessageConsumer(
        coordinatorFactory, properties.getBrokerAddresses(), properties.getPartitionCount());
  }
}
