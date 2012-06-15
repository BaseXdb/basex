package org.basex.test.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests some XQuery math functions prefixed with "math".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNMathTest extends AdvancedQueryTest {
  /**
   * Test method for the math:crc32() function.
   */
  @Test
  public void mathCRC32() {
    check(_MATH_CRC32);
    query(_MATH_CRC32.args(""), "00000000");
    query(_MATH_CRC32.args("BaseX"), "4C06FC7F");
  }

  /**
   * Test method for the math:uuid() function.
   */
  @Test
  public void mathUuid() {
    check(_MATH_UUID);
    final String s1 = query(_MATH_UUID.args());
    final String s2 = query(_MATH_UUID.args());
    assertTrue(!s1.equals(s2));
  }
}
