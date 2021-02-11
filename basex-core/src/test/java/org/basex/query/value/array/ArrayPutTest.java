package org.basex.query.value.array;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link XQArray#put(long, org.basex.query.value.Value)}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class ArrayPutTest extends ArrayTest {
  /**
   * Sets all values of a big array individually.
   */
  @Test public void setAllTest() {
    final int n = 5000;
    final ArrayBuilder builder = new ArrayBuilder();
    for(int i = 0; i < n; i++) {
      builder.append(Int.get(i));
    }
    final XQArray array = builder.freeze();
    for(int i = 0; i < n; i++) {
      final XQArray arr = array.put(i, Int.get(-i));
      for(int j = 0; j < n; j++) {
        assertEquals(i == j ? -j : j, ((Int) arr.get(j)).itr());
      }
    }
  }
}
