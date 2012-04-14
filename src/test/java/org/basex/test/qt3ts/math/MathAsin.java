package org.basex.test.qt3ts.math;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the math:asin function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MathAsin extends QT3TestSet {

  /**
   * Evaluate the function math:asin() with the argument ().
   */
  @org.junit.Test
  public void mathAsin001() {
    final XQuery query = new XQuery(
      "math:asin(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Evaluate the function math:asin() with the argument 0.
   */
  @org.junit.Test
  public void mathAsin002() {
    final XQuery query = new XQuery(
      "math:asin(0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0.0e0")
    );
  }

  /**
   * Evaluate the function math:asin() with the argument -0.0e0.
   */
  @org.junit.Test
  public void mathAsin003() {
    final XQuery query = new XQuery(
      "math:asin(-0.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-0.0e0")
    );
  }

  /**
   * Evaluate the function math:asin() with the argument math:pi() div 4.
   */
  @org.junit.Test
  public void mathAsin004() {
    final XQuery query = new XQuery(
      "math:asin(1.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1.5707963267948966e0")
    );
  }

  /**
   * Evaluate the function math:asin() with the argument -math:pi() div 4.
   */
  @org.junit.Test
  public void mathAsin005() {
    final XQuery query = new XQuery(
      "math:asin(-1.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1.5707963267948966e0")
    );
  }

  /**
   * Evaluate the function math:asin() with the argument math:pi() div 2.
   */
  @org.junit.Test
  public void mathAsin006() {
    final XQuery query = new XQuery(
      "math:asin(2.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function math:asin() with the argument xs:double('NaN').
   */
  @org.junit.Test
  public void mathAsin007() {
    final XQuery query = new XQuery(
      "math:asin(xs:double('NaN'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function math:asin() with the argument xs:double('INF').
   */
  @org.junit.Test
  public void mathAsin08() {
    final XQuery query = new XQuery(
      "math:asin(xs:double('INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function math:asin() with the argument xs:double('-INF').
   */
  @org.junit.Test
  public void mathAsin09() {
    final XQuery query = new XQuery(
      "math:asin(xs:double('-INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }
}
