package org.basex.test.qt3ts.math;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the math:atan2 function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-13, BSD License
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
   * Evaluate the function math:atan2() with the arguments -0.0e0 and -0.0e0.
   */
  @org.junit.Test
  public void mathAtan2002() {
    final XQuery query = new XQuery(
      "math:atan2(-0.0e0, 0.0e0)",
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
   * Evaluate the function math:atan2() with the arguments +0.0e0 and -0.0e0.
   */
  @org.junit.Test
  public void mathAtan2003() {
    final XQuery query = new XQuery(
      "math:atan2(+0.0e0, -0.0e0)",
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
      assertQuery("abs($result - 3.141592653589793e0) lt 1e-14")
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("abs($result + 3.141592653589793e0) lt 1e-14")
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("abs($result + 1.5707963267948966e0) lt 1e-14")
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("abs($result - 1.5707963267948966e0) lt 1e-14")
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("abs($result + 3.141592653589793e0) lt 1e-14")
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
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("abs($result - 3.141592653589793e0) lt 1e-14")
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
   * Evaluate the function math:atan2() with the arguments +0.0e0 and +1.
   */
  @org.junit.Test
  public void mathAtan2010() {
    final XQuery query = new XQuery(
      "math:atan2(+0.0e0, +1)",
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
      assertEq("+0.0e0")
    );
  }
}
