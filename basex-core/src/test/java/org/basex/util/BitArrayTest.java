package org.basex.util;

import static org.junit.Assert.*;

import org.junit.*;

/**
 * Tests for {@link BitArray}.
 *
 * @author BaseX Team 2005-13, BSD License
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
    a.init(new long[] {-1L, 0L}, 64);
    assertEquals("Array not trimmed correctly", 1, a.toArray().length);
  }

  /** Test method for {@link BitArray#get(int)}. */
  @Test
  public void get() {
    a.init(new long[] {-1L, 0L}, 65);
    assertTrue("Bit 63 is 0", a.get(63));
    assertFalse("Bit 64 is 1", a.get(64));
  }

  /** Test method for {@link BitArray#set(int)}. */
  @Test
  public void set() {
    a.set(128);
    assertTrue("Bit 128 is 0", a.get(128));
    a.init();
    a.set(129);
    assertTrue("Bit 129 is 0", a.get(129));
  }

  /** Test method for {@link BitArray#clear(int)}. */
  @Test
  public void clear() {
    a.init(new long[] {-1L, 0L}, 64);
    a.clear(63);
    assertFalse("Bit 63 is 1", a.get(63));
  }

  /** Test method for {@link BitArray#nextFree(int)}. */
  @Test
  public void nextFree() {
    a.init(new long[] {-1L, 0L}, 64);
    assertEquals("Incorrect next clear bit", 64, a.nextFree(0));
  }

  /** Test method for {@link BitArray#nextFree(int)}. */
  @Test
  public void nextSet() {
    a.init(new long[] {0L, -1L}, 128);
    assertEquals("Incorrect next clear bit", 64, a.nextSet(0));
    assertEquals("Incorrect next clear bit", 64, a.nextSet(64));
    assertEquals("Incorrect next clear bit", 67, a.nextSet(67));
    assertEquals("Incorrect next clear bit", -1, a.nextSet(128));
  }

  /** Test method for {@link BitArray#cardinality()}. */
  @Test
  public void cardinality() {
    a.init(new long[] { 0L }, 0);
    assertEquals(0, a.cardinality());
    a.set(1);
    assertEquals(1, a.cardinality());
    a.set(64);
    assertEquals(2, a.cardinality());
    a.set(3);
    assertEquals(3, a.cardinality());
    a.clear(64);
    assertEquals(2, a.cardinality());
    a.clear(3);
    assertEquals(1, a.cardinality());
    a.clear(1);
    assertEquals(0, a.cardinality());
  }
}
