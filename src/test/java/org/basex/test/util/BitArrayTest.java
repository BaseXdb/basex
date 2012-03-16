package org.basex.test.util;

import static org.junit.Assert.*;
import org.basex.util.BitArray;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link BitArray}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public final class BitArrayTest {
  /** Instance of {@link BitArray}. */
  private BitArray a;

  /** Create an instance of {@link BitArray}. */
  @Before
  public void setUp() {
    a = new BitArray();
  }

  /** Test method for {@link BitArray#toArray()}. */
  @Test
  public void getTrimmedWords() {
    a.setWords(new long[] {-1L, 0L}, 64);
    assertEquals("Array not trimmed correctly", 1, a.toArray().length);
  }

  /** Test method for {@link BitArray#get(int)}. */
  @Test
  public void getInt() {
    a.setWords(new long[] {-1L, 0L}, 65);
    assertTrue("Bit 63 is 0", a.get(63));
    assertFalse("Bit 64 is 1", a.get(64));
  }

  /** Test method for {@link BitArray#set(int)}. */
  @Test
  public void setInt() {
    a.set(128);
    assertTrue("Bit 128 is 0", a.get(128));
    a.init();
    a.set(129);
    assertTrue("Bit 129 is 0", a.get(129));
  }

  /** Test method for {@link BitArray#clear(int)}. */
  @Test
  public void clearInt() {
    a.setWords(new long[] {-1L, 0L}, 64);
    a.clear(63);
    assertFalse("Bit 63 is 1", a.get(63));
  }

  /** Test method for {@link BitArray#nextClearBit(int)}. */
  @Test
  public void nextClearBitInt() {
    a.setWords(new long[] {-1L, 0L}, 64);
    assertEquals("Incorrect next clear bit", 64, a.nextClearBit(0));
  }

  /** Test method for {@link BitArray#setAll}. */
  @Test
  public void setAll() {
    a.setWords(new long[] {0L}, 32);
    a.setAll();
    assertTrue("First 32 should be true", a.get(0) && a.get(1) && a.get(31));
    assertFalse("Bit 32+ should be false", a.get(32) || a.get(33) || a.get(63));
    assertTrue("All bits should be true", a.getAll());
    a.set(33);
    assertFalse("Bit 32 should be false", a.get(32));
    assertTrue("Bit 33 should be true", a.get(33));
    assertFalse("All bits should be false", a.getAll());
    a.set(32);
    assertTrue("All bits should be true", a.getAll());
    a.init();
    a.set(31);
    a.setAll();
    a.clear(8);
    assertFalse("All bits should be false", a.getAll());
    assertTrue("Bits != 8 should be true", a.get(7) && a.get(9) && a.get(31));
    assertFalse("Bit 8 should be false", a.get(8));
  }
}
