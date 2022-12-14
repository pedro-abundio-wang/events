package com.events.cdc.service.config.others;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Profiles;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ActiveMQOrRabbitMQOrRedisCondition implements Condition {
  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    return (context.getEnvironment().acceptsProfiles(Profiles.of("ActiveMQ"))
            || context.getEnvironment().acceptsProfiles(Profiles.of("RabbitMQ")))
        || context.getEnvironment().acceptsProfiles(Profiles.of("Redis"));
  }
}
