package org.basex.io;

import static org.junit.Assert.*;

import org.basex.util.*;
import org.junit.*;

/**
 * Test class for IO methods.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class IOTest {
  /** URL to file conversions. */
  @Test
  public void urlToFile() {
    if(Prop.WIN) {
      assertEquals("C:/x y", IO.get("file:/c:/x%20y").path());
      assertEquals("C:/x y", IO.get("file://C:/x y").path());
      assertEquals("D:/x+y", IO.get("file:///D:/x%2By").path());
      assertEquals("G:/X", IO.get("file:///G:/X").path());
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
