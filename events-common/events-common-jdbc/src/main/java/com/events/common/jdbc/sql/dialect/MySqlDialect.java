package com.events.common.jdbc.sql.dialect;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class MySqlDialect extends AbstractEventsSqlDialect {

  public MySqlDialect() {
    super(
        Collections.unmodifiableSet(new HashSet<>(Arrays.asList("com.mysql.jdbc.Driver"))),
        Collections.unmodifiableSet(new HashSet<>(Arrays.asList("mysql", "mariadb"))),
        "ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000)");
  }
}
