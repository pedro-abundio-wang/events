package com.events.common.jdbc.sql.dialect;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

public class EventsSqlDialectSelector {

  private final Collection<EventsSqlDialect> eventsSqlDialects;

  public EventsSqlDialectSelector(Collection<EventsSqlDialect> eventsSqlDialects) {
    this.eventsSqlDialects = eventsSqlDialects;
  }

  public EventsSqlDialect getDialect(String driver) {
    return getDialect(eventsSqlDialect -> eventsSqlDialect.supports(driver), driver);
  }

  private EventsSqlDialect getDialect(Predicate<EventsSqlDialect> predicate, String driver) {
    return eventsSqlDialects.stream()
        .filter(predicate)
        .min(Comparator.comparingInt(EventsSqlDialectOrder::getOrder))
        .orElseThrow(
            () ->
                new IllegalStateException(
                    String.format(
                        "Sql Dialect not found (%s), "
                            + "you can specify environment variable '%s' to solve the issue",
                        driver, "EVENTS_CURRENT_TIME_IN_MILLISECONDS_SQL")));
  }
}
