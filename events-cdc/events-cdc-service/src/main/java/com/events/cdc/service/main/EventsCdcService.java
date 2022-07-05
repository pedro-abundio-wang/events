package com.events.cdc.service.main;

import com.events.cdc.connector.db.transaction.log.messaging.EventWithSourcing;
import com.events.cdc.pipeline.CdcPipeline;
import com.events.cdc.pipeline.CdcPipelineFactory;
import com.events.cdc.reader.leadership.CdcReaderLeadership;
import com.events.cdc.reader.factory.CdcReaderFactory;
import com.events.cdc.reader.properties.CdcReaderProperties;
import com.events.cdc.reader.provider.CdcReaderProvider;
import com.events.cdc.service.config.pipeline.SourceTableNameResolver;
import com.events.cdc.pipeline.CdcPipelineProperties;
import com.events.cdc.reader.*;
import com.events.cdc.service.properties.PropertyReader;
import com.events.cdc.service.properties.EventsCdcServiceProperties;
import com.events.messaging.leadership.coordination.LeaderSelectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

public class EventsCdcService {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final PropertyReader propertyReader = new PropertyReader();

  private final Map<String, CdcReaderProperties> cdcReaderProperties = new HashMap<>();

  private final Map<String, CdcPipelineProperties> cdcPipelineProperties = new HashMap<>();

  private final List<CdcPipeline> cdcPipelines = new ArrayList<>();

  @Autowired private Collection<CdcReaderFactory> cdcReaderFactories;

  @Autowired private Collection<CdcPipelineFactory> cdcPipelineFactories;

  @Autowired private CdcReaderProvider cdcReaderProvider;

  @Autowired private LeaderSelectorFactory leaderSelectorFactory;

  @Autowired private SourceTableNameResolver sourceTableNameResolver;

  @Autowired private EventsCdcServiceProperties eventsCdcServiceProperties;

  //  @Autowired private CdcMessageCleaners cdcMessageCleaners;

  @PostConstruct
  public void initialize() {

    logger.info("Events cdc service starting");

    // step 1: create CdcReader by factory type: postgres-wal, mysql-binlog, polling
    // step 2: create database connection pool
    // step 3: create CdcReaderLeadership with CdcReader
    // step 4: create CdcReaderProvider with CdcReaderLeadership
    if (eventsCdcServiceProperties.isReaderPropertiesDeclared()) {
      eventsCdcServiceProperties.getReader().forEach(this::createCdcReader);
    } else {
      throw new RuntimeException("No CdcReader defined!");
    }

    // step 1: create CdcPipeline by factory type: transactional-messaging, event-sourcing
    // step 2: query CdcReader with name defined by CdcPipeline
    // step 3: create TransactionLogEntryHandler with SchemaAndTable defined by CdcPipeline
    // step 4: bound TransactionLogEntryHandler with CdcPublisher
    // step 5: bound TransactionLogEntryHandler in CdcReader
    // step 6: bound CdcPublisher in CdcPipeline
    if (eventsCdcServiceProperties.isPipelinePropertiesDeclared()) {
      eventsCdcServiceProperties.getPipeline().forEach(this::createCdcPipeline);
    } else {
      throw new RuntimeException("No CdcPipeline defined!");
    }

    // step 1: CdcReader selecting leadership
    // step 2: CdcReader reading transaction log from database
    // step 3: CdcPublisher connecting to message-broker
    // step 4: TODO: What CdcMessageCleaners do ???
    start();
  }

  @PreDestroy
  public void stop() {
    cdcReaderProvider.stop();
    cdcPipelines.forEach(CdcPipeline::stop);
    // cdcMessageCleaners.stopMessageCleaners();
  }

  private void start() {
    cdcReaderProvider.start();
    cdcPipelines.forEach(CdcPipeline::start);
    // cdcMessageCleaners.start(cdcPipelineProperties, cdcReaderProperties);
    logger.info("Events cdc service started");
  }

  private void createCdcPipeline(String pipeline, Map<String, Object> properties) {

    propertyReader.checkForUnknownProperties(properties, CdcPipelineProperties.class);

    CdcPipelineProperties cdcPipelineProperties =
        propertyReader.convertMapToPropertyClass(properties, CdcPipelineProperties.class);

    cdcPipelineProperties.validate();

    if (cdcPipelineProperties.getSourceTableName() == null) {
      cdcPipelineProperties.setSourceTableName(
          sourceTableNameResolver.resolve(cdcPipelineProperties.getType()));
    }

    this.cdcPipelineProperties.put(pipeline.toLowerCase(), cdcPipelineProperties);

    CdcPipelineFactory<?> cdcPipelineFactory =
        findCdcPipelineFactory(cdcPipelineProperties.getType());
    CdcPipeline<?> cdcPipeline = cdcPipelineFactory.create(cdcPipelineProperties);
    cdcPipelines.add(cdcPipeline);
  }

  private void createCdcReader(String name, Map<String, Object> properties) {

    CdcReaderProperties cdcReaderProperties =
        propertyReader.convertMapToPropertyClass(properties, CdcReaderProperties.class);
    cdcReaderProperties.setReaderName(name);
    cdcReaderProperties.validate();

    CdcReaderFactory<? extends CdcReaderProperties, ? extends CdcReader> cdcReaderFactory =
        findCdcReaderFactory(cdcReaderProperties.getType());

    propertyReader.checkForUnknownProperties(properties, cdcReaderFactory.propertyClass());

    CdcReaderProperties exactCdcReaderProperties =
        propertyReader.convertMapToPropertyClass(properties, cdcReaderFactory.propertyClass());
    exactCdcReaderProperties.setReaderName(name);
    exactCdcReaderProperties.validate();

    this.cdcReaderProperties.put(name.toLowerCase(), exactCdcReaderProperties);

    CdcReader cdcReader = ((CdcReaderFactory) cdcReaderFactory).create(exactCdcReaderProperties);

    CdcReaderLeadership cdcReaderLeadership =
        new CdcReaderLeadership(
            exactCdcReaderProperties.getLeadershipLockPath(), leaderSelectorFactory, cdcReader);

    cdcReaderProvider.add(name, cdcReaderLeadership);
  }

  private CdcPipelineFactory<EventWithSourcing> findCdcPipelineFactory(String type) {
    return cdcPipelineFactories.stream()
        .filter(factory -> factory.supports(type))
        .findAny()
        .orElseThrow(
            () ->
                new RuntimeException(
                    String.format("pipeline factory not found for type %s", type)));
  }

  private CdcReaderFactory<? extends CdcReaderProperties, CdcReader> findCdcReaderFactory(
      String type) {
    return cdcReaderFactories.stream()
        .filter(factory -> factory.supports(type))
        .findAny()
        .orElseThrow(
            () ->
                new RuntimeException(
                    String.format("cdc reader factory not found for type %s", type)));
  }
}
