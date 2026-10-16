package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

/**
 * Token tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TokenTest {
  /** Converts valid numeric strings to doubles. */
  @Test public void toDouble() {
    for(final String string : new String[] {
      "0.0", "-0.0", "+0.0", "1.", "-1.", ".5", "-.5", "+.5", "323.41", "9876.00", " 1.5 ",
      "0.1", "0.3", "123456789012345.", "1234567890123456.7", "0.0000000000000000000001",
      "0.00000000000000000000001", "1.7976931348623157e308", "1e-400", "4.9e-324", "1E3",
      "-1.5e-3", "1234567890", "-1234567890", "999999999.5", "5", "+5", "-5", " 12 ", "\t-3.25\n",
      "12345678901234567890"
    }) check(string);

    final Random random = new Random(0);
    for(int i = 0; i < 100000; i++) {
      final StringBuilder sb = new StringBuilder();
      if(random.nextInt(4) == 0) sb.append(random.nextBoolean() ? '-' : '+');
      final int digits = 1 + random.nextInt(20), dot = random.nextInt(digits + 1);
      for(int d = 0; d < digits; d++) {
        if(d == dot) sb.append('.');
        sb.append((char) ('0' + random.nextInt(10)));
      }
      check(sb.toString());
    }
  }

  /** Rejects invalid numeric strings. */
  @Test public void toDoubleInvalid() {
    for(final String string : new String[] {
      "", " ", ".", "+", "-", "+.", "-.", "1.1.1", "1-", "1e", "e1", "1e.1", "--1", "+-1", "1 1",
      "NaN", "INF", "0x10", "1,5", "1+2", "e", "1 .5", "1d", "1.5f", "1e5d", "Infinity", "0x1p3",
      "1234567890123456789x"
    }) assertTrue(Double.isNaN(Token.toDouble(Token.token(string))), string);
  }

  /**
   * Compares the result with {@link Double#parseDouble(String)}.
   * @param string numeric string
   */
  private static void check(final String string) {
    final double expected = Double.parseDouble(string.trim());
    final double actual = Token.toDouble(Token.token(string));
    assertEquals(Double.doubleToRawLongBits(expected), Double.doubleToRawLongBits(actual), string);
  }
}
