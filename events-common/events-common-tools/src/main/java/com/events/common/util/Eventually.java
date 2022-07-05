package com.events.common.util;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Eventually {

  private static final int DEFAULT_INTERVAL_IN_MILLIS =
      Optional.ofNullable(System.getenv("EVENTS_TEST_UTIL_DEFAULT_INTERVAL_IN_MILLIS"))
          .map(Integer::parseInt)
          .orElse(500);

  static int DEFAULT_ITERATIONS =
      Optional.ofNullable(System.getenv("EVENTS_TEST_UTIL_DEFAULT_ITERATIONS"))
          .map(Integer::parseInt)
          .orElse(20);

  public static void run(Runnable runnable) {
    run(DEFAULT_ITERATIONS, DEFAULT_INTERVAL_IN_MILLIS, TimeUnit.MILLISECONDS, runnable);
  }

  public static void run(String message, Runnable runnable) {
    run(message, DEFAULT_ITERATIONS, DEFAULT_INTERVAL_IN_MILLIS, TimeUnit.MILLISECONDS, runnable);
  }

  public static void run(int iterations, int timeout, TimeUnit timeUnit, Runnable runnable) {
    run(null, iterations, timeout, timeUnit, runnable);
  }

  public static void run(
      String message, int iterations, int timeout, TimeUnit timeUnit, Runnable runnable) {
    runThenReturning(
        message,
        iterations,
        timeout,
        timeUnit,
        () -> {
          runnable.run();
          return null;
        });
  }

  public static <T> T runThenReturning(String message, Supplier<T> supplier) {
    return runThenReturning(
        message, DEFAULT_ITERATIONS, DEFAULT_INTERVAL_IN_MILLIS, TimeUnit.MILLISECONDS, supplier);
  }

  public static <T> T runThenReturning(Supplier<T> supplier) {
    return runThenReturning(
        null, DEFAULT_ITERATIONS, DEFAULT_INTERVAL_IN_MILLIS, TimeUnit.MILLISECONDS, supplier);
  }

  public static <T> T runThenReturning(
      int iterations, int timeout, TimeUnit timeUnit, Supplier<T> supplier) {
    return runThenReturning(null, iterations, timeout, timeUnit, supplier);
  }

  public static <T> T runThenReturning(
      String message, int iterations, int timeout, TimeUnit timeUnit, Supplier<T> supplier) {
    Throwable t = null;
    for (int i = 0; i < iterations; i++) {
      try {
        return supplier.get();
      } catch (Throwable throwable) {
        t = throwable;
        try {
          timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
    if (message == null)
      throw new EventsException(
          String.format("Failed after %s iterations every %s milliseconds", iterations, timeout),
          t);
    else
      throw new EventsException(
          String.format(
              message + " - " + "Failed after %s iterations every %s milliseconds",
              iterations,
              timeout),
          t);
  }
}
