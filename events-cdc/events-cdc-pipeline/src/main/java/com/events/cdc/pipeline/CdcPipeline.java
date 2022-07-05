package com.events.cdc.pipeline;

import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogMessage;
import com.events.cdc.publisher.CdcPublisher;

public class CdcPipeline<TLM extends TransactionLogMessage> {

  private CdcPublisher<TLM> cdcPublisher;

  public CdcPipeline(CdcPublisher<TLM> cdcPublisher) {
    this.cdcPublisher = cdcPublisher;
  }

  public void start() {
    cdcPublisher.start();
  }

  public void stop() {
    cdcPublisher.stop();
  }
}
