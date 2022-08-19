package com.events.common.jdbc.schema;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JdbcUrlParser {

  private static final String JDBC_PATTERN =
      "jdbc:[a-zA-Z0-9]+://([^:/]+)(:[0-9]+)?/([^?]+)(\\?.*)?$";

  public static JdbcUrl parse(String dataSourceURL) {
    Pattern pattern = Pattern.compile(JDBC_PATTERN);
    Matcher matcher = pattern.matcher(dataSourceURL);
    if (!matcher.matches()) throw new RuntimeException(dataSourceURL);
    String host = matcher.group(1);
    String port = matcher.group(2);
    String database = matcher.group(3);
    return new JdbcUrl(host, port == null ? 3306 : Integer.parseInt(port.substring(1)), database);
  }
}
