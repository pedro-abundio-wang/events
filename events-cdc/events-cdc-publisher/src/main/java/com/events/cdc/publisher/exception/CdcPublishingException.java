package com.events.cdc.publisher.exception;

public class CdcPublishingException extends RuntimeException {

  public CdcPublishingException(String message, Exception cause) {
    super(message, cause);
  }
}
