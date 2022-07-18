package com.events.messaging.kafka.producer;

import com.events.common.json.mapper.JsonMapper;
import com.events.common.util.Eventually;
import com.events.messaging.kafka.config.KafkaMessageConsumerConfiguration;
import com.events.messaging.kafka.config.KafkaMessageProducerConfiguration;
import com.events.messaging.kafka.consumer.KafkaMessageConsumer;
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
@SpringBootTest(classes = KafkaTest.TestConfiguration.class)
@ActiveProfiles("kafka")
public class KafkaTest {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final String key = UUID.randomUUID().toString();
  private final String topic = "UserClicked";
  private final String message =
      JsonMapper.toJson(ImmutableMap.of("link", System.currentTimeMillis()));
  private final String subscriberId = "UserClickedSubscriberId";

  @Autowired private KafkaMessageProducer kafkaMessageProducer;
  @Autowired private KafkaMessageConsumer kafkaMessageConsumer;

  @Test
  public void shouldProduceMessage() {
    kafkaMessageProducer.send(topic, key, message);
  }

  @Test
  public void shouldConsumeMessage() {
    Eventually.run(
        () -> {
          kafkaMessageConsumer.subscribe(
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
    KafkaMessageProducerConfiguration.class,
    KafkaMessageConsumerConfiguration.class
  })
  public static class TestConfiguration {}
}
