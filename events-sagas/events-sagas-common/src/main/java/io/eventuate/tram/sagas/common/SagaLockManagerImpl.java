package io.eventuate.tram.sagas.common;

import com.events.common.jdbc.exception.EventsDuplicateKeyException;
import com.events.common.jdbc.executor.EventsJdbcStatementExecutor;
import com.events.common.jdbc.schema.EventsSchema;
import com.events.common.json.mapper.JsonMapper;
import com.events.core.messaging.message.Message;
import com.events.core.messaging.message.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SagaLockManagerImpl implements SagaLockManager {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final EventsJdbcStatementExecutor eventsJdbcStatementExecutor;

  private final SagaLockManagerSql sagaLockManagerSql;

  public SagaLockManagerImpl(
      EventsJdbcStatementExecutor eventsJdbcStatementExecutor, EventsSchema eventsSchema) {
    this.eventsJdbcStatementExecutor = eventsJdbcStatementExecutor;
    sagaLockManagerSql = new SagaLockManagerSql(eventsSchema);
  }

  @Override
  public boolean claimLock(String sagaType, String sagaId, String target) {
    while (true)
      try {
        eventsJdbcStatementExecutor.update(
            sagaLockManagerSql.getInsertIntoSagaLockTableSql(), target, sagaType, sagaId);
        logger.debug("Saga {} {} has locked {}", sagaType, sagaId, target);
        return true;
      } catch (EventsDuplicateKeyException e) {
        Optional<String> owningSagaId = selectForUpdate(target);
        if (owningSagaId.isPresent()) {
          if (owningSagaId.get().equals(sagaId)) return true;
          else {
            logger.debug(
                "Saga {} {} is blocked by {} which has locked {}",
                sagaType,
                sagaId,
                owningSagaId,
                target);
            return false;
          }
        }
        logger.debug("{}  is repeating attempt to lock {}", sagaId, target);
      }
  }

  private Optional<String> selectForUpdate(String target) {
    return eventsJdbcStatementExecutor
        .query(
            sagaLockManagerSql.getSelectFromSagaLockTableSql(),
            (rs, rowNum) -> rs.getString("saga_id"),
            target)
        .stream()
        .findFirst();
  }

  @Override
  public void stashMessage(String sagaType, String sagaId, String target, Message message) {

    logger.debug("Stashing message from {} for {} : {}", sagaId, target, message);

    eventsJdbcStatementExecutor.update(
        sagaLockManagerSql.getInsertIntoSagaStashTableSql(),
        message.getRequiredHeader(Message.ID),
        target,
        sagaType,
        sagaId,
        JsonMapper.toJson(message.getHeaders()),
        message.getPayload());
  }

  @Override
  public Optional<Message> unlock(String sagaId, String target) {
    Optional<String> owningSagaId = selectForUpdate(target);

    if (!owningSagaId.isPresent()) {
      throw new RuntimeException("owningSagaId is not present");
    }

    if (!owningSagaId.get().equals(sagaId)) {
      throw new RuntimeException(
          String.format("Expected owner to be %s but is %s", sagaId, owningSagaId.get()));
    }

    logger.debug("Saga {} has unlocked {}", sagaId, target);

    List<StashedMessage> stashedMessages =
        eventsJdbcStatementExecutor.query(
            sagaLockManagerSql.getSelectFromSagaStashTableSql(),
            (rs, rowNum) -> {
              return new StashedMessage(
                  rs.getString("saga_type"),
                  rs.getString("saga_id"),
                  MessageBuilder.createInstance()
                      .withPayload(rs.getString("message_payload"))
                      .withExtraHeaders(
                          "", JsonMapper.fromJson(rs.getString("message_headers"), Map.class))
                      .build());
            },
            target);

    if (stashedMessages.isEmpty()) {
      assertEqualToOne(
          eventsJdbcStatementExecutor.update(
              sagaLockManagerSql.getDeleteFromSagaLockTableSql(), target));
      return Optional.empty();
    }

    StashedMessage stashedMessage = stashedMessages.get(0);

    logger.debug("unstashed from {}  for {} : {}", sagaId, target, stashedMessage.getMessage());

    assertEqualToOne(
        eventsJdbcStatementExecutor.update(
            sagaLockManagerSql.getUpdateSagaLockTableSql(),
            stashedMessage.getSagaType(),
            stashedMessage.getSagaId(),
            target));
    assertEqualToOne(
        eventsJdbcStatementExecutor.update(
            sagaLockManagerSql.getDeleteFromSagaStashTableSql(),
            stashedMessage.getMessage().getId()));

    return Optional.of(stashedMessage.getMessage());
  }

  private void assertEqualToOne(int n) {
    if (n != 1) throw new RuntimeException("Expected to update one row but updated: " + n);
  }
}
