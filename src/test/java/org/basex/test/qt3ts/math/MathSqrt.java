package org.basex.test.qt3ts.math;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the math:sqrt function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MathSqrt extends QT3TestSet {

  /**
   * Evaluate the function math:sqrt() with the argument ().
   */
  @org.junit.Test
  public void mathSqrt001() {
    final XQuery query = new XQuery(
      "math:sqrt(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Evaluate the function math:sqrt() with the argument 0.0e0.
   */
  @org.junit.Test
  public void mathSqrt002() {
    final XQuery query = new XQuery(
      "math:sqrt(0.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0.0e0")
    );
  }

  /**
   * Evaluate the function math:sqrt() with the argument -0.0e0.
   */
  @org.junit.Test
  public void mathSqrt003() {
    final XQuery query = new XQuery(
      "math:sqrt(-0.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0.0e0")
    );
  }

  /**
   * Evaluate the function math:sqrt() with the argument -0.0e0.
   */
  @org.junit.Test
  public void mathSqrt004() {
    final XQuery query = new XQuery(
      "math:sqrt(-0.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-0.0e0")
    );
  }

  /**
   * Evaluate the function math:sqrt() with the argument 1.0e6.
   */
  @org.junit.Test
  public void mathSqrt005() {
    final XQuery query = new XQuery(
      "math:sqrt(1.0e6)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1.0e3")
    );
  }

  /**
   * Evaluate the function math:sqrt() with the argument 2.0e0.
   */
  @org.junit.Test
  public void mathSqrt006() {
    final XQuery query = new XQuery(
      "math:sqrt(2.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1.4142135623730951e0")
    );
  }

  /**
   * Evaluate the function math:sqrt() with the argument xs:double('NaN').
   */
  @org.junit.Test
  public void mathSqrt007() {
    final XQuery query = new XQuery(
      "math:sqrt(xs:double('NaN'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function math:sqrt() with the argument INF.
   */
  @org.junit.Test
  public void mathSqrt008() {
    final XQuery query = new XQuery(
      "math:sqrt(xs:double('INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("xs:double('INF')")
    );
  }

  /**
   * Evaluate the function math:sqrt() with the argument -INF.
   */
  @org.junit.Test
  public void mathSqrt010() {
    final XQuery query = new XQuery(
      "math:sqrt(xs:double('-INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }
}
