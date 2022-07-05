package com.events.common.jdbc.sql.dialect;

import java.util.Set;

public abstract class AbstractEventsSqlDialect implements EventsSqlDialect {

  private final Set<String> drivers;

  private final Set<String> names;

  private final String currentTimeInMillisecondsExpression;

  public AbstractEventsSqlDialect(
      Set<String> drivers, Set<String> names, String currentTimeInMillisecondsExpression) {
    this.drivers = drivers;
    this.names = names;
    this.currentTimeInMillisecondsExpression = currentTimeInMillisecondsExpression;
  }

  public String getCurrentTimeInMillisecondsExpression() {
    return currentTimeInMillisecondsExpression;
  }

  @Override
  public boolean supports(String driver) {
    return drivers.contains(driver);
  }

  @Override
  public boolean accepts(String name) {
    return names.contains(name);
  }

  @Override
  public int getOrder() {
    return Integer.MIN_VALUE;
  }
}
