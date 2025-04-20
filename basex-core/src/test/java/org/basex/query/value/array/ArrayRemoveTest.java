package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests the {@link XQArray#removeMember(long, QueryContext)} method.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class ArrayRemoveTest extends ArrayTest {
  /** Remove one element from singleton array. */
  @Test public void singletonTest() {
    final XQArray array = XQArray.get(Int.get(42));
    assertSame(XQArray.empty(), array.removeMember(0, qc));
  }

  /** Delete each element once from arrays of varying length. */
  @Test public void deleteOneTest() {
    final int n = 200;
    XQArray array1 = XQArray.empty();
    for(int k = 0; k < n; k++) {
      for(int i = 0; i < k; i++) {
        final XQArray array2 = array1.removeMember(i, qc);
        final Iterator<Value> iter2 = array2.iterator(0);
        for(int j = 0; j < k - 1; j++) {
          assertTrue(iter2.hasNext());
          assertEquals(j < i ? j : j + 1, ((Int) iter2.next()).itr());
        }
        assertFalse(iter2.hasNext());
      }
      array1 = array1.appendMember(Int.get(k), qc);
      assertEquals(k + 1, array1.structSize());
      assertEquals(k, ((Int) array1.memberAt(k)).itr());
    }
  }

  /** Delete elements so that the middle tree collapses. */
  @Test public void collapseMiddleTest() {
    final XQArray array = from(0, 1, 2, 3, 4, 5, 6, 7, 8);

    XQArray array2 = array.subArray(1, array.structSize() - 1, qc);
    array2 = array2.removeMember(4, qc);
    array2 = array2.removeMember(2, qc);
    assertContains(array2, 1, 2, 4, 6, 7, 8);

    array2 = array.insertMember(0, Int.get(-1), qc).appendMember(Int.get(9), qc);
    array2 = array2.removeMember(5, qc);
    array2 = array2.removeMember(5, qc);
    assertContains(array2, -1, 0, 1, 2, 3, 6, 7, 8, 9);

    array2 = array.insertMember(0, Int.get(-1), qc);
    array2 = array2.removeMember(5, qc);
    array2 = array2.removeMember(5, qc);
    assertContains(array2, -1, 0, 1, 2, 3, 6, 7, 8);
  }

  /** Delete elements so that the left digit is emptied. */
  @Test public void emptyLeftDigitTest() {
    XQArray array = from(0, 1, 2, 3, 4, 5, 6, 7, 8);
    array = array.removeMember(0, qc);
    array = array.removeMember(0, qc);
    array = array.removeMember(0, qc);
    array = array.removeMember(0, qc);
    assertContains(array, 4, 5, 6, 7, 8);
  }

  /** Delete elements so that the right digit is emptied. */
  @Test public void emptyRightDigitTest() {
    XQArray array = from(0, 1, 2, 3, 4, 5, 6, 7, 8);
    array = array.removeMember(8, qc);
    array = array.removeMember(7, qc);
    array = array.removeMember(6, qc);
    array = array.removeMember(5, qc);
    assertContains(array, 0, 1, 2, 3, 4);

    array = from(1, 2, 3, 4, 5, 6, 7, 8, 9).insertMember(0, Int.ZERO, qc);
    for(int i = 9; i >= 4; i--) {
      array = array.removeMember(i, qc);
    }
    assertContains(array, 0, 1, 2, 3);
  }

  /** Delete in the left digit of a deep node. */
  @Test public void deepLeftTest() {
    XQArray array = from(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    array = array.removeMember(3, qc);
    assertContains(array, 0, 1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

    array = from(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    array = array.removeMember(6, qc);
    assertContains(array, 0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15);

    array = from(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24);
    array = array.removeMember(9, qc);
    array = array.removeMember(6, qc);
    array = array.removeMember(5, qc);
    array = array.removeMember(4, qc);
    array = array.removeMember(3, qc);
    array = array.removeMember(3, qc);
    assertContains(array,
        0, 1, 2, 8, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24);

    array = from(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24);
    array = array.removeMember(6, qc);
    array = array.removeMember(5, qc);
    array = array.removeMember(4, qc);
    array = array.removeMember(3, qc);
    array = array.removeMember(3, qc);
    assertContains(array,
        0, 1, 2, 8, 9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24);

    array = from(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24);
    array = array.removeMember(17, qc);
    array = array.removeMember(16, qc);
    array = array.removeMember(15, qc);
    array = array.removeMember(6, qc);
    array = array.removeMember(5, qc);
    array = array.removeMember(4, qc);
    array = array.removeMember(3, qc);
    array = array.removeMember(3, qc);
    assertContains(array, 0, 1, 2, 8, 9, 10, 11, 12, 13, 14, 18, 19, 20, 21, 22, 23, 24);

    array = from(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
        21
    );
    for(int i = 12; i >= 4; i--) {
      array = array.removeMember(i, qc);
    }
    assertContains(array, 0, 1, 2, 3, 13, 14, 15, 16, 17, 18, 19, 20, 21);
  }

  /** Delete in the middle tree of a deep node. */
  @Test public void deepMiddleTest() {
    XQArray array = from(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26);

    for(int i = 8; i >= 6; i--) {
      array = array.removeMember(i, qc);
    }
    for(int i = 15; i >= 9; i--) {
      array = array.removeMember(i - 3, qc);
    }

    assertContains(array, 0, 1, 2, 3, 4, 5, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26);

    array = from(
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30);
    for(int i = 4; i >= 0; i--) array = array.insertMember(0, Int.get(i), qc);
    for(int i = 31; i <= 35; i++) array = array.appendMember(Int.get(i), qc);
    for(int i = 22; i >= 16; i--) array = array.removeMember(i, qc);
    assertContains(array, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
            14, 15, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35);
  }

  /** Delete in the right digit of a deep node. */
  @Test public void deepRightTest() {
    XQArray array = from(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17);
    for(int i = 12; i >= 8; i--) array = array.removeMember(i, qc);
    array = array.removeMember(8, qc);
    assertContains(array, 0, 1, 2, 3, 4, 5, 6, 7, 14, 15, 16, 17);

    array = from(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17);
    for(int i = 12; i >= 9; i--) array = array.removeMember(i, qc);
    array = array.removeMember(9, qc);
    assertContains(array, 0, 1, 2, 3, 4, 5, 6, 7, 8, 14, 15, 16, 17);
  }

  /**
   * Randomly delete elements until an array is empty.
   */
  @Test public void fuzzyTest() {
    final int n = 20_000;
    final ArrayList<Value> list = new ArrayList<>(n);
    for(int i = 0; i < n; i++) list.add(Int.get(i));

    final ArrayBuilder ab = new ArrayBuilder(qc);
    for(final Value value : list) ab.add(value);
    XQArray array = ab.array();

    final Random rng = new Random(42);
    for(int i = 0; i < n; i++) {
      final int delPos = rng.nextInt(n - i);
      list.remove(delPos);
      array = array.removeMember(delPos, qc);
      final int size = n - i - 1;
      assertEquals(size, array.structSize());
      assertEquals(size, list.size());

      if(i % 1000 == 999) {
        for(int j = 0; j < size; j++) {
          assertEquals(((Int) list.get(j)).itr(), ((Int) array.memberAt(j)).itr());
        }
      }
    }
  }

  /**
   * Simple remove test.
   */
  @Test public void removeTest() {
    final int n = 100;
    XQArray array1 = XQArray.empty();

    for(int k = 0; k < n; k++) {
      assertEquals(k, array1.structSize());
      for(int i = 0; i < k; i++) {
        final XQArray array2 = array1.removeMember(i, qc);
        assertEquals(k - 1, array2.structSize());

        final Iterator<Value> iter2 = array2.iterator(0);
        for(int j = 0; j < k - 1; j++) {
          assertTrue(iter2.hasNext());
          assertEquals(j < i ? j : j + 1, ((Int) iter2.next()).itr());
        }
        assertFalse(iter2.hasNext());
      }
      array1 = array1.appendMember(Int.get(k), qc);
    }
  }

  /**
   * Creates an array containing {@link Int} instances representing the given integers.
   * @param values values in the array
   * @return the array
   */
  private static XQArray from(final int... values) {
    final ArrayBuilder ab = new ArrayBuilder(qc);
    for(final int value : values) ab.add(Int.get(value));
    return ab.array();
  }

  /**
   * Checks that the given array contains the given integers.
   * @param array array to check the contents of
   * @param values integers to look for
   * @throws AssertionError of the check fails
   */
  private static void assertContains(final XQArray array, final int... values) {
    final Iterator<Value> iter = array.iterator(0);
    for(final int value : values) {
      assertTrue(iter.hasNext());
      assertEquals(value, ((Int) iter.next()).itr());
    }
    assertFalse(iter.hasNext());
  }
}
