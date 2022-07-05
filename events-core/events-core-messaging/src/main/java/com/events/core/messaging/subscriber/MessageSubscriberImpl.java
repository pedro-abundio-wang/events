package com.events.core.messaging.subscriber;

import com.events.core.messaging.channel.MessageChannelMapping;
import com.events.core.messaging.consumer.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MessageSubscriberImpl implements MessageSubscriber {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final MessageChannelMapping messageChannelMapping;
  private final MessageConsumer messageConsumer;
  private final MessageHandlerDecoratorFactory messageHandlerDecoratorFactory;

  public MessageSubscriberImpl(
      MessageChannelMapping messageChannelMapping,
      MessageConsumer messageConsumer,
      MessageHandlerDecoratorFactory messageHandlerDecoratorFactory) {
    this.messageChannelMapping = messageChannelMapping;
    this.messageConsumer = messageConsumer;
    this.messageHandlerDecoratorFactory = messageHandlerDecoratorFactory;
  }

  @Override
  public MessageSubscription subscribe(
      String subscriberId, Set<String> channels, MessageHandler handler) {

    logger.info("Subscribing: subscriberId = {}, channels = {}", subscriberId, channels);

    Consumer<SubscriberIdAndMessage> decoratedHandler =
        messageHandlerDecoratorFactory.decorate(handler);

    MessageSubscription messageSubscription =
        messageConsumer.subscribe(
            subscriberId,
            channels.stream().map(messageChannelMapping::transform).collect(Collectors.toSet()),
            message -> decoratedHandler.accept(new SubscriberIdAndMessage(subscriberId, message)));

    logger.info("Subscribed: subscriberId = {}, channels = {}", subscriberId, channels);

    return messageSubscription;
  }

  @Override
  public String getId() {
    return messageConsumer.getId();
  }

  @Override
  public void close() {
    logger.info("Closing consumer");
    messageConsumer.close();
    logger.info("Closed consumer");
  }
}
