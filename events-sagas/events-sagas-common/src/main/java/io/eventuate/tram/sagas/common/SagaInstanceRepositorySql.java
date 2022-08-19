package io.eventuate.tram.sagas.common;


import com.events.common.jdbc.schema.EventsSchema;

public class SagaInstanceRepositorySql {

  private String insertIntoSagaInstanceSql;
  private String insertIntoSagaInstanceParticipantsSql;
  private String selectFromSagaInstanceSql;
  private String selectFromSagaInstanceParticipantsSql;
  private String updateSagaInstanceSql;

  public SagaInstanceRepositorySql(EventsSchema eventsSchema) {
    String sagaInstanceTable = eventsSchema.qualifyTable("saga_instance");
    String sagaInstanceParticipantsTable = eventsSchema.qualifyTable("saga_instance_participants");

    insertIntoSagaInstanceSql = String.format("INSERT INTO %s(saga_type, saga_id, state_name, last_request_id, saga_data_type, saga_data_json, end_state, compensating) VALUES(?, ?, ?, ?, ?, ?, ?, ?)", sagaInstanceTable);
    insertIntoSagaInstanceParticipantsSql = String.format("INSERT INTO %s(saga_type, saga_id, destination, resource) values(?,?,?,?)", sagaInstanceParticipantsTable);

    selectFromSagaInstanceSql = String.format("SELECT * FROM %s WHERE saga_type = ? AND saga_id = ?", sagaInstanceTable);
    selectFromSagaInstanceParticipantsSql = String.format("SELECT destination, resource FROM %s WHERE saga_type = ? AND saga_id = ?", sagaInstanceParticipantsTable);

    updateSagaInstanceSql = String.format("UPDATE %s SET state_name = ?, last_request_id = ?, saga_data_type = ?, saga_data_json = ?, end_state = ?, compensating = ? where saga_type = ? AND saga_id = ?", sagaInstanceTable);
  }

  public String getInsertIntoSagaInstanceSql() {
    return insertIntoSagaInstanceSql;
  }

  public String getInsertIntoSagaInstanceParticipantsSql() {
    return insertIntoSagaInstanceParticipantsSql;
  }

  public String getSelectFromSagaInstanceSql() {
    return selectFromSagaInstanceSql;
  }

  public String getSelectFromSagaInstanceParticipantsSql() {
    return selectFromSagaInstanceParticipantsSql;
  }

  public String getUpdateSagaInstanceSql() {
    return updateSagaInstanceSql;
  }
}
