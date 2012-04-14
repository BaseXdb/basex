package org.basex.test.qt3ts.math;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the math:pi function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MathPi extends QT3TestSet {

  /**
   * function pi() multipled by 2.
   */
  @org.junit.Test
  public void mathPi001() {
    final XQuery query = new XQuery(
      "2*math:pi()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("6.283185307179586e0")
    );
  }

  /**
   * function pi() converts an angle of 60 degrees to radians.
   */
  @org.junit.Test
  public void mathPi002() {
    final XQuery query = new XQuery(
      "60 * (math:pi() div 180)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("math:pi() div 3")
    );
  }
}
