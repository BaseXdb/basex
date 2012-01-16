package org.basex.test.io;

import static org.junit.Assert.*;

import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.io.IOUrl;
import org.junit.Test;

/**
 * Test class for IO methods.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class IOTest {
  /** URL to file conversions. */
  @Test
  public void urlToFile() {
    assertEquals("/x", IOUrl.file("file:/x"));
    assertEquals("c:/x y", IOUrl.file("file:/c:/x%20y"));
    assertEquals("c:/x y", IOUrl.file("file:/c:/x y"));
    assertEquals("D:/x+y", IOUrl.file("file:///D:/x%2By"));
    assertEquals("/GG:/X", IOUrl.file("file:///GG:/X"));
  }

  /** File to URL conversions. */
  @Test
  public void fileToURL() {
    final String url = new IOFile("X Y").url();
    assertTrue(url.startsWith(IO.FILEPREF + '/'));
    assertTrue(url.endsWith("X%20Y"));
  }
}
