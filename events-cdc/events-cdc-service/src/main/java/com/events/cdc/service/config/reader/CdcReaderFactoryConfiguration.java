package com.events.cdc.service.config.reader;

import com.events.cdc.reader.provider.CdcReaderProvider;
import com.events.cdc.service.config.reader.postgresql.wal.PostgresWalCdcReaderFactoryConfiguration;
import com.events.cdc.service.rest.CdcReaderController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({PostgresWalCdcReaderFactoryConfiguration.class})
public class CdcReaderFactoryConfiguration {

  @Bean
  public CdcReaderProvider cdcReaderProvider() {
    return new CdcReaderProvider();
  }

  @Bean
  public CdcReaderController cdcReaderController(CdcReaderProvider cdcReaderProvider) {
    return new CdcReaderController(cdcReaderProvider);
  }
}
