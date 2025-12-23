package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.core.jobs.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests the {@link XQArray#subArray(long, long, Job)} method.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class ArraySliceTest extends ArrayTest {
  /** Exhaustively tests creating subarrays of arrays of a range of lengths. */
  @Test public void testSlice() {
    XQArray array = XQArray.empty();
    for(int len = 0; len < 100; len++) {
      assertEquals(len, array.structSize());
      for(int pos = 0; pos < len; pos++) {
        for(int k = 0; k <= len - pos; k++) {
          final XQArray sub = array.subArray(pos, k, job);
          assertEquals(k, sub.structSize());
          final Iterator<Value> iter = sub.iterator(0);
          for(int i = 0; i < k; i++) {
            final long result = ((Itr) iter.next()).itr();
            if(result != pos + i) {
              fail("Wrong value: " + result + " vs. " + (pos + i));
            }
          }
        }
      }
      array = array.appendMember(Itr.get(len), job);
    }
  }
}
