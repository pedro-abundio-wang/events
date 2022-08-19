package io.eventuate.tram.sagas.orchestration;

import com.events.common.jdbc.schema.EventsSchema;
import io.eventuate.tram.sagas.common.SagaInstanceRepositorySql;

public class SagaInstanceRepositoryJdbcDefaultSchemaTest
    extends SagaInstanceRepositoryJdbcSchemaTest {

  @Override
  protected SagaInstanceRepositorySql getSagaInstanceRepositoryJdbcSql() {
    return new SagaInstanceRepositorySql(new EventsSchema());
  }

  @Override
  protected String getExpectedPrefix() {
    return EventsSchema.DEFAULT_SCHEMA + ".";
  }
}
