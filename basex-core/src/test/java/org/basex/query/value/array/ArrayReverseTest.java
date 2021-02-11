package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link XQArray#reverse(QueryContext)}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class ArrayReverseTest extends ArrayTest {
  /** Traverses an array and its reverse from both ends. */
  @Test public void randomTest() {
    final Random rng = new Random(42);
    for(int n = 0; n < 1_000; n++) {
      XQArray arr = XQArray.empty();
      for(int i = 0; i < n; i++) arr = arr.insertBefore(rng.nextInt(i + 1), Int.get(i), qc);
      assertEquals(n, arr.arraySize());
      final XQArray rev = arr.reverseArray(qc);
      final ListIterator<Value> af = arr.iterator(0), ab = arr.iterator(n);
      final ListIterator<Value> rf = rev.iterator(0), rb = rev.iterator(n);
      for(int i = 0; i < n; i++) {
        assertTrue(af.hasNext());
        assertTrue(ab.hasPrevious());
        assertTrue(rf.hasNext());
        assertTrue(rb.hasPrevious());
        assertEquals(((Int) af.next()).itr(), ((Int) rb.previous()).itr());
        assertEquals(((Int) ab.previous()).itr(), ((Int) rf.next()).itr());
      }
      assertFalse(af.hasNext());
      assertFalse(ab.hasPrevious());
      assertFalse(rf.hasNext());
      assertFalse(rb.hasPrevious());
    }
  }
}
