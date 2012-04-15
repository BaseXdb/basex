package org.basex.test.qt3ts.math;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the math:atan function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MathAtan extends QT3TestSet {

  /**
   * Evaluate the function math:atan() with the argument ().
   */
  @org.junit.Test
  public void mathAtan001() {
    final XQuery query = new XQuery(
      "math:atan(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Evaluate the function math:atan() with the argument 0.
   */
  @org.junit.Test
  public void mathAtan002() {
    final XQuery query = new XQuery(
      "math:atan(0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0.0e0")
    );
  }

  /**
   * Evaluate the function math:atan() with the argument -0.0e0.
   */
  @org.junit.Test
  public void mathAtan003() {
    final XQuery query = new XQuery(
      "math:atan(-0.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-0.0e0")
    );
  }

  /**
   * Evaluate the function math:atan() with the argument 1.0e0.
   */
  @org.junit.Test
  public void mathAtan004() {
    final XQuery query = new XQuery(
      "math:atan(1.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0.7853981633974483e0")
    );
  }

  /**
   * Evaluate the function math:atan() with the argument -1.0e0.
   */
  @org.junit.Test
  public void mathAtan005() {
    final XQuery query = new XQuery(
      "math:atan(-1.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-0.7853981633974483e0")
    );
  }

  /**
   * Evaluate the function math:atan() with the argument 2.0e0.
   */
  @org.junit.Test
  public void mathAtan006() {
    final XQuery query = new XQuery(
      "math:atan(-1.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-0.7853981633974483e0")
    );
  }

  /**
   * Evaluate the function math:atan() with the argument xs:double('NaN').
   */
  @org.junit.Test
  public void mathAtan007() {
    final XQuery query = new XQuery(
      "math:atan(xs:double('NaN'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function math:atan() with the argument xs:double('INF').
   */
  @org.junit.Test
  public void mathAtan008() {
    final XQuery query = new XQuery(
      "math:atan(xs:double('INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1.5707963267948966e0")
    );
  }

  /**
   * Evaluate the function math:atan() with the argument xs:double('-INF').
   */
  @org.junit.Test
  public void mathAtan009() {
    final XQuery query = new XQuery(
      "math:atan(xs:double('-INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1.5707963267948966e0")
    );
  }
}
