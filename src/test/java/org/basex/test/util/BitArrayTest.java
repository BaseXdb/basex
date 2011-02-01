package org.basex.test.util;

import static org.junit.Assert.*;
import org.basex.util.BitArray;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link BitArray}.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Dimitar Popov
 */
public class BitArrayTest {

  /** Big numeric value. */
  // private static final long BIG_NUM = Integer.MAX_VALUE;
  private static final long BIG_NUM = Integer.MAX_VALUE >>> 3L;

  /** Instance of {@ling BitArray}. */
  private BitArray a;

  /** Create an instance of {@link BitArray}. */
  @Before
  public void setUp() {
    a = new BitArray();
  }

  /** Test method for {@link BitArray#BitArray(long)}. */
  @Test
  public void testBitArrayLong() {
    final long length = BIG_NUM + 65L;
    a.init(length);
    final int wordCount = a.getWords().length;
    assertTrue("Allocate long number of words", 64L * wordCount >= length);
  }

  /** Test method for {@link BitArray#getTrimmedWords()}. */
  @Test
  public void testGetTrimmedWords() {
    a.setWords(new long[] {-1L, 0L}, 64);
    assertTrue("Array not trimmed correctly", a.getTrimmedWords().length == 1);
  }

  /** Test method for {@link BitArray#get(int)}. */
  @Test
  public void testGetInt() {
    a.setWords(new long[] {-1L, 0L}, 65);
    assertTrue("Bit 63 is 0", a.get(63));
    assertFalse("Bit 64 is 1", a.get(64));
  }

  /** Test method for {@link BitArray#set(int)}. */
  @Test
  public void testSetInt() {
    a.set(128);
    assertTrue("Bit 128 is 0", a.get(128));
    a.init();
    a.set(129);
    assertTrue("Bit 129 is 0", a.get(129));
  }

  /** Test method for {@link BitArray#set(int,int)}. */
  @Test
  public void testSetIntInt() {
    a.setWords(new long[] {0L, 0L, 0L}, 192);
    a.set(32, 160);
    assertTrue(a.getWords()[0] == -1L << 32);
    assertTrue(a.getWords()[1] == -1L);
    assertTrue(a.getWords()[2] == -1L >>> 32);
  }

  /** Test method for {@link BitArray#clear(int)}. */
  @Test
  public void testClearInt() {
    a.setWords(new long[] {-1L, 0L}, 64);
    a.clear(63);
    assertFalse("Bit 63 is 1", a.get(63));
  }

  /** Test method for {@link BitArray#clear(int,int)}. */
  @Test
  public void testClearIntInt() {
    a.setWords(new long[] {-1L, -1L, -1L}, 192);
    a.clear(32, 160);
    assertTrue(a.getWords()[0] == -1L >>> 32);
    assertTrue(a.getWords()[1] == 0L);
    assertTrue(a.getWords()[2] == -1L << 32);
  }

  /** Test method for {@link BitArray#nextSetBit(int)}. */
  @Test
  public void testNextSetBitInt() {
    a.setWords(new long[] {0L, -1L}, 128);
    assertTrue("Incorrect next set bit", a.nextSetBit(0) == 64);
  }

  /** Test method for {@link BitArray#nextClearBit(int)}. */
  @Test
  public void testNextClearBitInt() {
    a.setWords(new long[] {-1L, 0L}, 64);
    assertTrue("Incorrect next clear bit", a.nextClearBit(0) == 64);
  }

  /** Test method for {@link BitArray#nextClearBits(int, int)}. */
  @Test
  public void testNextClearBitsInt() {
    a.setWords(new long[] {-1L, 0L}, 65);
    int[] r = a.nextClearBits(0, 2);
    assertTrue("Incorrect next clear bits: expected 1 bit", r.length == 1);
    assertTrue("Incorrect next clear bits: expected bit 64", r[0] == 64);
    
    a.setWords(new long[] {-1L, 0L}, 64);
    assertTrue("Incorrect next clear bits", a.nextClearBits(0, 2).length == 0);
  }

  /** Test method for {@link BitArray#get(long)}. */
  @Test
  public void testGetLong() {
    final long i = BIG_NUM + 64L;
    a.init(i + 1L);
    a.set(i);
    assertTrue("Bit " + i + " is 0", a.get(i));
  }

  /** Test method for {@link BitArray#set(long)}. */
  @Test
  public void testSetLong() {
    final long i = BIG_NUM + 64L;
    a.init(i + 1L);
    a.set(i);
    assertTrue("Bit " + i + " is 0", a.get(i));
  }

  /** Test method for {@link BitArray#set(long,long)}. */
  @Test
  public void testSetLongLong() {
    a.setWords(new long[] {0L, 0L, 0L}, 192);
    a.set(32L, 160L);
    assertTrue(a.getWords()[0] == -1L << 32);
    assertTrue(a.getWords()[1] == -1L);
    assertTrue(a.getWords()[2] == -1L >>> 32);
  }

  /** Test method for {@link BitArray#clear(long)}. */
  @Test
  public void testClearLong() {
    final long i = BIG_NUM + 64L;
    a.init(i + 1L);
    a.set(i);
    assertTrue("Bit " + i + " is 0", a.get(i));
    a.clear(i);
    assertFalse("Bit " + i + " is 1", a.get(i));
  }

  /** Test method for {@link BitArray#clear(long,long)}. */
  @Test
  public void testClearLongLong() {
    a.setWords(new long[] {-1L, -1L, -1L}, 192);
    a.clear(32L, 160L);
    assertTrue(a.getWords()[0] == -1L >>> 32);
    assertTrue(a.getWords()[1] == 0L);
    assertTrue(a.getWords()[2] == -1L << 32);
  }

  /** Test method for {@link BitArray#nextSetBit(long)}. */
  @Test
  public void testNextSetBitLong() {
    final long i = BIG_NUM + 64L;
    a.init(i + 1L);
    a.set(i);
    assertTrue("Bit " + i + " is 0", a.get(i));
    assertTrue("Incorrect next set bit", a.nextSetBit(0L) == i);
  }

  /** Test method for {@link BitArray#nextClearBit(long)}. */
  @Test
  public void testNextClearBitLong() {
    final long i = BIG_NUM + 64L;
    a.init(i + 1L);
    a.set(i - 1L);
    assertTrue("Bit " + i + " is 0", a.get(i - 1L));
    assertTrue("Incorrect next set bit", a.nextClearBit(i - 1L) == i);
  }

  /** Test method for {@link BitArray#nextClearBits(long, int)}. */
  @Test
  public void testNextClearBitsLong() {
    a.setWords(new long[] {-1L, 0L}, 65);
    long[] r = a.nextClearBits(0L, 2);
    assertTrue("Incorrect next clear bits: expected 1 bit", r.length == 1);
    assertTrue("Incorrect next clear bits: expected bit 64", r[0] == 64);
    
    a.setWords(new long[] {-1L, 0L}, 64);
    assertTrue("Incorrect next clear bits", a.nextClearBits(0L, 2).length == 0);
  }
}
