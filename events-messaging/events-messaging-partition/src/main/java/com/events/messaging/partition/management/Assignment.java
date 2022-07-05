package com.events.messaging.partition.management;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Assignment {

  private Set<String> channels;
  private Map<String, Set<Integer>> partitionAssignmentsByChannel;

  public Assignment() {}

  public Assignment(Assignment copy) {
    this.channels = new HashSet<>(copy.getChannels());
    this.partitionAssignmentsByChannel = new HashMap<>();
    copy.getPartitionAssignmentsByChannel()
        .forEach(
            (channel, partitions) ->
                partitionAssignmentsByChannel.put(channel, new HashSet<>(partitions)));
  }

  public Assignment(Set<String> channels, Map<String, Set<Integer>> partitionAssignmentsByChannel) {
    this.channels = channels;
    this.partitionAssignmentsByChannel = partitionAssignmentsByChannel;
  }

  public Set<String> getChannels() {
    return channels;
  }

  public void setChannels(Set<String> channels) {
    this.channels = channels;
  }

  public Map<String, Set<Integer>> getPartitionAssignmentsByChannel() {
    return partitionAssignmentsByChannel;
  }

  public void setPartitionAssignmentsByChannel(
      Map<String, Set<Integer>> partitionAssignmentsByChannel) {
    this.partitionAssignmentsByChannel = partitionAssignmentsByChannel;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override
  public boolean equals(Object o) {
    return EqualsBuilder.reflectionEquals(this, o);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
}
