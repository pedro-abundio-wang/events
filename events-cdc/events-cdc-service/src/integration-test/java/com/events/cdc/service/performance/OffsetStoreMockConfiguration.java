package com.events.cdc.service.performance;

import com.events.cdc.service.config.others.TransactionLogFileOffsetStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OffsetStoreMockConfiguration {

  @Bean
  @Primary
  public TransactionLogFileOffsetStore offsetStore() {
    return new TransactionLogFileOffsetStoreMock();
  }
}
