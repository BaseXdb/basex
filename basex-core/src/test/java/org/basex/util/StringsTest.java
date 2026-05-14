package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Strings tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringsTest {
  /** Test. */
  @Test public void capitalize() {
    assertEquals("", Strings.capitalize(""));
    assertEquals("A", Strings.capitalize("a"));
    assertEquals("A", Strings.capitalize("A"));
    assertEquals("Ab", Strings.capitalize("ab"));
    assertEquals(".a", Strings.capitalize(".a"));
  }

  /** Test. */
  @Test public void camelCase() {
    assertEquals("", Strings.camelCase(""));
    assertEquals("a", Strings.camelCase("a"));
    assertEquals("aB", Strings.camelCase("a-b"));
    assertEquals("aBC", Strings.camelCase("a-b--c"));
    assertEquals("a.bC", Strings.camelCase("a.b-c"));
    assertEquals("a/b.cD", Strings.camelCase("a/b.c-D"));
  }

  /** Test. */
  @Test public void className() {
    assertEquals("", Strings.uriToClasspath(""));
    assertEquals("A", Strings.uriToClasspath("a"));
    assertEquals(".", Strings.uriToClasspath("."));
    assertEquals(".A", Strings.uriToClasspath(".a"));
    assertEquals(".Ab", Strings.uriToClasspath(".ab"));
    assertEquals("String", Strings.uriToClasspath("string"));
    assertEquals("java.lang.String", Strings.uriToClasspath("java.lang.string"));
    assertEquals("java.lang.String", Strings.uriToClasspath("java.lang.string"));
    assertEquals("java.lang.String", Strings.uriToClasspath("java/lang/string"));
    assertEquals("org.basex.modules.MD", Strings.uriToClasspath("org.basex.modules.m-d"));
    assertEquals("a.BC", Strings.uriToClasspath("a/-b-c"));
  }

  /** Test. */
  @Test public void uri2Path() {
    assertEquals("a", Strings.uri2path("a"));
    assertEquals("a", Strings.uri2path("/a"));
    assertEquals("a/b", Strings.uri2path("a/b"));
    assertEquals("a-c", Strings.uri2path("a-c"));
    assertEquals("A", Strings.uri2path("%41"));
    assertEquals("a/b", Strings.uri2path("a///b"));
    assertEquals("a/index", Strings.uri2path("a/"));
    assertEquals("index", Strings.uri2path("/"));
    assertEquals("index", Strings.uri2path(""));

    assertEquals("org/index", Strings.uri2path("http://org"));
    assertEquals("org/index", Strings.uri2path("http://org/"));
    assertEquals("org/basex/m/hello/World", Strings.uri2path("http://basex.org/m/hello/World"));
    assertEquals("com/example/www/index", Strings.uri2path("http://www.example.com"));
    assertEquals("a/b/c", Strings.uri2path("a:b:c"));
    assertEquals("A/A", Strings.uri2path("http://%41/%41"));

    assertEquals("-gg", Strings.uri2path("%gg"));
    assertEquals("-", Strings.uri2path(";"));
    assertEquals("http-/-gg", Strings.uri2path("http://%gg"));

    assertEquals("a/b/c", Strings.uri2path("a:b:c"));
  }

  /** Test. */
  @Test public void canonical() {
    // pure ASCII passes through unchanged
    assertEquals("", Strings.canonical(""));
    assertEquals("hello.txt", Strings.canonical("hello.txt"));

    // CP437→UTF-8 mojibake fix: 1-byte (ü), 2-byte (€), 3-byte (日), 4-byte (𝄞)
    assertEquals("Prüfung.txt", Strings.canonical("Pr├╝fung.txt"));
    assertEquals("€", Strings.canonical("Γé¼"));
    assertEquals("日", Strings.canonical("µùÑ"));
    assertEquals("𝄞", Strings.canonical("≡¥ä₧"));

    // genuine CP437 stays unchanged (0x81 / 'ü' is a UTF-8 continuation byte → invalid)
    assertEquals("Prüfung.txt", Strings.canonical("Prüfung.txt"));
    // Shift_JIS "日本.txt" (93 FA 96 7B) decoded via CP437 is not valid UTF-8 either
    assertEquals("ô·û{.txt", Strings.canonical("ô·û{.txt"));

    // NFD composes to NFC ('e' + U+0301 → 'é')
    assertEquals("Café.txt", Strings.canonical("Cafe\u0301.txt"));

    // combined mojibake + NFD→NFC: bytes 43 61 66 65 CC 81 2E 74 78 74 (UTF-8 of NFD
    // "Café.txt") read as CP437 yield "Cafe╠ü.txt"; canonical recovers UTF-8 then NFC
    assertEquals("Café.txt", Strings.canonical("Cafe╠ü.txt"));

    // path stays a path; mojibake is fixed within each segment
    assertEquals("dir/Prüfung.txt", Strings.canonical("dir/Pr├╝fung.txt"));

    // known heuristic limitation: a CP437 string whose bytes happen to form valid UTF-8
    // is false-positively "fixed" (C3 A9 in CP437 = "├⌐", as UTF-8 = "é"); extremely
    // rare in real filenames, inherent to the round-trip approach
    assertEquals("é", Strings.canonical("├⌐"));
  }
}
