package org.basex.query.simple;

import static org.basex.query.QueryError.*;

import org.basex.query.ast.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.junit.jupiter.api.*;

/**
 * Arithmetic tests.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ArithTest extends QueryPlanTest {
  /** Test method. */
  @Test public void plus() {
    check("for $i in (1 to 2)[. != 0] return ($i * $i) + 1", "2\n5", exists(Arith.class));

    // neutral number
    check("for $i in (1 to 2)[. != 0] return $i + 0",
        "1\n2", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 0 + $i",
        "1\n2", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 0e0 + $i",
        "1\n2", exists(Cast.class));
    check("for $i in (1 to 2)[. != 0] return $i + 0e0",
        "1\n2", exists(Cast.class));

    // counts
    check("let $n := <_>1</_>[. = 1] return count($n) + count($n)",
        2, count(Function.COUNT, 1), exists(Function._UTIL_REPLICATE));
    check("let $n := <_>1</_>[. = 1] return count($n) + count($n) > 0",
        true, root(CmpSimpleG.class));
  }

  /** Test method. */
  @Test public void minus() {
    check("for $i in (1 to 2)[. != 0] return ($i * $i) - 1", "0\n3", exists(Arith.class));

    // neutral number
    check("for $i in (1 to 2)[. != 0] return $i - 0",
        "1\n2", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return $i - 0e0",
        "1\n2", exists(Cast.class));
    check("for $i in (1 to 2)[. != 0] return 0 - $i",
        "-1\n-2", exists(Unary.class));

    // identical arguments
    check("for $i in (1, xs:double('NaN'))[. != 0] return $i - $i",
        "0\nNaN", exists(Arith.class));
    check("for $i in (1 to 2)[. != 0] return $i - $i",
        "0\n0", empty(Arith.class), empty(GFLWOR.class));

    query("string(xs:dateTime('2017-07-07T18:30:00.1') - xs:dayTimeDuration('PT1S'))",
        "2017-07-07T18:29:59.1");
    query("string(xs:dateTime('2017-07-07T18:00:59.1') - xs:dayTimeDuration('PT1M'))",
        "2017-07-07T17:59:59.1");
  }

  /** Test method. */
  @Test public void mult() {
    check("for $i in (1 to 2)[. != 0] return $i * 2", "2\n4", exists(Arith.class));

    // neutral number
    check("for $i in (1 to 2)[. != 0] return $i * 1",
        "1\n2", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 1 * $i",
        "1\n2", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 1e0 * $i",
        "1\n2", exists(Cast.class));
    check("for $i in (1 to 2)[. != 0] return $i * 1e0",
        "1\n2", exists(Cast.class));

    // absorbing number
    check("for $i in (1 to 2)[. != 0] return $i * 0",
        "0\n0", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 0 * $i",
        "0\n0", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 0e0 * $i",
        "0\n0", exists(Arith.class));
    check("for $i in (1 to 2)[. != 0] return $i * 0e0",
        "0\n0", exists(Arith.class));
  }

  /** Test method. */
  @Test public void div() {
    check("for $i in (2, 4)[. != 0] return $i div 2", "1\n2", exists(Arith.class));
    check("for $i in (2, 4)[. != 0] return 1 div $i", "0.5\n0.25", exists(Arith.class));

    // neutral number
    check("for $i in (2.0, 4.0) return $i div 1", "2\n4", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (2e0, 4e0) return $i div 1", "2\n4", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (2, 4)[. != 0] return $i div 1e0", "2\n4", exists(Cast.class));

    // identical arguments
    check("for $i in (1, xs:double('NaN'))[. != 0] return $i div $i",
        "1\nNaN", exists(Arith.class));
    check("for $i in (2.0, 4.0)[. != 0] return $i div $i", "1\n1",
        empty(Arith.class), empty(GFLWOR.class));

    error("xs:dayTimeDuration('PT0S') div xs:dayTimeDuration('PT0S')", DIVZERO_X);
    error("xs:yearMonthDuration('P0M') div xs:yearMonthDuration('P0M')", DIVZERO_X);
  }

  /** Test method. */
  @Test public void idiv() {
    check("for $i in (2, 4)[. != 0] return $i idiv 2", "1\n2", exists(Arith.class));
    check("for $i in (2, 4)[. != 0] return 1 idiv $i", "0\n0", exists(Arith.class));

    // neutral number
    check("for $i in (2, 4) return $i idiv 1e0", "2\n4", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (2, 4) return $i idiv 1", "2\n4", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (2, 4) return $i idiv 1", "2\n4", empty(Arith.class), empty(GFLWOR.class));

    // identical arguments
    error("for $i in (1, xs:double('NaN')) return $i idiv $i", DIVFLOW_X);
    check("for $i in (2, 4) return $i idiv $i", "1\n1", empty(Arith.class), empty(GFLWOR.class));
  }

  /** Test method. */
  @Test public void mod() {
    check("for $i in (-1, 0, 1) return $i mod 1", "0\n0\n0", empty(Arith.class));
  }

  /** Merge arithmetic expressions. */
  @Test public void gh1938() {
    check("(<_>1</_> + 1 - 1)[. instance of xs:double]", 1, root(Cast.class));
    check("<_>6</_> div 3 * 2", 4, count(Arith.class, 1));
    check("(1 to 2) ! (<_>1</_> + . - .)", "1\n1", empty(Arith.class), exists(Cast.class));
    check("(1 to 2) ! (<_>{ . }</_> + . - .)", "1\n2", empty(Arith.class), exists(Cast.class));
  }
}
