package com.events.common.jdbc.operation;

import com.events.common.id.IdGenerator;
import com.events.common.id.spring.config.IdGeneratorConfiguration;
import com.events.common.jdbc.exception.EventsDuplicateKeyException;
import com.events.common.jdbc.executor.EventsJdbcStatementExecutor;
import com.events.common.jdbc.schema.EventsSchema;
import com.events.common.jdbc.spring.config.EventsJdbcOperationsConfiguration;
import com.events.common.jdbc.sql.dialect.EventsSqlDialect;
import com.events.common.jdbc.sql.dialect.EventsSqlDialectSelector;
import com.events.common.jdbc.transaction.EventsTransactionTemplate;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SpringBootTest(classes = EventsJdbcOperationsTest.TestConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class EventsJdbcOperationsTest extends AbstractEventsJdbcOperationsTest {

  @Configuration
  @EnableAutoConfiguration
  @Import({EventsJdbcOperationsConfiguration.class, IdGeneratorConfiguration.class})
  public static class TestConfiguration {}

  @Value("${spring.datasource.driver-class-name}")
  private String driver;

  @Autowired private EventsSchema eventsSchema;

  @Autowired private EventsJdbcOperations eventsJdbcOperations;

  @Autowired private EventsJdbcStatementExecutor eventsJdbcStatementExecutor;

  @Autowired private EventsTransactionTemplate eventsTransactionTemplate;

  @Autowired private DataSource dataSource;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private EventsSqlDialectSelector eventsSqlDialectSelector;

  @Autowired private IdGenerator idGenerator;

  @Test(expected = EventsDuplicateKeyException.class)
  @Override
  public void eventsDuplicateKeyExceptionTest() {
    super.eventsDuplicateKeyExceptionTest();
  }

  @Test
  @Override
  public void generatedIdOfEventsTableRowTest() {
    super.generatedIdOfEventsTableRowTest();
  }

  @Test
  @Override
  public void generatedIdOfMessageTableRowTest() {
    super.generatedIdOfMessageTableRowTest();
  }

  @Test
  @Override
  public void insertIntoEventsTableTest() throws SQLException {
    super.insertIntoEventsTableTest();
  }

  @Test
  @Override
  public void insertIntoMessageTableTest() throws SQLException {
    super.insertIntoMessageTableTest();
  }

  @Test
  public void jsonColumnToStringConversionTest() {

    String payloadData = generateId();
    String rawPayload = "\"" + payloadData + "\"";

    String messageId =
        eventsJdbcOperations.insertIntoMessageTable(
            idGenerator, rawPayload, StringUtils.EMPTY, new HashMap<>());

    IdColumnAndValue idColumnAndValue = messageIdToRowId(messageId);

    SqlRowSet sqlRowSet =
        jdbcTemplate.queryForRowSet(
            String.format(
                "select payload from %s where %s = ?",
                eventsSchema.qualifyTable("message"), idColumnAndValue.getColumn()),
            idColumnAndValue.getValue());

    sqlRowSet.next();

    Object payload = sqlRowSet.getObject("payload");

    String payloadString = payload.toString();

    Assert.assertTrue(payloadString.contains(payloadData));
  }

  @Override
  protected String insertIntoMessageTable(
      String payload, String destination, Map<String, String> headers) {
    return eventsTransactionTemplate.executeInTransaction(
        () ->
            eventsJdbcOperations.insertIntoMessageTable(
                idGenerator, payload, destination, headers));
  }

  @Override
  protected String insertIntoEventsTable(
      String entityId,
      String eventData,
      String eventType,
      String entityType,
      Optional<String> triggeringEvent,
      Optional<String> metadata) {
    return eventsTransactionTemplate.executeInTransaction(
        () ->
            eventsJdbcOperations.insertIntoEventsTable(
                idGenerator,
                entityId,
                eventData,
                eventType,
                entityType,
                triggeringEvent,
                metadata));
  }

  @Override
  protected void insertIntoEntitiesTable(
      String entityId, String entityType, EventsSchema eventsSchema) {
    String table = eventsSchema.qualifyTable("entities");
    String sql = String.format("insert into %s values (?, ?, ?);", table);
    eventsTransactionTemplate.executeInTransaction(
        () -> eventsJdbcStatementExecutor.update(sql, entityId, entityType, System.nanoTime()));
  }

  @Override
  protected EventsSchema getEventsSchema() {
    return eventsSchema;
  }

  @Override
  protected EventsTransactionTemplate getEventsTransactionTemplate() {
    return eventsTransactionTemplate;
  }

  @Override
  protected IdGenerator getIdGenerator() {
    return idGenerator;
  }

  @Override
  protected DataSource getDataSource() {
    return dataSource;
  }

  @Override
  protected EventsSqlDialect getEventsSqlDialect() {
    return eventsSqlDialectSelector.getDialect(driver);
  }
}
