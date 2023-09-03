package org.basex.query.ast;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for compiling function items.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Leo Woerteler
 */
public final class FuncItemTest extends QueryPlanTest {
  /** Resets optimizations. */
  @BeforeEach public void init() {
    inline(true);
  }

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
    check("for $sub in ('a', 'b', 'c', 'd', 'e', 'f')" +
        "return starts-with(?, $sub)('a')",
        "true\nfalse\nfalse\nfalse\nfalse\nfalse",
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
        "function-lookup(xs:QName(('local:foo')[" + _RANDOM_DOUBLE.args() + " < 1]), 0)()(-42)",
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
    check("for $i in 1 to 6 "
        + "let $f := function($x) { $i * $x },"
        + "    $g := function($y) { 2 * $f($y) }"
        + "return $g($g(42))",
        "168\n672\n1512\n2688\n4200\n6048",
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
        "<a><b/></a>\n<b/>"
    );
  }

  /** Tests if {@code fn:error()} is allowed with impossible types. */
  @Test public void gh958() {
    error("declare function local:f() as item()+ { error() }; local:f()", FUNERR1);
    error("function() as item()+ { error() }()", FUNERR1);
  }

  /** Checks that runtime values are not inlined into the static AST. */
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

  /** Simplification of map/array arguments. */
  @Test public void simplify() {
    check("[0](data(<_>1</_>))", 0, empty(DATA));
    check("map { 'a': 0 }(data(<_>a</_>))", 0, empty(DATA));
    check("map { 1: 0 }(data(<_>1</_>))", "", empty(DATA));
  }

  /** Fold optimizations. */
  @Test public void fold() {
    final String seq = "1 to 1000000000000000000";

    // return unchanged result
    check("fold-left(" + seq + ", 456, fn($r, $i) { $r })", 456, root(Int.class));
    check("fold-right(" + seq + ", 456, fn($i, $r) { $r })", 456, root(Int.class));

    // return constant value
    check("fold-left(" + seq + ", 1, fn($r, $i) { 123 })", 123, root(Int.class));
    check("fold-right(" + seq + ", 1, fn($i, $r) { 123 })", 123, root(Int.class));

    // exit if result will not change anymore
    query("fold-left(" + seq + ", 1, fn($r, $i) { if($r < 100) then $r + $i else $r })",
        106);
    query("fold-left(" + seq + ", 1, fn($r, $i) { if($r > 100) then $r + $i else $r })",
        1);
    query("fold-right(" + seq + ", 1, fn($i, $r) { if($r < 10) then $r + $i else $r })",
        1000000000000000001L);
    query("fold-right(" + seq + ", 1, fn($i, $r) { if($r > 10) then $r else $r + $i })",
        1000000000000000001L);

    // bug fix
    query("fold-right(1 to 100000, 1, fn($a, $b) { if($b > 10000000) then $b else $a + $b })",
        10094951);

    final String array = "array { 1 to 100000 }";

    // return unchanged result
    check("array:fold-left(" + array + ", 456, fn($r, $i) { $r })", 456, root(Int.class));
    check("array:fold-right(" + array + ", 456, fn($i, $r) { $r })", 456, root(Int.class));

    // bug fix
    query("array:fold-right(" + array + ", 1, fn($a, $b) { if($b > 10000000) then $b else $a+$b })",
        10094951);
  }
}
