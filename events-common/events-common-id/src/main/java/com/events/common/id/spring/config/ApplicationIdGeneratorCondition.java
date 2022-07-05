package com.events.common.id.spring.config;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ApplicationIdGeneratorCondition extends SpringBootCondition {
  @Override
  public ConditionOutcome getMatchOutcome(
      ConditionContext context, AnnotatedTypeMetadata metadata) {
    boolean match = context.getEnvironment().getProperty("events.outbox.id") == null;
    return new ConditionOutcome(
        match,
        match
            ? "application id generator condition matched"
            : "application id generator condition failed");
  }
}
