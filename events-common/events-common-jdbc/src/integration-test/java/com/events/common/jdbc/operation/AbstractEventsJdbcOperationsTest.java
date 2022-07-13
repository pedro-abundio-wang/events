package com.events.common.jdbc.operation;

import com.events.common.id.IdGenerator;
import com.events.common.id.Int128;
import com.events.common.jdbc.schema.EventsSchema;
import com.events.common.jdbc.sql.dialect.EventsSqlDialect;
import com.events.common.jdbc.sql.dialect.MySqlDialect;
import com.events.common.jdbc.transaction.EventsTransactionTemplate;
import com.events.common.json.mapper.JsonMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.events.common.jdbc.operation.EventsJdbcOperationsUtils.EVENT_AUTO_GENERATED_ID_COLUMN;
import static com.events.common.jdbc.operation.EventsJdbcOperationsUtils.MESSAGE_AUTO_GENERATED_ID_COLUMN;

public abstract class AbstractEventsJdbcOperationsTest {

  protected abstract EventsSchema getEventsSchema();

  protected abstract EventsTransactionTemplate getEventsTransactionTemplate();

  protected abstract IdGenerator getIdGenerator();

  protected abstract DataSource getDataSource();

  protected abstract EventsSqlDialect getEventsSqlDialect();

  protected abstract String insertIntoMessageTable(
      String payload, String destination, Map<String, String> headers);

  protected abstract String insertIntoEventsTable(
      String entityId,
      String eventData,
      String eventType,
      String entityType,
      Optional<String> triggeringEvent,
      Optional<String> metadata);

  protected abstract void insertIntoEntitiesTable(
      String entityId, String entityType, EventsSchema eventsSchema);

  public void eventsDuplicateKeyExceptionTest() {
    String entityId = generateId();
    String entityType = generateId();
    insertIntoEntitiesTable(entityId, entityType, getEventsSchema());
    insertIntoEntitiesTable(entityId, entityType, getEventsSchema());
  }

  public void insertIntoEventsTableTest() throws SQLException {
    String entityId = generateId();
    String eventData = generateId();
    String eventType = generateId();
    String entityType = generateId();
    String triggeringEvent = generateId();
    String metadata = generateId();

    String eventId =
        insertIntoEventsTable(
            entityId,
            eventData,
            eventType,
            entityType,
            Optional.of(triggeringEvent),
            Optional.of(metadata));

    List<Map<String, Object>> events = getEvents(eventIdToRowId(eventId));

    Assert.assertEquals(1, events.size());

    Map<String, Object> event = events.get(0);

    boolean eventIdIsEmpty = StringUtils.isEmpty((String) event.get("event_id"));
    if (getIdGenerator().databaseIdRequired()) {
      Assert.assertTrue(eventIdIsEmpty);
    } else {
      Assert.assertFalse(eventIdIsEmpty);
    }

    Assert.assertEquals(eventType, event.get("event_type"));
    Assert.assertEquals(eventData, event.get("event_data"));
    Assert.assertEquals(entityType, event.get("entity_type"));
    Assert.assertEquals(entityId, event.get("entity_id"));
    Assert.assertEquals(triggeringEvent, event.get("triggering_event"));
    Assert.assertEquals(metadata, event.get("metadata"));
  }

  public void insertIntoMessageTableTest() throws SQLException {
    String payload = "\"" + generateId() + "\"";
    String destination = generateId();
    Map<String, String> expectedHeaders = new HashMap<>();
    expectedHeaders.put("headerKey", "headerValue");

    String messageId = insertIntoMessageTable(payload, destination, expectedHeaders);

    List<Map<String, Object>> messages = getMessages(messageIdToRowId(messageId));

    Assert.assertEquals(1, messages.size());

    Map<String, Object> event = messages.get(0);

    Map<String, String> actualHeaders =
        JsonMapper.fromJson(event.get("headers").toString(), Map.class);

    if (!getIdGenerator().databaseIdRequired()) {
      Assert.assertTrue(actualHeaders.containsKey("id"));
      Assert.assertEquals(messageId, actualHeaders.get("id"));
    }

    Assert.assertEquals(destination, event.get("destination"));
    Assert.assertEquals(payload, event.get("payload"));
    // since time is generated automatically now, it is hard to predict accurate time. So there is
    // estimated time is used (5 min accuracy)
    Assert.assertTrue(
        System.currentTimeMillis() - (long) event.get("creation_time") < 5 * 60 * 1000);
    Assert.assertEquals(expectedHeaders, actualHeaders);
  }

  protected void generatedIdOfEventsTableRowTest() {
    generatedIdTest(this::insertRandomEvent, this::assertIdAnchorEventCreated);
  }

  protected void generatedIdOfMessageTableRowTest() {
    generatedIdTest(this::insertRandomMessage, this::assertIdAnchorMessageCreated);
  }

  private long insertRandomEvent() {
    return (long)
        eventIdToRowId(
                insertIntoEventsTable(
                    generateId(),
                    generateId(),
                    generateId(),
                    generateId(),
                    Optional.of(generateId()),
                    Optional.of(generateId())))
            .getValue();
  }

