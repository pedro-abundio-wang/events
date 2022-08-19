package com.events.cdc.service.config.cleaner;

import com.events.cdc.pipeline.CdcPipelineProperties;
import com.events.cdc.service.config.pipeline.SourceTableNameResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CdcPipelinePropertiesConfiguration {

  @Value("${events.database.schema:#{null}}")
  private String eventsDatabaseSchema;

  // @Autowired protected EventsCdcProperties eventsCdcProperties;

  @Bean
  public CdcPipelineProperties cdcPipelineProperties(
      SourceTableNameResolver sourceTableNameResolver) {
    CdcPipelineProperties cdcPipelineProperties = new CdcPipelineProperties();

    //    cdcPipelineProperties.setType("default");
    //    cdcPipelineProperties.setReader(eventsCdcProperties.getReaderName());
    //    cdcPipelineProperties.setEventsDatabaseSchema(eventsDatabaseSchema);
    //    cdcPipelineProperties.setSourceTableName(
    //        Optional.ofNullable(eventsCdcProperties.getSourceTableName())
    //            .orElse(sourceTableNameResolver.resolve("default")));

    return cdcPipelineProperties;
  }
}
