package org.basex.test.qt3ts.math;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the math:sin function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-13, BSD License
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
   * Evaluate the function math:sin() with the argument 0.
   */
  @org.junit.Test
  public void mathSin002() {
    final XQuery query = new XQuery(
      "math:sin(0)",
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
   * Evaluate the function math:sin() with the argument -0.0e0.
   */
  @org.junit.Test
  public void mathSin003() {
    final XQuery query = new XQuery(
      "math:sin(-0.0e0)",
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
   * Evaluate the function math:sin() with the argument math:pi() div 2.
   */
  @org.junit.Test
  public void mathSin004() {
    final XQuery query = new XQuery(
      "math:sin(math:pi() div 2)",
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   * Evaluate the function math:sin() with the argument xs:double('INF').
   */
  @org.junit.Test
  public void mathSin008() {
    final XQuery query = new XQuery(
      "math:sin(xs:double('INF'))",
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
   * Evaluate the function math:sin() with the argument xs:double('-INF').
   */
  @org.junit.Test
  public void mathSin009() {
    final XQuery query = new XQuery(
      "math:sin(xs:double('-INF'))",
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
