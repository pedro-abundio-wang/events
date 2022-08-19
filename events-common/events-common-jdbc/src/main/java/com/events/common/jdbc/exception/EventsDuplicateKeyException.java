package com.events.common.jdbc.exception;

import java.sql.SQLException;

public class EventsDuplicateKeyException extends EventsSqlException {

  public EventsDuplicateKeyException(SQLException sqlException) {
    super(sqlException);
  }

  public EventsDuplicateKeyException(Exception exception) {
    super(exception);
  }
}
