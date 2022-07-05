package com.events.cdc.service.cleaners;

import com.events.cdc.pipeline.CdcPipelineProperties;
import com.events.cdc.reader.properties.CdcReaderProperties;
import com.events.cdc.reader.connection.pool.ConnectionPoolProperties;
import com.events.cdc.reader.connection.pool.DataSourceFactory;
import com.events.cdc.service.properties.PropertyReader;
import com.events.cdc.service.properties.EventsCdcServiceProperties;
import com.events.cdc.service.properties.MessageCleanerProperties;
import com.events.common.jdbc.schema.EventsSchema;
import com.events.common.jdbc.sql.dialect.EventsSqlDialect;
import com.events.common.jdbc.sql.dialect.EventsSqlDialectSelector;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CdcMessageCleaners {

  private final PropertyReader propertyReader = new PropertyReader();

  @Autowired private EventsCdcServiceProperties eventsCdcServiceProperties;

  @Autowired private ConnectionPoolProperties connectionPoolProperties;

  @Autowired private EventsSqlDialectSelector sqlDialectSelector;

  @Autowired private CdcPipelineProperties cdcPipelineProperties;

  @Autowired private CdcReaderProperties cdcReaderProperties;

  private final List<MessageCleaner> messageCleaners = new ArrayList<>();

  public void start(
      Map<String, CdcPipelineProperties> cdcPipelineProperties,
      Map<String, CdcReaderProperties> cdcReaderProperties) {

    eventsCdcServiceProperties
        .getCleaner()
        .forEach(
            (cleaner, properties) -> {
              propertyReader.checkForUnknownProperties(properties, MessageCleanerProperties.class);

              MessageCleanerProperties messageCleanerProperties =
                  propertyReader.convertMapToPropertyClass(
                      properties, MessageCleanerProperties.class);

              messageCleanerProperties.validate();

              createAndStartMessageCleaner(
                  messageCleanerProperties,
                  createConnectionInfo(
                      messageCleanerProperties, cdcPipelineProperties, cdcReaderProperties));
            });
  }

  public void stopMessageCleaners() {
    messageCleaners.forEach(MessageCleaner::stop);
  }

  private void createAndStartMessageCleaner(
      MessageCleanerProperties messageCleanerProperties, ConnectionInfo connectionInfo) {

    MessageCleaner messageCleaner =
        new MessageCleaner(
            connectionInfo.getEventsSqlDialect(),
            connectionInfo.getDataSource(),
            connectionInfo.getEventsSchema(),
            messageCleanerProperties.getPurge());

    messageCleaner.start();

    messageCleaners.add(messageCleaner);
  }

  private ConnectionInfo createConnectionInfo(
      MessageCleanerProperties messageCleanerProperties,
      Map<String, CdcPipelineProperties> cdcPipelineProperties,
      Map<String, CdcReaderProperties> cdcPipelineReaderProperties) {
    if (messageCleanerProperties.getPipeline() != null) {
      if (messageCleanerProperties.getPipeline().equalsIgnoreCase("default")) {
        return createDefaultPipelineCleanerConnectionInfo();
      } else {
        return createPipelineCleanerConnectionInfo(
            messageCleanerProperties,
            cdcPipelineProperties.get(messageCleanerProperties.getPipeline().toLowerCase()),
            cdcPipelineReaderProperties);
      }
    } else {
      return createCustomCleanerConnectionInfo(messageCleanerProperties);
    }
  }

  private ConnectionInfo createDefaultPipelineCleanerConnectionInfo() {
    DataSource dataSource =
        DataSourceFactory.createDataSource(
            cdcReaderProperties.getDataSourceUrl(),
            cdcReaderProperties.getDataSourceDriverClassName(),
            cdcReaderProperties.getDataSourceUserName(),
            cdcReaderProperties.getDataSourcePassword(),
            connectionPoolProperties);

    EventsSchema eventsSchema = createEventsSchema(cdcPipelineProperties.getEventsDatabaseSchema());
    EventsSqlDialect sqlDialect =
        sqlDialectSelector.getDialect(cdcReaderProperties.getDataSourceDriverClassName());

    return new ConnectionInfo(dataSource, eventsSchema, sqlDialect);
  }

  private ConnectionInfo createPipelineCleanerConnectionInfo(
      MessageCleanerProperties messageCleanerProperties,
      CdcPipelineProperties pipelineProperties,
      Map<String, CdcReaderProperties> cdcPipelineReaderProperties) {
    if (pipelineProperties == null) {
      throw new RuntimeException(
          String.format(
              "Cannot start cleaner pipeline %s is not found.",
              messageCleanerProperties.getPipeline()));
    }

    String reader = pipelineProperties.getReader().toLowerCase();
    CdcReaderProperties readerProperties = cdcPipelineReaderProperties.get(reader);

    DataSource dataSource =
        DataSourceFactory.createDataSource(
            readerProperties.getDataSourceUrl(),
            readerProperties.getDataSourceDriverClassName(),
            readerProperties.getDataSourceUserName(),
            readerProperties.getDataSourcePassword(),
            connectionPoolProperties);

    EventsSchema eventsSchema = createEventsSchema(pipelineProperties.getEventsDatabaseSchema());
    EventsSqlDialect sqlDialect =
        sqlDialectSelector.getDialect(readerProperties.getDataSourceDriverClassName());

    return new ConnectionInfo(dataSource, eventsSchema, sqlDialect);
  }

  private ConnectionInfo createCustomCleanerConnectionInfo(
      MessageCleanerProperties messageCleanerProperties) {
    DataSource dataSource =
        DataSourceFactory.createDataSource(
            messageCleanerProperties.getDataSourceUrl(),
            messageCleanerProperties.getDataSourceDriverClassName(),
            messageCleanerProperties.getDataSourceUserName(),
            messageCleanerProperties.getDataSourcePassword(),
            connectionPoolProperties);

    EventsSchema eventsSchema = createEventsSchema(messageCleanerProperties.getEventsSchema());
    EventsSqlDialect sqlDialect =
        sqlDialectSelector.getDialect(messageCleanerProperties.getDataSourceDriverClassName());

    return new ConnectionInfo(dataSource, eventsSchema, sqlDialect);
  }

  private EventsSchema createEventsSchema(String schema) {
    return new EventsSchema(schema == null ? EventsSchema.DEFAULT_SCHEMA : schema);
  }

  static class ConnectionInfo {

    private final DataSource dataSource;
    private final EventsSchema eventsSchema;
    private final EventsSqlDialect eventsSqlDialect;

    public ConnectionInfo(
        DataSource dataSource, EventsSchema eventsSchema, EventsSqlDialect eventsSqlDialect) {
      this.dataSource = dataSource;
      this.eventsSchema = eventsSchema;
      this.eventsSqlDialect = eventsSqlDialect;
    }

    public DataSource getDataSource() {
      return dataSource;
    }

    public EventsSchema getEventsSchema() {
      return eventsSchema;
    }

    public EventsSqlDialect getEventsSqlDialect() {
      return eventsSqlDialect;
    }
  }
}
