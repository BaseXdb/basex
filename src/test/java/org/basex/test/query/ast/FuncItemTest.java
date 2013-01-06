package org.basex.test.query.ast;

import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.junit.*;

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
        "exists(//" + Util.name(FuncItem.class) + ")"
    );
  }

  /** Checks if a function literal is pre-compiled. */
  @Test
  public void literalTest() {
    check("lower-case#1('FooBar')",
        "foobar",
        "exists(//" + Util.name(FuncItem.class) + ")"
    );
  }

  /** Checks if a partial application is pre-compiled. */
  @Test
  public void partAppTest() {
    check("starts-with('foobar', ?)('foo')",
        "true",
        "exists(//" + Util.name(FuncItem.class) + ")"
    );
  }

  /** Checks if a partial application with non-empty closure is left alone. */
  @Test
  public void partApp2Test() {
    check("for $sub in ('foo', 'bar')" +
        "return starts-with(?, $sub)('foobar')",
        "true false",
        "empty(//" + Util.name(FuncItem.class) + ")",
        "exists(//" + Util.name(PartFunc.class) + ")"
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
        "exists(//" + Util.name(DynamicFunc.class) + ")",
        "every $f in outermost(//" + Util.name(DynamicFunc.class) + ")/* satisfies" +
        "  $f instance of element(" + Util.name(FuncItem.class) + ")"
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
        "exists(//" + Util.name(PartFunc.class) + ")"
    );
  }

  /**
   * Checks if statically used functions are compiled at compile time.
   * Added because of issue GH-382.
   */
  @Test
  public void compStatUsedTest() {
    check("declare function local:a() { local:b() };" +
        "declare function local:b() { 42 };" +
        "local:a#0()",
        "42",
        "exists(//" + Util.name(FuncItem.class) + ")"
    );
  }

  /** Checks for circular references leading to stack overflows. */
  @Test
  public void noLoopTest() {
    check("declare function local:Y($f) { $f(function() { $f }) };" +
        "let $f := local:Y(function($x) { $x() }) return $f[2]",
        "",
        "exists(//" + Util.name(FuncItem.class) + ")"
    );
  }

  /** Checks for circular references leading to stack overflows. */
  @Test
  public void noLoopTest2() {
    check("declare function local:Y($f) { $f(function() { $f }) };" +
        "map(local:Y#1, function($x) { $x() })[2]",
        "",
        "exists(//" + Util.name(FuncItem.class) + ")"
    );
  }

  /** Checks for circular references leading to stack overflows. */
  @Test
  public void noLoopTest3() {
    check("declare function local:Y($f) { $f(function() { $f }) };" +
        "let $f := map(local:Y#1, function($x) { $x() }) return $f[2]",
        "",
        "exists(//" + Util.name(FuncItem.class) + ")"
    );
  }

  /** Checks for circular references leading to stack overflows. */
  @Test
  public void noLoopTest4() {
    check("declare function local:foo($x) { function($f) { $f($x) } };" +
        "declare function local:bar($f) { $f(function($_) { $f }) };" +
        "let $a := local:foo(local:foo(function($e) { $e() })) " +
        "let $b := local:bar($a) " +
        "return $b[2]",
        "",
        "exists(//" + Util.name(FuncItem.class) + ")"
    );
  }

  /** Checks for circular references leading to stack overflows. */
  @Test
  public void noLoopTest5() {
    check("declare function local:foo($f) { $f($f) };" +
        "let $id := local:foo(function($g) { $g })" +
        "return $id(42)",
        "42",
        "exists(//" + Util.name(FuncItem.class) + ")"
    );
  }
}
