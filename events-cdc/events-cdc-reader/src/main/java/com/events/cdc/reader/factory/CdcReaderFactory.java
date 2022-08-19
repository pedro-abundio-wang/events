package com.events.cdc.reader.factory;

import com.events.cdc.reader.CdcReader;
import com.events.cdc.reader.properties.CdcReaderProperties;

public interface CdcReaderFactory<
    PROPERTIES extends CdcReaderProperties, READER extends CdcReader> {

  boolean supports(String type);

  Class<PROPERTIES> propertyClass();

  READER create(PROPERTIES cdcReaderProperties);
}
