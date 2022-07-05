package com.events.core.commands.publisher;

import com.events.core.commands.common.Command;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CommandPublisherTestConfiguration.class)
public class CommandPublisherTest {

  @Autowired private CommandPublisher commandPublisher;

  @Test
  public void shouldPublishCommand() {
    String channel = "userService";
    String replyTo = "userService-replyTo";
    commandPublisher.publish(channel, null, new Command() {}, replyTo, Collections.emptyMap());
  }
}
