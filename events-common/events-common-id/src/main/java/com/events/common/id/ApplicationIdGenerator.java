package com.events.common.id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Optional;

public class ApplicationIdGenerator implements IdGenerator {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private static final long MAX_COUNTER = 1 << 16;

  private final Long macAddress;

  private long currentTimeMillis = timeNow();

  private long counter = 0;

  public ApplicationIdGenerator() {
    try {
      macAddress = getMacAddress();
      logger.debug("Mac address {}", macAddress);
      if (macAddress == null) throw new RuntimeException("Cannot find mac address");
    } catch (SocketException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean databaseIdRequired() {
    return false;
  }

  private Long getMacAddress() throws SocketException {
    String mac = System.getenv("MAC_ADDRESS");
    if (mac != null) return Long.parseLong(mac);
    Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
    while (ifaces.hasMoreElements()) {
      NetworkInterface iface = ifaces.nextElement();
      if (iface.getHardwareAddress() != null) {
        return toLong(iface.getHardwareAddress());
      }
    }
    return null;
  }

  private Long toLong(byte[] bytes) {
    long result = 0L;
    for (byte b : bytes) {
      result = (result << 8) + (b & 0xff);
    }
    return result;
  }

  private long timeNow() {
    return System.currentTimeMillis();
  }

  private Int128 makeId() {
    return new Int128(currentTimeMillis, macAddress & 0x0000ffffffffffffL | counter << 48);
  }

  public Int128 genIdInternal() {
    long now = timeNow();
    if (currentTimeMillis != now || counter == MAX_COUNTER) {
      long oldTimeMillis = this.currentTimeMillis;
      while ((this.currentTimeMillis = timeNow()) <= oldTimeMillis) {
        // Just do nothing
      }
      counter = 0;
    }
    Int128 id = makeId();
    counter = counter + 1;
    return id;
  }

  @Override
  public synchronized Int128 genId(Long baseId) {
    return genIdInternal();
  }

  @Override
  public Optional<Int128> incrementIdIfPossible(Int128 anchorId) {
    return Optional.of(genId(null));
  }
}
