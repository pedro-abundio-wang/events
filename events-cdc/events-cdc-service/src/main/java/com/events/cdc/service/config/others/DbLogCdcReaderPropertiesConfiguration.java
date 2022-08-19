package com.events.cdc.service.config.others;

import com.events.cdc.service.config.cleaner.CdcReaderPropertiesConfiguration;
import com.events.cdc.reader.properties.DbLogCdcReaderProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DbLogCdcReaderPropertiesConfiguration extends CdcReaderPropertiesConfiguration {

  protected void initDbLogCdcReaderProperties(DbLogCdcReaderProperties dbLogCdcReaderProperties) {
//    dbLogCdcReaderProperties.setTransactionLogConnectionTimeoutInMilliseconds(
//        eventsCdcProperties.getTransactionLogConnectionTimeoutInMilliseconds());
//    dbLogCdcReaderProperties.setTransactionLogMaxAttemptsForConnection(
//        eventsCdcProperties.getMaxAttemptsForTransactionLogConnection());
//    dbLogCdcReaderProperties.setOffsetStorageTopicName(
//        eventsCdcProperties.getOffsetStorageTopicName());
//    dbLogCdcReaderProperties.setMonitoringSchema(
//        eventsCdcProperties.getMonitoringSchema());
  }
}
