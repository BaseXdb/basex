package org.basex.qt3ts.math;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the math:atan function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-14, BSD License
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1.5707963267948966e0")
    );
  }
}
