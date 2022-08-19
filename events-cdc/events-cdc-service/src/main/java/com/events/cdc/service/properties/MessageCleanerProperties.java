package com.events.cdc.service.properties;

import com.events.common.util.ValidatableProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.util.Assert;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageCleanerProperties implements ValidatableProperties {
  private String dataSourceUrl;
  private String dataSourceUserName;
  private String dataSourcePassword;
  private String dataSourceDriverClassName;
  private String eventsSchema;

  private String pipeline;

  private MessagePurgeProperties purge = new MessagePurgeProperties();

  @Override
  public void validate() {
    if (pipeline == null) {
      Assert.notNull(dataSourceUrl, "dataSourceUrl must not be null if pipeline is not specified");
      Assert.notNull(dataSourceUserName, "dataSourceUserName must not be null if pipeline is not specified");
      Assert.notNull(dataSourcePassword, "dataSourcePassword must not be null if pipeline is not specified");
      Assert.notNull(dataSourceDriverClassName, "dataSourceDriverClassName must not be null if pipeline is not specified");
    }
  }


  public String getDataSourceUrl() {
    return dataSourceUrl;
  }

  public void setDataSourceUrl(String dataSourceUrl) {
    this.dataSourceUrl = dataSourceUrl;
  }

  public String getDataSourceUserName() {
    return dataSourceUserName;
  }

  public void setDataSourceUserName(String dataSourceUserName) {
    this.dataSourceUserName = dataSourceUserName;
  }

  public String getDataSourcePassword() {
    return dataSourcePassword;
  }

  public void setDataSourcePassword(String dataSourcePassword) {
    this.dataSourcePassword = dataSourcePassword;
  }

  public String getDataSourceDriverClassName() {
    return dataSourceDriverClassName;
  }

  public void setDataSourceDriverClassName(String dataSourceDriverClassName) {
    this.dataSourceDriverClassName = dataSourceDriverClassName;
  }

  public String getEventsSchema() {
    return eventsSchema;
  }

  public String getPipeline() {
    return pipeline;
  }

  public void setPipeline(String pipeline) {
    this.pipeline = pipeline;
  }

  public void setEventsSchema(String eventsSchema) {
    this.eventsSchema = eventsSchema;
  }

  public MessagePurgeProperties getPurge() {
    return purge;
  }

  public void setPurge(MessagePurgeProperties purge) {
    this.purge = purge;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this);
  }
}
