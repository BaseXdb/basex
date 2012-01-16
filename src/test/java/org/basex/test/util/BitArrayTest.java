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
    assertTrue("Array not trimmed correctly", a.toArray().length == 1);
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
    assertTrue("Incorrect next clear bit", a.nextClearBit(0) == 64);
  }
}
