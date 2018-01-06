package org.basex.query.value.array;

import static org.junit.Assert.*;

import org.basex.query.value.item.*;
import org.junit.*;

/**
 * Tests for {@link Array#put(long, org.basex.query.value.Value)}.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Leo Woerteler
 */
public final class ArrayPutTest extends ArrayTest {
  /**
   * Sets all values of a big array individually.
   */
  @Test
  public void setAllTest() {
    final int n = 5000;
    final ArrayBuilder builder = new ArrayBuilder();
    for(int i = 0; i < n; i++) {
      builder.append(Int.get(i));
    }
    final Array array = builder.freeze();
    for(int i = 0; i < n; i++) {
      final Array arr = array.put(i, Int.get(-i));
      for(int j = 0; j < n; j++) {
        assertEquals(i == j ? -j : j, ((Int) arr.get(j)).itr());
      }
    }
  }
}
