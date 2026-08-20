package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.util.hash.*;
import org.junit.jupiter.api.*;

/**
 * Token object map tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TokenObjectMapTest {
  /** Token object map. */
  private final TokenObjectMap<String> map = new TokenObjectMap<>();

  /** Tests operations on an empty map. */
  @Test public void emptyMap() {
    assertTrue(map.isEmpty(), "Map is not empty.");
    assertEquals(0, map.size());
    assertNull(map.get(Token.token("x")));
    assertEquals(0, map.remove(Token.token("x")));
    assertFalse(map.values().iterator().hasNext(), "Map is not empty.");
  }

  /** Tests stored values. */
  @Test public void put() {
    for(int i = 0; i < 100; i++) map.put(Token.token(i), Integer.toString(i));
    assertEquals(100, map.size());
    for(int i = 0; i < 100; i++) assertEquals(Integer.toString(i), map.get(Token.token(i)));
    assertNull(map.get(Token.token(100)));
    assertEquals("x", map.computeIfAbsent(Token.token(100), () -> "x"));
    assertEquals("x", map.get(Token.token(100)));
  }
}
