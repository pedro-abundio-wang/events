package com.events.cdc.publisher.producer.wrappers.activemq;

import com.events.cdc.publisher.producer.CdcProducer;
import com.events.messaging.activemq.producer.ActiveMQMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class ActiveMQCdcProducer implements CdcProducer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final ActiveMQMessageProducer activeMQMessageProducer;

  public ActiveMQCdcProducer(ActiveMQMessageProducer activeMQMessageProducer) {
    this.activeMQMessageProducer = activeMQMessageProducer;
  }

  @Override
  public CompletableFuture<?> send(String channel, String key, String message) {
    return activeMQMessageProducer.send(channel, key, message);
  }

  @Override
  public void close() {
    logger.info("closing EventsActiveMQCdcProducerWrapper");
    activeMQMessageProducer.close();
    logger.info("closed EventsActiveMQCdcProducerWrapper");
  }
}
