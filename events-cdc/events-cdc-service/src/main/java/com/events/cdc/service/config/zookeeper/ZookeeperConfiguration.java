package com.events.cdc.service.config.zookeeper;

import com.events.cdc.service.properties.ZookeeperProperties;
import com.events.messaging.leadership.coordination.LeaderSelectorFactory;
import com.events.messaging.rabbitmq.leadership.ZkLeaderSelector;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("zookeeper")
public class ZookeeperConfiguration {
  @Bean
  public ZookeeperProperties zookeeperProperties() {
    return new ZookeeperProperties();
  }

  @Bean(destroyMethod = "close")
  public CuratorFramework curatorFramework(ZookeeperProperties zookeeperProperties) {
    String connectionString = zookeeperProperties.getConnectionString();
    return makeStartedCuratorClient(connectionString);
  }

  @Bean
  public LeaderSelectorFactory leaderselectorfactory(CuratorFramework curatorFramework) {
    return (lockId, leaderId, leaderSelectedCallback, leaderRemovedCallback) ->
        new ZkLeaderSelector(
            curatorFramework, lockId, leaderId, leaderSelectedCallback, leaderRemovedCallback);
  }

  static CuratorFramework makeStartedCuratorClient(String connectionString) {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(100, Integer.MAX_VALUE);
    CuratorFramework client =
        CuratorFrameworkFactory.builder()
            .retryPolicy(retryPolicy)
            .connectString(connectionString)
            .build();
    client.start();
    return client;
  }
}
