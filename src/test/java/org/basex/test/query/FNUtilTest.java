package org.basex.test.query;

import org.basex.core.BaseXException;
import org.basex.query.func.FunDef;
import org.basex.query.util.Err;
import org.junit.Test;

/**
 * This class tests the XQuery utility functions prefixed with "util".
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FNUtilTest extends AdvancedQueryTest {
  /** Constructor. */
  public FNUtilTest() {
    super("util");
  }

  /**
   * Test method for the util:eval() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testEval() throws BaseXException {
    final String fun = check(FunDef.EVAL, String.class);
    query(fun + "('1')", "1");
    query(fun + "('1 + 2')", "3");
    error(fun + "('1+')", Err.INCOMPLETE);
    error("declare variable $a := 1; " + fun + "('$a')", Err.VARUNDEF);
    error("for $a in (1,2) return " + fun + "('$a')", Err.VARUNDEF);
  }

  /**
   * Test method for the util:run() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testRun() throws BaseXException {
    final String fun = check(FunDef.RUN, String.class);
    query(fun + "('etc/xml/input.xq')", "XML");
    error(fun + "('etc/xml/xxx.xq')", Err.UNDOC);
  }

  /**
   * Test method for the util:mb() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testMB() throws BaseXException {
    final String fun = check(FunDef.MB, (Class<?>) null, Boolean.class);
    query(fun + "(())");
    query(fun + "(1 to 1000, false())");
    query(fun + "(1 to 1000, true())");
  }

  /**
   * Test method for the util:ms() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testMS() throws BaseXException {
    final String fun = check(FunDef.MS, (Class<?>) null, Boolean.class);
    query(fun + "(())");
    query(fun + "(1 to 1000, false())");
    query(fun + "(1 to 1000, true())");
  }
}
