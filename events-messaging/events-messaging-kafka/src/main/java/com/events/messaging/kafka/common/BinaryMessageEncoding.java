package com.events.messaging.kafka.common;

import java.nio.charset.StandardCharsets;

public class BinaryMessageEncoding {

  public static String bytesToString(byte[] bytes) {
    return new String(bytes, StandardCharsets.UTF_8);
  }

  public static byte[] stringToBytes(String string) {
    return string.getBytes(StandardCharsets.UTF_8);
  }
}
