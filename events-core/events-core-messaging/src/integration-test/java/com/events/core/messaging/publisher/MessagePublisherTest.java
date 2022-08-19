package com.events.core.messaging.publisher;

import com.events.core.messaging.config.MessagePublisherConfiguration;
import com.events.core.messaging.message.MessageBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MessagePublisherTest.TestConfiguration.class)
public class MessagePublisherTest {

  private final long uniqueId = System.currentTimeMillis();
  private final String destination = "destination" + uniqueId;
  private final String payload = "{" + "\"Hello\":" + uniqueId + "}";

  @Autowired private MessagePublisher messagePublisher;

  @Test
  public void shouldPublishMessage() {
    messagePublisher.publish(
        destination, MessageBuilder.createInstance().withPayload(payload).build());
  }

  @Configuration
  @EnableAutoConfiguration
  @Import({MessagePublisherConfiguration.class})
  public static class TestConfiguration {}
}
