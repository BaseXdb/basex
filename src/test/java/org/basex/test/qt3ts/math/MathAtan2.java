package org.basex.test.qt3ts.math;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the math:atan2 function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MathAtan2 extends QT3TestSet {

  /**
   * Evaluate the function math:atan2() with the arguments +0.0e0 and -0.0e0.
   */
  @org.junit.Test
  public void mathAtan2001() {
    final XQuery query = new XQuery(
      "math:atan2(+0.0e0, 0.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0.0e0")
    );
  }

  /**
   * Evaluate the function math:atan2() with the arguments -0.0e0 and -0.0e0.
   */
  @org.junit.Test
  public void mathAtan2002() {
    final XQuery query = new XQuery(
      "math:atan2(-0.0e0, 0.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-0.0e0")
    );
  }

  /**
   * Evaluate the function math:atan2() with the arguments +0.0e0 and -0.0e0.
   */
  @org.junit.Test
  public void mathAtan2003() {
    final XQuery query = new XQuery(
      "math:atan2(+0.0e0, -0.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("math:pi()")
    );
  }

  /**
   * Evaluate the function math:atan2() with the arguments -0.0e0 and -0.0e0.
   */
  @org.junit.Test
  public void mathAtan2004() {
    final XQuery query = new XQuery(
      "math:atan2(-0.0e0, -0.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-math:pi()")
    );
  }

  /**
   * Evaluate the function math:atan2() with the arguments -1 and -0.0e0.
   */
  @org.junit.Test
  public void mathAtan2005() {
    final XQuery query = new XQuery(
      "math:atan2(-1, -0.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-math:pi() div 2")
    );
  }

  /**
   * Evaluate the function math:atan2() with the arguments +1 and 0.0e0.
   */
  @org.junit.Test
  public void mathAtan2006() {
    final XQuery query = new XQuery(
      "math:atan2(+1, 0.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("+math:pi() div 2")
    );
  }

  /**
   * Evaluate the function math:atan2() with the arguments -0.0e0 and -1.
   */
  @org.junit.Test
  public void mathAtan2007() {
    final XQuery query = new XQuery(
      "math:atan2(-0.0e0, -1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-math:pi()")
    );
  }

  /**
   * Evaluate the function math:atan2() with the arguments +0.0e0 and -1.
   */
  @org.junit.Test
  public void mathAtan2008() {
    final XQuery query = new XQuery(
      "math:atan2(+0.0e0, -1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("+math:pi()")
    );
  }

  /**
   * Evaluate the function math:atan2() with the arguments -0.0e0 and +1.
   */
  @org.junit.Test
  public void mathAtan2009() {
    final XQuery query = new XQuery(
      "math:atan2(-0.0e0, +1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-0.0e0")
    );
  }

  /**
   * Evaluate the function math:atan2() with the arguments +0.0e0 and +1.
   */
  @org.junit.Test
  public void mathAtan2010() {
    final XQuery query = new XQuery(
      "math:atan2(+0.0e0, +1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("+0.0e0")
    );
  }
}
