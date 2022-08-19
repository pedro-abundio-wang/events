package io.eventuate.tram.sagas.orchestration;

import com.events.common.jdbc.schema.EventsSchema;
import io.eventuate.tram.sagas.common.SagaInstanceRepositorySql;

public class SagaInstanceRepositoryJdbcCustomSchemaTest
    extends SagaInstanceRepositoryJdbcSchemaTest {

  private String custom = "custom";

  @Override
  protected SagaInstanceRepositorySql getSagaInstanceRepositoryJdbcSql() {
    return new SagaInstanceRepositorySql(new EventsSchema(custom));
  }

  @Override
  protected String getExpectedPrefix() {
    return custom + ".";
  }
}
