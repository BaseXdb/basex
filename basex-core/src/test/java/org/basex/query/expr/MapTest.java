package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

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

  /** Tests keys. */
  @Test public void keys() {
    error(" map{ ('a', 'b'): 'b' }", SEQFOUND_X);
    error(" map{ 'a': 'b', 'a': 'c' }", MAPDUPLKEY_X_X_X);
    error(" map{ xs:time('01:01:01'):1, xs:time('01:01:01'):1 }", MAPDUPLKEY_X_X_X);

    query(_MAP_SIZE.args(" map{ xs:time('01:01:01'):1, xs:time('02:02:02'):2 }"), "2");
    error(" map{ xs:time('01:01:01'):1, xs:time('01:01:02+01:00'):2 }", MAPTZ);
    error(" map{ xs:time('01:01:01'):1, xs:time('01:01:02+01:00'):2 }", MAPTZ);

    error("let $k1 := xs:time('01:01:01')"
        + "let $k2 := xs:dateTime('2001-01-01T01:01:01+01:00')"
        + "let $m := map { $k1:1 }"
        + "return map:put($m, $k2, 2)($k2)", MAPTZ);
    query("let $k1 := xs:time('01:01:01')"
        + "let $k2 := xs:time('01:01:02+01:00')"
        + "let $m := map { $k1:1 }"
        + "return map:put(map:remove($m, $k1), $k2, 2)($k2)", "2");
    error("let $k1 := xs:time('01:01:01')"
        + "let $k2 := xs:time('01:01:02+01:00')"
        + "let $k3 := xs:time('01:01:03+01:00')"
        + "let $m := map { $k1:1, $k2:2 }"
        + "return map:put(map:remove($m,$k1), $k3, 3)($k3)", MAPTZ);
    error("let $k1 := xs:time('01:01:01')"
        + "let $k2 := xs:time('01:01:02+01:00')"
        + "let $k3 := xs:time('01:01:03+01:00')"
        + "let $m := map { $k1:1, $k2:2 }"
        + "return map:merge((map:remove($m, $k1), map { $k3: 3}))($k3)", MAPTZ);
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

  /** GitHub bug (#1012). */
  @Test public void gh1012() {
    error("map {}(())", EMPTYFOUND);
  }
}
