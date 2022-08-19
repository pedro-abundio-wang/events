package com.events.cdc.reader.status;

public interface CdcReaderProcessingStatusService {

  CdcReaderProcessingStatus getCurrentStatus();

  void saveEndingOffsetOfLastProcessedEvent(long endingOffsetOfLastProcessedEvent);
}
