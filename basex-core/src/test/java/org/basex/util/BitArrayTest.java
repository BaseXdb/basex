package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link BitArray}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
public final class BitArrayTest {
  /** Test method for {@link BitArray#toArray()}. */
  @Test public void getTrimmedWords() {
    final BitArray ba = new BitArray(new long[] { -1L, 0L }, 64);
    assertEquals(1, ba.toArray().length, "Array not trimmed correctly");
  }

  /** Test method for {@link BitArray#get(int)}. */
  @Test public void get() {
    final BitArray ba = new BitArray(new long[] { -1L, 0L }, 65);
    assertTrue(ba.get(63), "Bit 63 is 0");
    assertFalse(ba.get(64), "Bit 64 is 1");
  }

  /** Test method for {@link BitArray#set(int)}. */
  @Test public void set() {
    BitArray ba = new BitArray();
    ba.set(128);
    assertTrue(ba.get(128), "Bit 128 is 0");
    ba = new BitArray();
    ba.set(129);
    assertTrue(ba.get(129), "Bit 129 is 0");
  }

  /** Test method for {@link BitArray#clear(int)}. */
  @Test public void clear() {
    final BitArray ba = new BitArray(new long[] { -1L, 0L }, 64);
    ba.clear(63);
    assertFalse(ba.get(63), "Bit 63 is 1");
  }

  /** Test method for {@link BitArray#nextFree()}. */
  @Test public void nextFree() {
    final BitArray ba = new BitArray(new long[] { -1L, 0L }, 64);
    assertEquals(64, ba.nextFree(), "Incorrect next clear bit");
  }

  /** Test method for {@link BitArray#nextSet(int)}. */
  @Test public void nextSet() {
    final BitArray ba = new BitArray(new long[] { 0L, -1L }, 128);
    assertEquals(64, ba.nextSet(0), "Incorrect next clear bit");
    assertEquals(64, ba.nextSet(64), "Incorrect next clear bit");
    assertEquals(67, ba.nextSet(67), "Incorrect next clear bit");
    assertEquals(-1, ba.nextSet(128), "Incorrect next clear bit");
  }

  /** Test method for {@link BitArray#cardinality()}. */
  @Test public void cardinality() {
    final BitArray ba = new BitArray(new long[] { 0L }, 0);
    assertEquals(0, ba.cardinality());
    ba.set(1);
    assertEquals(1, ba.cardinality());
    ba.set(64);
    assertEquals(2, ba.cardinality());
    ba.set(3);
    assertEquals(3, ba.cardinality());
    ba.clear(64);
    assertEquals(2, ba.cardinality());
    ba.clear(3);
    assertEquals(1, ba.cardinality());
    ba.clear(1);
    assertEquals(0, ba.cardinality());
  }

  /** Creates the constructor which fills the entries with a specified value. */
  @Test public void preFill() {
    // map with zero entries
    BitArray ba = new BitArray(0, true);
    assertFalse(ba.get(0), "Incorrect value");
    // map with one entry
    ba = new BitArray(1, true);
    assertTrue(ba.get(0), "Incorrect value");
    assertFalse(ba.get(1), "Incorrect value");
    // map with 63 entries
    int max = BitArray.WORD_SIZE - 1;
    ba = new BitArray(max, true);
    for(int i = 0; i < max; i++) assertTrue(ba.get(i), "Incorrect value");
    assertFalse(ba.get(max), "Incorrect value");
    // map with 64 entries
    max = BitArray.WORD_SIZE;
    ba = new BitArray(max, true);
    for(int i = 0; i < max; i++) assertTrue(ba.get(i), "Incorrect value");
    assertFalse(ba.get(max), "Incorrect value");
    // map with 65 entries
    max = BitArray.WORD_SIZE + 1;
    ba = new BitArray(max, true);
    for(int i = 0; i < max; i++) assertTrue(ba.get(i), "Incorrect value");
    assertFalse(ba.get(max), "Incorrect value");
    // map with 1025 entries
    max = (BitArray.WORD_SIZE << 4) + 1;
    ba = new BitArray(max, true);
    for(int i = 0; i < max; i++) assertTrue(ba.get(i), "Incorrect value");
    assertFalse(ba.get(max), "Incorrect value");
  }
}
