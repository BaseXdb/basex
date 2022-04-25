package org.basex.io;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Test class for IO methods.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class IOTest {
  /** URL to file conversions. */
  @Test public void urlToFile() {
    if(Prop.WIN) {
      assertEquals("C:/x y", IO.get("file:/c:/x%20y").path());
      assertEquals("D:/x+y", IO.get("file:///D:/x%2By").path());
      assertEquals("G:/X", IO.get("file:///G:/X").path());
      assertEquals("G:/X/", IO.get("file:///G:/X/").path());
    } else {
      assertEquals("/x y", IO.get("file:///x%20y").path());
      assertEquals("/x y/", IO.get("file:///x%20y/").path());
    }
  }

  /** File to URL conversions. */
  @Test public void fileToURL() {
    final String url = new IOFile("X Y").url();
    assertTrue(url.startsWith("file:/"));
    assertTrue(url.endsWith("X%20Y"));
  }

  /** URL construction. */
  @Test public void ioURL() {
    assertEquals("http:/"                   , new IOUrl("http:/").toString());
    assertEquals("http://a"                 , new IOUrl("http://a").toString());
    assertEquals("http://a"                 , new IOUrl("http://a").toString());
    assertEquals("http://a/b"               , new IOUrl("http://a/b").toString());
    assertEquals("http://a/b/"              , new IOUrl("http://a/b/").toString());
    assertEquals("http://."                 , new IOUrl("http://.").toString());
    assertEquals("http://./a"               , new IOUrl("http://./a").toString());
    assertEquals("http://./a/"              , new IOUrl("http://./a/").toString());
    assertEquals("http://a/.."              , new IOUrl("http://a/..").toString());
    assertEquals("http://a"                 , new IOUrl("http://a/.").toString());
    assertEquals("http://a/"                , new IOUrl("http://a/./").toString());
    assertEquals("http://a/b/c"             , new IOUrl("http://a/b/./c").toString());
    assertEquals("http://a/b/c/"            , new IOUrl("http://a/b/./c/").toString());
    assertEquals("http://a/"                , new IOUrl("http://a/b/../").toString());
    assertEquals("http://a/c"               , new IOUrl("http://a/b/../c").toString());
    assertEquals("http://a/c/"              , new IOUrl("http://a/b/../c/").toString());
    assertEquals("http://a/x/"              , new IOUrl("http://a/b/c/../../x/").toString());
    assertEquals("http://A/?a=a/./c"        , new IOUrl("http://A/?a=a/./c").toString());
    assertEquals("http://A/#a=a/./c"        , new IOUrl("http://A/#a=a/./c").toString());
    assertEquals("http://A/?a=a/./c#a=a/./c", new IOUrl("http://A/?a=a/./c#a=a/./c").toString());
  }
}
