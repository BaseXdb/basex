package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Math Module.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class MathModuleTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void crc32() {
    query(_MATH_CRC32.args(""), "00000000");
    query(_MATH_CRC32.args("BaseX"), "4C06FC7F");
  }

  /** Test method. */
  @Test
  public void pi() {
    query(_MATH_PI.args(), StrictMath.PI);
  }

  /** Test method. */
  @Test
  public void e() {
    query(_MATH_E.args(), StrictMath.E);
  }

  /** Test method. */
  @Test
  public void sinh() {
    query(_MATH_SINH.args(" 0"), "0");
  }

  /** Test method. */
  @Test
  public void cosh() {
    query(_MATH_COSH.args(" 0"), "1");
  }

  /** Test method. */
  @Test
  public void tanh() {
    query(_MATH_TANH.args(" 0"), "0");
  }
}
