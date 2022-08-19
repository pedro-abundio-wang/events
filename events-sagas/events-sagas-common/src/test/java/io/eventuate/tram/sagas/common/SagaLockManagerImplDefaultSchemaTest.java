package io.eventuate.tram.sagas.common;

import com.events.common.jdbc.schema.EventsSchema;

public class SagaLockManagerImplDefaultSchemaTest extends SagaLockManagerImplSchemaTest {

  @Override
  protected SagaLockManagerSql getSagaLockManagerSql() {
    return new SagaLockManagerSql(new EventsSchema());
  }

  @Override
  protected String getExpectedInsertIntoSagaLockTable() {
    return String.format(
        "INSERT INTO %s.saga_lock_table(target, saga_type, saga_id) VALUES(?, ?,?)",
        EventsSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedInsertIntoSagaStashTable() {
    return String.format(
        "INSERT INTO %s.saga_stash_table(message_id, target, saga_type, saga_id, message_headers, message_payload) VALUES(?, ?, ?, ?, ?, ?)",
        EventsSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedSelectFromSagaLockTable() {
    return String.format(
        "select saga_id from %s.saga_lock_table WHERE target = ? FOR UPDATE",
        EventsSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedSelectFromSagaStashTable() {
    return String.format(
        "select message_id, target, saga_type, saga_id, message_headers, message_payload from %s.saga_stash_table WHERE target = ? ORDER BY message_id LIMIT 1",
        EventsSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedUpdateSagaLockTable() {
    return String.format(
        "update %s.saga_lock_table set saga_type = ?, saga_id = ? where target = ?",
        EventsSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedDeleteFromSagaLockTable() {
    return String.format(
        "delete from %s.saga_lock_table where target = ?", EventsSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedDeleteFromSagaStashTable() {
    return String.format(
        "delete from %s.saga_stash_table where message_id = ?", EventsSchema.DEFAULT_SCHEMA);
  }
}
