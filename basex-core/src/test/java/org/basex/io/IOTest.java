package org.basex.io;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Test class for IO methods.
 *
 * @author BaseX Team, BSD License
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

  /** Path normalization must preserve non-BMP characters (surrogate pairs). */
  @Test public void surrogatePairPath() {
    final IOFile io = new IOFile("/x/🐾.txt");
    assertEquals("🐾.txt", io.name());
    assertTrue(io.path().endsWith("/x/🐾.txt"), "path: " + io.path());
    assertTrue(io.url().endsWith("/x/🐾.txt"), "url: " + io.url());
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

  /** Internationalized domain names: host is converted to Punycode. */
  @Test public void ioURLIdn() {
    assertEquals("https://www.xn--luft-bei-dir-gcb.de/",
        new IOUrl("https://www.läuft-bei-dir.de/").toString());
    assertEquals("https://xn--bcher-kva.example/path",
        new IOUrl("https://bücher.example/path").toString());
    assertEquals("https://user@xn--bcher-kva.example:8080/p",
        new IOUrl("https://user@bücher.example:8080/p").toString());
    // ASCII hosts and IPv6 are left unchanged
    assertEquals("http://a", new IOUrl("http://a").toString());
    assertEquals("http://[::1]:8080/", new IOUrl("http://[::1]:8080/").toString());
  }

  /** IDN encoding helper used by IOUrl and the HTTP client. */
  @Test public void toAscii() {
    // ASCII URLs pass through unchanged
    assertEquals("http://a/b", IOUrl.toAscii("http://a/b"));
    assertEquals("http://[::1]:8080/", IOUrl.toAscii("http://[::1]:8080/"));
    assertEquals("not-a-url", IOUrl.toAscii("not-a-url"));
    // Non-ASCII hosts are Punycode-encoded; path/query/fragment are preserved verbatim
    assertEquals("https://xn--bcher-kva.example/",
        IOUrl.toAscii("https://bücher.example/"));
    assertEquals("https://xn--bcher-kva.example",
        IOUrl.toAscii("https://bücher.example"));
    assertEquals("https://xn--bcher-kva.example/p?q=ä",
        IOUrl.toAscii("https://bücher.example/p?q=ä"));
    assertEquals("https://xn--bcher-kva.example#frag",
        IOUrl.toAscii("https://bücher.example#frag"));
    assertEquals("https://user@xn--bcher-kva.example:8080/p",
        IOUrl.toAscii("https://user@bücher.example:8080/p"));
  }
}
