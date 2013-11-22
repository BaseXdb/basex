package org.basex.query.ast;

import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Tests for compiling function items.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public final class FuncItemTest extends QueryPlanTest {
  /** Checks if the identity function is pre-compiled. */
  @Test
  public void idTest() {
    check("function($x) { $x }(42)",
        "42",
        "empty(//" + Util.className(InlineFunc.class) + ')'
    );
  }

  /** Checks if a function literal is pre-compiled. */
  @Test
  public void literalTest() {
    check("lower-case#1('FooBar')",
        "foobar",
        "empty(//" + Util.className(FuncLit.class) + ')'
    );
  }

  /** Checks if a partial application is pre-compiled. */
  @Test
  public void partAppTest() {
    check("starts-with('foobar', ?)('foo')",
        "true",
        "empty(//" + Util.className(PartFunc.class) + ')'
    );
  }

  /** Checks if a partial application with non-empty closure is left alone. */
  @Test
  public void partApp2Test() {
    check("for $sub in ('foo', 'bar')" +
        "return starts-with(?, $sub)('foobar')",
        "true false",
        "exists(//" + Util.className(PartFunc.class) + ')'
    );
  }

  /** Checks that the Y combinator is pre-compiled. */
  @Test
  public void yCombinatorTest() {
    check("function($f) {" +
        "  let $loop := function($x) { $f(function() { $x($x) }) }" +
        "  return $loop($loop)" +
        "}(function($f) { 42 })",
        "42",
        // both outer inline functions are pre-compiled
        "exists(//" + Util.className(DynFuncCall.class) + ')',
        "every $f in outermost(//" + Util.className(DynFuncCall.class) + ")/* satisfies" +
        "  $f instance of element(" + Util.className(FuncItem.class) + ')'
    );
  }

  /** Checks if {@code fold-left1(...)} can be used. */
  @Test
  public void foldLeft1Test() {
    check("hof:fold-left1(1 to 42, function($a, $b) { max(($a, $b)) })",
        "42"
    );
  }

  /** Checks if statically unused functions are compiled at runtime. */
  @Test
  public void compStatUnusedTest() {
    check("declare function local:foo() { abs(?) };" +
        "function-lookup(xs:QName('local:foo'), 0)()(-42)",
        "42",
        "exists(//" + Util.className(PartFunc.class) + ')'
    );
  }

  /**
   * Checks if statically used functions are compiled at compile time.
   * Added because of issue GH-382.
   */
  @Test
  public void compStatUsedTest() {
    check("declare function local:a() { local:b() };" +
        "declare function local:b() { 42 };" +
        "local:a#0()",
        "42",
        "empty(//" + Util.className(FuncLit.class) + ')'
    );
  }

  /** Checks for circular references leading to stack overflows. */
  @Test
  public void noLoopTest() {
    check("declare function local:Y($f) { $f(function() { $f }) };" +
        "let $f := local:Y(function($x) { $x() }) return $f[2]",
        "",
        "exists(//" + Util.className(FuncItem.class) + ')'
    );
  }

  /** Checks for circular references leading to stack overflows. */
  @Test
  public void noLoopTest2() {
    check("declare function local:Y($f) { $f(function() { $f }) };" +
        "for-each(function($x) { $x() }, local:Y#1)[2]",
        "",
        "exists(//" + Util.className(FuncItem.class) + ')'
    );
  }

  /** Checks for circular references leading to stack overflows. */
  @Test
  public void noLoopTest3() {
    check("declare function local:Y($f) { $f(function() { $f }) };" +
        "let $f := for-each(function($x) { $x() }, local:Y#1) return $f[2]",
        "",
        "exists(//" + Util.className(FuncItem.class) + ')'
    );
  }

  /** Checks for circular references leading to stack overflows. */
  @Test
  public void noLoopTest4() {
    check("declare function local:foo($x) { function($f) { $f($x) } };" +
        "declare function local:bar($f) { $f(function($_) { $f }) };" +
        "let $a := local:foo(local:foo(function($e) { $e() })) " +
        "let $b := local:bar($a) " +
        "return $b[2]",
        "",
        "exists(//" + Util.className(FuncItem.class) + ')'
    );
  }

  /** Checks for circular references leading to stack overflows. */
  @Test
  public void noLoopTest5() {
    check("declare function local:foo($f) { $f($f) };" +
        "let $id := local:foo(function($g) { $g })" +
        "return $id(42)",
        "42",
        "exists(//" + Util.className(FuncItem.class) + ')'
    );
  }

  /** Checks in non-recursive function items are inlined. */
  @Test
  public void funcItemInlining() {
    check("let $fold-left :=" +
        "  function($f, $start, $seq) {" +
        "    let $go :=" +
        "      function($go, $acc, $xs) {" +
        "        if(empty($xs)) then $acc" +
        "        else $go($go, $f($acc, head($xs)), tail($xs))" +
        "      }" +
        "    return $go($go, $start, $seq)" +
        "  }" +
        "return $fold-left(function($a,$b) {$a + $b}, 0, 1 to 100000)",

        "5000050000",

        // all inline functions are pre-compiled
        "empty(//" + Util.className(InlineFunc.class) + ')',
        // the outer function item was inlined and removed
        "every $f in //" + Util.className(FuncItem.class) + " satisfies $f/*[1]/@name = '$go'",
        // the addition function was inlined
        "count(//" + Util.className(DynFuncCall.class) + ") = 3",
        // there are only three variables left
        "count(distinct-values(//" + Util.className(Var.class) + "/@id)) = 3"
    );
  }

  /**
   * Checks if function items that have a non-empty closure but no arguments are correctly inlined.
   * @see <a href="https://github.com/BaseXdb/basex/issues/796">GH-796</a>
   */
  @Test
  public void closureOnlyInlining() {
    check("declare function local:f($x as item()) { function() { $x } };" +
        "declare function local:g($f, $x) {if(fn:empty($f())) then local:f($x) else local:f(())};" +
        "declare variable $x := local:g(function() { () }, function() { () });" +
        "fn:count($x())",
        "1",
        // the query should be pre-evaluated
        "QueryPlan/Int/@value = 1"
    );
  }
}
