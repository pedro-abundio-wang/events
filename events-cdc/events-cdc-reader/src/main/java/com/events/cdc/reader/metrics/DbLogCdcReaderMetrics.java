package com.events.cdc.reader.metrics;

import com.google.common.collect.ImmutableList;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class DbLogCdcReaderMetrics extends AbstractCdcReaderMetrics {

  private final CdcReaderMonitoringDao cdcReaderMonitoringDao;
  private final long replicationLagMeasuringIntervalInMilliseconds;
  private final AtomicInteger connected = new AtomicInteger(0);

  private Timer eventPublisherTimer;
  private DistributionSummary lag;

  private volatile long lastTimeEventReceived = -1;

  public DbLogCdcReaderMetrics(
      MeterRegistry meterRegistry,
      CdcReaderMonitoringDao cdcReaderMonitoringDao,
      String readerName,
      long replicationLagMeasuringIntervalInMilliseconds) {
    super(meterRegistry, readerName);
    this.cdcReaderMonitoringDao = cdcReaderMonitoringDao;
    this.replicationLagMeasuringIntervalInMilliseconds =
        replicationLagMeasuringIntervalInMilliseconds;
    tags = ImmutableList.of(Tag.of("readerName", readerName));
    initMetrics();
  }

  public void start() {
    if (meterRegistry == null) {
      return;
    }
    initLagMeasurementTimer();
  }

  public void stop() {
    if (meterRegistry == null) {
      return;
    }
    eventPublisherTimer.cancel();
  }

  public void onLagMeasurementEventReceived(long timestamp) {

    if (meterRegistry == null) {
      return;
    }

    lastTimeEventReceived = System.currentTimeMillis();

    lag.record(System.currentTimeMillis() - timestamp);
  }

  public void onTransactionLogEntryProcessed() {
    meterRegistry.counter("cdc.reader.transaction.log.entries.processed", tags).increment();
  }

  public void onConnected() {
    connected.set(1);
    meterRegistry.counter("cdc.reader.connection.attempts", tags).increment();
  }

  public void onDisconnected() {
    connected.set(0);
  }

  private void initLagMeasurementTimer() {

    eventPublisherTimer = new Timer();

    eventPublisherTimer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            cdcReaderMonitoringDao.update(readerName);
          }
        },
        0,
        replicationLagMeasuringIntervalInMilliseconds);
  }

  private void initMetrics() {
    if (meterRegistry != null) {
      Number lagAge =
          new Number() {
            @Override
            public int intValue() {
              return -1;
            }

            @Override
            public long longValue() {
              if (lastTimeEventReceived == -1) {
                return -1;
              }

              return System.currentTimeMillis() - lastTimeEventReceived;
            }

            @Override
            public float floatValue() {
              return -1;
            }

            @Override
            public double doubleValue() {
              return longValue();
            }
          };

      lag = meterRegistry.summary("cdc.reader.replication.lag", tags);
      meterRegistry.gauge("cdc.reader.replication.lag.age", tags, lagAge);
      meterRegistry.gauge("cdc.reader.connected.to.database", tags, connected);
    }
  }
}
