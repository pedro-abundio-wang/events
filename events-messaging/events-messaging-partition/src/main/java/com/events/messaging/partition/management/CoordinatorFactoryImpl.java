package com.events.messaging.partition.management;

import com.events.messaging.leadership.coordination.LeaderRemovedCallback;
import com.events.messaging.leadership.coordination.LeaderSelectedCallback;
import com.events.messaging.leadership.coordination.LeaderSelectorFactory;

import java.util.Set;
import java.util.function.Consumer;

public class CoordinatorFactoryImpl implements CoordinatorFactory {

  private final AssignmentManager assignmentManager;
  private final AssignmentListenerFactory assignmentListenerFactory;
  private final MemberGroupManagerFactory memberGroupManagerFactory;
  private final LeaderSelectorFactory leaderSelectorFactory;
  private final GroupMemberFactory groupMemberFactory;
  private final int partitionCount;

  public CoordinatorFactoryImpl(
      AssignmentManager assignmentManager,
      AssignmentListenerFactory assignmentListenerFactory,
      MemberGroupManagerFactory memberGroupManagerFactory,
      LeaderSelectorFactory leaderSelectorFactory,
      GroupMemberFactory groupMemberFactory,
      int partitionCount) {

    this.assignmentManager = assignmentManager;
    this.assignmentListenerFactory = assignmentListenerFactory;
    this.memberGroupManagerFactory = memberGroupManagerFactory;
    this.leaderSelectorFactory = leaderSelectorFactory;
    this.groupMemberFactory = groupMemberFactory;
    this.partitionCount = partitionCount;
  }

  @Override
  public Coordinator makeCoordinator(
      String subscriberId,
      Set<String> channels,
      String subscriptionId,
      Consumer<Assignment> assignmentUpdatedCallback,
      String lockId,
      LeaderSelectedCallback leaderSelectedCallback,
      LeaderRemovedCallback leaderRemovedCallback) {

    return new Coordinator(
        subscriptionId,
        subscriberId,
        channels,
        partitionCount,
        groupMemberFactory,
        memberGroupManagerFactory,
        assignmentManager,
        assignmentListenerFactory,
        leaderSelectorFactory,
        assignmentUpdatedCallback,
        lockId,
        leaderSelectedCallback,
        leaderRemovedCallback);
  }
}
