package com.events.common.json.mapper;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.annotation.PostConstruct;

public class JsonMapperInitializer {

  @PostConstruct
  public void initialize() {
    registerModule();
  }

  public static void registerModule() {
    JsonMapper.objectMapper.registerModule(new JavaTimeModule());
    JsonMapper.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }
}
