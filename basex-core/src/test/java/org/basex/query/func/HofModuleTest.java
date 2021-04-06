package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.ast.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Higher-Order Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class HofModuleTest extends QueryPlanTest {
  /** Resets optimizations. */
  @AfterEach public void init() {
    inline(false);
    unroll(false);
  }

  /** Test method. */
  @Test public void constTest() {
    final Function func = _HOF_CONST;
    query(func.args(" (), error()"), "");
    query(func.args(" <x/>, 123"), "<x/>");
    query(func.args(" 1 to 10", " error('foo')"), "1\n2\n3\n4\n5\n6\n7\n8\n9\n10");
  }

  /** Test method. */
  @Test public void dropWhile() {
    final Function func = _HOF_DROP_WHILE;
    query(func.args(" (1)", " function($x) { $x < 2 }"), "");
    query(func.args(" (1, 2)", " function($x) { $x < 2 }"), 2);
    query(func.args(" (1, 2, 3)", " function($x) { $x < 2 }"), "2\n3");
    query(func.args(" (1, 2, 3)", " function($x) { $x < 2 }") + " => sort()", "2\n3");
    query(func.args(" (8 to 10)", " function($x) { $x < 10 }") + " => sort()", 10);
    query(func.args(" (8 to 10)[. > 9]", " function($x) { $x < 9 }") + " => sort()", 10);
    query(func.args(" (8 to 10)", " function($x) { $x < 10 }") + " => count()", 1);
  }

  /** Test method. */
  @Test public void foldLeft1Test() {
    final Function func = _HOF_FOLD_LEFT1;
    query(func.args(" 1 to 10", " function($x, $y) { $x + $y }"), 55);
    error(func.args(" ()", " function($x, $y) { $x + $y }"), EMPTYFOUND);

    // should not be unrolled
    check(func.args(" 1 to 6", " function($a, $b) { $a + $b }"),
        21,
        exists(func));

    // should be unrolled and evaluated at compile time
    unroll(true);
    check(func.args(" 1 to 5", " function($a, $b) { $a + $b }"),
        15,
        empty(func),
        exists(Int.class));
    // should be unrolled but not evaluated at compile time
    check(func.args(" 1 to 5", " function($a, $b) { 0 * random:double() + $b }"),
        5,
        exists(Int.class),
        empty(func),
        count(Util.className(Arith.class) + "[@op = '+']", 4));
  }

  /** Test method. */
  @Test public void idTest() {
    final Function func = _HOF_ID;
    query(func.args(" ()"), "");
    query(func.args(" <x/>"), "<x/>");
    query(func.args(" 1 to 10"), "1\n2\n3\n4\n5\n6\n7\n8\n9\n10");
  }

  /** Test method. */
  @Test public void sortWithTest() {
    final Function func = _HOF_SORT_WITH;
    query(func.args(" ()", " function($a, $b) { $a < $b }"), "");
    query(func.args(" 1 to 5", " function($a, $b) { $a > $b }"), "5\n4\n3\n2\n1");
    error(func.args(" 1 to 5", " <x/>"), INVTYPE_X_X_X);
  }

  /** Test method. */
  @Test public void topKByTest() {
    final Function func = _HOF_TOP_K_BY;
    query(func.args(" 1 to 1000", " function($x) { -$x }, 0"), "");
    query(func.args(" ()", " function($x) { -$x }", 5), "");
    query(func.args(" 1 to 1000", " function($x) { -$x }", 5), "1\n2\n3\n4\n5");
  }

  /** Test method. */
  @Test public void takeWhile() {
    final Function func = _HOF_TAKE_WHILE;
    query(func.args(" (1)", " function($x) { $x < 2 }"), 1);
    query(func.args(" (1, 2)", " function($x) { $x < 2 }"), 1);
    query(func.args(" (1, 2, 3)", " function($x) { $x < 3 }"), "1\n2");
    query(func.args(" (1, 2, 3)", " function($x) { $x < 3 }") + " => sort()", "1\n2");
    query(func.args(" (1 to 10)", " function($x) { $x < 3 }") + " => sort()", "1\n2");
    query(func.args(" (1 to 10)[. > 1]", " function($x) { $x < 3 }") + " => sort()", 2);
    query(func.args(" (1 to 10)", " function($x) { $x < 3 }") + " => count()", 2);
  }

  /** Test method. */
  @Test public void topKWithTest() {
    final Function func = _HOF_TOP_K_WITH;
    query(func.args(" 1 to 1000", " function($x, $y) { $x > $y }", 0), "");
    query(func.args(" ()", " function($x, $y) { $x > $y }", 5), "");
    query(func.args(" 1 to 5", " function($x, $y) { $x > $y }", 5), "1\n2\n3\n4\n5");
  }

  /** Test method. */
  @Test public void untilTest() {
    final Function func = _HOF_UNTIL;
    query(func.args(" function($x) { $x >= 1000 }, function($x) { $x * 2 }, 1"), 1024);
    query(func.args(" function($xs) { count($xs) > 3 }, function($x) { $x, $x }, 1"), "1\n1\n1\n1");
  }
}
