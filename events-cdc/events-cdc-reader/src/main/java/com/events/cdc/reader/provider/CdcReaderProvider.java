package com.events.cdc.reader.provider;

import com.events.cdc.reader.leadership.CdcReaderLeadership;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CdcReaderProvider {

  private final ConcurrentMap<String, CdcReaderLeadership> clients = new ConcurrentHashMap<>();

  public void add(String name, CdcReaderLeadership cdcReaderLeadership) {
    clients.put(name.toLowerCase(), cdcReaderLeadership);
  }

  public CdcReaderLeadership get(String name) {
    return clients.get(name.toLowerCase());
  }

  public void start() {
    clients.values().forEach(CdcReaderLeadership::start);
  }

  public Collection<CdcReaderLeadership> getAll() {
    return clients.values();
  }

  public void stop() {
    clients.values().forEach(CdcReaderLeadership::stop);
  }
}
