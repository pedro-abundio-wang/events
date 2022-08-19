package com.events.common.jdbc.exception;

import java.sql.SQLException;

public class EventsSqlException extends RuntimeException {

  public EventsSqlException(String message) {
    super(message);
  }

  public EventsSqlException(SQLException e) {
    super(e);
  }

  public EventsSqlException(Exception e) {
    super(e);
  }
}
