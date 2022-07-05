package com.events.core.messaging.consumer.in.memory;

import com.events.core.messaging.consumer.MessageConsumer;
import com.events.core.messaging.message.Message;
import com.events.core.messaging.subscriber.MessageHandler;
import com.events.core.messaging.subscriber.MessageSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.util.Collections.singleton;

public class InMemoryMessageConsumerWrapper implements MessageConsumer {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private Executor executor = Executors.newCachedThreadPool();

  private ConcurrentHashMap<String, List<MessageHandler>> subscriptions = new ConcurrentHashMap<>();
  private List<MessageHandler> wildcardSubscriptions = new ArrayList<>();

  public InMemoryMessageConsumerWrapper() {}

  public void dispatchMessage(Message message) {
    String destination = message.getRequiredHeader(Message.CHANNEL);
    List<MessageHandler> handlers =
        subscriptions.getOrDefault(destination, Collections.emptyList());
    logger.info(
        "sending to channel {} that has {} subscriptions this message {} ",
        destination,
        handlers.size(),
        message);
    dispatchMessageToHandlers(destination, message, handlers);
    logger.info(
        "sending to wildcard channel {} that has {} subscriptions this message {} ",
        destination,
        wildcardSubscriptions.size(),
        message);
    dispatchMessageToHandlers(destination, message, wildcardSubscriptions);
  }

  private void dispatchMessageToHandlers(
      String destination, Message message, List<MessageHandler> handlers) {
    for (MessageHandler handler : handlers) {
      executor.execute(() -> handler.accept(message));
    }
  }

  @Override
  public MessageSubscription subscribe(
      String subscriberId, Set<String> channels, MessageHandler handler) {
    if (singleton("*").equals(channels)) {
      logger.info("subscribing {} to wildcard channels", subscriberId);
      wildcardSubscriptions.add(handler);
    } else {
      logger.info("subscribing {} to channels {}", subscriberId, channels);
      for (String channel : channels) {
        List<MessageHandler> handlers =
            subscriptions.computeIfAbsent(channel, k -> new ArrayList<>());
        handlers.add(handler);
      }
    }
    return () -> {
      logger.info("Closing in-memory consumer");
      wildcardSubscriptions.remove(handler);
      for (String channel : channels) {
        subscriptions.get(channel).remove(handler);
      }
      logger.info("Closed in-memory consumer");
    };
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public void close() {}
}
