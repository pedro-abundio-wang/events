package com.events.common.jdbc.spring.config;

import com.events.common.jdbc.sql.dialect.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Configuration
public class EventsSqlDialectConfiguration {

  @Bean
  public PostgresDialect postgreSqlDialect() {
    return new PostgresDialect();
  }

  public MySqlDialect mySqlDialect() {
    return new MySqlDialect();
  }

  @Bean
  public DefaultEventsSqlDialect defaultSqlDialect(
      @Value("${events.current.time.in.milliseconds.sql:#{null}}")
          String currentTimeInMillisecondsExpression) {
    return new DefaultEventsSqlDialect(currentTimeInMillisecondsExpression);
  }

  @Bean
  public EventsSqlDialectSelector eventsSqlDialectSelector(Collection<EventsSqlDialect> dialects) {
    return new EventsSqlDialectSelector(dialects);
  }
}
