package org.basex.io;

import static org.junit.Assert.*;

import org.basex.util.*;
import org.junit.*;

/**
 * Test class for IO methods.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class IOTest {
  /** URL to file conversions. */
  @Test
  public void urlToFile() {
    if(Prop.WIN) {
      assertEquals("C:/x y", IOUrl.toFile("file:/c:/x%20y"));
      assertEquals("C:/x y", IOUrl.toFile("file://c:/x y"));
      assertEquals("D:/x+y", IOUrl.toFile("file:///D:/x%2By"));
      assertEquals("G:/X", IOUrl.toFile("file:///G:/X"));
    } else {
      assertEquals("/x y", IOUrl.toFile("file:///x%20y"));
    }
  }

  /** File to URL conversions. */
  @Test
  public void fileToURL() {
    final String url = new IOFile("X Y").url();
    assertTrue(url.startsWith("file:/"));
    assertTrue(url.endsWith("X%20Y"));
  }
}
