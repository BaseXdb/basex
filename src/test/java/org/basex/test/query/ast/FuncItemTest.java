package org.basex.test.query.ast;

import org.junit.Test;

/**
 * Tests for compiling function items.
 * @author Leo Woerteler
 */
public class FuncItemTest extends QueryPlanTest {

  /** Checks if the identity function is precompiled. */
  @Test public void idTest() {
    check("function($x) { $x }(42)",
        "42",
        "exists(//FuncItem)"
    );
  }

  /** Checks if a function literal is precompiled. */
  @Test public void literalTest() {
    check("lower-case#1('FooBar')",
        "foobar",
        "exists(//FuncItem)"
    );
  }

  /** Checks if a partial application is precompiled. */
  @Test public void partAppTest() {
    check("starts-with('foobar', ?)('foo')",
        "true",
        "exists(//FuncItem)"
    );
  }

  /** Checks if a partial application with non-empty closure is left alone. */
  @Test public void partApp2Test() {
    check("for $sub in ('foo', 'bar')" +
        "return starts-with(?, $sub)('foobar')",
        "true false",
        "empty(//FuncItem)",
        "exists(//InlineFunc)"
    );
  }

  /** Checks that the Y combinator is precompiled. */
  @Test public void yCombinatorTest() {
    check("function($f) {" +
        "  let $loop := function($x) { $f(function() { $x($x) }) }" +
        "  return $loop($loop)" +
        "}(function($f) { 42 })",
        "42",
        // both outer inline functions are precompiled
        "exists(//DynFuncCall)",
        "every $f in outermost(//DynFuncCall)/* satisfies" +
        "  $f instance of element(FuncItem)"
    );
  }
}
