package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.basex.io.*;
import org.junit.jupiter.api.*;

/**
 * Higher-order function tests.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class HigherOrderTest extends SandboxTest {
  /**
   * Test for name shadowing.
   */
  @Test public void shadowingTest() {
    query("let $x := 1 to 9 " +
      "return fold-left($x, 0, function($x, $y) { $x * 10 + $y })",
      123456789);
  }

  /**
   * Test for name heavy currying.
   */
  @Test public void curryTest() {
    query("let $digits := 1 to 9," +
      " $base-cmb := function($b, $n, $d) { $b * $n + $d }," +
      " $dec-cmb := $base-cmb(10, ?, ?)," +
      " $from-digits := fold-left(?, 0, $dec-cmb)" +
      " return $from-digits($digits)",
      123456789);
  }

  /**
   * Test for name heavy currying.
   */
  @Test public void curryTest2() {
    query("let $digits := 1 to 9," +
      " $base-cmb := function($n, $d) { 10 * $n + $d }," +
      " $from-digits := fold-left(?, 0, $base-cmb)" +
      "return $from-digits(1 to 9)",
      123456789);
  }

  /**
   * Test for name heavy currying.
   */
  @Test public void foldRightTest() {
    query("declare function local:before-first(" +
      "  $input as item()*," +
      "  $pred as function(item()) as item()*" +
      ") as item()* {" +
      "  fold-right($input, ()," +
      "    function($x, $xs) { if($pred($x)) then () else ($x, $xs) })" +
      "};" +
      "local:before-first((<h1/>, <p/>, <h1/>, <h2/>, <h3/>)," +
      "  function($it) { name($it) = 'h2' })",
      "<h1/>\n<p/>\n<h1/>");
  }

  /**
   * Test for name heavy currying.
   */
  @Test public void typeTest() {
    query("declare function local:f($x as xs:long, $y as xs:NCName)" +
      "    as element(e) {" +
      "  <e x='{ $x }' y='{ $y }'/>" +
      "};" +
      "local:f#2 instance of function(xs:long, xs:NCName) as element(e)",
      true);
  }

  /** Closure test (#1023). */
  @Test public void closureTest() {
    query("for $n in (<a/>, <b/>) let $f := function() as element()* { trace($n) } return $f()",
        "<a/>\n<b/>");
  }

  /**  Test for name heavy currying. */
  @Test public void placeHolderTest() {
    error("string-join(('a', 'b'), )(',')", FUNCARG_X);
  }

  /**  Test for invalid function expressions. */
  @Test public void invalidFunTest() {
    error("()()", EMPTYFOUND);
    error("1()", INVFUNCITEM_X_X);
    error("1.0()", INVFUNCITEM_X_X);
    error("1e0()", INVFUNCITEM_X_X);
    error("'x'()", INVFUNCITEM_X_X);
  }

  /**  Tests the creation of a cast function as function item. */
  @Test public void xsNCNameTest() {
    query("xs:NCName(?)('two')", "two");
  }

  /**  Tests the creation of a cast function as function item. */
  @Test public void wrongArityTest() {
    error("count(concat#2('1','2','3'))", INVARITY_X_X_X);
  }

  /** Tests using a partial function application as the context value. */
  @Test public void gh579() {
    query("declare context item := contains(?, 'a'); .('abc')", true);
  }

  /** Tests using a function literal that is used before the function. */
  @Test public void gh698() {
    query("declare function local:a($a) { local:b#1($a) };" +
          "declare function local:b($b) { $b }; local:a(1)", 1);
  }

  /** Do not pre-evaluate function items with non-deterministic expressions. */
  @Test public void gh1191() {
    final IOFile sandbox = sandbox();
    query(
      "let $files := ('a','b') ! (\"" + sandbox + "/\" || . || '.txt')" +
      "return (\n" +
      "  for $file in $files return (file:write-text($file, ?))(''),\n" +
      "  $files ! file:exists(.),\n" +
      "  $files ! file:delete(?)(.),\n" +
      "  $files ! file:exists(.)\n" +
        ')',
      "true\ntrue\nfalse\nfalse"
    );
  }

  /** Tests the %non-deterministic annotation. */
  @Test public void gh1212() {
    // FLWOR will be optimized away (empty result)
    query("for $f in (prof:void#1(?), error#0) let $ignore := $f() return ()", "");
    // FLWOR expression will be evaluated (due to non-deterministic keyword)
    query("try {"
        + "  let $f := error#0 let $err := non-deterministic $f() return ()"
        + "} catch * { 'ERR' }", "ERR");
    query("try {"
        + "  for $f in (prof:void#1(?), error#0)"
        + "  let $err := non-deterministic $f() return ()"
        + "} catch * { 'ERR' }", "ERR");
    query("try {"
        + "  let $f := error#0 let $err := $f() return ()"
        + "} catch * { 'ERR' }", "");
    query("try {"
        + "  let $f := function() { fn:error(()) }"
        + "  let $e := $f()"
        + "  return ()"
        + "} catch * { 'ERR' }", "");
  }

  /** Ensures that updating flag is not assigned before function body is known. */
  @Test public void gh1222() {
    query("declare function local:f($i) {"
        + "  for-each($i, function($k) { if($k) then local:f('') else () })"
        + "};"
        + "local:f('a')", "");
  }
}
