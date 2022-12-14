package com.events.common.jdbc.schema;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

public class SchemaAndTable {

  private final String schema;

  private final String tableName;

  public SchemaAndTable(String schema, String tableName) {
    this.schema = schema;
    this.tableName = tableName.toLowerCase();
  }

  public String getSchema() {
    return schema;
  }

  public String getTableName() {
    return tableName;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(schema, tableName);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
}
