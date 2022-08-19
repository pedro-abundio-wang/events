package com.events.core.messaging.producer.in.memory;

import com.events.common.id.IdGenerator;
import com.events.core.messaging.message.Message;
import com.events.core.messaging.producer.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageProducerInMemoryImpl implements MessageProducer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final IdGenerator idGenerator;

  public MessageProducerInMemoryImpl(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  @Override
  public String sendInternal(Message message) {
    // TODO In Memory Message Producer should use for unit-test
    logger.debug("send msg to memory {}", message);
    return idGenerator.genId(null).asString();
  }
}
