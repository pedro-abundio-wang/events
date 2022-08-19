package com.events.common.json.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.IOException;

public class JsonMapper {

  public static ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.configure(
        com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.registerModule(new Jdk8Module().configureAbsentsAsNulls(true));
  }

  public static String toJson(Object o) {
    try {
      return objectMapper.writeValueAsString(o);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T fromJson(String json, Class<T> targetType) {
    try {
      return objectMapper.readValue(json, targetType);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T fromJsonByName(String json, String targetType) {
    try {
      return objectMapper.readValue(
          json, (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(targetType));
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
