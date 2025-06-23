package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link XQArray#insertMember(long, Value, QueryContext)}.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class ArrayInsertTest extends ArrayTest {
  /** Randomly insert elements and compare the result to an array list. */
  @Test public void fuzzyTest() {
    final int n = 50_000;
    final ArrayList<Integer> list = new ArrayList<>(n);
    XQArray array = XQArray.empty();

    final Random rng = new Random(42);
    for(int i = 0; i < n; i++) {
      final int insPos = rng.nextInt(i + 1);
      list.add(insPos, i);
      array = array.insertMember(insPos, Itr.get(i), qc);
      final int size = i + 1;
      assertEquals(size, array.structSize());
      assertEquals(size, list.size());

      if(i % 1000 == 999) {
        for(int j = 0; j < size; j++) {
          assertEquals(list.get(j).intValue(), ((Itr) array.memberAt(j)).itr());
        }
      }
    }
  }

  /**
   * Simple insert test.
   */
  @Test public void insertTest() {
    final int n = 1_000;
    XQArray array = XQArray.empty();

    for(int i = 0; i < n; i++) array = array.appendMember(Itr.get(i), qc);
    assertEquals(n, array.structSize());

    final Itr val = Itr.get(n);
    for(int i = 0; i <= n; i++) {
      final XQArray array2 = array.insertMember(i, val, qc);
      assertEquals(n, ((Itr) array2.memberAt(i)).itr());
      assertEquals(n + 1L, array2.structSize());
      for(int j = 0; j < n; j++) {
        assertEquals(j, ((Itr) array2.memberAt(j < i ? j : j + 1)).itr());
      }
    }
  }
}
