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
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class ArrayReverseTest extends ArrayTest {
  /** Traverses an array and its reverse from both ends. */
  @Test public void randomTest() {
    final Random rng = new Random(42);
    for(int n = 0; n < 1_000; n++) {
      XQArray array = XQArray.empty();
      for(int i = 0; i < n; i++) array = array.insertMember(rng.nextInt(i + 1), Itr.get(i), qc);
      assertEquals(n, array.structSize());
      final XQArray rev = array.reverseArray(qc);
      final ListIterator<Value> af = array.iterator(0), ab = array.iterator(n);
      final ListIterator<Value> rf = rev.iterator(0), rb = rev.iterator(n);
      for(int i = 0; i < n; i++) {
        assertTrue(af.hasNext());
        assertTrue(ab.hasPrevious());
        assertTrue(rf.hasNext());
        assertTrue(rb.hasPrevious());
        assertEquals(((Itr) af.next()).itr(), ((Itr) rb.previous()).itr());
        assertEquals(((Itr) ab.previous()).itr(), ((Itr) rf.next()).itr());
      }
      assertFalse(af.hasNext());
      assertFalse(ab.hasPrevious());
      assertFalse(rf.hasNext());
      assertFalse(rb.hasPrevious());
    }
  }
}
