package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests the {@link XQArray#subArray(long, long, QueryContext)} method.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class ArraySliceTest extends ArrayTest {
  /** Exhaustively tests creating sub-arrays of arrays of a range of lengths. */
  @Test public void testSlice() {
    XQArray arr = XQArray.empty();
    for(int len = 0; len < 100; len++) {
      assertEquals(len, arr.arraySize());
      for(int pos = 0; pos < len; pos++) {
        for(int k = 0; k <= len - pos; k++) {
          final XQArray sub = arr.subArray(pos, k, qc);
          assertEquals(k, sub.arraySize());
          sub.checkInvariants();
          final Iterator<Value> iter = sub.iterator(0);
          for(int i = 0; i < k; i++) {
            final long result = ((Int) iter.next()).itr();
            if(result != pos + i) {
              fail("Wrong value: " + result + " vs. " + (pos + i));
            }
          }
        }
      }
      arr = arr.snoc(Int.get(len));
    }
  }
}
