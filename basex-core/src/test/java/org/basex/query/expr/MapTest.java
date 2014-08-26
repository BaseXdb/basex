package org.basex.query.expr;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.junit.*;

/**
 * Tests for XQuery maps.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class MapTest extends AdvancedQueryTest {
  /** A map as key should lead to FOTY0013. */
  @Test public void mapAsKeyTest() {
    error("declare variable $m := map { 'a': 'b' };" +
          "declare variable $q := map { $m: 'a' };" +
          "$q", FIATOM_X);
  }

  /** Tests the the new syntax for map literals (see GH-755). */
  @Test public void jsonSyntax() {
    query("(<x><y/></x> / map { 'test':y, 42:'asdf' })('test')", "<y/>");
  }

  /** Tests invalid keys. */
  @Test public void keys() {
    error(" map{ ('a', 'b'): 'b' }", MAPKEY_X);
    error(" map{ 'a': 'b', 'a': 'c' }", MAPDUPLKEY_X_X_X);
  }

  /** Stack overflow bug. */
  @Test public void so() {
    query("let $x := map { 'f': { 1: 1, 2: 2 } } "
        + "return (every $k in map:keys($x('f')) satisfies $k eq $x('f')($k))", true);
  }

  /** Stack overflow bug. */
  @Test public void soType() {
    query("count(( map { }, array { <a/> } ))", 2);
    query("count(( array { <a/> }, map { } ))", 2);
  }
}
