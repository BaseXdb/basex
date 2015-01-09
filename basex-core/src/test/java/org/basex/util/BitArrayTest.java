package org.basex.util;

import static org.junit.Assert.*;

import org.junit.*;

/**
 * Tests for {@link BitArray}.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Dimitar Popov
 */
public final class BitArrayTest {
  /** Test method for {@link BitArray#toArray()}. */
  @Test
  public void getTrimmedWords() {
    final BitArray ba = new BitArray();
    ba.init(new long[] {-1L, 0L}, 64);
    assertEquals("Array not trimmed correctly", 1, ba.toArray().length);
  }

  /** Test method for {@link BitArray#get(int)}. */
  @Test
  public void get() {
    final BitArray ba = new BitArray();
    ba.init(new long[] {-1L, 0L}, 65);
    assertTrue("Bit 63 is 0", ba.get(63));
    assertFalse("Bit 64 is 1", ba.get(64));
  }

  /** Test method for {@link BitArray#set(int)}. */
  @Test
  public void set() {
    final BitArray ba = new BitArray();
    ba.set(128);
    assertTrue("Bit 128 is 0", ba.get(128));
    ba.init();
    ba.set(129);
    assertTrue("Bit 129 is 0", ba.get(129));
  }

  /** Test method for {@link BitArray#clear(int)}. */
  @Test
  public void clear() {
    final BitArray ba = new BitArray();
    ba.init(new long[] {-1L, 0L}, 64);
    ba.clear(63);
    assertFalse("Bit 63 is 1", ba.get(63));
  }

  /** Test method for {@link BitArray#nextFree(int)}. */
  @Test
  public void nextFree() {
    final BitArray ba = new BitArray();
    ba.init(new long[] {-1L, 0L}, 64);
    assertEquals("Incorrect next clear bit", 64, ba.nextFree(0));
  }

  /** Test method for {@link BitArray#nextFree(int)}. */
  @Test
  public void nextSet() {
    final BitArray ba = new BitArray();
    ba.init(new long[] {0L, -1L}, 128);
    assertEquals("Incorrect next clear bit", 64, ba.nextSet(0));
    assertEquals("Incorrect next clear bit", 64, ba.nextSet(64));
    assertEquals("Incorrect next clear bit", 67, ba.nextSet(67));
    assertEquals("Incorrect next clear bit", -1, ba.nextSet(128));
  }

  /** Test method for {@link BitArray#cardinality()}. */
  @Test
  public void cardinality() {
    final BitArray ba = new BitArray();
    ba.init(new long[] { 0L }, 0);
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
  @Test
  public void preFill() {
    // map with zero entries
    BitArray ba = new BitArray(0, true);
    assertEquals("Incorrect value", false, ba.get(0));
    // map with one entry
    ba = new BitArray(1, true);
    assertEquals("Incorrect value", true, ba.get(0));
    assertEquals("Incorrect value", false, ba.get(1));
    // map with 63 entries
    int max = BitArray.WORD_SIZE - 1;
    ba = new BitArray(max, true);
    for(int i = 0; i < max; i++) assertEquals("Incorrect value", true, ba.get(i));
    assertEquals("Incorrect value", false, ba.get(max));
    // map with 64 entries
    max = BitArray.WORD_SIZE;
    ba = new BitArray(max, true);
    for(int i = 0; i < max; i++) assertEquals("Incorrect value", true, ba.get(i));
    assertEquals("Incorrect value", false, ba.get(max));
    // map with 65 entries
    max = BitArray.WORD_SIZE + 1;
    ba = new BitArray(max, true);
    for(int i = 0; i < max; i++) assertEquals("Incorrect value", true, ba.get(i));
    assertEquals("Incorrect value", false, ba.get(max));
    // map with 1025 entries
    max = (BitArray.WORD_SIZE << 4) + 1;
    ba = new BitArray(max, true);
    for(int i = 0; i < max; i++) assertEquals("Incorrect value", true, ba.get(i));
    assertEquals("Incorrect value", false, ba.get(max));
  }
}
