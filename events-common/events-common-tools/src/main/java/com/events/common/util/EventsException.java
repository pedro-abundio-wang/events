package com.events.common.util;

public class EventsException extends RuntimeException {
  public EventsException(String message, Throwable t) {
    super(message, t);
  }
}
