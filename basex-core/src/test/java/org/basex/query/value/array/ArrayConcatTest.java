  package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link XQArray#concat(XQArray)}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class ArrayConcatTest extends ArrayTest {
  /** Generates and concatenates random arrays of a given size. */
  @Test public void fuzzyTest() {
    final Random rng = new Random();
    for(int n = 0; n < 1_00; n++) {
      for(int k = 0; k < 10; k++) {
        rng.setSeed(10L * n + k);
        final int l = rng.nextInt(n + 1), r = n - l;

        XQArray a1 = XQArray.empty(), b1 = XQArray.empty();
        final ArrayList<Integer> a2 = new ArrayList<>(l), b2 = new ArrayList<>(r);
        for(int i = 0; i < l; i++) {
          final int pos = rng.nextInt(i + 1);
          a1 = a1.insertBefore(pos, Int.get(i), qc);
          a2.add(pos, i);
        }

        for(int i = 0; i < r; i++) {
          final int pos = rng.nextInt(i + 1);
          b1 = b1.insertBefore(pos, Int.get(l + i), qc);
          b2.add(pos, l + i);
        }

        a1 = a1.concat(b1);
        a1.checkInvariants();
        a2.addAll(b2);
        assertEquals(n, a1.arraySize());
        assertEquals(n, a2.size());

        final Iterator<Value> iter1 = a1.iterator(0);
        final Iterator<Integer> iter2 = a2.iterator();
        while(iter1.hasNext()) {
          assertTrue(iter2.hasNext());
          final long i1 = ((Int) iter1.next()).itr(), i2 = iter2.next();
          assertEquals(i2, i1);
        }
        assertFalse(iter2.hasNext());
      }
    }
  }

  /**
   * Simple concat test.
   */
  @Test public void concatTest() {
    XQArray seq1 = XQArray.empty();
    XQArray seq2 = XQArray.empty();
    final int n = 200_000;
    for(int i = 0; i < n; i++) {
      final Value value = Int.get(i);
      seq1 = seq1.cons(value);
      seq2 = seq2.snoc(value);
    }

    assertEquals(n, seq1.arraySize());
    assertEquals(n, seq2.arraySize());
    final XQArray seq = seq1.concat(seq2);
    assertEquals(2 * n, seq.arraySize());

    for(int i = 0; i < 2 * n; i++) {
      final int diff = i - n, j = diff < 0 ? -(diff + 1) : diff;
      assertEquals(j, ((Int) seq.get(i)).itr());
    }
  }
}
