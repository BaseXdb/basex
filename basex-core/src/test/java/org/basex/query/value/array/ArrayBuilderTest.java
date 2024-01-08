package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link ArrayBuilder}.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class ArrayBuilderTest extends ArrayTest {
  /** Tests building arrays only with {@link ArrayBuilder#append(Value)}. */
  @Test public void builderTestAscending() {
    for(int len = 0; len < 2_000; len++) {
      final ArrayBuilder ab = new ArrayBuilder();
      for(int i = 0; i < len; i++) ab.append(Int.get(i));
      final XQArray array = ab.array();
      array.checkInvariants();
      assertEquals(len, array.arraySize());
      final Iterator<Value> iter = array.iterator(0);
      for(int i = 0; i < len; i++) {
        assertTrue(iter.hasNext());
        assertEquals(i, ((Int) iter.next()).itr());
      }
      assertFalse(iter.hasNext());
    }
  }

  /** Tests building arrays only with {@link ArrayBuilder#prepend(Value)}. */
  @Test public void builderTestDescending() {
    for(int len = 0; len < 2_000; len++) {
      final ArrayBuilder ab = new ArrayBuilder();
      for(int i = 0; i < len; i++) ab.prepend(Int.get(len - 1 - i));
      final XQArray array = ab.array();
      array.checkInvariants();
      assertEquals(len, array.arraySize());
      final Iterator<Value> iter = array.iterator(0);
      for(int i = 0; i < len; i++) {
        assertTrue(iter.hasNext());
        assertEquals(i, ((Int) iter.next()).itr());
      }
      assertFalse(iter.hasNext());
    }
  }

  /**
   * Tests building arrays only with {@link ArrayBuilder#prepend(Value)} and
   * {@link ArrayBuilder#append(Value)} in alternating order.
   */
  @Test public void builderTestAlternating() {
    for(int len = 0; len < 2_000; len++) {
      final ArrayBuilder ab = new ArrayBuilder();

      final int mid = len / 2;
      if(len % 2 == 0) {
        for(int i = 0; i < len; i++) {
          if(i % 2 == 0) ab.prepend(Int.get(mid - 1 - i / 2));
          else ab.append(Int.get(mid + i / 2));
        }
      } else {
        for(int i = 0; i < len; i++) {
          if(i % 2 == 0) ab.prepend(Int.get(mid - i / 2));
          else ab.append(Int.get(mid + 1 + i / 2));
        }
      }

      final XQArray array = ab.array();
      array.checkInvariants();
      assertEquals(len, array.arraySize());
      final Iterator<Value> iter = array.iterator(0);
      for(int i = 0; i < len; i++) {
        assertTrue(iter.hasNext());
        assertEquals(i, ((Int) iter.next()).itr());
      }
      assertFalse(iter.hasNext());
    }
  }

  /**
   * Tests building arrays only with {@link ArrayBuilder#prepend(Value)} and
   * {@link ArrayBuilder#append(Value)} in random order.
   */
  @Test public void builderTestRandom() {
    final Random rng = new Random(42);
    final ArrayDeque<Integer> deque = new ArrayDeque<>();
    for(int len = 0; len < 2_000; len++) {
      deque.clear();
      final ArrayBuilder ab = new ArrayBuilder();

      for(int i = 0; i < len; i++) {
        final Value value = Int.get(i);
        if(rng.nextBoolean()) {
          ab.prepend(value);
          deque.addFirst(i);
        } else {
          ab.append(value);
          deque.addLast(i);
        }
      }

      final XQArray array = ab.array();
      array.checkInvariants();
      assertEquals(len, array.arraySize());
      final Iterator<Integer> iter1 = deque.iterator();
      final Iterator<Value> iter2 = array.iterator(0);
      while(iter1.hasNext()) {
        assertTrue(iter2.hasNext());
        assertEquals(iter1.next().intValue(), ((Int) iter2.next()).itr());
      }
      assertFalse(iter2.hasNext());
    }
  }

  /** Test for {@link ArrayBuilder#append(XQArray)}. */
  @Test public void appendArrayTest() {
    final XQArray array1 = fromInts(0, 1, 2, 3, 4), array2 = fromInts(5);
    final ArrayBuilder ab = new ArrayBuilder();
    for(int i = 0; i < 39; i++) ab.append(Int.get(i));
    XQArray array3 = ab.array();
    for(int i = 0; i < 6; i++) array3 = array3.tail();

    new ArrayBuilder().append(array1).array().checkInvariants();
    new ArrayBuilder().append(array2).array().checkInvariants();
    new ArrayBuilder().append(array3).array().checkInvariants();
  }

  /** Test for {@link ArrayBuilder#append(XQArray)}. */
  @Test public void appendArray2Test() {
    final ArrayBuilder lb = new ArrayBuilder();
    for(int i = 0; i < 63; i++) lb.append(Int.get(i));
    final XQArray left = lb.array();

    final ArrayBuilder rb = new ArrayBuilder();
    for(int i = 0; i < 67; i++) rb.append(Int.get(i + 63));
    final XQArray right = rb.array();

    final XQArray result = new ArrayBuilder().append(left).
        append(XQArray.member(Int.get(999))).append(right).array();
    result.checkInvariants();

    assertEquals(left.arraySize() + 1 + right.arraySize(), result.arraySize());
  }

  /**
   * Creates an array containing {@link Int}s with the given values.
   * @param xs values
   * @return resulting array
   */
  private static XQArray fromInts(final int... xs) {
    final ArrayBuilder ab = new ArrayBuilder();
    for(final int x : xs) ab.append(Int.get(x));
    return ab.array();
  }
}
