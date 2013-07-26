package org.basex.test.query.ast;

import org.basex.query.expr.*;
import org.junit.*;

/**
 * Tests for inlining.
 *
 * @author BaseX Team 2005-12, BSD License
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
}
