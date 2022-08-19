package com.events.cdc.service.config.cleaner;

import com.events.cdc.reader.properties.CdcReaderProperties;
import org.springframework.beans.factory.annotation.Value;

public class CdcReaderPropertiesConfiguration {

  @Value("${spring.profiles.active:#{\"\"}}")
  private String springProfilesActive;

  @Value("${spring.datasource.url:#{null}}")
  private String dataSourceURL;

  @Value("${spring.datasource.username:#{null}}")
  private String dataSourceUserName;

  @Value("${spring.datasource.password:#{null}}")
  private String dataSourcePassword;

  @Value("${spring.datasource.driver.class.name:#{null}}")
  private String dataSourceDriverClassName;

  // @Autowired protected EventsCdcProperties eventsCdcProperties;

  protected void initCdcReaderProperties(CdcReaderProperties cdcReaderProperties) {
    //    cdcReaderProperties.setDataSourceUrl(dataSourceURL);
    //    cdcReaderProperties.setDataSourceUserName(dataSourceUserName);
    //    cdcReaderProperties.setDataSourcePassword(dataSourcePassword);
    //    cdcReaderProperties.setDataSourceDriverClassName(dataSourceDriverClassName);
    //    cdcReaderProperties.setLeadershipLockPath(
    //        eventsCdcProperties.getLeadershipLockPath());
    //    cdcReaderProperties.setReaderName(eventsCdcProperties.getReaderName());
    //    cdcReaderProperties.setOutboxId(eventsCdcProperties.getOutboxId());
  }
}
