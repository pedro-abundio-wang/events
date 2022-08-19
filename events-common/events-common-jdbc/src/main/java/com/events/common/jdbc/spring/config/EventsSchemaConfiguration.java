package com.events.common.jdbc.spring.config;

import com.events.common.jdbc.schema.EventsSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventsSchemaConfiguration {

  @Bean
  public EventsSchema eventsSchema(
      @Value("${events.database.schema:#{null}}") String eventsDatabaseSchema) {
    return new EventsSchema(eventsDatabaseSchema);
  }
}
