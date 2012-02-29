package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.util.*;
import org.basex.test.query.AdvancedQueryTest;
import org.junit.Test;

/**
 * This class tests the functions of the {@link org.basex.query.func.FNHof} class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class FNHofTest extends AdvancedQueryTest {
  /** Test method for the {@code hof:id()} function. */
  @Test
  public void hofIdTest() {
    check(_HOF_ID);
    query(_HOF_ID.args("()"), "");
    query(_HOF_ID.args("<x/>"), "<x/>");
    query("hof:id(1 to 10)", "1 2 3 4 5 6 7 8 9 10");
  }

  /** Test method for the {@code hof:const()} function. */
  @Test
  public void hofConstTest() {
    check(_HOF_CONST);
    query(_HOF_CONST.args("(), error()"), "");
    query(_HOF_CONST.args("<x/>, 123"), "<x/>");
    query("hof:const(1 to 10, error('foo'))", "1 2 3 4 5 6 7 8 9 10");
  }

  /** Test method for the {@code hof:sort-with()} function. */
  @Test
  public void hofSortWithTest() {
    check(_HOF_SORT_WITH);
    query("hof:sort-with(function($a, $b) { $a < $b }, ())", "");
    query("hof:sort-with(function($a, $b) { $a > $b }, 1 to 5)", "5 4 3 2 1");
    error("hof:sort-with(<x/>, 1 to 5)", Err.XPTYPE);
  }

  /** Test method for the {@code hof:fold-left1()} function. */
  @Test
  public void hofFoldLeft1Test() {
    check(_HOF_FOLD_LEFT1);
    query("hof:fold-left1(function($x, $y) { $x + $y }, 1 to 10)", "55");
    error("hof:fold-left1(function($x, $y) { $x + $y }, ())", Err.XPTYPE);
  }

  /** Test method for the {@code hof:until()} function. */
  @Test
  public void hofUntilTest() {
    check(_HOF_UNTIL);
    query("hof:until(function($x) { $x >= 1000 }, function($x) { $x * 2 }, 1)", "1024");
    query("hof:until(function($xs) {count($xs)>3}, function($x) {$x,$x}, 1)", "1 1 1 1");
  }

  /** Test method for the {@code hof:top-k-by()} function. */
  @Test
  public void hofTopKByTest() {
    check(_HOF_TOP_K_BY);
    query("hof:top-k-by(function($x) {-$x}, 0, 1 to 1000)", "");
    query("hof:top-k-by(function($x) {-$x}, 5, ())", "");
    query("hof:top-k-by(function($x) {-$x}, 5, 1 to 1000)", "1 2 3 4 5");
  }

  /** Test method for the {@code hof:top-k-with()} function. */
  @Test
  public void hofTopKWithTest() {
    check(_HOF_TOP_K_WITH);
    query("hof:top-k-with(function($x,$y) {$x > $y}, 0, 1 to 1000)", "");
    query("hof:top-k-with(function($x,$y) {$x > $y}, 5, ())", "");
    query("hof:top-k-with(function($x,$y) {$x > $y}, 5, 1 to 5)", "1 2 3 4 5");
  }
}
