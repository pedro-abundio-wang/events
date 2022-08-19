package com.events.messaging.rabbitmq.leadership;

import com.events.messaging.leadership.coordination.LeaderRemovedCallback;
import com.events.messaging.leadership.coordination.LeaderSelector;
import com.events.messaging.leadership.coordination.LeaderSelectedCallback;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.CancelLeadershipException;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class ZkLeaderSelector implements LeaderSelector {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final String lockId;
  private final String leaderId;
  private final LeaderSelectedCallback leaderSelectedCallback;
  private final LeaderRemovedCallback leaderRemovedCallback;
  private final CuratorFramework curatorFramework;

  private org.apache.curator.framework.recipes.leader.LeaderSelector leaderSelector;

  public ZkLeaderSelector(
      CuratorFramework curatorFramework,
      String lockId,
      String leaderId,
      LeaderSelectedCallback leaderSelectedCallback,
      LeaderRemovedCallback leaderRemovedCallback) {
    this.curatorFramework = curatorFramework;
    this.lockId = lockId;
    this.leaderId = leaderId;
    this.leaderSelectedCallback = leaderSelectedCallback;
    this.leaderRemovedCallback = leaderRemovedCallback;
  }

  @Override
  public void start() {

    logger.info("Starting leader selector");

    leaderSelector =
        new org.apache.curator.framework.recipes.leader.LeaderSelector(
            curatorFramework,
            lockId,
            new LeaderSelectorListener() {
              @Override
              public void takeLeadership(CuratorFramework client) {
                CountDownLatch stopCountDownLatch = new CountDownLatch(1);
                try {
                  logger.info("Calling leaderSelectedCallback, leaderId : {}", leaderId);
                  leaderSelectedCallback.run(new ZkLeadershipController(stopCountDownLatch));
                  logger.info("Called leaderSelectedCallback, leaderId : {}", leaderId);
                } catch (Exception e) {
                  logger.error(e.getMessage(), e);
                  logger.info("Calling leaderRemovedCallback, leaderId : {}", leaderId);
                  leaderRemovedCallback.run();
                  logger.info("Called leaderRemovedCallback, leaderId : {}", leaderId);
                  return;
                }
                try {
                  stopCountDownLatch.await();
                } catch (InterruptedException e) {
                  logger.error("Leadership interrupted", e);
                }
                try {
                  logger.info("Calling leaderRemovedCallback, leaderId : {}", leaderId);
                  leaderRemovedCallback.run();
                  logger.info("Called leaderRemovedCallback, leaderId : {}", leaderId);
                } catch (Exception e) {
                  logger.error(e.getMessage(), e);
                }
              }

              @Override
              public void stateChanged(CuratorFramework client, ConnectionState newState) {
                logger.info("StateChanged, state : {}, leaderId : {}", newState, leaderId);
                if (newState == ConnectionState.SUSPENDED || newState == ConnectionState.LOST) {
                  throw new CancelLeadershipException();
                }
              }
            });

    leaderSelector.autoRequeue();

    leaderSelector.start();

    logger.info("Started leader selector");
  }

  @Override
  public void stop() {
    logger.info("Closing leader selector, leaderId : {}", leaderId);
    leaderSelector.close();
    logger.info("Closed leader selector, leaderId : {}", leaderId);
  }
}
