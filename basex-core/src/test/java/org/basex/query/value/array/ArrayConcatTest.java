package org.basex.query.value.array;

import static org.junit.Assert.*;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.*;

/**
 * Tests for  {@link Array#concat(Array)}.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class ArrayConcatTest {
  /** Generates and concatenates random arrays of a given size. */
  @Test public void fuzzyTest() {
    final Random rng = new Random();
    for(int n = 0; n < 1_000; n++) {
      for(int k = 0; k < 10; k++) {
        rng.setSeed(10 * n + k);
        final int l = rng.nextInt(n + 1), r = n - l;

        Array a1 = Array.empty(), b1 = Array.empty();
        final ArrayList<Integer> a2 = new ArrayList<>(l), b2 = new ArrayList<>(r);
        for(int i = 0; i < l; i++) {
          final int pos = rng.nextInt(i + 1);
          a1 = a1.insertBefore(pos, Int.get(i));
          a2.add(pos, i);
        }

        for(int i = 0; i < r; i++) {
          final int pos = rng.nextInt(i + 1);
          b1 = b1.insertBefore(pos, Int.get(l + i));
          b2.add(pos, l + i);
        }

        a1 = a1.concat(b1);
        a1.checkInvariants();
        a2.addAll(b2);
        assertEquals(n, a1.arraySize());
        assertEquals(n, a2.size());

        final Iterator<Value> it1 = a1.iterator(0);
        final Iterator<Integer> it2 = a2.iterator();
        while(it1.hasNext()) {
          assertTrue(it2.hasNext());
          final long i1 = ((Int) it1.next()).itr(), i2 = it2.next();
          assertEquals(i2, i1);
        }
        assertFalse(it2.hasNext());
      }
    }
  }
}
