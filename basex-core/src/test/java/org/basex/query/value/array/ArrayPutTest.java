package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link XQArray#put(long, org.basex.query.value.Value)}.
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
    final ArrayBuilder ab = new ArrayBuilder(qc);
    for(int i = 0; i < n; i++) {
      ab.add(Int.get(i));
    }
    final XQArray array1 = ab.array();
    for(int i = 0; i < n; i++) {
      final XQArray array2 = array1.put(i, Int.get(-i));
      for(int j = 0; j < n; j++) {
        assertEquals(i == j ? -j : j, ((Int) array2.memberAt(j)).itr());
      }
    }
  }
}
