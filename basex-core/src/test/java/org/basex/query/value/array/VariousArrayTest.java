package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the {@link XQArray} data structure.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class VariousArrayTest extends ArrayTest {
  /**
   * Test for {@link XQArray#prepend(Value)} and {@link XQArray#append(Value)}.
   */
  @Test public void consSnocTest() {
    final int n = 200_000;
    XQArray array = XQArray.empty();
    for(int i = 0; i < n; i++) {
      final Int val = Int.get(i);
      array = array.prepend(val).append(val);
    }

    assertEquals(2 * n, array.structSize());
    for(long i = 0; i < 2 * n; i++) {
      final long diff = i - n, j = diff < 0 ? -(diff + 1) : diff;
      assertEquals(j, ((Int) array.get(i)).itr());
    }
  }

  /**
   * Test an {@link XQArray} used as a FIFO queue.
   */
  @Test public void queueTest() {
    final int n = 2_000_000, k = n / 100;
    XQArray array = XQArray.empty();
    for(int i = 0; i < k; i++) array = array.prepend(Int.get(i));

    for(int i = k; i < n; i++) {
      assertEquals(k, array.structSize());
      assertEquals(i - k, ((Int) array.foot()).itr());
      array = array.trunk();
      array = array.prepend(Int.get(i));
    }

    assertEquals(k, array.structSize());
    for(int i = 0; i < k; i++) {
      assertEquals(n - k + i, ((Int) array.foot()).itr());
      array = array.trunk();
      assertEquals(k - i - 1, array.structSize());
    }

    assertSame(array, XQArray.empty());
  }

  /**
   * Test an {@link XQArray} used as a LIFO stack.
   */
  @Test public void stackTest() {
    final int n = 2_000_000;
    XQArray array = XQArray.empty();

    for(int i = 0; i < n; i++) {
      assertEquals(i, array.structSize());
      final Int val = Int.get(i);
      array = array.prepend(val).prepend(val);
      assertEquals(i, ((Int) array.head()).itr());
      array = array.tail();
    }

    assertEquals(n, array.structSize());

    for(int i = n; --i >= 0;) {
      assertEquals(i, ((Int) array.head()).itr());
      array = array.tail();
      assertEquals(i, array.structSize());
    }

    assertSame(array, XQArray.empty());
  }

  /**
   * Test for {@link XQArray#iterable()}.
   */
  @Test public void iteratorTest() {
    final int n = 1_000;
    XQArray array = XQArray.empty();
    assertFalse(array.iterator(0).hasNext());

    for(int i = 0; i < n; i++) {
      final Int val = Int.get(i);
      array = array.prepend(val).append(val);
      final int k = 2 * (i + 1);
      final Iterator<Value> iter = array.iterator(0);
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
    XQArray array = XQArray.empty();
    for(int i = 0; i < 15; i++) {
      array = array.append(Int.get(i));
    }

    assertEquals(0, ((Int) array.head()).itr());
    assertEquals(15, array.structSize());
    array = array.tail();
    assertEquals(1, ((Int) array.head()).itr());
    assertEquals(14, array.structSize());
  }
}
