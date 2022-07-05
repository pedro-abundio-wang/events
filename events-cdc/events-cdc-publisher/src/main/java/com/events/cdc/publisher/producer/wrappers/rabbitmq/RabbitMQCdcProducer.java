package com.events.cdc.publisher.producer.wrappers.rabbitmq;

import com.events.cdc.publisher.producer.CdcProducer;
import com.events.messaging.rabbitmq.producer.RabbitMQMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class RabbitMQCdcProducer implements CdcProducer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final RabbitMQMessageProducer rabbitMQMessageProducer;

  public RabbitMQCdcProducer(RabbitMQMessageProducer rabbitMQMessageProducer) {
    this.rabbitMQMessageProducer = rabbitMQMessageProducer;
  }

  @Override
  public CompletableFuture<?> send(String channel, String key, String message) {
    return rabbitMQMessageProducer.send(channel, key, message);
  }

  @Override
  public void close() {
    logger.info("closing EventsRabbitMQCdcProducerWrapper");
    rabbitMQMessageProducer.close();
    logger.info("closed EventsRabbitMQCdcProducerWrapper");
  }
}
