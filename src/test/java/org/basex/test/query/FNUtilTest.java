package org.basex.test.query;

import org.basex.core.BaseXException;
import org.junit.Test;

/**
 * This class tests the XQuery utility functions prefixed with "util".
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FNUtilTest extends AdvancedQueryTest {
  /**
   * Test method for the util:eval() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testEval() throws BaseXException {
    // test wrong arguments
    args("util:eval", String.class);

    // dynamically evaluate query expressions
    query("util:eval('1')", "1");
    query("util:eval('1 + 2')", "3");
    error("util:eval('1+')", "XPST0003");
    error("declare variable $a := 1; util:eval('$a')", "XPST0008");
    error("for $a in (1,2) return util:eval('$a')", "XPST0008");
  }

  /**
   * Test method for the util:run() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testRun() throws BaseXException {
    // test wrong arguments
    args("util:run", String.class);

    // dynamically run query files
    query("util:run('etc/xml/input.xq')", "XML");
    error("util:run('etc/xml/xxx.xq')", "FODC0002");
  }

  /**
   * Test method for the util:mb() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testMB() throws BaseXException {
    // wrong arguments
    error("util:mb()", "XPST0017");
    error("util:mb('a','b')", "XPTY0004");
    error("util:mb('a','b','c')", "XPST0017");
    error("util:mb(1+)", "XPST0003");

    // measure memory (will always yield different results)
    query("util:mb(())");
    query("util:mb(1 to 10000, false())");
    query("util:mb(1 to 10000, true())");
  }

  /**
   * Test method for the util:ms() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testMS() throws BaseXException {
    // wrong arguments
    error("util:ms()", "XPST0017");
    error("util:ms('a','b')", "XPTY0004");
    error("util:ms('a','b','c')", "XPST0017");
    error("util:ms(1+)", "XPST0003");

    // measure time (will always yield different results)
    query("util:ms(())");
    query("util:ms(1 to 10000, false())");
    query("util:ms(1 to 10000, true())");
  }
}
