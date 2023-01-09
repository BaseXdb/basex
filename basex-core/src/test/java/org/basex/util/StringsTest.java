package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Strings tests.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class StringsTest {
  /** Test. */
  @Test public void capitalize() {
    assertEquals("", Strings.capitalize(""));
    assertEquals("A", Strings.capitalize("a"));
    assertEquals("A", Strings.capitalize("A"));
    assertEquals("Ab", Strings.capitalize("ab"));
    assertEquals(".a", Strings.capitalize(".a"));
  }
}
