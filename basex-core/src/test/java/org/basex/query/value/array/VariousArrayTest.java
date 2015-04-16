package org.basex.query.value.array;

import static org.junit.Assert.*;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.*;

/**
 * Tests for the {@link Array} data structure.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class VariousArrayTest {
  /**
   * Test for {@link Array#cons(Value)} and {@link Array#snoc(Value)}.
   */
  @Test
  public void consSnocTest() {
    final int n = 200_000;
    Array seq = Array.empty();
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
   * Test for {@link Array#concat(Array)}.
   */
  @Test
  public void concatTest() {
    Array seq1 = Array.empty();
    Array seq2 = Array.empty();
    final int n = 200_000;
    for(int i = 0; i < n; i++) {
      final Value val = Int.get(i);
      seq1 = seq1.cons(val);
      seq2 = seq2.snoc(val);
    }

    assertEquals(n, seq1.arraySize());
    assertEquals(n, seq2.arraySize());
    final Array seq = seq1.concat(seq2);
    assertEquals(2 * n, seq.arraySize());

    for(int i = 0; i < 2 * n; i++) {
      final int diff = i - n, j = diff < 0 ? -(diff + 1) : diff;
      assertEquals(j, ((Int) seq.get(i)).itr());
    }
  }

  /**
   * Test an {@link Array} used as a FIFO queue.
   */
  @Test
  public void queueTest() {
    final int n = 2_000_000, k = n / 100;
    Array seq = Array.empty();
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
   * Test an {@link Array} used as a LIFO stack.
   */
  @Test
  public void stackTest() {
    final int n = 2_000_000;
    Array seq = Array.empty();

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
   * Test for {@link Array#insertBefore(long, Value)}.
   */
  @Test
  public void insertTest() {
    final int n = 1_000;
    Array seq = Array.empty();

    for(int i = 0; i < n; i++) seq = seq.snoc(Int.get(i));
    assertEquals(n, seq.arraySize());

    final Int val = Int.get(n);
    for(int i = 0; i <= n; i++) {
      final Array seq2 = seq.insertBefore(i, val);
      assertEquals(n, ((Int) seq2.get(i)).itr());
      assertEquals(n + 1L, seq2.arraySize());
      for(int j = 0; j < n; j++) {
        assertEquals(j, ((Int) seq2.get(j < i ? j : j + 1)).itr());
      }
    }
  }

  /**
   * Test for {@link Array#remove(long)}.
   */
  @Test
  public void removeTest() {
    final int n = 100;
    Array seq = Array.empty();

    for(int k = 0; k < n; k++) {
      assertEquals(k, seq.arraySize());
      for(int i = 0; i < k; i++) {
        final Array seq2 = seq.remove(i);
        assertEquals(k - 1, seq2.arraySize());

        final Iterator<Value> iter = seq2.iterator(0);
        for(int j = 0; j < k - 1; j++) {
          assertTrue(iter.hasNext());
          assertEquals(j < i ? j : j + 1, ((Int) iter.next()).itr());
        }
        assertFalse(iter.hasNext());
      }
      seq = seq.snoc(Int.get(k));
    }
  }

  /**
   * Test for {@link Array#members()}.
   */
  @Test
  public void iteratorTest() {
    final int n = 1_000;
    Array seq = Array.empty();
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

  /** Tests {@link Array#tail()}. */
  @Test
  public void tailTest() {
    Array seq = Array.empty();
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
