package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * Tests for XQuery maps.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class MapTest extends SandboxTest {
  /** A map as key should lead to FOTY0013. */
  @Test public void mapAsKeyTest() {
    error("declare variable $m := map { 'a': 'b' };" +
          "declare variable $q := map { $m: 'a' };" +
          "$q", FIATOM_X);
  }

  /** Tests the the new syntax for map literals. */
  @Test public void gh755() {
    query("(<x><y/></x> / map { 'test': y, 42: 'asdf' })('test')", "<y/>");
  }

  /** Tests keys. */
  @Test public void keys() {
    error(" map { ('a', 'b'): 'b' }", SEQFOUND_X);
    error(" map { 'a': 'b', 'a': 'c' }", MAPDUPLKEY_X_X_X);
    error(" map { xs:time('01:01:01'): 1, xs:time('01:01:01'): 1 }", MAPDUPLKEY_X_X_X);

    query(_MAP_SIZE.args(" map { xs:time('01:01:01'): 1, xs:time('02:02:02'): 2 }"), 2);
    query("let $k1 := xs:time('01:01:01')"
        + "let $k2 := xs:time('01:01:02+01:00')"
        + "let $m := map { $k1: 1 }"
        + "return map:put(map:remove($m, $k1), $k2, 2)($k2)", 2);

    query(" map {  xs:time('01:01:01'): 1, xs:time('01:01:02+01:00'): 2 }");
    query(" map {  xs:time('01:01:01'): 1, xs:time('01:01:02+01:00'): 2 }");

    query("let $k1 := xs:time('01:01:01')"
        + "let $k2 := xs:dateTime('2001-01-01T01:01:01+01:00')"
        + "let $m := map { $k1: 1 }"
        + "return map:put($m, $k2, 2)");
    query("let $k1 := xs:time('01:01:01')"
        + "let $k2 := xs:time('01:01:02')"
        + "let $k3 := xs:time('01:01:03+01:00')"
        + "let $m := map { $k1: 1, $k2: 2 }"
        + "return map:put(map:remove($m,$k2), $k3, 3)");
    query("let $k1 := xs:time('01:01:01')"
        + "let $k2 := xs:time('01:01:02')"
        + "let $k3 := xs:time('01:01:02+01:00')"
        + "let $m := map { $k1: 1, $k2: 2 }"
        + "return map:merge((map:remove($m, $k2), map { $k3: 3 }))");
  }

  /** Stack overflow bug. */
  @Test public void so() {
    query("let $x := map { 'f': map { 1: 1, 2: 2 } } "
        + "return (every $k in map:keys($x('f')) satisfies $k eq $x('f')($k))", true);
  }

  /** Stack overflow bug. */
  @Test public void soType() {
    query("count(( map { }, array { <a/> } ))", 2);
    query("count(( array { <a/> }, map { } ))", 2);
  }

  /** GitHub bug (#1012). */
  @Test public void gh1012() {
    error("map { }(())", EMPTYFOUND);
  }

  /** GitHub bug (#1297). */
  @Test public void gh1297() {
    query("let $m := map { 'A_': 1, 'B@': 2, 'C!': 3 }"
        + "return (map:remove($m, 'A_')('A_'), map:remove($m, 'C!')('C!'))", "");
  }

  /** GitHub bug (#1480). */
  @Test public void gh1480() {
    query("map:merge(( map { 'AQ': 'A' }, map { 'B2': 'C' }, map { 'B2': 'X' }),"
        + "map { 'duplicates': 'use-last' })?B2", "X");
    query("map:merge(( map { 'AQ': 'A' }, map { 'B2': 'C' }, map { 'B2': 'X' }),"
        + "map { 'duplicates': 'use-first' })?B2", "C");
    query("map:merge(( map { 'AQ': 'A' }, map { 'B2': 'C' }, map { 'B2': 'X' }),"
        + "map { 'duplicates': 'combine' })?B2", "C\nX");
    error("map:merge(( map { 'AQ': 'A' }, map { 'B2': 'C' }, map { 'B2': 'X' }),"
        + "map { 'duplicates': 'reject' })?B2", MERGE_DUPLICATE_X);
  }

  /** Atomize key. */
  @Test public void atomKey() {
    query("map { 'x': 42 }([ 'x' ])", 42);
  }
}
