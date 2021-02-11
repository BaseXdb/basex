package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the {@link XQArray} data structure.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class VariousArrayTest extends ArrayTest {
  /**
   * Test for {@link XQArray#cons(Value)} and {@link XQArray#snoc(Value)}.
   */
  @Test public void consSnocTest() {
    final int n = 200_000;
    XQArray seq = XQArray.empty();
    for(int i = 0; i < n; i++) {
      final Int val = Int.get(i);
      seq = seq.cons(val).snoc(val);
    }

    assertEquals(2 * n, seq.arraySize());
    for(long i = 0; i < 2 * n; i++) {
      final long diff = i - n, j = diff < 0 ? -(diff + 1) : diff;
      assertEquals(j, ((Int) seq.get(i)).itr());
    }
  }

  /**
   * Test an {@link XQArray} used as a FIFO queue.
   */
  @Test public void queueTest() {
    final int n = 2_000_000, k = n / 100;
    XQArray seq = XQArray.empty();
    for(int i = 0; i < k; i++) seq = seq.cons(Int.get(i));

    for(int i = k; i < n; i++) {
      assertEquals(k, seq.arraySize());
      assertEquals(i - k, ((Int) seq.last()).itr());
      seq = seq.init();
      seq = seq.cons(Int.get(i));
    }

    assertEquals(k, seq.arraySize());
    for(int i = 0; i < k; i++) {
      assertEquals(n - k + i, ((Int) seq.last()).itr());
      seq = seq.init();
      assertEquals(k - i - 1, seq.arraySize());
    }

    assertTrue(seq.isEmptyArray());
  }

  /**
   * Test an {@link XQArray} used as a LIFO stack.
   */
  @Test public void stackTest() {
    final int n = 2_000_000;
    XQArray seq = XQArray.empty();

    for(int i = 0; i < n; i++) {
      assertEquals(i, seq.arraySize());
      final Int val = Int.get(i);
      seq = seq.cons(val).cons(val);
      assertEquals(i, ((Int) seq.head()).itr());
      seq = seq.tail();
    }

    assertEquals(n, seq.arraySize());

    for(int i = n; --i >= 0;) {
      assertEquals(i, ((Int) seq.head()).itr());
      seq = seq.tail();
      assertEquals(i, seq.arraySize());
    }

    assertTrue(seq.isEmptyArray());
  }

  /**
   * Test for {@link XQArray#members()}.
   */
  @Test public void iteratorTest() {
    final int n = 1_000;
    XQArray seq = XQArray.empty();
    assertFalse(seq.iterator(0).hasNext());

    for(int i = 0; i < n; i++) {
      final Int val = Int.get(i);
      seq = seq.cons(val).snoc(val);
      final int k = 2 * (i + 1);
      final Iterator<Value> iter = seq.iterator(0);
      for(int j = 0; j < k; j++) {
        assertTrue(iter.hasNext());
        final Value next = iter.next();
        final int expected = j <= i ? i - j : j - (i + 1);
        assertEquals(expected, ((Int) next).itr());
      }
      assertFalse(iter.hasNext());
    }
  }

  /** Tests {@link XQArray#tail()}. */
  @Test public void tailTest() {
    XQArray seq = XQArray.empty();
    for(int i = 0; i < 15; i++) {
      seq = seq.snoc(Int.get(i));
    }

    assertEquals(0, ((Int) seq.head()).itr());
    assertEquals(15, seq.arraySize());
    seq = seq.tail();
    assertEquals(1, ((Int) seq.head()).itr());
    assertEquals(14, seq.arraySize());
  }
}
