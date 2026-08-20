package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.util.hash.*;
import org.junit.jupiter.api.*;

/**
 * Integer set tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IntSetTest {
  /** Integer set. */
  private final IntSet set = new IntSet();

  /** Tests operations on an empty set. */
  @Test public void emptySet() {
    assertTrue(set.isEmpty(), "Set is not empty.");
    assertEquals(0, set.size());
    assertFalse(set.contains(1), "Key was found.");
    assertEquals(0, set.index(1));
    assertEquals(0, set.keys().length);
  }

  /** Tests added keys. */
  @Test public void add() {
    for(int i = 0; i < 100; i++) assertTrue(set.add(i), "Key was already indexed.");
    assertEquals(100, set.size());
    for(int i = 0; i < 100; i++) {
      assertTrue(set.contains(i), "Key is missing.");
      assertEquals(i, set.key(set.index(i)));
      assertFalse(set.add(i), "Key was indexed twice.");
    }
    assertFalse(set.contains(100), "Key was found.");
  }
}
