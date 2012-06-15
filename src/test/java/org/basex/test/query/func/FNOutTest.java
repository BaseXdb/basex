package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery output functions prefixed with "out".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNOutTest extends AdvancedQueryTest {
  /**
   * Test method for the out:nl() function.
   */
  @Test
  public void outNl() {
    check(_OUT_NL);
    // query() function removes all newlines..
    query(_OUT_NL.args(), "");
  }

  /**
   * Test method for the out:tab() function.
   */
  @Test
  public void outTab() {
    check(_OUT_TAB);
    query(_OUT_TAB.args(), "\t");
  }

  /**
   * Test method for the out:format() function.
   */
  @Test
  public void outFormat() {
    check(_OUT_FORMAT);
    query(_OUT_FORMAT.args("x", "x"), "x");
    query(_OUT_FORMAT.args("%d", " 1"), "1");
    query(_OUT_FORMAT.args("%2d", " 1"), " 1");
    query(_OUT_FORMAT.args("%05d", " 123"), "00123");
  }
}
