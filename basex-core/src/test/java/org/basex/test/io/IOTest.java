package org.basex.test.io;

import static org.junit.Assert.*;

import org.basex.core.*;
import org.basex.io.*;
import org.junit.*;

/**
 * Test class for IO methods.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class IOTest {
  /** URL to file conversions. */
  @Test
  public void urlToFile() {
    if(Prop.WIN) {
      assertEquals("C:/x y", IOFile.get("file:/c:/x%20y").path());
      assertEquals("C:/x y", IOFile.get("file://c:/x y").path());
      assertEquals("D:/x+y", IOFile.get("file:///D:/x%2By").path());
      assertEquals("G:/X", IOFile.get("file:///G:/X").path());
    } else {
      assertEquals("/x y", IOFile.get("file:///x%20y").path());
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
