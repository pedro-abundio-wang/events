package com.events.messaging.rabbitmq.consumer;

import com.events.messaging.partition.management.MemberGroupManager;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class ZkMemberGroupManager implements MemberGroupManager {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final String groupId;
  private final String memberId;
  private final String path;
  private final TreeCache treeCache;

  public ZkMemberGroupManager(
      CuratorFramework curatorFramework,
      String groupId,
      String memberId,
      Consumer<Set<String>> groupMembersUpdatedCallback) {
    this.groupId = groupId;
    this.memberId = memberId;
    this.path = ZkUtil.pathForMemberGroup(groupId);
    this.treeCache = new TreeCache(curatorFramework, this.path);
    this.treeCache
        .getListenable()
        .addListener(
            (client, event) -> {
              Set<String> members = this.getCurrentMemberIds();
              this.logger.info(
                  "Calling groupMembersUpdatedCallback.accept, members: {}, group: {}, member: {}",
                  members,
                  groupId,
                  memberId);
              groupMembersUpdatedCallback.accept(members);
              this.logger.info(
                  "Called groupMembersUpdatedCallback.accept, members: {}, group: {}, member: {}",
                  members,
                  groupId,
                  memberId);
            });

    try {
      this.logger.info("Starting group manager, group: {}, member: {}", groupId, memberId);
      this.treeCache.start();
      this.logger.info("Started group manager, group: {}, member: {}", groupId, memberId);
    } catch (Exception e) {
      this.logger.error("Starting group manager failed, group: {}, member: {}", groupId, memberId);
      this.logger.error("Starting group manager failed", e);
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    this.logger.info("Stopping group manager, group: {}, member: {}", this.groupId, this.memberId);
    this.treeCache.close();
    this.logger.info("Stopped group manager, group: {}, member: {}", this.groupId, this.memberId);
  }

  private Set<String> getCurrentMemberIds() {
    return (Set)
        Optional.ofNullable(this.treeCache.getCurrentChildren(this.path))
            .map(Map::keySet)
            .orElseGet(Collections::emptySet);
  }
}
