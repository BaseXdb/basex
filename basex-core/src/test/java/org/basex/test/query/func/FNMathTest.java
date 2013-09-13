package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Math Module.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNMathTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void crc32() {
    query(_MATH_CRC32.args(""), "00000000");
    query(_MATH_CRC32.args("BaseX"), "4C06FC7F");
  }
}
