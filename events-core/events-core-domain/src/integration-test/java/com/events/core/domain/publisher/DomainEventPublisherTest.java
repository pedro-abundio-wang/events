package com.events.core.domain.publisher;

import com.events.core.domain.common.DomainEvent;
import com.events.core.domain.spring.config.DomainEventPublisherConfiguration;
import com.events.core.messaging.config.MessagePublisherConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DomainEventPublisherTest.TestConfiguration.class)
public class DomainEventPublisherTest {

  @Autowired private DomainEventPublisher domainEventPublisher;

  @Test
  public void shouldPublishDomainEvent() {
    String aggregateType = "com.ftgo.consumer.service.api.model.User";
    long aggregrateId = 1L;
    List<DomainEvent> domainEvents = Collections.singletonList(new DomainEvent() {});
    domainEventPublisher.publish(aggregateType, aggregrateId, domainEvents);
  }

  @Configuration
  @EnableAutoConfiguration
  @Import({DomainEventPublisherConfiguration.class, MessagePublisherConfiguration.class})
  public static class TestConfiguration {}
}
