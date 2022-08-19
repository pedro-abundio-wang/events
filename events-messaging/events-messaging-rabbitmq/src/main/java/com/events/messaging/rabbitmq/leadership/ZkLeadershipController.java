package com.events.messaging.rabbitmq.leadership;

import com.events.messaging.leadership.coordination.LeadershipController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class ZkLeadershipController implements LeadershipController {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final CountDownLatch stopCountDownLatch;

  public ZkLeadershipController(CountDownLatch stopCountDownLatch) {
    this.stopCountDownLatch = stopCountDownLatch;
  }

  @Override
  public void relinquishLeadership() {
    logger.info("Relinquishing leadership");
    stopCountDownLatch.countDown();
    logger.info("Relinquished leadership");
  }
}
