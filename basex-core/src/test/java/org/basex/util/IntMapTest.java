package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.util.hash.*;
import org.junit.jupiter.api.*;

/**
 * Integer map tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IntMapTest {
  /** Integer map. */
  private final IntMap map = new IntMap();

  /** Tests operations on an empty map. */
  @Test public void emptyMap() {
    assertTrue(map.isEmpty(), "Map is not empty.");
    assertEquals(0, map.size());
    assertEquals(Integer.MIN_VALUE, map.get(1));
  }

  /** Tests stored values. */
  @Test public void put() {
    for(int i = 0; i < 100; i++) map.put(i, i + 1);
    assertEquals(100, map.size());
    for(int i = 0; i < 100; i++) assertEquals(i + 1, map.get(i));
    assertEquals(Integer.MIN_VALUE, map.get(100));
  }
}
