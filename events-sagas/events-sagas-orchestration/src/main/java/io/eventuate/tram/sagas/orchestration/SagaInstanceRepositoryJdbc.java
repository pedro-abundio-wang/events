package io.eventuate.tram.sagas.orchestration;

import com.events.common.id.IdGenerator;
import com.events.common.jdbc.exception.EventsDuplicateKeyException;
import com.events.common.jdbc.executor.EventsJdbcStatementExecutor;
import com.events.common.jdbc.schema.EventsSchema;
import io.eventuate.tram.sagas.common.SagaInstanceRepositorySql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class SagaInstanceRepositoryJdbc implements SagaInstanceRepository {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventsJdbcStatementExecutor eventsJdbcStatementExecutor;
  private IdGenerator idGenerator;

  private SagaInstanceRepositorySql sagaInstanceRepositorySql;

  public SagaInstanceRepositoryJdbc(
      EventsJdbcStatementExecutor eventsJdbcStatementExecutor,
      IdGenerator idGenerator,
      EventsSchema eventsSchema) {
    this.eventsJdbcStatementExecutor = eventsJdbcStatementExecutor;
    this.idGenerator = idGenerator;
    sagaInstanceRepositorySql = new SagaInstanceRepositorySql(eventsSchema);
  }

  @Override
  public void save(SagaInstance sagaInstance) {
    sagaInstance.setId(idGenerator.genId(null).asString());
    logger.info("Saving {} {}", sagaInstance.getSagaType(), sagaInstance.getId());
    eventsJdbcStatementExecutor.update(
        sagaInstanceRepositorySql.getInsertIntoSagaInstanceSql(),
        sagaInstance.getSagaType(),
        sagaInstance.getId(),
        sagaInstance.getStateName(),
        sagaInstance.getLastRequestId(),
        sagaInstance.getSerializedSagaData().getSagaDataType(),
        sagaInstance.getSerializedSagaData().getSagaDataJSON(),
        sagaInstance.isEndState(),
        sagaInstance.isCompensating());

    saveDestinationsAndResources(sagaInstance);
  }

  private void saveDestinationsAndResources(SagaInstance sagaInstance) {
    for (DestinationAndResource dr : sagaInstance.getDestinationsAndResources()) {
      try {
        eventsJdbcStatementExecutor.update(
            sagaInstanceRepositorySql.getInsertIntoSagaInstanceParticipantsSql(),
            sagaInstance.getSagaType(),
            sagaInstance.getId(),
            dr.getDestination(),
            dr.getResource());
      } catch (EventsDuplicateKeyException e) {
        logger.info(
            "key duplicate: sagaType = {}, sagaId = {}, destination = {}, resource = {}",
            sagaInstance.getSagaType(),
            sagaInstance.getId(),
            dr.getDestination(),
            dr.getResource());
      }
    }
  }

  @Override
  public SagaInstance find(String sagaType, String sagaId) {
    logger.info("finding {} {}", sagaType, sagaId);

    Set<DestinationAndResource> destinationsAndResources =
        new HashSet<>(
            eventsJdbcStatementExecutor.query(
                sagaInstanceRepositorySql.getSelectFromSagaInstanceParticipantsSql(),
                (rs, rownum) ->
                    new DestinationAndResource(
                        rs.getString("destination"), rs.getString("resource")),
                sagaType,
                sagaId));

    return eventsJdbcStatementExecutor
        .query(
            sagaInstanceRepositorySql.getSelectFromSagaInstanceSql(),
            (rs, rownum) ->
                new SagaInstance(
                    sagaType,
                    sagaId,
                    rs.getString("state_name"),
                    rs.getString("last_request_id"),
                    new SerializedSagaData(
                        rs.getString("saga_data_type"), rs.getString("saga_data_json")),
                    destinationsAndResources),
            sagaType,
            sagaId)
        .stream()
        .findFirst()
        .orElseThrow(
            () ->
                new RuntimeException(
                    String.format("Cannot find saga instance %s %s", sagaType, sagaId)));
  }

  @Override
  public void update(SagaInstance sagaInstance) {
    logger.info("Updating {} {}", sagaInstance.getSagaType(), sagaInstance.getId());
    int count =
        eventsJdbcStatementExecutor.update(
            sagaInstanceRepositorySql.getUpdateSagaInstanceSql(),
            sagaInstance.getStateName(),
            sagaInstance.getLastRequestId(),
            sagaInstance.getSerializedSagaData().getSagaDataType(),
            sagaInstance.getSerializedSagaData().getSagaDataJSON(),
            sagaInstance.isEndState(),
            sagaInstance.isCompensating(),
            sagaInstance.getSagaType(),
            sagaInstance.getId());

    if (count != 1) {
      throw new RuntimeException("Should be 1 : " + count);
    }

    saveDestinationsAndResources(sagaInstance);
  }
}
