package com.events.messaging.kafka.consumer;

import com.events.messaging.kafka.common.BinaryMessageEncoding;
import com.events.messaging.kafka.common.KafkaMultiMessage;
import com.events.messaging.kafka.common.KafkaMultiMessageConverter;
import com.events.messaging.kafka.consumer.basic.KafkaConsumerFactory;
import com.events.messaging.kafka.consumer.basic.KafkaConsumerWrapper;
import com.events.messaging.kafka.consumer.swimlane.SwimlaneBasedDispatcher;
import com.events.messaging.kafka.properties.KafkaMessageConsumerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class KafkaMessageConsumer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final String id = UUID.randomUUID().toString();

  private final String bootstrapServers;
  private final List<KafkaConsumerWrapper> consumers = new ArrayList<>();
  private final KafkaMessageConsumerProperties kafkaMessageConsumerProperties;
  private final KafkaConsumerFactory kafkaConsumerFactory;
  private final KafkaMultiMessageConverter kafkaMultiMessageConverter =
      new KafkaMultiMessageConverter();

  public KafkaMessageConsumer(
      String bootstrapServers,
      KafkaMessageConsumerProperties kafkaMessageConsumerProperties,
      KafkaConsumerFactory kafkaConsumerFactory) {
    this.bootstrapServers = bootstrapServers;
    this.kafkaMessageConsumerProperties = kafkaMessageConsumerProperties;
    this.kafkaConsumerFactory = kafkaConsumerFactory;
  }

  public KafkaSubscription subscribe(
      String subscriberId, Set<String> channels, KafkaMessageHandler handler) {

    SwimlaneBasedDispatcher swimlaneBasedDispatcher =
        new SwimlaneBasedDispatcher(subscriberId, Executors.newCachedThreadPool());

    KafkaMessageConsumerHandler kcHandler =
        (record, callback) ->
            swimlaneBasedDispatcher.dispatch(
                new RawKafkaMessage(record.value()),
                record.partition(),
                message -> handle(message, callback, handler));

    KafkaConsumerWrapper kc =
        new KafkaConsumerWrapper(
            subscriberId,
            kcHandler,
            new ArrayList<>(channels),
            bootstrapServers,
            kafkaMessageConsumerProperties,
            kafkaConsumerFactory);

    consumers.add(kc);

    kc.start();

    return new KafkaSubscription(
        () -> {
          kc.stop();
          consumers.remove(kc);
        });
  }

  public void handle(
      RawKafkaMessage message,
      BiConsumer<Void, Throwable> callback,
      KafkaMessageHandler kafkaMessageHandler) {
    try {
      if (kafkaMultiMessageConverter.isMultiMessage(message.getPayload())) {
        kafkaMultiMessageConverter
            .convertBytesToMessages(message.getPayload())
            .getMessages()
            .stream()
            .map(KafkaMultiMessage::getValue)
            .map(KafkaMessage::new)
            .forEach(kafkaMessageHandler);
      } else {
        kafkaMessageHandler.accept(
            new KafkaMessage(BinaryMessageEncoding.bytesToString(message.getPayload())));
      }
      callback.accept(null, null);
    } catch (Throwable e) {
      callback.accept(null, e);
      throw e;
    }
  }

  public void close() {
    consumers.forEach(KafkaConsumerWrapper::stop);
  }

  public String getId() {
    return id;
  }
}
