package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.core.jobs.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link XQArray#putMember(long, Value, Job)}.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class ArrayPutTest extends ArrayTest {
  /**
   * Sets all values of a big array individually.
   */
  @Test public void setAllTest() {
    final int n = 5000;
    final ArrayBuilder ab = new ArrayBuilder(job);
    for(int i = 0; i < n; i++) {
      ab.add(Itr.get(i));
    }
    final XQArray array1 = ab.array();
    for(int i = 0; i < n; i++) {
      final XQArray array2 = array1.putMember(i, Itr.get(-i), job);
      for(int j = 0; j < n; j++) {
        assertEquals(i == j ? -j : j, ((Itr) array2.valueAt(j)).itr());
      }
    }
  }
}
