package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Math Module.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class MathModuleTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void crc32() {
    final Function func = _MATH_CRC32;
    query(func.args(" ()"), "");
    query(func.args(" ()"), "");
    query("string( " + func.args("") + ')', "00000000");
    query("string( " + func.args("BaseX") + ')', "4C06FC7F");
  }

  /** Test method. */
  @Test
  public void pi() {
    final Function func = _MATH_PI;
    query(func.args(), StrictMath.PI);
  }

  /** Test method. */
  @Test
  public void e() {
    final Function func = _MATH_E;
    query(func.args(), StrictMath.E);
  }

  /** Test method. */
  @Test
  public void sinh() {
    final Function func = _MATH_SINH;
    query(func.args(" 0"), 0);
  }

  /** Test method. */
  @Test
  public void cosh() {
    final Function func = _MATH_COSH;
    query(func.args(" 0"), 1);
  }

  /** Test method. */
  @Test
  public void tanh() {
    final Function func = _MATH_TANH;
    query(func.args(" 0"), 0);
  }
}
