package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery functions prefixed with "xqunit".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNXQUnitTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void faill() {
    error(_XQUNIT_FAIL.args("1"), Err.BXUN_FAIL);
  }

  /** Test method. */
  @Test
  public void assrt() {
    query(_XQUNIT_ASSERT.args("1"), "");
    query(_XQUNIT_ASSERT.args("(<a/>,<b/>)"), "");
    error(_XQUNIT_ASSERT.args("()"), Err.BXUN_ASSERT);
    error(_XQUNIT_ASSERT.args("()", "X"), Err.BXUN_ERROR);
  }

  /** Test method. */
  @Test
  public void test() {
    String func = "declare %xqunit:test function local:x() { 1 }; ";
    query(func + COUNT.args(_XQUNIT_TEST.args()), "1");

    func = "declare %xqunit:test function local:x() { xqunit:fail('') }; ";
    query(func + COUNT.args(_XQUNIT_TEST.args() + "//failure"), "1");

    func = "declare %xqunit:test function local:x() { 1+<a/> }; ";
    query(func + COUNT.args(_XQUNIT_TEST.args() + "//error"), "1");
  }
}
