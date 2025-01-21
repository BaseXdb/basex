package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Higher-Order Module.
 *
 * @author BaseX Team, BSD License
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
    query(func.args(" ()", " function($x, $y) { $x + $y }"), "");

    // should not be unrolled
    check(func.args(" 1 to 6", " function($a, $b) { $a + $b }"),
        21,
        exists(func));
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
