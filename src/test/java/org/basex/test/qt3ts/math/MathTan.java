package org.basex.test.qt3ts.math;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the math:tan function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MathTan extends QT3TestSet {

  /**
   * Evaluate the function math:tan() with the argument ().
   */
  @org.junit.Test
  public void mathTan001() {
    final XQuery query = new XQuery(
      "math:tan(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Evaluate the function math:tan() with the argument 0.
   */
  @org.junit.Test
  public void mathTan002() {
    final XQuery query = new XQuery(
      "math:tan(0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0.0e0")
    );
  }

  /**
   * Evaluate the function math:tan() with the argument -0.0e0.
   */
  @org.junit.Test
  public void mathTan003() {
    final XQuery query = new XQuery(
      "math:tan(-0.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-0.0e0")
    );
  }

  /**
   * Evaluate the function math:tan() with the argument math:pi() div 4.
   */
  @org.junit.Test
  public void mathTan004() {
    final XQuery query = new XQuery(
      "math:tan(math:pi() div 4)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("abs($result - 1.0e0) lt 0.0000001")
    );
  }

  /**
   * Evaluate the function math:tan() with the argument -math:pi() div 4.
   */
  @org.junit.Test
  public void mathTan005() {
    final XQuery query = new XQuery(
      "math:tan(-math:pi() div 4)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("abs($result - -1.0e0) lt 0.0000001")
    );
  }

  /**
   * Evaluate the function math:tan() with the argument math:pi() div 2.
   */
  @org.junit.Test
  public void mathTan006() {
    final XQuery query = new XQuery(
      "math:tan(math:pi() div 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1.633123935319537E16")
    );
  }

  /**
   * Evaluate the function math:tan() with the argument math:pi() div 2.
   */
  @org.junit.Test
  public void mathTan007() {
    final XQuery query = new XQuery(
      "math:tan(-math:pi() div 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1.633123935319537E16")
    );
  }

  /**
   * Evaluate the function math:tan() with the argument math:pi().
   */
  @org.junit.Test
  public void mathTan008() {
    final XQuery query = new XQuery(
      "math:tan(math:pi())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("abs($result) < 1e12")
    );
  }

  /**
   * Evaluate the function math:tan() with the argument xs:double('NaN').
   */
  @org.junit.Test
  public void mathTan009() {
    final XQuery query = new XQuery(
      "math:tan(xs:double('NaN'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function math:tan() with the argument xs:double('INF').
   */
  @org.junit.Test
  public void mathTan010() {
    final XQuery query = new XQuery(
      "math:tan(xs:double('INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function math:tan() with the argument xs:double('-INF').
   */
  @org.junit.Test
  public void mathTan011() {
    final XQuery query = new XQuery(
      "math:tan(xs:double('-INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }
}
