package com.events.cdc.service.config.publisher;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class EventSourcingCondition implements Condition {
  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    String type = context.getEnvironment().resolvePlaceholders("${events.cdc.type:}");
    return "EventSourcing".equals(type);
  }
}
