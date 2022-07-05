package com.events.core.domain.publisher;

import com.events.core.domain.common.DomainEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DomainEventPublisherTestConfiguration.class)
public class DomainEventPublisherTest {

  @Autowired private DomainEventPublisher domainEventPublisher;

  @Test
  public void shouldPublishDomainEvent() {
    String aggregateType = "com.ftgo.consumer.service.api.model.User";
    long aggregrateId = 1L;
    List<DomainEvent> domainEvents = Collections.singletonList(new DomainEvent() {});
    domainEventPublisher.publish(aggregateType, aggregrateId, domainEvents);
  }
}
