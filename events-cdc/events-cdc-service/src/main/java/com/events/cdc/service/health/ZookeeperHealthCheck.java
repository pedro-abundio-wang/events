package com.events.cdc.service.health;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class ZookeeperHealthCheck extends AbstractHealthCheck {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Value("${events.cdc.zookeeper.connection.string}")
  private String zkUrl;

  @Override
  protected void determineHealth(HealthBuilder builder) {

    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

    CuratorFramework curatorFramework =
        CuratorFrameworkFactory.builder().retryPolicy(retryPolicy).connectString(zkUrl).build();

    try {
      curatorFramework.start();
      curatorFramework.checkExists().forPath("/events/cdc/leader");
      builder.addDetail("Connected to Zookeeper");
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      builder.addError("Connection to zookeeper failed");
    } finally {
      try {
        curatorFramework.close();
      } catch (Exception ce) {
        logger.error(ce.getMessage(), ce);
      }
    }
  }
}
