package com.events.common.jdbc.sql.dialect;

import java.util.Collections;

public class DefaultEventsSqlDialect extends AbstractEventsSqlDialect {

  public DefaultEventsSqlDialect(String currentTimeInMillisecondsExpression) {
    super(Collections.emptySet(), Collections.emptySet(), currentTimeInMillisecondsExpression);
  }

  @Override
  public boolean supports(String driver) {
    return true;
  }

  @Override
  public boolean accepts(String name) {
    return true;
  }
}
