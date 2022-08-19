package com.events.messaging.partition.management;

import com.events.messaging.leadership.coordination.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Coordinator {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final String subscriptionId;
  private final String subscriberId;
  private final Set<String> channels;
  private final int partitionCount;
  private final GroupMember groupMember;
  private final MemberGroupManagerFactory memberGroupManagerFactory;
  private final AssignmentListener assignmentListener;
  private final LeaderSelector leaderSelector;
  private final AssignmentManager assignmentManager;
  private final LeaderSelectedCallback leaderSelected;
  private final LeaderRemovedCallback leaderRemoved;

  private MemberGroupManager memberGroupManager;
  private PartitionManager partitionManager;
  private Set<String> previousGroupMembers;

  public Coordinator(
      String subscriptionId,
      String subscriberId,
      Set<String> channels,
      int partitionCount,
      GroupMemberFactory groupMemberFactory,
      MemberGroupManagerFactory memberGroupManagerFactory,
      AssignmentManager assignmentManager,
      AssignmentListenerFactory assignmentListenerFactory,
      LeaderSelectorFactory leaderSelectorFactory,
      Consumer<Assignment> assignmentUpdatedCallback,
      String lockId,
      LeaderSelectedCallback leaderSelected,
      LeaderRemovedCallback leaderRemoved) {

    this.leaderSelected = leaderSelected;
    this.leaderRemoved = leaderRemoved;
    this.subscriptionId = subscriptionId;
    this.subscriberId = subscriberId;
    this.channels = channels;
    this.partitionCount = partitionCount;
    this.assignmentManager = assignmentManager;
    this.memberGroupManagerFactory = memberGroupManagerFactory;

    createInitialAssignments();
    groupMember = groupMemberFactory.create(subscriberId, subscriptionId);
    assignmentListener =
        assignmentListenerFactory.create(subscriberId, subscriptionId, assignmentUpdatedCallback);

    leaderSelector =
        leaderSelectorFactory.create(
            lockId,
            String.format("[subscriberId: %s, subscriptionId: %s]", subscriberId, subscriptionId),
            this::leaderSelectedCallback,
            this::leaderRemovedCallback);

    leaderSelector.start();
  }

  private void createInitialAssignments() {
    try {
      logger.info("Creating initial assignments");
      Map<String, Set<Integer>> partitionAssignmentsByChannel = new HashMap<>();
      channels.forEach(channel -> partitionAssignmentsByChannel.put(channel, new HashSet<>()));
      Assignment assignment = new Assignment(channels, partitionAssignmentsByChannel);
      assignmentManager.initializeAssignment(subscriberId, subscriptionId, assignment);
      logger.info("Created initial assignments");
    } catch (Exception e) {
      logger.error("Creation of initial assignments failed", e);
      throw new RuntimeException(e);
    }
  }

  private void leaderSelectedCallback(LeadershipController leadershipController) {
    logger.info("Calling onLeaderSelected");
    leaderSelected.run(leadershipController);
    partitionManager = new PartitionManager(partitionCount);
    previousGroupMembers = new HashSet<>();
    memberGroupManager =
        memberGroupManagerFactory.create(subscriberId, subscriptionId, this::onGroupMembersUpdated);
    logger.info("Called onLeaderSelected");
  }

  private void leaderRemovedCallback() {
    logger.info(
        "Calling memberGroupManager.stop(), subscriberId : {}, subscriptionId : {}",
        subscriberId,
        subscriptionId);
    memberGroupManager.stop();
    logger.info(
        "Called memberGroupManager.stop(), subscriberId : {}, subscriptionId : {}",
        subscriberId,
        subscriptionId);

    logger.info(
        "Calling leaderRemoved, subscriberId : {}, subscriptionId : {}",
        subscriberId,
        subscriptionId);
    leaderRemoved.run();
    logger.info(
        "Called leaderRemoved, subscriberId : {}, subscriptionId : {}",
        subscriberId,
        subscriptionId);
  }

  private void onGroupMembersUpdated(Set<String> expectedGroupMembers) {
    logger.info(
        "Updating group members, expectedGroupMembers : {}, subscriberId : {}, subscriptionId : {}",
        expectedGroupMembers,
        subscriberId,
        subscriptionId);

    try {
      if (!partitionManager.isInitialized()) {
        initializePartitionManager(expectedGroupMembers);
      } else {
        rebalance(expectedGroupMembers);
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return;
    }

    previousGroupMembers = expectedGroupMembers;
    logger.info(
        "Updated group members, subscriberId : {}, subscriptionId : {}",
        subscriberId,
        subscriptionId);
  }

  private void initializePartitionManager(Set<String> expectedGroupMembers) {
    logger.info(
        "Initializing partition manager, expectedGroupMembers : {}, subscriberId : {}, subscriptionId : {}",
        expectedGroupMembers,
        subscriberId,
        subscriptionId);
    Map<String, Assignment> assignments =
        expectedGroupMembers.stream()
            .collect(Collectors.toMap(Function.identity(), this::readAssignment));

    partitionManager.initialize(assignments).forEach(this::saveAssignment);
  }

  private void rebalance(Set<String> expectedGroupMembers) {
    logger.info(
        "Preparing to rebalance, expectedGroupMembers : {}, subscriberId : {}, subscriptionId : {}",
        expectedGroupMembers,
        subscriberId,
        subscriptionId);

    Set<String> removedGroupMembers =
        previousGroupMembers.stream()
            .filter(groupMember -> !expectedGroupMembers.contains(groupMember))
            .collect(Collectors.toSet());

    Map<String, Set<String>> addedGroupMembersWithTheirSubscribedChannels =
        expectedGroupMembers.stream()
            .filter(groupMember -> !previousGroupMembers.contains(groupMember))
            .collect(
                Collectors.toMap(
                    Function.identity(),
                    groupMember -> readAssignment(subscriptionId).getChannels()));

    partitionManager
        .rebalance(addedGroupMembersWithTheirSubscribedChannels, removedGroupMembers)
        .forEach(this::saveAssignment);
  }

  private Assignment readAssignment(String groupMemberId) {
    return assignmentManager.readAssignment(subscriberId, groupMemberId);
  }

  private void saveAssignment(String groupMemberId, Assignment assignment) {
    assignmentManager.saveAssignment(subscriberId, groupMemberId, assignment);
  }

  public void close() {
    assignmentListener.remove();
    groupMember.remove();
    leaderSelector.stop();
  }
}
