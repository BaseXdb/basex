package org.basex.query.value.array;

import static org.junit.Assert.*;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.*;

/**
 * Tests for {@link Array#insertBefore(long, Value)}.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class ArrayInsertTest {
  /** Randomly insert elements and compare the result to an array list. */
  @Test
  public void fuzzyTest() {
    final int n = 50_000;
    final ArrayList<Integer> list = new ArrayList<>(n);
    Array arr = Array.empty();

    final Random rng = new Random(42);
    for(int i = 0; i < n; i++) {
      final int insPos = rng.nextInt(i + 1);
      list.add(insPos, i);
      arr = arr.insertBefore(insPos, Int.get(i));
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
}
