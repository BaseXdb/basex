package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Strings tests.
 *
 * @author BaseX Team 2005-21, BSD License
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
    assertEquals("", Strings.className(""));
    assertEquals("A", Strings.className("a"));
    assertEquals(".", Strings.className("."));
    assertEquals(".A", Strings.className(".a"));
    assertEquals(".Ab", Strings.className(".ab"));
    assertEquals("String", Strings.className("string"));
    assertEquals("java.lang.String", Strings.className("java.lang.string"));
    assertEquals("java.lang.String", Strings.className("java.lang.string"));
    assertEquals("java.lang.String", Strings.className("java/lang/string"));
    assertEquals("org.basex.modules.MetaData", Strings.className("org.basex.modules.meta-data"));
    assertEquals("a.BC", Strings.className("a/-b-c"));
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
    assertEquals("org/basex/modules/hello/World",
        Strings.uri2path("http://basex.org/modules/hello/World"));
    assertEquals("com/example/www/index", Strings.uri2path("http://www.example.com"));
    assertEquals("a/b/c", Strings.uri2path("a:b:c"));
    assertEquals("A/A", Strings.uri2path("http://%41/%41"));

    assertEquals("-gg", Strings.uri2path("%gg"));
    assertEquals("-", Strings.uri2path(";"));
    assertEquals("http-/-gg", Strings.uri2path("http://%gg"));

    assertEquals("a/b/c", Strings.uri2path("a:b:c"));
  }
}
