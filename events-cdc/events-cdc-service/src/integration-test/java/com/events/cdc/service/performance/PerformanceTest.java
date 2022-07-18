package com.events.cdc.service.performance;

import com.events.cdc.connector.db.transaction.log.converter.TransactionLogEntryToEventWithSourcingConverter;
import com.events.cdc.connector.db.transaction.log.messaging.EventWithSourcing;
import com.events.cdc.connector.postgres.wal.PostgresWalClient;
import com.events.cdc.reader.SourceTableNameSupplier;
import com.events.cdc.service.helper.TestHelper;
import com.events.cdc.publisher.CdcPublisher;
import com.events.cdc.publisher.filter.PublishingFilter;
import com.events.common.id.ApplicationIdGenerator;
import com.events.common.jdbc.schema.EventsSchema;
import com.events.common.util.Eventually;
import com.events.messaging.kafka.producer.KafkaMessageProducer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PerformanceTest.TestConfiguration.class})
@ActiveProfiles("kafka")
public class PerformanceTest {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Configuration
  @EnableAutoConfiguration
  @Import({PostgreSqlWalKafkaCdcTestConfiguration.class, OffsetStoreMockConfiguration.class})
  public static class TestConfiguration {
    @Bean
    @Primary
    public PublishingFilter publishingFilter() {
      return (sourceBinlogFileOffset, destinationTopic) -> true;
    }
  }

  @Autowired private PostgresWalClient postgresWalClient;

  @Autowired private TestHelper testHelper;

  @Autowired private SourceTableNameSupplier sourceTableNameSupplier;

  @Autowired private CdcPublisher<EventWithSourcing> cdcPublisher;

  @Autowired private KafkaMessageProducer kafkaMessageProducer;

  private final int nEvents = 10000;

  // ----------------------------------------------------------------------------------------------------

  @Test
  public void testAllEventsSameTopicSameId() {
    runTest(nEvents, makeFixedEntityTypeGenerator(), makeFixedEntityIdSupplier());
  }

  @Test
  public void testAllEventsDifferentTopicsSameId() {
    runTest(nEvents, makeUniqueEntityTypeGenerator(), makeFixedEntityIdSupplier());
  }

  @Test
  public void test2TopicsSameId() {
    runTest(nEvents, makeNEntityTypeGenerator(2), makeFixedEntityIdSupplier());
  }

  @Test
  public void test4TopicsSameId() {
    runTest(nEvents, makeNEntityTypeGenerator(4), makeFixedEntityIdSupplier());
  }

  @Test
  public void test10TopicsSameId() {
    runTest(nEvents, makeNEntityTypeGenerator(10), makeFixedEntityIdSupplier());
  }

  @Test
  public void test100TopicsSameId() {
    runTest(nEvents, makeNEntityTypeGenerator(100), makeFixedEntityIdSupplier());
  }

  // ----------------------------------------------------------------------------------------------------

  @Test
  public void testAllEventsSameTopicDifferentIds() {
    runTest(nEvents, makeFixedEntityTypeGenerator(), makeUniqueEntityIdSupplier());
  }

  @Test
  public void testAllEventsDifferentTopicsDifferentIds() {
    runTest(nEvents, makeUniqueEntityTypeGenerator(), makeUniqueEntityIdSupplier());
  }

  @Test
  public void test2TopicsDifferentIds() {
    runTest(nEvents, makeNEntityTypeGenerator(2), makeUniqueEntityIdSupplier());
  }

  @Test
  public void test4TopicsDifferentIds() {
    runTest(nEvents, makeNEntityTypeGenerator(4), makeUniqueEntityIdSupplier());
  }

  @Test
  public void test10TopicsDifferentIds() {
    runTest(nEvents, makeNEntityTypeGenerator(10), makeUniqueEntityIdSupplier());
  }

  @Test
  public void test100TopicsDifferentIds() {
    runTest(nEvents, makeNEntityTypeGenerator(100), makeUniqueEntityIdSupplier());
  }

  // ----------------------------------------------------------------------------------------------------

  private void runTest(
      int nEvents,
      Function<Integer, String> entityTypeGenerator,
      Supplier<String> entityIdGenerator) {
    testPerformance(
        () -> {
          List<EntityTypeAndId> entityTypesAndIds = new ArrayList<>();
          for (int i = 0; i < nEvents; i++) {
            entityTypesAndIds.add(
                new EntityTypeAndId(entityTypeGenerator.apply(i), entityIdGenerator.get()));
          }
          prepareTopics(entityTypesAndIds);
          generateEvents(entityTypesAndIds);
        });
  }

  private Function<Integer, String> makeFixedEntityTypeGenerator() {
    String entityType = generateId();
    return (i) -> entityType;
  }

  private Function<Integer, String> makeUniqueEntityTypeGenerator() {
    return (i) -> generateId();
  }

  private Function<Integer, String> makeNEntityTypeGenerator(int n) {
    String entityType = generateId();
    return (i) -> entityType + (i % n);
  }

  private Supplier<String> makeUniqueEntityIdSupplier() {
    return this::generateId;
  }

  private Supplier<String> makeFixedEntityIdSupplier() {
    String id = generateId();
    return () -> id;
  }

  private void prepareTopics(List<EntityTypeAndId> entityTypesAndIds) {
    entityTypesAndIds.stream()
        .map(EntityTypeAndId::getType)
        .collect(Collectors.toSet())
        .forEach(
            eventType -> {
              try {
                kafkaMessageProducer.partitionsFor(eventType);
                kafkaMessageProducer.send(eventType, generateId(), generateId());
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
  }

  private void generateEvents(List<EntityTypeAndId> entityTypesAndIds) {
    entityTypesAndIds.forEach(
        entityTypeAndId -> {
          testHelper.saveEvent(
              entityTypeAndId.getType(), generateId(), generateId(), entityTypeAndId.getId());
        });
  }

  private void testPerformance(Runnable eventCreator) {

    cdcPublisher.start();

    eventCreator.run();

    postgresWalClient.addCdcPipelineHandler(
        new EventsSchema(EventsSchema.DEFAULT_SCHEMA),
        sourceTableNameSupplier.getSourceTableName(),
        new TransactionLogEntryToEventWithSourcingConverter(new ApplicationIdGenerator()),
        cdcPublisher::publishMessage);

    testHelper.runInSeparateThread(postgresWalClient::start);

    Eventually.run(
        1000,
        500,
        TimeUnit.MILLISECONDS,
        () -> {
          logger.info("--------------------");
          Assert.assertEquals(nEvents, cdcPublisher.getTotallyProcessedEventCount());
          logger.info(
              String.format(
                  "%s event processing, average send time is %s ms",
                  nEvents, cdcPublisher.getPublishTimeAccumulator() / (double) nEvents / 1000000d));
        });
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }

  private static class EntityTypeAndId {

    private final String type;
    private final String id;

    public EntityTypeAndId(String type, String id) {
      this.type = type;
      this.id = id;
    }

    public String getType() {
      return type;
    }

    public String getId() {
      return id;
    }
  }
}
