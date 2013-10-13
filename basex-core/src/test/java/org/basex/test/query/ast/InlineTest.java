package org.basex.test.query.ast;

import org.basex.query.expr.*;
import org.junit.*;

/**
 * Tests for inlining.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public class InlineTest extends QueryPlanTest {
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
    check("let $x := 1 ! 42 return switch(42) case 23 return $x case 84 return $x" +
        " case $x return 123 default return 1337", "123", "empty(//GFLWOR)");
    // $x is used twice
    check("let $x := 1 ! 42 return switch(23) case $x return 123 case 23 return $x" +
        " default return 1337", "42", "exists(//GFLWOR)");
  }

  /** Regression test for Issue GH-738, "switch with contains". */
  @Test public void gh738() {
    check("let $item:=<item>blah blah</item> " +
        "let $type:= switch (fn:true())" +
        "  case $item contains text \"blah\" return <type>a</type>" +
        "  default return ()" +
        "return $type", "<type>a</type>", "count(//Let) = 2");
  }
}
