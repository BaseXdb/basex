package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link TreeArrayBuilder}.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class TreeArrayBuilderTest extends ArrayTest {
  /** Tests building arrays only with {@link TreeArrayBuilder#add(Value)}. */
  @Test public void builderTestAscending() {
    for(int len = 0; len < 2_000; len++) {
      final TreeArrayBuilder ab = new TreeArrayBuilder();
      for(int i = 0; i < len; i++) ab.add(Int.get(i));
      final XQArray array = ab.array();
      assertEquals(len, array.structSize());
      final Iterator<Value> iter = array.iterator(0);
      for(int i = 0; i < len; i++) {
        assertTrue(iter.hasNext());
        assertEquals(i, ((Int) iter.next()).itr());
      }
      assertFalse(iter.hasNext());
    }
  }

  /** Tests building arrays only with {@link TreeArrayBuilder#prepend(Value)}. */
  @Test public void builderTestDescending() {
    for(int len = 0; len < 2_000; len++) {
      final TreeArrayBuilder ab = new TreeArrayBuilder();
      for(int i = 0; i < len; i++) ab.prepend(Int.get(len - 1 - i));
      final XQArray array = ab.array();
      assertEquals(len, array.structSize());
      final Iterator<Value> iter = array.iterator(0);
      for(int i = 0; i < len; i++) {
        assertTrue(iter.hasNext());
        assertEquals(i, ((Int) iter.next()).itr());
      }
      assertFalse(iter.hasNext());
    }
  }

  /**
   * Tests building arrays only with {@link TreeArrayBuilder#prepend(Value)} and
   * {@link TreeArrayBuilder#add(Value)} in alternating order.
   */
  @Test public void builderTestAlternating() {
    for(int len = 0; len < 2_000; len++) {
      final TreeArrayBuilder ab = new TreeArrayBuilder();

      final int mid = len / 2;
      if(len % 2 == 0) {
        for(int i = 0; i < len; i++) {
          if(i % 2 == 0) ab.prepend(Int.get(mid - 1 - i / 2));
          else ab.add(Int.get(mid + i / 2));
        }
      } else {
        for(int i = 0; i < len; i++) {
          if(i % 2 == 0) ab.prepend(Int.get(mid - i / 2));
          else ab.add(Int.get(mid + 1 + i / 2));
        }
      }

      final XQArray array = ab.array();
      assertEquals(len, array.structSize());
      final Iterator<Value> iter = array.iterator(0);
      for(int i = 0; i < len; i++) {
        assertTrue(iter.hasNext());
        assertEquals(i, ((Int) iter.next()).itr());
      }
      assertFalse(iter.hasNext());
    }
  }

  /**
   * Tests building arrays only with {@link TreeArrayBuilder#prepend(Value)} and
   * {@link TreeArrayBuilder#add(Value)} in random order.
   */
  @Test public void builderTestRandom() {
    final Random rng = new Random(42);
    final ArrayDeque<Integer> deque = new ArrayDeque<>();
    for(int len = 0; len < 2_000; len++) {
      deque.clear();
      final TreeArrayBuilder ab = new TreeArrayBuilder();

      for(int i = 0; i < len; i++) {
        final Value value = Int.get(i);
        if(rng.nextBoolean()) {
          ab.prepend(value);
          deque.addFirst(i);
        } else {
          ab.add(value);
          deque.addLast(i);
        }
      }

      final XQArray array = ab.array();
      assertEquals(len, array.structSize());
      final Iterator<Integer> iter1 = deque.iterator();
      final Iterator<Value> iter2 = array.iterator(0);
      while(iter1.hasNext()) {
        assertTrue(iter2.hasNext());
        assertEquals(iter1.next().intValue(), ((Int) iter2.next()).itr());
      }
      assertFalse(iter2.hasNext());
    }
  }
}
