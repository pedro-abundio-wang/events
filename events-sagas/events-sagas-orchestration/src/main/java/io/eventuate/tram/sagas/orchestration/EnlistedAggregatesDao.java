package io.eventuate.tram.sagas.orchestration;

import com.events.common.jdbc.exception.EventsDuplicateKeyException;
import com.events.common.jdbc.executor.EventsJdbcStatementExecutor;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class EnlistedAggregatesDao {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventsJdbcStatementExecutor eventsJdbcStatementExecutor;

  public EnlistedAggregatesDao(EventsJdbcStatementExecutor eventsJdbcStatementExecutor) {
    this.eventsJdbcStatementExecutor = eventsJdbcStatementExecutor;
  }

  public void save(String sagaId, Set<EnlistedAggregate> enlistedAggregates) {
    for (EnlistedAggregate ela : enlistedAggregates) {
      try {
        eventsJdbcStatementExecutor.update(
            "INSERT INTO saga_enlisted_aggregates(saga_id, aggregate_type, aggregate_id) values(?,?,?)",
            sagaId,
            ela.getAggregateClass(),
            ela.getAggregateId());
      } catch (EventsDuplicateKeyException e) {
        logger.info(
            "Cannot save aggregate, key duplicate: sagaId = {}, aggregateClass = {}, aggregateId = {}",
            sagaId,
            ela.getAggregateClass(),
            ela.getAggregateId());
        // ignore
      }
    }
  }

  public Set<EnlistedAggregate> findEnlistedAggregates(String sagaId) {
    return new HashSet<>(
        eventsJdbcStatementExecutor.query(
            "Select aggregate_type, aggregate_id from saga_enlisted_aggregates where saga_id = ?",
            (rs, rowNum) -> {
              try {
                return new EnlistedAggregate(
                    (Class<Object>) ClassUtils.getClass(rs.getString("aggregate_type")),
                    rs.getString("aggregate_id"));
              } catch (ClassNotFoundException e) {
                logger.error("Class not found", e);
                throw new RuntimeException("Class not found", e);
              }
            },
            sagaId));
  }

  public Set<String> findSagas(Class aggregateType, String aggregateId) {
    return new HashSet<>(
        eventsJdbcStatementExecutor.query(
            "Select saga_id from saga_enlisted_aggregates where aggregate_type = ? AND  aggregate_id = ?",
            (rs, rowNum) -> rs.getString("aggregate_type"),
            aggregateType,
            aggregateId));
  }
}
