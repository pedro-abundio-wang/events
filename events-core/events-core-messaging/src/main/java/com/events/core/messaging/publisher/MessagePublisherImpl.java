package com.events.core.messaging.publisher;

import com.events.common.util.HttpDateHeaderFormatUtil;
import com.events.core.messaging.channel.MessageChannelMapping;
import com.events.core.messaging.message.Message;
import com.events.core.messaging.interceptor.MessageInterceptor;
import com.events.core.messaging.producer.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class MessagePublisherImpl implements MessagePublisher {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final MessageInterceptor[] messageInterceptors;

  private final MessageChannelMapping messageChannelMapping;

  private final MessageProducer messageProducer;

  public MessagePublisherImpl(
      MessageInterceptor[] messageInterceptors,
      MessageChannelMapping messageChannelMapping,
      MessageProducer messageProducer) {
    this.messageInterceptors = messageInterceptors;
    this.messageChannelMapping = messageChannelMapping;
    this.messageProducer = messageProducer;
  }

  @Override
  public void publish(String destination, Message message) {
    prepareMessageHeaders(destination, message);
    // TODO is withContext a design pattern??!
    // TODO is withContext will be multi-thread??!
    messageProducer.withContext(() -> publish(message));
  }

  private void publish(Message message) {
    preSend(message);
    try {
      messageProducer.send(message);
      postSend(message, null);
    } catch (RuntimeException e) {
      logger.error("Sending failed", e);
      postSend(message, e);
      throw e;
    }
  }

  private void preSend(Message message) {
    Arrays.stream(messageInterceptors).forEach(interceptor -> interceptor.preSend(message));
  }

  private void postSend(Message message, RuntimeException e) {
    Arrays.stream(messageInterceptors).forEach(interceptor -> interceptor.postSend(message, e));
  }

  private void prepareMessageHeaders(String destination, Message message) {
    // There is 3 message channel type:
    // domain-event-channel, command-channel, command-reply-channel
    // destination to channel mapping
    message.getHeaders().put(Message.CHANNEL, messageChannelMapping.transform(destination));
    // GMT-Zero-ZonedDateTime
    message.getHeaders().put(Message.DATE, HttpDateHeaderFormatUtil.nowAsHttpDateString());
  }
}
