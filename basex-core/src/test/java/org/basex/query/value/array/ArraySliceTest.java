package org.basex.query.value.array;

import static org.junit.Assert.*;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.*;

/**
 * Tests the {@link Array#subArray(long, long)} method.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class ArraySliceTest {
  /** Exhaustively tests creating sub-arrays of arrays of a range of lengths. */
  @Test
  public void testSlice() {
    Array arr = Array.empty();
    for(int len = 0; len < 180; len++) {
      assertEquals(len, arr.arraySize());
      for(int pos = 0; pos < len; pos++) {
        for(int k = 0; k <= len - pos; k++) {
          final Array sub = arr.subArray(pos, k);
          assertEquals(k, sub.arraySize());
          sub.checkInvariants();
          final Iterator<Value> iter = sub.iterator(0);
          for(int i = 0; i < k; i++) {
            final long res = ((Int) iter.next()).itr();
            if(res != pos + i) {
              fail("Wrong value: " + res + " vs. " + (pos + i));
            }
          }
        }
      }
      arr = arr.snoc(Int.get(len));
    }
  }
}
