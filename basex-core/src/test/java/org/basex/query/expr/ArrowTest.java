package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.junit.*;

/**
 * Arrow operator tests.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ArrowTest extends AdvancedQueryTest {
  /** Test. */
  @Test public void simple() {
    query("1 => count()", "1");
    query("() => count()", "0");
    query("() => count() => count()", "1");
  }

  /** Test. */
  @Test public void chained() {
    query("(for $i in ('a', 'b') return $i => string-length()) => count()", "2");
    query("let $string := 'a b c' "
        + "let $result := $string=>upper-case()=>normalize-unicode()=>tokenize('\\s+')"
        + "return ($result, count($result))", "A B C 3");
  }

  /** Test. */
  @Test public void dynamic() {
    query("1 => (count#1)()", "1");
    query("'ab' => substring(?)(2)", "b");
    query("'ab' => (substring(?, 2))()", "b");
    query("'ab' => (substring(?, ?))(?)(2)", "b");
    query("let $a := count#1 return 1 => $a()", "1");
  }

  /** Test. */
  @Test public void error() {
    error("1 => 1", ARROWSPEC);
    error("1 => (1)()", INVFUNCITEM_X);
  }
}
