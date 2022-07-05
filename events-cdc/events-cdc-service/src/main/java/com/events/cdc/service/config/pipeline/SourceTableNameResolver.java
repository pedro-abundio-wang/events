package com.events.cdc.service.config.pipeline;

public interface SourceTableNameResolver {

  String MESSAGE = "message";
  String EVENT = "events";
  String DEFAULT = "default";

  String resolve(String pipelineType);
}
