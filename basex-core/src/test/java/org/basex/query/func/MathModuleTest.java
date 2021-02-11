package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.ast.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Math Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class MathModuleTest extends QueryPlanTest {
  /** Test method. */
  @Test public void crc32() {
    final Function func = _MATH_CRC32;
    query(func.args(" ()"), "");
    query(func.args(" ()"), "");
    query("string( " + func.args("") + ')', "00000000");
    query("string( " + func.args("BaseX") + ')', "4C06FC7F");
  }

  /** Test method. */
  @Test public void cosh() {
    final Function func = _MATH_COSH;
    query(func.args(" 0"), 1);
  }

  /** Test method. */
  @Test public void e() {
    final Function func = _MATH_E;
    query(func.args(), StrictMath.E);
  }

  /** Test method. */
  @Test public void pi() {
    final Function func = _MATH_PI;
    query(func.args(), StrictMath.PI);
  }

  /** Test method. */
  @Test public void pow() {
    final Function func = _MATH_POW;
    check(func.args(2, 2), 4, root(Dbl.class));

    check(func.args(" ()", " <_>1</_>"), "", empty());
    check(func.args(1, " <_>1</_>"), 1, root(Dbl.class));

    check(func.args(" <_>5</_>", 0), 1, root(Dbl.class));
    check(func.args(" <_>5</_>", 1), 5, root(Cast.class));
  }

  /** Test method. */
  @Test public void sinh() {
    final Function func = _MATH_SINH;
    query(func.args(" 0"), 0);
  }

  /** Test method. */
  @Test public void tanh() {
    final Function func = _MATH_TANH;
    query(func.args(" 0"), 0);
  }
}
