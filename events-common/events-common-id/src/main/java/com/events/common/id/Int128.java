package com.events.common.id;

public class Int128 {

  private final long high;
  private final long low;

  public Int128(long high, long low) {
    this.high = high;
    this.low = low;
  }

  public String asString() {
    return String.format("%016x-%016x", high, low);
  }

  @Override
  public String toString() {
    return "Int128{" + asString() + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Int128 int128 = (Int128) o;
    return high == int128.high && low == int128.low;
  }

  @Override
  public int hashCode() {
    int result = (int) (high ^ (high >>> 32));
    result = 31 * result + (int) (low ^ (low >>> 32));
    return result;
  }

  public static Int128 fromString(String str) {
    String[] splits = str.split("-");
    if (splits.length != 2)
      throw new IllegalArgumentException("Should have length of 2, split by - : " + str);
    return new Int128(Long.parseUnsignedLong(splits[0], 16), Long.parseUnsignedLong(splits[1], 16));
  }

  public int compareTo(Int128 other) {
    int x = Long.compare(high, other.high);
    return x == 0 ? Long.compare(low, other.low) : x;
  }

  public long getHigh() {
    return high;
  }

  public long getLow() {
    return low;
  }
}