  private void assertIdAnchorEventCreated() {
    if (!(getEventsSqlDialect() instanceof MySqlDialect)) {
      return;
    }

    getEventsTransactionTemplate()
        .executeInTransaction(
            () -> {
              String table = getEventsSchema().qualifyTable("events");
              String sql =
                  String.format("select * from %s where event_type = 'CDC-IGNORED'", table);
              try (Connection connection = getDataSource().getConnection();
                  PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet rs = preparedStatement.executeQuery()) {
                  Assert.assertTrue(rs.next());
                }
              } catch (SQLException e) {
                throw new RuntimeException(e);
              }

              return null;
            });
  }

  private long insertRandomMessage() {
    return (long)
        messageIdToRowId(
                insertIntoMessageTable(
                    "\"" + generateId() + "\"", generateId(), Collections.emptyMap()))
            .getValue();
  }

  private void assertIdAnchorMessageCreated() {
    if (!(getEventsSqlDialect() instanceof MySqlDialect)) {
      return;
    }

    getEventsTransactionTemplate()
        .executeInTransaction(
            () -> {
              String table = getEventsSchema().qualifyTable("message");
              String sql =
                  String.format("select * from %s where destination = 'CDC-IGNORED'", table);
              try (Connection connection = getDataSource().getConnection();
                  PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet rs = preparedStatement.executeQuery()) {
                  Assert.assertTrue(rs.next());
                }
              } catch (SQLException e) {
                throw new RuntimeException(e);
              }

              return null;
            });
  }

  private void generatedIdTest(
      Supplier<Long> insertOperation, Runnable idAnchorVerificationCallback) {
    if (!getIdGenerator().databaseIdRequired()) return; // nothing to do

    idAnchorVerificationCallback.run();

    long rowId = insertOperation.get();

    assertIdSequenceUsesCurrentTimeAsStartingValue(rowId);
  }

  protected String generateId() {
    return UUID.randomUUID().toString();
  }

  protected List<Map<String, Object>> getEvents(IdColumnAndValue idColumnAndValue) {
    return getEventsTransactionTemplate()
        .executeInTransaction(
            () -> {
              String table = getEventsSchema().qualifyTable("events");
              String sql =
                  String.format(
                      "select %s, event_type, event_data, entity_type, entity_id, triggering_event, metadata from %s where %s = ?",
                      idColumnAndValue.getColumn(), table, idColumnAndValue.getColumn());

              try (Connection connection = getDataSource().getConnection();
                  PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setObject(1, idColumnAndValue.getValue());

                List<Map<String, Object>> events = new ArrayList<>();

                try (ResultSet rs = preparedStatement.executeQuery()) {
                  while (rs.next()) {
                    Map<String, Object> event = new HashMap<>();

                    event.put("event_id", rs.getString("event_id"));
                    event.put("event_type", rs.getString("event_type"));
                    event.put("event_data", rs.getString("event_data"));
                    event.put("entity_type", rs.getString("entity_type"));
                    event.put("entity_id", rs.getString("entity_id"));
                    event.put("triggering_event", rs.getString("triggering_event"));
                    event.put("metadata", rs.getString("metadata"));

                    events.add(event);
                  }
                }

                return events;

              } catch (SQLException e) {
                throw new RuntimeException(e);
              }
            });
  }

  protected List<Map<String, Object>> getMessages(IdColumnAndValue idColumnAndValue) {
    return getEventsTransactionTemplate()
        .executeInTransaction(
            () -> {
              String table = getEventsSchema().qualifyTable("message");
              String sql =
                  String.format(
                      "select %s, destination, headers, payload, creation_time from %s where %s = ?",
                      idColumnAndValue.getColumn(), table, idColumnAndValue.getColumn());

              try (Connection connection = getDataSource().getConnection();
                  PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setObject(1, idColumnAndValue.getValue());

                List<Map<String, Object>> messages = new ArrayList<>();

                try (ResultSet rs = preparedStatement.executeQuery()) {
                  while (rs.next()) {
                    Map<String, Object> message = new HashMap<>();

                    message.put("destination", rs.getString("destination"));
                    message.put("headers", rs.getString("headers"));
                    message.put("payload", rs.getString("payload"));
                    message.put("creation_time", rs.getLong("creation_time"));

                    messages.add(message);
                  }
                }

                return messages;

              } catch (SQLException e) {
                throw new RuntimeException(e);
              }
            });
  }

  protected IdColumnAndValue messageIdToRowId(String messageId) {
    if (getIdGenerator().databaseIdRequired()) {
      return new IdColumnAndValue(
          MESSAGE_AUTO_GENERATED_ID_COLUMN, extractRowIdFromEventId(messageId));
    }

    return new IdColumnAndValue("id", messageId);
  }

  protected IdColumnAndValue eventIdToRowId(String eventId) {
    if (getIdGenerator().databaseIdRequired()) {
      return new IdColumnAndValue(EVENT_AUTO_GENERATED_ID_COLUMN, extractRowIdFromEventId(eventId));
    }

    return new IdColumnAndValue("event_id", eventId);
  }

  private long extractRowIdFromEventId(String id) {
    return Int128.fromString(id).getHigh();
  }

  // (database id generation) The auto generated values must greater than any existing message IDs
  // https://github.com/Events-foundation/Events-common/issues/53
  private void assertIdSequenceUsesCurrentTimeAsStartingValue(long id) {
    final long precision = TimeUnit.HOURS.toMillis(1);

    long currentTime = System.currentTimeMillis();

    Assert.assertTrue(
        String.format(
            "Row id should start from current time in milliseconds after migration (current time: %s, id: %s)",
            currentTime, id),
        currentTime - id < precision);
  }

  protected static class IdColumnAndValue {

    private final String column;
    private final Object value;

    public IdColumnAndValue(String column, Object value) {
      this.column = column;
      this.value = value;
    }

    public String getColumn() {
      return column;
    }

    public Object getValue() {
      return value;
    }
  }
}
