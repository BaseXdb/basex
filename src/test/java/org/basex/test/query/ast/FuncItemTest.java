package org.basex.test.query.ast;

import org.junit.Test;

/**
 * Tests for compiling function items.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class FuncItemTest extends QueryPlanTest {
  /** Checks if the identity function is pre-compiled. */
  @Test
  public void idTest() {
    check("function($x) { $x }(42)",
        "42",
        "exists(//FuncItem)"
    );
  }

  /** Checks if a function literal is pre-compiled. */
  @Test
  public void literalTest() {
    check("lower-case#1('FooBar')",
        "foobar",
        "exists(//FuncItem)"
    );
  }

  /** Checks if a partial application is pre-compiled. */
  @Test
  public void partAppTest() {
    check("starts-with('foobar', ?)('foo')",
        "true",
        "exists(//FuncItem)"
    );
  }

  /** Checks if a partial application with non-empty closure is left alone. */
  @Test
  public void partApp2Test() {
    check("for $sub in ('foo', 'bar')" +
        "return starts-with(?, $sub)('foobar')",
        "true false",
        "empty(//FuncItem)",
        "exists(//InlineFunc)"
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
        "exists(//DynamicFunc)",
        "every $f in outermost(//DynFuncCall)/* satisfies" +
        "  $f instance of element(FuncItem)"
    );
  }

  /** Checks if {@code fold-left1(...)} can be used. */
  @Test
  public void foldLeft1Test() {
    check("hof:fold-left1(function($a, $b) { max(($a, $b)) }, 1 to 42)",
        "42"
    );
  }

  /** Checks if statically unused functions are compiled at runtime. */
  @Test
  public void compStatUnusedTest() {
    check("declare function local:foo() { abs(?) };" +
        "function-lookup(xs:QName('local:foo'), 0)()(-42)",
        "42",
        "exists(//PartFunc)"
    );
  }
}
