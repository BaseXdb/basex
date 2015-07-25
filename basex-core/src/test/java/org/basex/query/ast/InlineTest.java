package org.basex.query.ast;

import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Tests for inlining.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class InlineTest extends QueryPlanTest {
  /** Tests if inlining works in {@link Arith} expressions. */
  @Test public void plusTest() {
    check("let $x := 21 return $x + 21", "42", "empty(//GFLWOR)");
    check("let $x := 21 return 21 + $x", "42", "empty(//GFLWOR)");
    check("let $x := 21 return $x + $x", "42", "empty(//GFLWOR)");

    check("let $x := <x>21</x> return $x + 21", "42", "exists(//GFLWOR)");
  }

  /** Tests if variable uses in {@link Switch} are counted correctly. */
  @Test public void switchTest() {
    // all paths use $x only once
    check("let $x := 42 return switch(42) case 23 return $x case 84 return $x" +
        " case $x return 123 default return 1337", "123", "empty(//GFLWOR)");
    // $x is used twice
    check("let $x := <x/> ! 42 return switch(23) case $x return 123 case 23 return $x" +
        " default return 1337", "42", "exists(//GFLWOR)");
  }

  /** Regression test for Issue GH-738, "switch with contains". */
  @Test public void gh738() {
    check("let $item:=<item>blah blah</item> " +
        "let $type:= switch (fn:true())" +
        "  case $item contains text \"blah\" return <type>a</type>" +
        "  default return ()" +
        "return $type",
        "<type>a</type>",
        "count(//Let) = 2");
  }

  /** Regression test for Issue GH-849, "Typing and Function items: XPTY0004". */
  @Test public void gh849() {
    check("let $f := function($s as xs:string) { $s }" +
        "return $f(let $x := <x>1</x> return if($x = 1.1) then () else 'x')",
        "x",
        "exists(//Str)");
  }

  /**
   * Tests if variables directly inside an FTDistanceExpr are correctly inlined.
   */
  @Test public void gh907() {
    check("let $n := 0 return 'x y' contains text 'x y' distance exactly $n paragraphs",
        "true",
        "empty(//GFLWOR)",
        "empty(//Var)");
  }

  /** Checks if forward-referencing function literals are inlined. */
  @Test public void gh1052() {
    check("declare function local:a() { local:b#1(42) };"
        + "declare function local:b($a) { $a };"
        + "local:a()",
        "42",
        "exists(/*/" + Util.className(Int.class) + "[@value = '42'])");

    check("declare function local:a() { local:b(?)(42) };"
        + "declare function local:b($a) { $a };"
        + "local:a()",
        "42",
        "exists(/*/" + Util.className(Int.class) + "[@value = '42'])");

    check("declare function local:a() { local:b#1(?)(42) };"
        + "declare function local:b($a) { $a };"
        + "local:a()",
        "42",
        "exists(/*/" + Util.className(Int.class) + "[@value = '42'])");
  }

  /** Checks that the simple map operator prohibits inlining a context item into its RHS. */
  @Test public void gh1055() {
    check("let $d := for-each(1 to 100, function($a) { $a }) "
        + "return count((1 to 2) ! $d)",
        "200",
        "exists(//Let)");
    check("let $d := for-each(1 to 10, function($a) { $a }) return count((1 to 2) ! $d[1])",
        "2",
        "exists(//CachedMap)");
    check("for $x in (<x/>, <x/>) where (1, 2) ! $x return $x",
        "<x/>\n<x/>",
        "exists(//IterMap)",
        "empty(//Context)");
  }

  /** Simple map operator. */
  @Test public void gh1094() {
    check("for $d in (true(), false()) where <a/>!<b/>!$d return $d", "true", "exists(//Where)");
    check("let $a := <a/> return 'bar' ! . ! $a", "<a/>", "exists(//Let)");
  }

  /**
   * Tests the annotation {@link Annotation#_BASEX_INLINE}.
   */
  @Test public void annotation() {
    // deactivate inlining globally, activate locally
    check("declare option db:inlinelimit '0';"
        + "declare %basex:inline function local:x($x) { $x }; local:x(123)",
        "123",
        "empty(//StaticFunc)",
        "exists(//Int)");

    // deactivate inlining globally and locally
    check("declare option db:inlinelimit '0';"
        + "declare %basex:inline(0) function local:x($x) { $x }; local:x(123)",
        "123",
        "exists(//StaticFunc)");

    // activate inlining globally, deactivate locally
    check("declare option db:inlinelimit '1000';"
        + "declare %basex:inline(0) function local:x($x) { $x }; local:x(123)",
        "123",
        "exists(//StaticFunc)");
  }

  /** Tests if all let clauses are removed. */
  @Test public void funcTest() {
    check("let $a := function($a) { trace($a) }"
        + "let $b := $a(1) let $c := $a(1) let $d := $a(1) return $b", "1", "count(//Let) != 2");
    check("let $a := 'foo' return 'bar' ! . ! $a", "foo", "empty(//Let)");
  }

  /** Checks that window clauses are recognized as loops. */
  @Test public void gh1126() {
    check("let $s := (1 to 2) ! . "
        + "for tumbling window $w in 1 to 2 start when true() end when true() return $s",
        "1\n2\n1\n2",
        "count(//Let) eq 1",
        "count(//Window) eq 1",
        "//Let << //Window");
  }
}
