package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.query.expr.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Bit Module.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BitModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void and() {
    final Function func = _BIT_AND;
    query(func.args(" ()", 1), "");
    query(func.args(1, " ()"), "");
    query(func.args(12, 10), 8);
    query(func.args(-1, 255), 255);
    query(func.args(-8, 15), 8);
    query(func.args(" <_>6</_>", 7), 6);
    error(func.args(1.5, 1), INVTYPE_X);
    error(func.args(" (1, 2)", 1), INVTYPE_X);

    check(func.args(INTEGER, -1), 5, root(Cast.class));
    check(func.args(-1, INTEGER), 5, root(Cast.class));
    check(func.args(" xs:byte(" + wrap(5) + ")", -1), 5, root(func));
    check(func.args(wrap(5), -1), 5, root(func));
    check(func.args(INTEGER, 0), 0, root(func));
    check(func.args(INTEGER, wrap(5)), 5, root(func), type(func, "xs:integer"));
  }

  /** Integer-typed argument that will not be pre-evaluated. */
  private static final String INTEGER = " xs:integer(" + wrap(5) + ")";

  /** Test method. */
  @Test public void count() {
    final Function func = _BIT_COUNT;
    query(func.args(" ()"), "");
    query(func.args(0), 0);
    query(func.args(255), 8);
    query(func.args(-1), 64);
    query(func.args(" -9223372036854775807 - 1"), 1);
  }

  /** Test method. */
  @Test public void not() {
    final Function func = _BIT_NOT;
    query(func.args(" ()"), "");
    query(func.args(0), -1);
    query(func.args(-1), 0);
    query(func.args(5), -6);

    check(func.args(" " + func.args(INTEGER)), 5, root(Cast.class));
    check(func.args(" " + func.args(wrap(5))), 5, root(func));
  }

  /** Test method. */
  @Test public void or() {
    final Function func = _BIT_OR;
    query(func.args(" ()", 1), "");
    query(func.args(12, 10), 14);
    query(func.args(-8, 3), -5);

    check(func.args(INTEGER, 0), 5, root(Cast.class));
    check(func.args(INTEGER, -1), -1, root(func));
  }

  /** Test method. */
  @Test public void rotate() {
    final Function func = _BIT_ROTATE;
    query(func.args(" ()", 1, 8), "");
    query(func.args(1, 1, 8), 2);
    query(func.args(128, 1, 8), 1);
    query(func.args(1, -1, 8), 128);
    query(func.args(1, 9, 8), 2);
    query(func.args(1, -9, 8), 128);
    query(func.args(3, 0, 8), 3);
    query(func.args(-1, 3, 8), 255);
    query(func.args(256, 1, 8), 0);
    query(func.args(1, 31, 32), 2147483648L);
    query(func.args(1, -1, 64), Long.MIN_VALUE);
    query(func.args(-2, 1, 64), -3);
    query(func.args(1, 1, 1), 1);
    query(func.args(1, 1, 0), 0);
    query(func.args(1, 0, 0), 0);
    query(func.args(INTEGER, 0, wrap(0)), 0);
    error(func.args(1, 1, -1), BIT_SIZE_X);
    error(func.args(1, 1, 65), BIT_SIZE_X);
    error(func.args(1, 0, 65), BIT_SIZE_X);

    check(func.args(INTEGER, 0, 8), 5, root(_BIT_UNSIGNED));
    check(func.args(INTEGER, 0, 64), 5, root(Cast.class));
    check(func.args(INTEGER, 8, 8), 5, root(func));
  }

  /** Test method. */
  @Test public void shift() {
    final Function func = _BIT_SHIFT;
    query(func.args(" ()", 1), "");
    query(func.args(1, 0), 1);
    query(func.args(1, 4), 16);
    query(func.args(16, -4), 1);
    query(func.args(-16, -2), -4);
    query(func.args(1, 63), Long.MIN_VALUE);
    query(func.args(1, 64), 0);
    query(func.args(1, 1000), 0);
    query(func.args(-1, -63), -1);
    query(func.args(-1, -64), -1);
    query(func.args(Long.MAX_VALUE, -1000), 0);
    query(func.args(1, " -9223372036854775807 - 1"), 0);

    check(func.args(INTEGER, 0), 5, root(Cast.class));
    check(func.args(wrap(5), 0), 5, root(func));
  }

  /** Test method. */
  @Test public void test() {
    final Function func = _BIT_TEST;
    query(func.args(" ()", 0), "");
    query(func.args(5, 0), true);
    query(func.args(5, 1), false);
    query(func.args(5, 2), true);
    query(func.args(Long.MIN_VALUE, 63), true);
    query(func.args(5, 64), false);
    query(func.args(-5, 64), true);
    query(func.args(-5, 1000), true);
    error(func.args(5, -1), INVTYPE_X);
  }

  /** Test method. */
  @Test public void unsigned() {
    final Function func = _BIT_UNSIGNED;
    query(func.args(" ()", 8), "");
    query(func.args(-1, 8), 255);
    query(func.args(-1, 32), 4294967295L);
    query(func.args(-1, 63), Long.MAX_VALUE);
    query(func.args(-1, 64), -1);
    query(func.args(300, 8), 44);
    query(func.args(5, 1), 1);
    query(func.args(1, 0), 0);
    query(func.args(-1, 0), 0);
    query(func.args(-1, 65), -1);
    query(func.args(-1, Long.MAX_VALUE), -1);
    error(func.args(1, -1), BIT_SIZE_X);

    check(func.args(INTEGER, 64), 5, root(Cast.class));
    check(func.args(INTEGER, 1000), 5, root(Cast.class));
    check(func.args(" " + func.args(INTEGER, 1000), 8), 5, count(func, 1), "//Itr = 8");
    check(func.args(wrap(5), 64), 5, root(func));
    check(func.args(" " + func.args(INTEGER, 16), 8), 5, count(func, 1), "//Itr = 8");
    check(func.args(" " + func.args(INTEGER, 8), 16), 5, count(func, 1), "//Itr = 8");
    check(func.args(" " + func.args(INTEGER, 0), 8), 0, count(func, 1), "//Itr = 0");
    error(func.args(" " + func.args(INTEGER, -1), 8), BIT_SIZE_X);
  }

  /** Test method. */
  @Test public void xor() {
    final Function func = _BIT_XOR;
    query(func.args(" ()", 1), "");
    query(func.args(12, 10), 6);
    query(func.args(-1, 5), -6);

    check(func.args(0, INTEGER), 5, root(Cast.class));
  }
}
