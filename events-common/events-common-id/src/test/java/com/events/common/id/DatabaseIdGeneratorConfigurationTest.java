package com.events.common.id;

import com.events.common.id.spring.config.IdGeneratorConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = IdGeneratorConfiguration.class, properties = "events.outbox.id=1")
@RunWith(SpringJUnit4ClassRunner.class)
public class DatabaseIdGeneratorConfigurationTest {

  @Autowired private IdGenerator idGenerator;

  @Test
  public void testThatDatabaseIdGeneratorIsUsed() {
    Assert.assertEquals(DatabaseIdGenerator.class, idGenerator.getClass());
  }
}
