package com.events.cdc.reader.metrics;

import com.google.common.collect.ImmutableList;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import java.util.List;

public class AbstractCdcReaderMetrics {

  protected MeterRegistry meterRegistry;
  protected List<Tag> tags;
  protected String readerName;

  public AbstractCdcReaderMetrics(MeterRegistry meterRegistry, String readerName) {
    this.meterRegistry = meterRegistry;
    this.readerName = readerName;
    tags = ImmutableList.of(Tag.of("readerName", readerName));
  }
}
