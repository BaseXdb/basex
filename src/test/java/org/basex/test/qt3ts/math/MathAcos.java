package org.basex.test.qt3ts.math;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the math:acos function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MathAcos extends QT3TestSet {

  /**
   * Evaluate the function math:acos() with the argument ().
   */
  @org.junit.Test
  public void mathAcos001() {
    final XQuery query = new XQuery(
      "math:acos(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Evaluate the function math:acos() with the argument 0.
   */
  @org.junit.Test
  public void mathAcos002() {
    final XQuery query = new XQuery(
      "math:acos(0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1.5707963267948966e0")
    );
  }

  /**
   * Evaluate the function math:acos() with the argument -0.0e0.
   */
  @org.junit.Test
  public void mathAcos003() {
    final XQuery query = new XQuery(
      "math:acos(-0.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1.5707963267948966e0")
    );
  }

  /**
   * Evaluate the function math:acos() with the argument 1.0e0.
   */
  @org.junit.Test
  public void mathAcos004() {
    final XQuery query = new XQuery(
      "math:acos(1.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0.0e0")
    );
  }

  /**
   * Evaluate the function math:acos() with the argument -1.0e0.
   */
  @org.junit.Test
  public void mathAcos005() {
    final XQuery query = new XQuery(
      "math:acos(-1.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("3.141592653589793e0")
    );
  }

  /**
   * Evaluate the function math:acos() with the argument 2.0e0.
   */
  @org.junit.Test
  public void mathAcos006() {
    final XQuery query = new XQuery(
      "math:acos(2.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function math:acos() with the argument xs:double('NaN').
   */
  @org.junit.Test
  public void mathAcos007() {
    final XQuery query = new XQuery(
      "math:acos(xs:double('NaN'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function math:acos() with the argument xs:double('INF').
   */
  @org.junit.Test
  public void mathAcos008() {
    final XQuery query = new XQuery(
      "math:acos(xs:double('INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function math:acos() with the argument xs:double('-INF').
   */
  @org.junit.Test
  public void mathAcos009() {
    final XQuery query = new XQuery(
      "math:acos(xs:double('-INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }
}
