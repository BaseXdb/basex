package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.util.*;
import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Unit Module.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNUnitTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void faill() {
    error(_UNIT_FAIL.args("1"), Err.UNIT_MESSAGE);
  }

  /** Test method. */
  @Test
  public void assrt() {
    query(_UNIT_ASSERT.args("1"), "");
    query(_UNIT_ASSERT.args("(<a/>,<b/>)"), "");
    error(_UNIT_ASSERT.args("()"), Err.UNIT_ASSERT);
    error(_UNIT_ASSERT.args("()", "X"), Err.UNIT_MESSAGE);
  }

  /** Test method. */
  @Test
  public void assertEquals() {
    query(_UNIT_ASSERT_EQUALS.args("1", "1"), "");
    error(_UNIT_ASSERT_EQUALS.args("1", "2"), Err.UNIT_ASSERT_EQUALS);
  }

  /** Test method. */
  @Test
  public void test() {
    String func = "declare %unit:test function local:x() { 1 }; ";
    query(func + COUNT.args(_UNIT_TEST.args()), "1");

    func = "declare %unit:test function local:x() { unit:fail('') }; ";
    query(func + COUNT.args(_UNIT_TEST.args() + "//failure"), "1");

    func = "declare %unit:test function local:x() { 1+<a/> }; ";
    query(func + COUNT.args(_UNIT_TEST.args() + "//error"), "1");

    func = "declare %unit:test function local:x() { 1+<a/> }; ";
    query(func + COUNT.args(_UNIT_TEST.args(_INSPECT_FUNCTIONS.args()) + "//error"), "1");
    query(func + _UNIT_TEST.args("()") + "/*/*", "");

    func = "declare %unit:test function unit:test() { function() { () }() }; ";
    query(func + COUNT.args(_UNIT_TEST.args()), "1");
  }
}
