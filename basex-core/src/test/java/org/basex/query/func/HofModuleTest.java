package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Higher-Order Module.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Leo Woerteler
 */
public final class HofModuleTest extends SandboxTest {
  /** Resets optimizations. */
  @AfterEach public void init() {
    inline(false);
    unroll(false);
  }

  /** Test method. */
  @Test public void foldLeft1() {
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
    check(func.args(" 1 to 5", " function($a, $b) { 0 * " + _RANDOM_DOUBLE.args() + " + $b }"),
        5,
        exists(Int.class),
        empty(func),
        count(Util.className(Arith.class) + "[@op = '+']", 4));
  }

  /** Test method. */
  @Test public void sortWith() {
    final Function func = _HOF_SORT_WITH;
    query(func.args(" ()", " function($a, $b) { $a < $b }"), "");
    query(func.args(" 1 to 5", " function($a, $b) { $a > $b }"), "5\n4\n3\n2\n1");
    error(func.args(" 1 to 5", " <x/>"), INVCONVERT_X_X_X);
    error(func.args(" 1 to 5", wrap("")), INVCONVERT_X_X_X);
  }

  /** Test method. */
  @Test public void topKBy() {
    final Function func = _HOF_TOP_K_BY;
    query(func.args(" 1 to 1000", " function($x) { -$x }, 0"), "");
    query(func.args(" ()", " function($x) { -$x }", 5), "");
    query(func.args(" 1 to 1000", " function($x) { -$x }", 5), "1\n2\n3\n4\n5");
  }

  /** Test method. */
  @Test public void topKWith() {
    final Function func = _HOF_TOP_K_WITH;
    query(func.args(" 1 to 1000", " function($x, $y) { $x > $y }", 0), "");
    query(func.args(" ()", " function($x, $y) { $x > $y }", 5), "");
    query(func.args(" 1 to 5", " function($x, $y) { $x > $y }", 5), "1\n2\n3\n4\n5");
  }
}
