package io.eventuate.tram.sagas.orchestration;

import com.events.common.jdbc.schema.EventsSchema;
import io.eventuate.tram.sagas.common.SagaInstanceRepositorySql;

public class SagaInstanceRepositoryJdbcEmptySchemaTest
    extends SagaInstanceRepositoryJdbcSchemaTest {

  @Override
  protected SagaInstanceRepositorySql getSagaInstanceRepositoryJdbcSql() {
    return new SagaInstanceRepositorySql(new EventsSchema(EventsSchema.EMPTY_SCHEMA));
  }

  @Override
  protected String getExpectedPrefix() {
    return "";
  }
}
