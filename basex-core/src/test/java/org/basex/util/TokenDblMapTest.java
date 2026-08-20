package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.util.hash.*;
import org.junit.jupiter.api.*;

/**
 * Token double map tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TokenDblMapTest {
  /** Token double map. */
  private final TokenDblMap map = new TokenDblMap();

  /** Tests operations on an empty map. */
  @Test public void emptyMap() {
    assertTrue(map.isEmpty(), "Map is not empty.");
    assertEquals(0, map.size());
    assertEquals(0, map.index(Token.token("x")));
    assertEquals(0, map.remove(Token.token("x")));
  }

  /** Tests stored values. */
  @Test public void put() {
    for(int i = 0; i < 100; i++) map.put(Token.token(i), i + 0.5);
    assertEquals(100, map.size());
    for(int i = 0; i < 100; i++) assertEquals(i + 0.5, map.value(map.index(Token.token(i))));
    assertEquals(0, map.index(Token.token(100)));
  }
}
