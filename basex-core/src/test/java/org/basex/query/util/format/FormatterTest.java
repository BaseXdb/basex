package org.basex.query.util.format;

import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.query.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the built-in formatters, which are used if ICU is not available.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FormatterTest {
  /**
   * German words.
   * @throws QueryException query exception
   */
  @Test public void german() throws QueryException {
    final Formatter de = new FormatterDE();
    check(de, 101000000, "w", "einhunderteine millionen");
    check(de, 2000000, "w", "zwei millionen");
    check(de, 1000000, "w;o", "einmillionste");
    check(de, 2000000, "w;o", "zweimillionste");
    check(de, 101000000, "w;o", "einhunderteinmillionste");
    check(de, 1000000, "Ww;o", "Einmillionste");
    check(de, 1000001, "w;o", "eine million erste");
    check(de, 1000000000, "w;o", "einmilliardste");
    check(de, 3000000000000000L, "w;o", "dreibilliardste");
    check(de, Long.MIN_VALUE, "w", "-9223372036854775808");
    check(de, Long.MIN_VALUE, "1;o", "-9223372036854775808.");
  }

  /**
   * English words.
   * @throws QueryException query exception
   */
  @Test public void english() throws QueryException {
    final Formatter en = new FormatterEN();
    check(en, 1000000000000000000L, "w", "one quintillion");
    check(en, Long.MIN_VALUE, "w;o", "-9223372036854775808th");
    check(en, Long.MIN_VALUE, "1;o", "-9223372036854775808th");
  }

  /**
   * French words.
   * @throws QueryException query exception
   */
  @Test public void french() throws QueryException {
    final Formatter fr = new FormatterFR();
    check(fr, 1, "w;o", "premier");
    check(fr, 21, "w;o", "vingt et unième");
    check(fr, 1000000, "w;o", "millionième");
    check(fr, 2000000, "w;o", "deux millionième");
    check(fr, 1000001, "w;o", "un million unième");
    check(fr, 2000001, "w;o", "deux millions unième");
    check(fr, Long.MIN_VALUE, "1;o", "-9223372036854775808e");
  }

  /** Language tags. */
  @Test public void languageTags() {
    assertInstanceOf(FormatterDE.class, Formatter.getInternal(token("DE")));
    assertInstanceOf(FormatterFR.class, Formatter.getInternal(token("Fr-CA")));
    assertNull(Formatter.getInternal(token("xx")));
  }

  /**
   * Checks a formatted integer.
   * @param formatter formatter
   * @param value value to be formatted
   * @param picture picture
   * @param expected expected result
   * @throws QueryException query exception
   */
  private static void check(final Formatter formatter, final long value, final String picture,
      final String expected) throws QueryException {
    assertEquals(expected, string(formatter.formatInt(value, new IntFormat(token(picture), null))));
  }
}
