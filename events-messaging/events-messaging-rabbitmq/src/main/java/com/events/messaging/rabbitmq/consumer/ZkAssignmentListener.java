package com.events.messaging.rabbitmq.consumer;

import com.events.common.json.mapper.JsonMapper;
import com.events.messaging.partition.management.Assignment;
import com.events.messaging.partition.management.AssignmentListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

public class ZkAssignmentListener implements AssignmentListener {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final NodeCache nodeCache;

  public ZkAssignmentListener(
      CuratorFramework curatorFramework,
      String groupId,
      String memberId,
      Consumer<Assignment> assignmentUpdatedCallback) {
    nodeCache = new NodeCache(curatorFramework, ZkUtil.pathForAssignment(groupId, memberId));

    try {
      logger.info("Starting node cache: groupId = {}, memberId = {}", groupId, memberId);
      nodeCache.start();
      logger.info("Started node cache: groupId = {}, memberId = {}", groupId, memberId);
    } catch (Exception e) {
      logger.error("Starting node cache failed: groupId = {}, memberId = {}", groupId, memberId);
      logger.error("Starting node cache failed", e);
      throw new RuntimeException(e);
    }

    logger.info("Adding node cache listener: groupId = {}, memberId = {}", groupId, memberId);
    nodeCache
        .getListenable()
        .addListener(
            () -> {
              Assignment assignment =
                  JsonMapper.fromJson(
                      ZkUtil.byteArrayToString(nodeCache.getCurrentData().getData()),
                      Assignment.class);

              assignmentUpdatedCallback.accept(assignment);
            });
    logger.info("Added node cache listener: groupId = {}, memberId = {}", groupId, memberId);
  }

  @Override
  public void remove() {
    try {
      logger.info("Closing node cache");
      nodeCache.close();
      logger.info("Closed node cache");
    } catch (IOException e) {
      logger.error("Closing node cache failed", e);
    }
  }
}
