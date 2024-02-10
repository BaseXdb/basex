  package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link XQArray#concat(XQArray)}.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class ArrayConcatTest extends ArrayTest {
  /** Generates and concatenates random arrays of a given size. */
  @Test public void fuzzyTest() {
    final Random rng = new Random();
    for(int n = 0; n < 100; n++) {
      for(int k = 0; k < 10; k++) {
        rng.setSeed(10L * n + k);
        final int l = rng.nextInt(n + 1), r = n - l;

        XQArray array1 = XQArray.empty(), array2 = XQArray.empty();
        final ArrayList<Integer> list1 = new ArrayList<>(l), list2 = new ArrayList<>(r);
        for(int i = 0; i < l; i++) {
          final int pos = rng.nextInt(i + 1);
          array1 = array1.insertBefore(pos, Int.get(i), qc);
          list1.add(pos, i);
        }

        for(int i = 0; i < r; i++) {
          final int pos = rng.nextInt(i + 1);
          array2 = array2.insertBefore(pos, Int.get(l + i), qc);
          list2.add(pos, l + i);
        }

        array1 = array1.concat(array2);
        array1.checkInvariants();
        list1.addAll(list2);
        assertEquals(n, array1.arraySize());
        assertEquals(n, list1.size());

        final Iterator<Value> iter1 = array1.iterator(0);
        final Iterator<Integer> iter2 = list1.iterator();
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
    XQArray array1 = XQArray.empty(), array2 = XQArray.empty();
    final int n = 200_000;
    for(int i = 0; i < n; i++) {
      final Value value = Int.get(i);
      array1 = array1.prepend(value);
      array2 = array2.append(value);
    }

    assertEquals(n, array1.arraySize());
    assertEquals(n, array2.arraySize());
    final XQArray array = array1.concat(array2);
    assertEquals(2 * n, array.arraySize());

    for(int i = 0; i < 2 * n; i++) {
      final int diff = i - n, j = diff < 0 ? -(diff + 1) : diff;
      assertEquals(j, ((Int) array.get(i)).itr());
    }
  }
}
