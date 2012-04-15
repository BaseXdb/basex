package org.basex.test.qt3ts.math;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the math:sin function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MathSin extends QT3TestSet {

  /**
   * Evaluate the function math:sin() with the argument ().
   */
  @org.junit.Test
  public void mathSin001() {
    final XQuery query = new XQuery(
      "math:sin(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Evaluate the function math:sin() with the argument 0.
   */
  @org.junit.Test
  public void mathSin002() {
    final XQuery query = new XQuery(
      "math:sin(0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0.0e0")
    );
  }

  /**
   * Evaluate the function math:sin() with the argument -0.0e0.
   */
  @org.junit.Test
  public void mathSin003() {
    final XQuery query = new XQuery(
      "math:sin(-0.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-0.0e0")
    );
  }

  /**
   * Evaluate the function math:sin() with the argument math:pi() div 2.
   */
  @org.junit.Test
  public void mathSin004() {
    final XQuery query = new XQuery(
      "math:sin(math:pi() div 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1.0e0")
    );
  }

  /**
   * Evaluate the function math:sin() with the argument -math:pi() div 2.
   */
  @org.junit.Test
  public void mathSin005() {
    final XQuery query = new XQuery(
      "math:sin(-math:pi() div 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1.0e0")
    );
  }

  /**
   * Evaluate the function math:sin() with the argument math:pi().
   */
  @org.junit.Test
  public void mathSin006() {
    final XQuery query = new XQuery(
      "math:sin(math:pi()) lt 1e-15",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Evaluate the function math:sin() with the argument xs:double('NaN').
   */
  @org.junit.Test
  public void mathSin007() {
    final XQuery query = new XQuery(
      "math:sin(xs:double('NaN'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function math:sin() with the argument xs:double('INF').
   */
  @org.junit.Test
  public void mathSin008() {
    final XQuery query = new XQuery(
      "math:sin(xs:double('INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function math:sin() with the argument xs:double('-INF').
   */
  @org.junit.Test
  public void mathSin009() {
    final XQuery query = new XQuery(
      "math:sin(xs:double('-INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }
}
