package com.events.cdc.service.health;

import com.events.cdc.reader.DbLogCdcReader;
import com.events.cdc.reader.CdcReader;
import com.events.cdc.reader.provider.CdcReaderProvider;
import org.springframework.beans.factory.annotation.Value;

public class CdcReaderHealthCheck extends AbstractHealthCheck {

  @Value("${events.cdc.max.event.interval.to.assume.reader.healthy:#{60000}}")
  private long maxEventIntervalToAssumeReaderHealthy;

  private final CdcReaderProvider cdcReaderProvider;

  public CdcReaderHealthCheck(CdcReaderProvider cdcReaderProvider) {
    this.cdcReaderProvider = cdcReaderProvider;
  }

  @Override
  protected void determineHealth(HealthBuilder builder) {

    cdcReaderProvider
        .getAll()
        .forEach(
            cdcReaderLeadership -> {
              CdcReader cdcReader = cdcReaderLeadership.getCdcReader();

              cdcReader
                  .getProcessingError()
                  .ifPresent(
                      error -> {
                        builder.addError(
                            String.format(
                                "%s got error during processing: %s",
                                cdcReader.getReaderName(), error));
                      });

              // TODO: MySql Support

              if (cdcReaderLeadership.isLeader()) {
                checkCdcReaderHealth(cdcReader, builder);
                if (cdcReader instanceof DbLogCdcReader) {
                  checkDbCdcReaderHealth((DbLogCdcReader) cdcReader, builder);
                }
              } else
                builder.addDetail(String.format("%s is not the leader", cdcReader.getReaderName()));
            });
  }

  private void checkDbCdcReaderHealth(DbLogCdcReader dbLogCdcReader, HealthBuilder builder) {
    if (dbLogCdcReader.isConnected()) {
      builder.addDetail(
          String.format("Reader with id %s is connected", dbLogCdcReader.getReaderName()));
    } else {
      builder.addError(
          String.format("Reader with id %s disconnected", dbLogCdcReader.getReaderName()));
    }
  }

  private void checkCdcReaderHealth(CdcReader cdcReader, HealthBuilder builder) {
    long age = System.currentTimeMillis() - cdcReader.getLastEventTime();
    boolean eventNotReceivedInTime = age > maxEventIntervalToAssumeReaderHealthy;

    if (eventNotReceivedInTime) {
      builder.addError(
          String.format(
              "Reader with id %s has not received message for %s milliseconds",
              cdcReader.getReaderName(), age));
    } else
      builder.addDetail(
          String.format(
              "Reader with id %s received message %s milliseconds ago",
              cdcReader.getReaderName(), age));
  }
}
