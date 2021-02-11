package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link XQArray#iterator(long)}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class ArrayMembersTest extends ArrayTest {
  /** Random movements inside the array. */
  @Test public void randomTest() {
    for(int n = 0; n < 1_000; n++) {
      final Random rng = new Random(1337 + n);
      XQArray arr = XQArray.empty();
      final ArrayList<Integer> list = new ArrayList<>(n);
      for(int i = 0; i < n; i++) {
        final int insPos = rng.nextInt(i + 1);
        arr = arr.insertBefore(insPos, Int.get(i), qc);
        list.add(insPos, i);
      }

      final int startPos = rng.nextInt(n + 1);
      final ListIterator<Value> iter1 = arr.iterator(startPos);
      final ListIterator<Integer> iter2 = list.listIterator(startPos);
      int pos = startPos;
      for(int i = 0; i < 100; i++) {
        final int k = rng.nextInt(n + 1);
        if(rng.nextBoolean()) {
          for(int j = 0; j < k; j++) {
            assertEquals(pos, iter2.nextIndex());
            assertEquals(pos, iter1.nextIndex());
            assertEquals(pos - 1, iter2.previousIndex());
            assertEquals(pos - 1, iter1.previousIndex());
            if(iter2.hasNext()) {
              assertTrue(iter1.hasNext());
              final long exp = iter2.next();
              final long got = ((Int) iter1.next()).itr();
              assertEquals(exp, got);
              pos++;
            } else {
              assertFalse(iter1.hasNext());
            }
          }
        } else {
          for(int j = 0; j < k; j++) {
            assertEquals(pos, iter2.nextIndex());
            assertEquals(pos, iter1.nextIndex());
            assertEquals(pos - 1, iter2.previousIndex());
            assertEquals(pos - 1, iter1.previousIndex());
            if(iter2.hasPrevious()) {
              assertTrue(iter1.hasPrevious());
              pos--;
              final long exp = iter2.previous();
              final long got = ((Int) iter1.previous()).itr();
              assertEquals(exp, got);
            } else {
              assertFalse(iter1.hasPrevious());
            }
          }
        }
      }
    }
  }
}
