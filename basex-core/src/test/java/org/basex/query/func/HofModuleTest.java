package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.ast.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.func.hof.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the functions of the Higher-Order Module.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Leo Woerteler
 */
public final class HofModuleTest extends QueryPlanTest {
  /** Test method. */
  @Test
  public void idTest() {
    query(_HOF_ID.args("()"), "");
    query(_HOF_ID.args("<x/>"), "<x/>");
    query("hof:id(1 to 10)", "1\n2\n3\n4\n5\n6\n7\n8\n9\n10");
  }

  /** Test method. */
  @Test
  public void constTest() {
    query(_HOF_CONST.args("(), error()"), "");
    query(_HOF_CONST.args("<x/>, 123"), "<x/>");
    query("hof:const(1 to 10, error('foo'))", "1\n2\n3\n4\n5\n6\n7\n8\n9\n10");
  }

  /** Test method. */
  @Test
  public void sortWithTest() {
    query("hof:sort-with((), function($a, $b) { $a < $b })", "");
    query("hof:sort-with(1 to 5, function($a, $b) { $a > $b })", "5\n4\n3\n2\n1");
    error("hof:sort-with(1 to 5, <x/>)", INVCAST_X_X_X);
  }

  /** Test method. */
  @Test
  public void foldLeft1Test() {
    query("hof:fold-left1(1 to 10, function($x, $y) { $x + $y })", "55");
    error("hof:fold-left1((), function($x, $y) { $x + $y })", EMPTYFOUND);

    // should be unrolled and evaluated at compile time
    final int limit = FnForEach.UNROLL_LIMIT;
    check("hof:fold-left1(1 to " + limit + ", function($a,$b) {$a+$b})",
        "55",
        "empty(//" + Util.className(HofFoldLeft1.class) + "[contains(@name, 'fold-left1')])",
        "exists(*/" + Util.className(Int.class) + ')');
    // should be unrolled but not evaluated at compile time
    check("hof:fold-left1(1 to " + limit + ", function($a,$b) {0*random:integer($a)+$b})",
        "10",
        "empty(//" + Util.className(HofFoldLeft1.class) + "[contains(@name, 'fold-left1')])",
        "empty(*/" + Util.className(Int.class) + ')',
        "count(//" + Util.className(Arith.class) + "[@op = '+']) eq 9");
    // should not be unrolled
    check("hof:fold-left1(1 to " + (limit + 1) + ", function($a,$b) {$a+$b})",
        "66",
        "exists(//" + Util.className(HofFoldLeft1.class) + "[contains(@name, 'fold-left1')])");
  }

  /** Test method. */
  @Test
  public void untilTest() {
    query("hof:until(function($x) { $x >= 1000 }, function($x) { $x * 2 }, 1)", "1024");
    query("hof:until(function($xs) {count($xs)>3}, function($x) {$x,$x}, 1)", "1\n1\n1\n1");
  }

  /** Test method. */
  @Test
  public void topKByTest() {
    query("hof:top-k-by(1 to 1000, function($x) {-$x}, 0)", "");
    query("hof:top-k-by((), function($x) {-$x}, 5)", "");
    query("hof:top-k-by(1 to 1000, function($x) {-$x}, 5)", "1\n2\n3\n4\n5");
  }

  /** Test method. */
  @Test
  public void topKWithTest() {
    query("hof:top-k-with(1 to 1000, function($x,$y) {$x > $y}, 0)", "");
    query("hof:top-k-with((), function($x,$y) {$x > $y}, 5)", "");
    query("hof:top-k-with(1 to 5, function($x,$y) {$x > $y}, 5)", "1\n2\n3\n4\n5");
  }
}
