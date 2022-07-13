package com.events.common.id;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class ApplicationIdGeneratorTest {

  @Test
  public void shouldGenerateId() {
    ApplicationIdGenerator idGen = new ApplicationIdGenerator();
    Int128 id = idGen.genId(null);
    assertNotNull(id);
  }

  @Test
  public void shouldGenerateMonotonicId() {
    ApplicationIdGenerator idGen = new ApplicationIdGenerator();
    Int128 before = idGen.genId(null);
    Int128 after = idGen.genId(null);
    assertTrue(before.compareTo(after) < 0);
  }

  @Test
  public void shouldGenerateLotsOfIds() throws InterruptedException {
    ApplicationIdGenerator idGen = new ApplicationIdGenerator();
    IntStream.range(1, 1000000).forEach(x -> idGen.genId(null));
    TimeUnit.SECONDS.sleep(1);
    IntStream.range(1, 1000000).forEach(x -> idGen.genId(null));
  }

}