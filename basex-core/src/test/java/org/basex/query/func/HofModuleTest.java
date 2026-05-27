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
    // a non-empty input may still yield an empty result (k < 1): the type must allow zero
    query("empty(" + func.args(" (1, (2 to 1000)[. > 0])", " function($x) { -$x }", 0) + ")", true);
  }

  /** Test method. */
  @Test public void topKWith() {
    final Function func = _HOF_TOP_K_WITH;
    query(func.args(" 1 to 1000", " function($x, $y) { $x > $y }", 0), "");
    query(func.args(" ()", " function($x, $y) { $x > $y }", 5), "");
    query(func.args(" 1 to 5", " function($x, $y) { $x > $y }", 5), "1\n2\n3\n4\n5");
    // a non-empty input may still yield an empty result (k < 1): the type must allow zero
    query("empty(" + func.args(" (1, (2 to 1000)[. > 0])",
        " function($x, $y) { $x > $y }", 0) + ")", true);
  }

  /** Test method. */
  @Test public void scanLeft() {
    final Function func = _HOF_SCAN_LEFT;
    query(func.args(" 1 to 3", 0, " function($a, $b) { $a + $b }"), "0\n1\n3\n6");
    query(func.args(" ()", 5, " function($a, $b) { $a + $b }"), 5);
    // a statically-empty (non-literal) input is optimized away, side-effects preserved
    check(func.args(" void(<a/>)", 5, " function($a, $b) { $a + $b }"), 5, empty(func));
    // result type derives from $zero and $action, not from the input
    query(func.args(" (1 to 3)[. > 0]", "x", " function($a, $b) { $a }")
        + " instance of xs:string+", true);
  }
}
