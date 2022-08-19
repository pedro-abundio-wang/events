package com.events.common.id.spring.config;

import com.events.common.id.ApplicationIdGenerator;
import com.events.common.id.DatabaseIdGenerator;
import com.events.common.id.IdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdGeneratorConfiguration {

  @Bean
  @Conditional(ApplicationIdGeneratorCondition.class)
  public IdGenerator applicationIdGenerator() {
    return new ApplicationIdGenerator();
  }

  @Bean
  @ConditionalOnProperty(name = "events.outbox.id")
  public IdGenerator databaseIdGenerator(@Value("${events.outbox.id:#{null}}") long id) {
    return new DatabaseIdGenerator(id);
  }
}
