package com.events.common.jdbc.sql.dialect;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class PostgresDialect extends AbstractEventsSqlDialect {

  public PostgresDialect() {
    super(
        Collections.singleton("org.postgresql.Driver"),
        Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("postgres", "postgresql", "pgsql", "pg"))),
        "(ROUND(EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000))");
  }
}
