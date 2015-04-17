package org.basex.query.value.array;

import static org.junit.Assert.*;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.*;

/**
 * Tests for {@link Array#iterator(long)}.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class ArrayMembersTest {
  /** Random movements inside the array. */
  @Test public void randomTest() {
    for(int n = 0; n < 1_000; n++) {
      final Random rng = new Random(1337 + n);
      Array arr = Array.empty();
      final ArrayList<Integer> list = new ArrayList<>(n);
      for(int i = 0; i < n; i++) {
        final int insPos = rng.nextInt(i + 1);
        arr = arr.insertBefore(insPos, Int.get(i));
        list.add(insPos, i);
      }

      final int startPos = rng.nextInt(n + 1);
      final ListIterator<Value> it1 = arr.iterator(startPos);
      final ListIterator<Integer> it2 = list.listIterator(startPos);
      int pos = startPos;
      for(int i = 0; i < 100; i++) {
        final int k = rng.nextInt(n + 1);
        if(rng.nextBoolean()) {
          for(int j = 0; j < k; j++) {
            assertEquals(pos, it2.nextIndex());
            assertEquals(pos, it1.nextIndex());
            assertEquals(pos - 1, it2.previousIndex());
            assertEquals(pos - 1, it1.previousIndex());
            if(it2.hasNext()) {
              assertTrue(it1.hasNext());
              final long exp = it2.next();
              final long got = ((Int) it1.next()).itr();
              assertEquals(exp, got);
              pos++;
            } else {
              assertFalse(it1.hasNext());
              continue;
            }
          }
        } else {
          for(int j = 0; j < k; j++) {
            assertEquals(pos, it2.nextIndex());
            assertEquals(pos, it1.nextIndex());
            assertEquals(pos - 1, it2.previousIndex());
            assertEquals(pos - 1, it1.previousIndex());
            if(it2.hasPrevious()) {
              assertTrue(it1.hasPrevious());
              pos--;
              final long exp = it2.previous();
              final long got = ((Int) it1.previous()).itr();
              assertEquals(exp, got);
            } else {
              assertFalse(it1.hasPrevious());
              continue;
            }
          }
        }
      }
    }
  }
}
