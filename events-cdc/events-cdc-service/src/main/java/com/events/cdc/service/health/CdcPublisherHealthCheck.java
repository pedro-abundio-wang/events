package com.events.cdc.service.health;

import com.events.cdc.publisher.CdcPublisher;

public class CdcPublisherHealthCheck extends AbstractHealthCheck {

  private final CdcPublisher cdcPublisher;

  public CdcPublisherHealthCheck(CdcPublisher cdcPublisher) {
    this.cdcPublisher = cdcPublisher;
  }

  @Override
  protected void determineHealth(HealthBuilder builder) {
    if (cdcPublisher.isLastMessagePublishingFailed())
      builder.addError("Last event publishing failed");
  }
}
