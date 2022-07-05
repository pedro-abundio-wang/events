package com.events.cdc.reader.metrics;

import io.micrometer.core.instrument.MeterRegistry;

import java.util.concurrent.atomic.AtomicInteger;

public class CdcReaderMetrics extends AbstractCdcReaderMetrics {

  private final AtomicInteger leader = new AtomicInteger(0);

  public CdcReaderMetrics(MeterRegistry meterRegistry, String readerName) {
    super(meterRegistry, readerName);
    initMetrics();
  }

  public void setLeader(boolean value) {
    leader.set(value ? 1 : 0);
  }

  public void onMessageProcessed() {
    meterRegistry.counter("cdc.reader.messages.processed", tags).increment();
  }

  private void initMetrics() {
    if (meterRegistry == null) {
      return;
    }
    meterRegistry.gauge("cdc.reader.leader", tags, leader);
  }
}
