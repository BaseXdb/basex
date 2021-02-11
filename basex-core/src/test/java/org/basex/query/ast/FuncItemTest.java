package org.basex.query.ast;

import static org.basex.query.QueryError.*;

import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for compiling function items.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class FuncItemTest extends QueryPlanTest {
  /** Checks if the identity function is pre-compiled. */
  @Test public void idTest() {
    check("function($x) { $x }(42)",
        42,
        empty(Closure.class)
    );
  }

  /** Checks if a function literal is pre-compiled. */
  @Test public void literalTest() {
    check("lower-case#1('FooBar')",
        "foobar",
        empty(FuncLit.class)
    );
  }

  /** Checks if a partial application is pre-compiled. */
  @Test public void partAppTest() {
    check("starts-with('foobar', ?)('foo')",
        true,
        empty(PartFunc.class)
    );
  }

  /** Checks if a partial application with non-empty closure is left alone. */
  @Test public void partApp2Test() {
    check("for $sub in ('foo', 'bar')" +
        "return starts-with(?, $sub)('foobar')",
        "true\nfalse",
        exists(PartFunc.class)
    );
  }

  /** Checks that the Y combinator is pre-compiled. */
  @Test public void yCombinatorTest() {
    check("function($f) {" +
        "  let $loop := function($x) { $f(function() { $x($x) }) }" +
        "  return $loop($loop)" +
        "}(function($f) { 42 })",
        42,
        // both outer inline functions are pre-compiled
        empty(Closure.class),
        "/*/" + Util.className(Int.class) + " = '42'"
    );
  }

  /** Checks if {@code fold-left1(...)} can be used. */
  @Test public void foldLeft1Test() {
    check("hof:fold-left1(1 to 42, function($a, $b) { max(($a, $b)) })",
        42
    );
  }

  /** Checks if statically unused functions are compiled at runtime. */
  @Test public void compStatUnusedTest() {
    check("declare function local:foo() { abs(?) };" +
        "function-lookup(xs:QName(('local:foo')[random:double() < 1]), 0)()(-42)",
        42,
        empty(Util.className(StaticFuncs.class) + "/*")
    );
  }

  /**
   * Checks if statically used functions are compiled at compile time.
   */
  @Test public void gh382() {
    check("declare function local:a() { local:b() };" +
        "declare function local:b() { 42 };" +
        "local:a#0()",
        42,
        empty(FuncLit.class)
    );
  }

  /** Checks for circular references leading to stack overflows. */
  @Test public void noLoopTest() {
    check("declare function local:Y($f) { $f(function() { $f }) };" +
        "let $f := local:Y(function($x) { $x() }) return ($f ! .)[1]",
        "(anonymous-function)#1",
        exists(FuncItem.class)
    );
  }

  /** Checks for circular references leading to stack overflows. */
  @Test public void noLoopTest2() {
    check("declare function local:Y($f) { $f(function() { $f }) };" +
        "for-each(function($x) { $x() }, local:Y#1)[2]",
        "",
        empty()
    );
  }

  /** Checks for circular references leading to stack overflows. */
  @Test public void noLoopTest3() {
    check("declare function local:Y($f) { $f(function() { $f }) };" +
        "let $f := for-each(function($x) { $x() }, local:Y#1) return $f[2]",
        "",
        empty()
    );
  }

  /** Checks for circular references leading to stack overflows. */
  @Test public void noLoopTest4() {
    check("declare function local:foo($x) { function($f) { $f($x) } };" +
        "declare function local:bar($f) { $f(function($_) { $f }) };" +
        "let $a := local:foo(local:foo(function($e) { $e() })) " +
        "let $b := local:bar($a) " +
        "return ($b ! .)[1]",
        "(anonymous-function)#1",
        exists(FuncItem.class)
    );
  }

  /** Checks for circular references leading to stack overflows. */
  @Test public void noLoopTest5() {
    check("declare function local:foo($f) { $f($f) };" +
        "let $id := local:foo(function($g) { $g })" +
        "return $id(42)",
        42,
        "/*/" + Util.className(Int.class) + " = '42'"
    );
  }

  /** Checks that recursive function items are not inlined. */
  @Test public void noLoopTest6() {
    check("let $f := function($f) { $f($f) } return $f($f)",
        null,
        exists(FuncItem.class)
    );
  }

  /** Checks in non-recursive function items are inlined. */
  @Test public void funcItemInlining() {
    check("let $fold-left := function($f, $start, $seq) {\n" +
        "  let $go :=\n" +
        "    function($go, $acc, $xs) {\n" +
        "      if(empty($xs)) then $acc\n" +
        "      else $go($go, $f($acc, head($xs)), tail($xs))\n" +
        "    }\n" +
        "  return $go($go, $start, $seq)\n" +
        "}\n" +
        "return $fold-left(function($a, $b) { $a + $b }, 0, 1 to 100000)",

        5000050000L,

        // all inline functions are pre-compiled
        empty(Closure.class),
        // the addition function was inlined
        count(Util.className(DynFuncCall.class), 3),
        // the outer function item was inlined and removed
        "every $f in //" + Util.className(FuncItem.class) + " satisfies $f/*[1]/@name = '$go'",
        // there are only three variables left
        "count(distinct-values(//" + Util.className(Var.class) + "/@id)) = 3"
    );
  }

  /**
   * Checks if function items that have a non-empty closure but no arguments are correctly inlined.
   */
  @Test public void gh796() {
    check("declare function local:f($x as item()) { function() { $x } };" +
        "declare function local:g($f, $x) {if(fn:empty($f())) then local:f($x) else local:f(())};" +
        "declare variable $x := local:g(function() { () }, function() { () });" +
        "fn:count($x())",
        1,
        // the query should be pre-evaluated
        "QueryPlan/Int = 1"
    );
  }

  /** Tests for coercion of function items. */
  @Test public void funcItemCoercion() {
    error("let $f := function($g as function() as item()) { $g() }" +
        "return $f(function() { 1, 2 })", INVPROMOTE_X_X_X);
  }

  /** Checks if nested closures are inlined. */
  @Test public void nestedClosures() {
    check("for $i in 1 to 3 "
        + "let $f := function($x) { $i * $x },"
        + "    $g := function($y) { 2 * $f($y) }"
        + "return $g($g(42))",
        "168\n672\n1512",
        count(Util.className(Closure.class), 1)
    );
  }

  /** Tests if all functions are compiled when reflection takes places. */
  @Test public void gh839() {
    check("declare function local:f() { function() { () } };"
        + "function-lookup(xs:QName('local:f'), 0)()(),"
        + "inspect:functions()()()", "");
  }

  /** Tests if recursive function items are inlined only once. */
  @Test public void gh879() {
    check("declare function local:foo($root) {" +
        "  let $go :=" +
        "    function($go, $e) {" +
        "      fold-left(" +
        "        $e/foo, (), function($acc, $e) {" +
        "          ($acc, xs:string($e/@ID), $go($go, $e))" +
        "        }" +
        "      )" +
        "    }" +
        "  return $go($go, $root)" +
        "};" +
        "local:foo(document { <foo ID=\"a\"><foo ID=\"b\"/></foo> })",
        "a\nb",
        empty(StaticFuncCall.class),
        exists(DynFuncCall.class),
        exists(FuncItem.class)
    );
  }

  /** Tests if not-yet-known function references are parsed correctly. */
  @Test public void gh953() {
    check("declare function local:go ($n) { $n, for-each($n/*, local:go(?)) };" +
        "let $source := <a><b/></a> return local:go($source)",
        "<a>\n<b/>\n</a>\n<b/>"
    );
  }

  /** Tests if {@code fn:error()} is allowed with impossible types. */
  @Test public void gh958() {
    error("declare function local:f() as item()+ { error() }; local:f()", FUNERR1);
    error("function() as item()+ { error() }()", FUNERR1);
  }

  /** Checks that run-time values are not inlined into the static AST. */
  @Test public void gh1023() {
    check("for $n in (<a/>, <b/>)"
        + "let $f := function() as element()* { trace($n) }"
        + "return $f()",
        "<a/>\n<b/>");
  }

  /** Checks that functions circularly referenced through function literals are compiled. */
  @Test public void gh1038() {
    check("declare function local:a() { let $a := local:c() return () };"
        + "declare function local:b() { let $a := function() { local:a() } return () };"
        + "declare function local:c() { local:b#0() };"
        + "local:c() ",
        "",
        empty());
  }

  /** Static typing. */
  @Test public void gh1649() {
    check("function($v) { if($v = 0) then () else $v }(<x>0</x>)",
        "",
        root(IterFilter.class));
  }
}
