package com.events.cdc.service.reader;

public class SourceTableNameSupplier {

  private final String sourceTableName;

  public SourceTableNameSupplier(String sourceTableName) {

    this.sourceTableName = sourceTableName;
  }

  public String getSourceTableName() {
    return sourceTableName;
  }
}
