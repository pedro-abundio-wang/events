package com.events.cdc.reader.leadership;

import com.events.cdc.reader.CdcReader;
import com.events.messaging.leadership.coordination.LeaderSelector;
import com.events.messaging.leadership.coordination.LeaderSelectorFactory;
import com.events.messaging.leadership.coordination.LeadershipController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class CdcReaderLeadership {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  private final CdcReader cdcReader;
  private final LeaderSelectorFactory leaderSelectorFactory;
  private final String leaderLockId;

  private volatile boolean leader;
  private LeaderSelector leaderSelector;
  private LeadershipController leadershipController;

  public CdcReaderLeadership(
      String leaderLockId, LeaderSelectorFactory leaderSelectorFactory, CdcReader cdcReader) {
    this.leaderLockId = leaderLockId;
    this.leaderSelectorFactory = leaderSelectorFactory;
    this.cdcReader = cdcReader;
    cdcReader.setExceptionCallback(this::relinquish);
  }

  public void start() {
    logger.info("Starting CdcReaderLeadership");
    leaderSelector =
        leaderSelectorFactory.create(
            leaderLockId,
            UUID.randomUUID().toString(),
            this::leaderSelectedCallback,
            this::leaderRemovedCallback);
    leaderSelector.start();
  }

  private void leaderSelectedCallback(LeadershipController leadershipController) {
    logger.info("Assigning leadership");
    this.leadershipController = leadershipController;
    leader = true;
    new Thread(cdcReader::start).start();
    logger.info("Assigned leadership");
  }

  private void leaderRemovedCallback() {
    logger.info("Resigning leadership");
    leader = false;
    cdcReader.stop(false);
    logger.info("Resigned leadership");
  }

  public void stop() {
    logger.info("Stopping CdcReaderLeadership");
    cdcReader.stop();
    leaderSelector.stop();
    logger.info("Stopped CdcReaderLeadership");
  }

  private void relinquish() {
    logger.info("Restarting CdcReaderLeadership");
    leadershipController.relinquishLeadership();
    logger.info("Restarted CdcReaderLeadership");
  }

  public CdcReader getCdcReader() {
    return cdcReader;
  }

  public boolean isLeader() {
    return leader;
  }
}
