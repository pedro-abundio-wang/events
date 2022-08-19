package com.events.core.messaging.consumer.kafka;

import com.events.common.json.mapper.JsonMapper;
import com.events.core.messaging.message.MessageImpl;
import com.events.core.messaging.consumer.MessageConsumer;
import com.events.core.messaging.subscriber.MessageHandler;
import com.events.core.messaging.subscriber.MessageSubscription;
import com.events.messaging.kafka.consumer.KafkaSubscription;
import com.events.messaging.kafka.consumer.KafkaMessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class KafkaMessageConsumerWrapper implements MessageConsumer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final KafkaMessageConsumer messageConsumerKafka;

  public KafkaMessageConsumerWrapper(KafkaMessageConsumer messageConsumerKafka) {
    this.messageConsumerKafka = messageConsumerKafka;
  }

  @Override
  public MessageSubscription subscribe(
      String subscriberId, Set<String> channels, MessageHandler handler) {
    logger.info("Subscribing: subscriberId = {}, channels = {}", subscriberId, channels);

    KafkaSubscription subscription =
        messageConsumerKafka.subscribe(
            subscriberId,
            channels,
            message ->
                handler.accept(JsonMapper.fromJson(message.getPayload(), MessageImpl.class)));

    logger.info("Subscribed: subscriberId = {}, channels = {}", subscriberId, channels);

    return subscription::close;
  }

  @Override
  public String getId() {
    return messageConsumerKafka.getId();
  }

  @Override
  public void close() {
    logger.info("Closing consumer");

    messageConsumerKafka.close();

    logger.info("Closed consumer");
  }
}
