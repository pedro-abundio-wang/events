package com.events.core.messaging.config;

import com.events.core.messaging.subscriber.DuplicateMessageDetector;
import com.events.core.messaging.subscriber.NoopDuplicateMessageDetector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NoopDuplicateMessageDetectorConfiguration {

  @Bean
  public DuplicateMessageDetector duplicateMessageDetector() {
    return new NoopDuplicateMessageDetector();
  }
}
