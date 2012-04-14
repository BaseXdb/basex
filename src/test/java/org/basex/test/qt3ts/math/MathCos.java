package org.basex.test.qt3ts.math;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the math:cos function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MathCos extends QT3TestSet {

  /**
   * Evaluate the function math:cos() with the argument ().
   */
  @org.junit.Test
  public void mathCos001() {
    final XQuery query = new XQuery(
      "math:cos(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Evaluate the function math:cos() with the argument 0.
   */
  @org.junit.Test
  public void mathCos002() {
    final XQuery query = new XQuery(
      "math:cos(0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1.0e0")
    );
  }

  /**
   * Evaluate the function math:cos() with the argument -0.0e0.
   */
  @org.junit.Test
  public void mathCos003() {
    final XQuery query = new XQuery(
      "math:cos(-0.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1.0e0")
    );
  }

  /**
   * Evaluate the function math:cos() with the argument math:pi() div 2.
   */
  @org.junit.Test
  public void mathCos004() {
    final XQuery query = new XQuery(
      "math:cos(math:pi() div 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("abs($result) lt 1e-15")
    );
  }

  /**
   * Evaluate the function math:cos() with the argument -math:pi() div 2.
   */
  @org.junit.Test
  public void mathCos005() {
    final XQuery query = new XQuery(
      "math:cos(-math:pi() div 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("abs($result) lt 1e-15")
    );
  }

  /**
   * Evaluate the function math:cos() with the argument math:pi().
   */
  @org.junit.Test
  public void mathCos006() {
    final XQuery query = new XQuery(
      "math:cos(math:pi())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1.0e0")
    );
  }

  /**
   * Evaluate the function math:cos() with the argument xs:double('NaN').
   */
  @org.junit.Test
  public void mathCos007() {
    final XQuery query = new XQuery(
      "math:cos(xs:double('NaN'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function math:cos() with the argument xs:double('INF').
   */
  @org.junit.Test
  public void mathCos008() {
    final XQuery query = new XQuery(
      "math:cos(xs:double('INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function math:cos() with the argument xs:double('-INF').
   */
  @org.junit.Test
  public void mathCos009() {
    final XQuery query = new XQuery(
      "math:cos(xs:double('-INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }
}
