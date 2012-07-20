package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the functions of the {@link org.basex.query.func.FNHof} class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class FNHofTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void idTest() {
    query(_HOF_ID.args("()"), "");
    query(_HOF_ID.args("<x/>"), "<x/>");
    query("hof:id(1 to 10)", "1 2 3 4 5 6 7 8 9 10");
  }

  /** Test method. */
  @Test
  public void constTest() {
    query(_HOF_CONST.args("(), error()"), "");
    query(_HOF_CONST.args("<x/>, 123"), "<x/>");
    query("hof:const(1 to 10, error('foo'))", "1 2 3 4 5 6 7 8 9 10");
  }

  /** Test method. */
  @Test
  public void sortWithTest() {
    query("hof:sort-with(function($a, $b) { $a < $b }, ())", "");
    query("hof:sort-with(function($a, $b) { $a > $b }, 1 to 5)", "5 4 3 2 1");
    error("hof:sort-with(<x/>, 1 to 5)", Err.XPTYPE);
  }

  /** Test method. */
  @Test
  public void foldLeft1Test() {
    query("hof:fold-left1(function($x, $y) { $x + $y }, 1 to 10)", "55");
    error("hof:fold-left1(function($x, $y) { $x + $y }, ())", Err.XPTYPE);
  }

  /** Test method. */
  @Test
  public void untilTest() {
    query("hof:until(function($x) { $x >= 1000 }, function($x) { $x * 2 }, 1)", "1024");
    query("hof:until(function($xs) {count($xs)>3}, function($x) {$x,$x}, 1)", "1 1 1 1");
  }

  /** Test method. */
  @Test
  public void topKByTest() {
    query("hof:top-k-by(1 to 1000, function($x) {-$x}, 0)", "");
    query("hof:top-k-by((), function($x) {-$x}, 5)", "");
    query("hof:top-k-by(1 to 1000, function($x) {-$x}, 5)", "1 2 3 4 5");
  }

  /** Test method. */
  @Test
  public void topKWithTest() {
    query("hof:top-k-with(1 to 1000, function($x,$y) {$x > $y}, 0)", "");
    query("hof:top-k-with((), function($x,$y) {$x > $y}, 5)", "");
    query("hof:top-k-with(1 to 5, function($x,$y) {$x > $y}, 5)", "1 2 3 4 5");
  }
}
