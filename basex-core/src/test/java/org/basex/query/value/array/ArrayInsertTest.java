package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link XQArray#insertBefore(long, Value, QueryContext)}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class ArrayInsertTest extends ArrayTest {
  /** Randomly insert elements and compare the result to an array list. */
  @Test public void fuzzyTest() {
    final int n = 50_000;
    final ArrayList<Integer> list = new ArrayList<>(n);
    XQArray arr = XQArray.empty();

    final Random rng = new Random(42);
    for(int i = 0; i < n; i++) {
      final int insPos = rng.nextInt(i + 1);
      list.add(insPos, i);
      arr = arr.insertBefore(insPos, Int.get(i), qc);
      final int size = i + 1;
      assertEquals(size, arr.arraySize());
      assertEquals(size, list.size());

      if(i % 1000 == 999) {
        arr.checkInvariants();
        for(int j = 0; j < size; j++) {
          assertEquals(list.get(j).intValue(), ((Int) arr.get(j)).itr());
        }
      }
    }
  }

  /**
   * Simple insert test.
   */
  @Test public void insertTest() {
    final int n = 1_000;
    XQArray seq = XQArray.empty();

    for(int i = 0; i < n; i++) seq = seq.snoc(Int.get(i));
    assertEquals(n, seq.arraySize());

    final Int val = Int.get(n);
    for(int i = 0; i <= n; i++) {
      final XQArray seq2 = seq.insertBefore(i, val, qc);
      assertEquals(n, ((Int) seq2.get(i)).itr());
      assertEquals(n + 1L, seq2.arraySize());
      for(int j = 0; j < n; j++) {
        assertEquals(j, ((Int) seq2.get(j < i ? j : j + 1)).itr());
      }
    }
  }
}
