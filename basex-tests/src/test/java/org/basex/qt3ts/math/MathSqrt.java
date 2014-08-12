package org.basex.qt3ts.math;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the math:sqrt function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-14, BSD License
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
   * Evaluate the function math:sqrt() with the argument 0.0e0.
   */
  @org.junit.Test
  public void mathSqrt002() {
    final XQuery query = new XQuery(
      "math:sqrt(0.0e0)",
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
   * Evaluate the function math:sqrt() with the argument -0.0e0.
   */
  @org.junit.Test
  public void mathSqrt003() {
    final XQuery query = new XQuery(
      "math:sqrt(-0.0e0)",
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
   * Evaluate the function math:sqrt() with the argument -0.0e0.
   */
  @org.junit.Test
  public void mathSqrt004() {
    final XQuery query = new XQuery(
      "math:sqrt(-0.0e0)",
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
   * Evaluate the function math:sqrt() with the argument 1.0e6.
   */
  @org.junit.Test
  public void mathSqrt005() {
    final XQuery query = new XQuery(
      "math:sqrt(1.0e6)",
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   * Evaluate the function math:sqrt() with the argument INF.
   */
  @org.junit.Test
  public void mathSqrt008() {
    final XQuery query = new XQuery(
      "math:sqrt(xs:double('INF'))",
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
}
