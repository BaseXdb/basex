package org.basex.query.ast;

import org.basex.query.expr.*;
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
        String.format("<x/>%n<x/>"),
        "exists(//IterMap)",
        "empty(//Context)");
  }
}
