package org.basex.query.ast;

import static org.basex.query.QueryError.*;

import org.basex.*;
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
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class InlineTest extends SandboxTest {
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
        root(Pipeline.class));
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
        exists("*/" + Util.className(Itr.class) + "[. = '42']"));

    check("declare function local:a() { local:b(?)(42) };"
        + "declare function local:b($a) { $a };"
        + "local:a()",
        42,
        exists(Util.className(Itr.class) + "[. = '42']"));

    check("declare function local:a() { local:b#1(?)(42) };"
        + "declare function local:b($a) { $a };"
        + "local:a()",
        42,
        exists(Util.className(Itr.class) + "[. = '42']"));
  }

  /** Checks that the simple map operator prohibits inlining a context value into its RHS. */
  @Test public void gh1055() {
    inline(true);
    check("(let $d := for-each(1 to 100, function($a) { $a }) "
        + "return (1 to 2) ! $d)[. = 0]",
        "",
        exists(SingletonSeq.class));
    check("(let $d := for-each(1 to 11, identity#1) "
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
    inline(true);

    // deactivate inlining globally, activate locally
    check("declare option db:inlinelimit '0';"
        + "declare %basex:inline function local:x($x) { $x }; local:x(123)",
        123,
        empty(StaticFunc.class),
        exists(Itr.class));

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

    // locking flag: disable inlining
    check("declare %basex:lock('x') function local:x($x) { $x }; local:x(123)",
        123,
        exists(StaticFunc.class));
  }

  /** Tests if all let clauses are removed. */
  @Test public void funcTest() {
    check("let $a := 'foo' return 'bar' ! . ! $a", "foo", empty(Let.class));
  }

  /** Ensures that nondeterministic clauses are not reordered. */
  @Test public void ndtFuncTest() {
    inline(true);
    check("let $a := function($d) { trace($d) }"
        + "let $b := $a('1st') let $c := $a('2nd') "
        + "return $b", "1st",
        root(Pipeline.class),
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

  /** An error raised while inlining into a FLWOR clause is reported at compile time. */
  @Test public void flworClauseError() {
    inline(true);
    error("declare function local:f($s) { "
        + "for $i in (1 to 1000000)[. < 1] let $x := xs:integer($s) return $x };"
        + "count(local:f('z'))", FUNCCAST_X_X);
  }

  /** Tests inlining into the clauses of a FLWOR expression. */
  @Test public void flworClauses() {
    // count clause: merged into the positional variable of the for clause
    check("for $i in 1 to 3 count $c return $c", "1\n2\n3", root(RangeSeq.class));
    check("let $x := 2 for $i in 1 to 3 count $c where $i = $x return $c", 2,
        empty(Let.class), empty(Count.class));
    // count clause after an order by clause: no merge, inlining passes through the clause
    check("let $x := -1 for $i in (1 to 5)[. != 9] order by $i * $x count $c return $c",
        "1\n2\n3\n4\n5", empty(Let.class), exists(Count.class));

    // while clause
    check("let $x := 3 for $i in 1 to 6 while $i < $x return $i", "1\n2",
        empty(Let.class), exists(While.class));

    // trace clause
    check("let $x := 'v' for $i in 1 to 2 trace $x return $i", "1\n2", exists(Trace.class));

    // order by clause
    check("let $x := -1 for $i in (3, 1, 2) order by $i * $x return $i", "3\n2\n1",
        empty(Let.class), exists(OrderBy.class));

    // window clause
    check("let $x := 3 for tumbling window $w in (1 to 6) start at $s when $s mod $x = 1 "
        + "return sum($w)", "6\n15", empty(Let.class), exists(Window.class));
  }

  /** Checks that inlining a nested closure works properly. */
  @Test public void gh1424() {
    inline(true);
    check("declare function local:f() {"
        + "  let $func := function($key) { { $key: 'ok' }($key) }"
        + "  let $input := <ok/>"
        + "  let $call := $func(name($input))"
        + "  return function() { $call }"
        + "};"
        + "local:f()()",
        "ok",
        exists(DynFuncCall.class),
        empty(StaticFunc.class),
        root(DynFuncCall.class));
  }

  /** Checks that the type checks of inlined functions are merged. */
  @Test public void typeCheck() {
    inline(true);
    check("declare function local:a($e) as xs:string? { local:b($e) }; " +
        "declare function local:b($e) as xs:string? { $e }; local:a(" + wrap("X") + ")", "X",
        count(TypeCheck.class, 1));
    check("declare function local:a($e) as xs:string? { local:b($e) }; " +
        "declare function local:b($e) as xs:string* { $e }; local:a(" + wrap("X") + ")", "X",
        count(TypeCheck.class, 1));
    check("declare function local:a($e) as xs:string* { local:b($e) }; " +
        "declare function local:b($e) as xs:string? { $e }; local:a(" + wrap("X") + ")", "X",
        count(TypeCheck.class, 1));

    query("declare function local:f() as item()  { data([ <_/> ]) }; local:f()", "");
    query("declare function local:f() as item()? { data([ <_/> ]) }; local:f()", "");
    query("declare function local:f() as item()+ { data([ <_/> ]) }; local:f()", "");
    query("declare function local:f() as item()* { data([ <_/> ]) }; local:f()", "");

    query("declare function local:f($a) as item()  { data($a) }; local:f(<_/>)", "");
    query("declare function local:f($a) as item()? { data($a) }; local:f(<_/>)", "");
    query("declare function local:f($a) as item()+ { data($a) }; local:f(<_/>)", "");
    query("declare function local:f($a) as item()* { data($a) }; local:f(<_/>)", "");
  }
}
