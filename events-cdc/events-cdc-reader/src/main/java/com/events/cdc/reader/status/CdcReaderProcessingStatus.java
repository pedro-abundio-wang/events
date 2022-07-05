package com.events.cdc.reader.status;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class CdcReaderProcessingStatus {

  private final long lastEventOffset;
  private final long logsHighwaterMark;
  private final boolean cdcProcessingFinished;

  public CdcReaderProcessingStatus(long lastEventOffset, long logsHighwaterMark) {
    this(lastEventOffset, logsHighwaterMark, lastEventOffset == logsHighwaterMark);
  }

  public CdcReaderProcessingStatus(
      long lastEventOffset, long logsHighwaterMark, boolean cdcProcessingFinished) {
    this.lastEventOffset = lastEventOffset;
    this.logsHighwaterMark = logsHighwaterMark;
    this.cdcProcessingFinished = cdcProcessingFinished;
  }

  public long getLastEventOffset() {
    return lastEventOffset;
  }

  public long getLogsHighwaterMark() {
    return logsHighwaterMark;
  }

  public boolean isCdcProcessingFinished() {
    return cdcProcessingFinished;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
