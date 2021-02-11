package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link ArrayBuilder}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class ArrayBuilderTest extends ArrayTest {
  /** Tests building arrays only with {@link ArrayBuilder#append(Value)}. */
  @Test public void builderTestAscending() {
    for(int len = 0; len < 2_000; len++) {
      final ArrayBuilder builder = new ArrayBuilder();
      for(int i = 0; i < len; i++) builder.append(Int.get(i));
      final XQArray arr = builder.freeze();
      arr.checkInvariants();
      assertEquals(len, arr.arraySize());
      final Iterator<Value> iter = arr.iterator(0);
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
      final ArrayBuilder builder = new ArrayBuilder();
      for(int i = 0; i < len; i++) builder.prepend(Int.get(len - 1 - i));
      final XQArray arr = builder.freeze();
      arr.checkInvariants();
      assertEquals(len, arr.arraySize());
      final Iterator<Value> iter = arr.iterator(0);
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
      final ArrayBuilder builder = new ArrayBuilder();

      final int mid = len / 2;
      if(len % 2 == 0) {
        for(int i = 0; i < len; i++) {
          if(i % 2 == 0) builder.prepend(Int.get(mid - 1 - i / 2));
          else builder.append(Int.get(mid + i / 2));
        }
      } else {
        for(int i = 0; i < len; i++) {
          if(i % 2 == 0) builder.prepend(Int.get(mid - i / 2));
          else builder.append(Int.get(mid + 1 + i / 2));
        }
      }

      final XQArray arr = builder.freeze();
      arr.checkInvariants();
      assertEquals(len, arr.arraySize());
      final Iterator<Value> iter = arr.iterator(0);
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
      final ArrayBuilder builder = new ArrayBuilder();

      for(int i = 0; i < len; i++) {
        final Value value = Int.get(i);
        if(rng.nextBoolean()) {
          builder.prepend(value);
          deque.addFirst(i);
        } else {
          builder.append(value);
          deque.addLast(i);
        }
      }

      final XQArray arr = builder.freeze();
      arr.checkInvariants();
      assertEquals(len, arr.arraySize());
      final Iterator<Integer> iter1 = deque.iterator();
      final Iterator<Value> iter2 = arr.iterator(0);
      while(iter1.hasNext()) {
        assertTrue(iter2.hasNext());
        assertEquals(iter1.next().intValue(), ((Int) iter2.next()).itr());
      }
      assertFalse(iter2.hasNext());
    }
  }

  /** Tests {@link XQArray#from(Value...)}. */
  @Test public void fromArrayTest() {
    final int n = 2_000;
    for(int k = 0; k < n; k++) {
      final Value[] vals = new Value[k];
      for(int i = 0; i < k; i++) vals[i] = Int.get(i);

      final XQArray arr = XQArray.from(vals);
      assertEquals(k, arr.arraySize());
      for(int i = 0; i < k; i++) assertEquals(i, ((Int) arr.get(i)).itr());
    }
  }

  /** Test for {@link ArrayBuilder#append(XQArray)}. */
  @Test public void appendArrayTest() {
    final XQArray a = fromInts(0, 1, 2, 3, 4);
    final XQArray b = fromInts(5);

    ArrayBuilder ab = new ArrayBuilder();
    for(int i = 0; i < 39; i++) ab.append(Int.get(i));
    XQArray c = ab.freeze();
    for(int i = 0; i < 6; i++) c = c.tail();

    ab = new ArrayBuilder();
    ab.append(a);
    ab.freeze().checkInvariants();
    ab.append(b);
    ab.freeze().checkInvariants();
    ab.append(c);
    ab.freeze().checkInvariants();
  }

  /** Test for {@link ArrayBuilder#append(XQArray)}. */
  @Test public void repro() {
    final ArrayBuilder leftBuilder = new ArrayBuilder();
    for(int i = 0; i < 63; i++) leftBuilder.append(Int.get(i));
    final XQArray left = leftBuilder.freeze();

    final ArrayBuilder rightBuilder = new ArrayBuilder();
    for(int i = 0; i < 67; i++) rightBuilder.append(Int.get(i + 63));
    final XQArray right = rightBuilder.freeze();

    final ArrayBuilder resBuilder = new ArrayBuilder();
    resBuilder.append(left);
    resBuilder.append(XQArray.singleton(Int.get(999)));
    resBuilder.append(right);
    final XQArray result = resBuilder.freeze();
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
    return ab.freeze();
  }
}
