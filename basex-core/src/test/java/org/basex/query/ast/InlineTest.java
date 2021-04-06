package org.basex.query.ast;

import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for inlining.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class InlineTest extends QueryPlanTest {
  /** Resets optimizations. */
  @BeforeEach public void init() {
    inline(false);
    unroll(false);
  }

  /** Tests if inlining works in {@link Arith} expressions. */
  @Test public void plusTest() {
    check("let $x := 21 return $x + 21", 42, empty(GFLWOR.class));
    check("let $x := 21 return 21 + $x", 42, empty(GFLWOR.class));
    check("let $x := 21 return $x + $x", 42, empty(GFLWOR.class));
    check("let $x := <x>21</x> return $x + 21", 42, empty(GFLWOR.class));
  }

  /** Tests if variable uses in {@link Switch} are counted correctly. */
  @Test public void switchTest() {
    // all paths use $x only once
    check("let $x := 42 return switch(42) case 23 return $x case 84 return $x" +
        " case $x return 123 default return 1337", 123, empty(GFLWOR.class));
    // $x is used twice, but first occurrence will be removed in typeswitch optimization
    check("let $x := <x/> return switch(23) case $x return 123 case 23 return $x" +
        " default return 1337", "<x/>", empty(GFLWOR.class));
  }

  /** Switch with contains. */
  @Test public void gh738() {
    check("let $item := <item>blah blah</item> " +
        "let $type := switch (fn:true()) " +
        "  case ($item contains text 'blah') return <type>a</type> " +
        "  default return () " +
        "return $type",
        "<type>a</type>",
        empty(Let.class),
        root(ItemMap.class));
  }

  /** Typing and Function items: XPTY0004. */
  @Test public void gh849() {
    check("let $f := function($s as xs:string) { $s }" +
        "return $f(let $x := <x>1</x> return if($x = 1.1) then () else 'x')",
        "x",
        exists(Str.class));
  }

  /** Tests if variables directly inside an FTDistanceExpr are correctly inlined. */
  @Test public void gh907() {
    check("let $n := 0 return 'x y' contains text 'x y' distance exactly $n paragraphs",
        true,
        empty(GFLWOR.class),
        empty(Var.class));
  }

  /** Checks if forward-referencing function literals are inlined. */
  @Test public void gh1052() {
    check("declare function local:a() { local:b#1(42) };"
        + "declare function local:b($a) { $a };"
        + "local:a()",
        42,
        exists("*/" + Util.className(Int.class) + "[. = '42']"));

    check("declare function local:a() { local:b(?)(42) };"
        + "declare function local:b($a) { $a };"
        + "local:a()",
        42,
        exists(Util.className(Int.class) + "[. = '42']"));

    check("declare function local:a() { local:b#1(?)(42) };"
        + "declare function local:b($a) { $a };"
        + "local:a()",
        42,
        exists(Util.className(Int.class) + "[. = '42']"));
  }

  /** Checks that the simple map operator prohibits inlining a context item into its RHS. */
  @Test public void gh1055() {
    inline(true);
    check("(let $d := for-each(1 to 100, function($a) { $a }) "
        + "return (1 to 2) ! $d)[. = 0]",
        "",
        exists(SingletonSeq.class));
    check("(let $d := for-each(1 to 11, hof:id#1) "
        + "return (1 to 2) ! $d[1])[. = 0]",
        "",
        exists(SingletonSeq.class));
    check("for $x in (<x/>, <x/>) where (1, 2) ! $x return $x",
        "<x/>\n<x/>",
        empty(ContextValue.class));
  }

  /** Simple map operator. */
  @Test public void gh1094() {
    check("for $d in (true(), false()) where boolean(<a/> ! (., .) ! (., .)) return $d",
        "true\nfalse", empty(GFLWOR.class));
    check("let $a := <a/> return 'bar' ! . ! $a", "<a/>", empty(Let.class));
  }

  /** Tests the annotation {@link Annotation#_BASEX_INLINE}. */
  @Test public void annotation() {
    // deactivate inlining globally, activate locally
    check("declare option db:inlinelimit '0';"
        + "declare %basex:inline function local:x($x) { $x }; local:x(123)",
        123,
        empty(StaticFunc.class),
        exists(Int.class));

    // deactivate inlining globally and locally
    check("declare option db:inlinelimit '0';"
        + "declare %basex:inline(0) function local:x($x) { $x }; local:x(123)",
        123,
        exists(StaticFunc.class));

    // activate inlining globally, deactivate locally
    check("declare option db:inlinelimit '1000';"
        + "declare %basex:inline(0) function local:x($x) { $x }; local:x(123)",
        123,
        exists(StaticFunc.class));
  }

  /** Tests if all let clauses are removed. */
  @Test public void funcTest() {
    check("let $a := 'foo' return 'bar' ! . ! $a", "foo", empty(Let.class));
  }

  /** Ensures that non-deterministic clauses are not reordered. */
  @Test public void ndtFuncTest() {
    inline(true);
    check("let $a := function($d) { trace($d) }"
        + "let $b := non-deterministic $a('1st') let $c := non-deterministic $a('2nd') "
        + "return $b", "1st",
        root(ItemMap.class),
        "//FnTrace[. = '1st'] << //FnTrace[. = '2nd']");
  }

  /** Checks that window clauses are recognized as loops. */
  @Test public void gh1126() {
    check("let $s := 1 ! <a>{ . }</a> "
        + "for tumbling window $w in 1 to 2 start when true() end when true() return $s",
        "<a>1</a>\n<a>1</a>",
        count(Let.class, 1),
        count(Window.class, 1),
        "//Let << //Window");
  }

  /** Checks that inlining a nested closure works properly. */
  @Test public void gh1424() {
    inline(true);
    check("declare function local:f() {"
        + "  let $func := function($key) { map { $key: 'ok' }($key) }"
        + "  let $input := <ok/>"
        + "  let $call := $func(name($input))"
        + "  return function() { $call }"
        + "};"
        + "local:f()()",
        "ok",
        exists(DynFuncCall.class),
        empty(StaticFunc.class),
        empty(Closure.class),
        root(ItemMap.class));
  }
}
