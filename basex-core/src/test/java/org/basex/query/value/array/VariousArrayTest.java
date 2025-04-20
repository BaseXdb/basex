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
   * Test for {@link XQArray#insertMember(long, Value, org.basex.query.QueryContext)}.
   */
  @Test public void consSnocTest() {
    final int n = 200_000;
    XQArray array = XQArray.empty();
    for(int i = 0; i < n; i++) {
      final Int val = Int.get(i);
      array = array.insertMember(0, val, qc).appendMember(val, qc);
    }

    assertEquals(2 * n, array.structSize());
    for(long i = 0; i < 2 * n; i++) {
      final long diff = i - n, j = diff < 0 ? -(diff + 1) : diff;
      assertEquals(j, ((Int) array.memberAt(i)).itr());
    }
  }

  /**
   * Test an {@link XQArray} used as a FIFO queue.
   */
  @Test public void queueTest() {
    final int n = 2_000_000, k = n / 100;
    XQArray array = XQArray.empty();
    for(int i = 0; i < k; i++) array = array.insertMember(0, Int.get(i), qc);

    for(int i = k; i < n; i++) {
      assertEquals(k, array.structSize());
      assertEquals(i - k, ((Int) array.memberAt(array.structSize() - 1)).itr());
      array = array.subArray(0, array.structSize() - 1, qc);
      array = array.insertMember(0, Int.get(i), qc);
    }

    assertEquals(k, array.structSize());
    for(int i = 0; i < k; i++) {
      assertEquals(n - k + i, ((Int) array.memberAt(array.structSize() - 1)).itr());
      array = array.subArray(0, array.structSize() - 1, qc);
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
      array = array.insertMember(0, val, qc).insertMember(0, val, qc);
      assertEquals(i, ((Int) array.memberAt(0)).itr());
      array = array.subArray(1, array.structSize() - 1, qc);
    }

    assertEquals(n, array.structSize());

    for(int i = n; --i >= 0;) {
      assertEquals(i, ((Int) array.memberAt(0)).itr());
      array = array.subArray(1, array.structSize() - 1, qc);
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
      array = array.insertMember(0, val, qc).appendMember(val, qc);
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
}
