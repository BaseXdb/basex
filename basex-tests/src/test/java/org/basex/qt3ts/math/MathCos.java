package org.basex.qt3ts.math;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the math:cos function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MathCos extends QT3TestSet {

  /**
   * Evaluate the function math:cos() with the argument ().
   */
  @org.junit.Test
  public void mathCos001() {
    final XQuery query = new XQuery(
      "math:cos(())",
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
   * Evaluate the function math:cos() with the argument 0.
   */
  @org.junit.Test
  public void mathCos002() {
    final XQuery query = new XQuery(
      "math:cos(0)",
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
   * Evaluate the function math:cos() with the argument -0.0e0.
   */
  @org.junit.Test
  public void mathCos003() {
    final XQuery query = new XQuery(
      "math:cos(-0.0e0)",
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
   * Evaluate the function math:cos() with the argument math:pi() div 2.
   */
  @org.junit.Test
  public void mathCos004() {
    final XQuery query = new XQuery(
      "math:cos(math:pi() div 2)",
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
      assertQuery("abs($result) lt 1e-15")
    );
  }

  /**
   * Evaluate the function math:cos() with the argument -math:pi() div 2.
   */
  @org.junit.Test
  public void mathCos005() {
    final XQuery query = new XQuery(
      "math:cos(-math:pi() div 2)",
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
      assertQuery("abs($result) lt 1e-15")
    );
  }

  /**
   * Evaluate the function math:cos() with the argument math:pi().
   */
  @org.junit.Test
  public void mathCos006() {
    final XQuery query = new XQuery(
      "math:cos(math:pi())",
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
   * Evaluate the function math:cos() with the argument xs:double('NaN').
   */
  @org.junit.Test
  public void mathCos007() {
    final XQuery query = new XQuery(
      "math:cos(xs:double('NaN'))",
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
   * Evaluate the function math:cos() with the argument xs:double('INF').
   */
  @org.junit.Test
  public void mathCos008() {
    final XQuery query = new XQuery(
      "math:cos(xs:double('INF'))",
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
   * Evaluate the function math:cos() with the argument xs:double('-INF').
   */
  @org.junit.Test
  public void mathCos009() {
    final XQuery query = new XQuery(
      "math:cos(xs:double('-INF'))",
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
