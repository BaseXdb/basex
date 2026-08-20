package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.util.hash.*;
import org.junit.jupiter.api.*;

/**
 * Integer object map tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IntObjectMapTest {
  /** Integer object map. */
  private final IntObjectMap<String> map = new IntObjectMap<>();

  /** Tests operations on an empty map. */
  @Test public void emptyMap() {
    assertTrue(map.isEmpty(), "Map is not empty.");
    assertEquals(0, map.size());
    assertNull(map.get(1));
    assertFalse(map.values().iterator().hasNext(), "Map is not empty.");
  }

  /** Tests stored values. */
  @Test public void put() {
    for(int i = 0; i < 100; i++) map.put(i, Integer.toString(i));
    assertEquals(100, map.size());
    for(int i = 0; i < 100; i++) assertEquals(Integer.toString(i), map.get(i));
    assertNull(map.get(100));
    assertEquals("x", map.computeIfAbsent(100, () -> "x"));
    assertEquals("x", map.get(100));
  }
}
