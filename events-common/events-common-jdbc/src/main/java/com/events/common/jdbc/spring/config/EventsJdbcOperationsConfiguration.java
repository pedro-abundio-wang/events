package com.events.common.jdbc.spring.config;

import com.events.common.jdbc.operation.EventsJdbcOperations;
import com.events.common.jdbc.operation.EventsJdbcOperationsUtils;
import com.events.common.jdbc.executor.EventsJdbcStatementExecutor;
import com.events.common.jdbc.executor.EventsSpringJdbcStatementExecutor;
import com.events.common.jdbc.schema.EventsSchema;
import com.events.common.jdbc.sql.dialect.EventsSqlDialect;
import com.events.common.jdbc.sql.dialect.EventsSqlDialectSelector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Import({
  EventsSchemaConfiguration.class,
  EventsSqlDialectConfiguration.class,
  EventsTransactionTemplateConfiguration.class
})
public class EventsJdbcOperationsConfiguration {

  @Bean
  public EventsJdbcStatementExecutor eventsJdbcStatementExecutor(JdbcTemplate jdbcTemplate) {
    return new EventsSpringJdbcStatementExecutor(jdbcTemplate);
  }

  @Bean
  public EventsJdbcOperations eventsJdbcOperations(
      EventsJdbcStatementExecutor eventsJdbcStatementExecutor,
      EventsSqlDialectSelector eventsSqlDialectSelector,
      @Value("${spring.datasource.driver-class-name}") String driver,
      EventsSchema eventsSchema) {

    EventsSqlDialect eventsSqlDialect = eventsSqlDialectSelector.getDialect(driver);

    return new EventsJdbcOperations(
        new EventsJdbcOperationsUtils(eventsSqlDialect), eventsJdbcStatementExecutor, eventsSchema);
  }
}
