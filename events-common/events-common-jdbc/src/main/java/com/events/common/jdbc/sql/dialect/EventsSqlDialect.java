package com.events.common.jdbc.sql.dialect;

public interface EventsSqlDialect extends EventsSqlDialectOrder {

  boolean supports(String driver);

  boolean accepts(String name);

  String getCurrentTimeInMillisecondsExpression();
}
