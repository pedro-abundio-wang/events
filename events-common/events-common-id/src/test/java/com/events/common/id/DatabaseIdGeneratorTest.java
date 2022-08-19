package com.events.common.id;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class DatabaseIdGeneratorTest {

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowAnExceptionOnNegativeInstanceId() {
    new DatabaseIdGenerator(-1L);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowAnExceptionOnTooBigInstanceId() {
    new DatabaseIdGenerator(DatabaseIdGenerator.OUTBOX_ID_MAX_VALUE + 1);
  }

  @Test
  public void shouldGenerateAnId() {
    IdGenerator idGenerator = new DatabaseIdGenerator(DatabaseIdGenerator.OUTBOX_ID_MAX_VALUE / 2);

    Int128 id = idGenerator.genId(Long.MAX_VALUE);

    Assert.assertEquals(DatabaseIdGenerator.OUTBOX_ID_MAX_VALUE / 2, id.getLow());
    Assert.assertEquals(Long.MAX_VALUE, id.getHigh());
  }

  @Test
  public void assertThatOutboxIdMaxValueIs48BitsSize() {
    String binaryString = Long.toBinaryString(DatabaseIdGenerator.OUTBOX_ID_MAX_VALUE);

    Assert.assertEquals(48, binaryString.length());
    Assert.assertTrue(binaryString.chars().allMatch(value -> value == '1'));
  }

  @Test
  public void testIdIncrement() {
    DatabaseIdGenerator databaseIdGenerator = new DatabaseIdGenerator(0);

    // counter is 0, should become 1
    Int128 id = new Int128(0, 0);
    Assert.assertEquals(
        Optional.of(
            new Int128(0, 0b0000000000000001000000000000000000000000000000000000000000000000L)),
        databaseIdGenerator.incrementIdIfPossible(id));

    // counter is 2^16-1, id should be regenerated
    id = new Int128(0, 0b1111111111111111000000000000000000000000000000000000000000000000L);
    Assert.assertEquals(Optional.empty(), databaseIdGenerator.incrementIdIfPossible(id));

    // counter is 2^16-2, should become 2^16-1
    id = new Int128(0, 0b1111111111111110000000000000000000000000000000000000000000000000L);
    Assert.assertEquals(
        Optional.of(
            new Int128(0, 0b1111111111111111000000000000000000000000000000000000000000000000L)),
        databaseIdGenerator.incrementIdIfPossible(id));
  }
}
