package com.events.cdc.publisher.producer;

import java.util.concurrent.CompletableFuture;

public interface CdcProducer {
  CompletableFuture<?> send(String channel, String key, String message);
  void close();
}