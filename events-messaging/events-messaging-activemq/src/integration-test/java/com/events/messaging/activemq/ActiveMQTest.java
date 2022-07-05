package com.events.messaging.activemq;

import com.events.common.json.mapper.JsonMapper;
import com.events.common.util.Eventually;
import com.events.messaging.activemq.config.ActiveMQConfiguration;
import com.events.messaging.activemq.config.ActiveMQMessageConsumerConfiguration;
import com.events.messaging.activemq.config.ActiveMQMessageProducerConfiguration;
import com.events.messaging.activemq.consumer.ActiveMQMessageConsumer;
import com.events.messaging.activemq.producer.ActiveMQMessageProducer;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ActiveMQTest.TestConfiguration.class)
@ActiveProfiles("activemq")
public class ActiveMQTest {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final String key = UUID.randomUUID().toString();
  private final String topic = "UserClicked";
  private final String message =
      JsonMapper.toJson(ImmutableMap.of("link", System.currentTimeMillis()));
  private final String subscriberId = "UserClickedSubscriberId";

  @Autowired private ActiveMQMessageProducer activeMQMessageProducer;
  @Autowired private ActiveMQMessageConsumer activeMQMessageConsumer;

  @Test
  public void shouldProduceMessage() {
    activeMQMessageProducer.send(topic, key, message);
  }

  @Test
  public void shouldConsumeMessage() {

    Eventually.run(
        () -> {
          activeMQMessageConsumer.subscribe(
              subscriberId,
              Collections.singleton(topic),
              (msg) -> {
                logger.info("Receive message = {} with key = {} for topic = {}", msg, key, topic);
              });
        });
  }

  @Configuration
  @EnableAutoConfiguration
  @Import({
    ActiveMQConfiguration.class,
    ActiveMQMessageProducerConfiguration.class,
    ActiveMQMessageConsumerConfiguration.class
  })
  public static class TestConfiguration {}
}
