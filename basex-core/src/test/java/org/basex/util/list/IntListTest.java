package org.basex.util.list;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link IntList}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IntListTest {
  /** Test method for {@link IntList#set(int, int)}: replaces an existing element. */
  @Test public void set() {
    final IntList list = new IntList().add(1).add(2).add(3);
    list.set(1, 9);
    assertArrayEquals(new int[] { 1, 9, 3 }, list.finish());
  }

  /** Test method for {@link IntList#set(int, int)}: appends beyond the current size. */
  @Test public void setBeyondSize() {
    final IntList list = new IntList().add(1);
    list.set(3, 9);
    assertArrayEquals(new int[] { 1, 0, 0, 9 }, list.finish());
  }

  /** Test method for {@link IntList#set(int, int)}: grows beyond the array capacity. */
  @Test public void setBeyondCapacity() {
    final IntList list = new IntList().add(1);
    list.set(1000, 9);
    assertEquals(1001, list.size());
    assertEquals(0, list.get(999));
    assertEquals(9, list.get(1000));
  }

  /** Test method for {@link IntList#set(int, int)}: a gap must not expose released values. */
  @Test public void setAfterTruncation() {
    final IntList list = new IntList().add(1).add(2).add(3);
    list.size(1);
    list.set(2, 9);
    assertArrayEquals(new int[] { 1, 0, 9 }, list.finish());
  }

  /** Test method for {@link IntList#insert(int, int...)}: shifts the remaining elements. */
  @Test public void insert() {
    final IntList list = new IntList().add(1).add(2).add(3);
    list.insert(1, 8, 9);
    assertArrayEquals(new int[] { 1, 8, 9, 2, 3 }, list.finish());
  }

  /** Test method for {@link IntList#insert(int, int...)}: grows beyond the array capacity. */
  @Test public void insertBeyondCapacity() {
    final IntList list = new IntList(2).add(1).add(2);
    list.insert(0, 8, 9);
    assertArrayEquals(new int[] { 8, 9, 1, 2 }, list.finish());
  }

  /** Test method for {@link IntList#set(int, int)}: rejects a negative index. */
  @Test public void setNegative() {
    final IntList list = new IntList();
    final Throwable th = assertThrows(ArrayIndexOutOfBoundsException.class, () -> list.set(-1, 9));
    assertEquals("Negative index: -1.", th.getMessage());
  }
}
