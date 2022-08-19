package com.events.cdc.service.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public abstract class AbstractHealthCheck implements HealthIndicator {

  @Override
  public Health health() {
    HealthBuilder builder = new HealthBuilder();
    determineHealth(builder);
    return builder.build();
  }

  protected abstract void determineHealth(HealthBuilder builder);

}
