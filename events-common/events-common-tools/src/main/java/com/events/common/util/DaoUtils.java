package com.events.common.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class DaoUtils {

  private static final Logger logger = LoggerFactory.getLogger(DaoUtils.class);

  private static <T> T handleConnectionLost(
      int maxAttempts,
      int intervalInMilliseconds,
      Callable<T> query,
      Runnable onInterruptedCallback,
      AtomicBoolean shouldRetryFlag) {
    int attempt = 0;

    while (shouldRetryFlag.get() || attempt == 0) {
      try {
        T result = query.call();
        if (attempt > 0) logger.info("Reconnected to database");
        return result;
      } catch (Exception e) {

        logger.error(
            String.format(
                "Could not access database %s - retrying in %s milliseconds",
                e.getMessage(), intervalInMilliseconds),
            e);

        if (attempt++ >= maxAttempts) {
          throw new RuntimeException("Max retry attempts exceeded", e);
        }

        try {
          Thread.sleep(intervalInMilliseconds);
        } catch (InterruptedException ie) {
          onInterruptedCallback.run();
          throw new RuntimeException(ie);
        }
      }
    }

    throw new ConnectionLostHandlerInterruptedException();
  }

  public static <T> T handleConnectionLost(
      int maxAttempts,
      int intervalInMilliseconds,
      Callable<T> query,
      Runnable onInterruptedCallback) {

    return handleConnectionLost(
        maxAttempts, intervalInMilliseconds, query, onInterruptedCallback, new AtomicBoolean(true));
  }
}
