package org.basex.test.qt3ts.math;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the math:tan function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-13, BSD License
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
   * Evaluate the function math:tan() with the argument 0.
   */
  @org.junit.Test
  public void mathTan002() {
    final XQuery query = new XQuery(
      "math:tan(0)",
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
   * Evaluate the function math:tan() with the argument -0.0e0.
   */
  @org.junit.Test
  public void mathTan003() {
    final XQuery query = new XQuery(
      "math:tan(-0.0e0)",
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
   * Evaluate the function math:tan() with the argument math:pi() div 4.
   */
  @org.junit.Test
  public void mathTan004() {
    final XQuery query = new XQuery(
      "math:tan(math:pi() div 4)",
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("abs($result - -1.0e0) lt 0.0000001")
    );
  }

  /**
   * Evaluate the function math:tan() with the argument math:pi() div 4.
   */
  @org.junit.Test
  public void mathTan006() {
    final XQuery query = new XQuery(
      "math:tan(math:pi() div 4)",
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
      assertEq("0.9999999999999999")
    );
  }

  /**
   * Evaluate the function math:tan() with the argument math:pi() div 4.
   */
  @org.junit.Test
  public void mathTan007() {
    final XQuery query = new XQuery(
      "math:tan(-math:pi() div 4)",
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
      assertEq("-0.9999999999999999")
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   * Evaluate the function math:tan() with the argument xs:double('INF').
   */
  @org.junit.Test
  public void mathTan010() {
    final XQuery query = new XQuery(
      "math:tan(xs:double('INF'))",
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
   * Evaluate the function math:tan() with the argument xs:double('-INF').
   */
  @org.junit.Test
  public void mathTan011() {
    final XQuery query = new XQuery(
      "math:tan(xs:double('-INF'))",
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
