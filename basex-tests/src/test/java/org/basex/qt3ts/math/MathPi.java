package org.basex.qt3ts.math;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the math:pi function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MathPi extends QT3TestSet {

  /**
   * function pi() multipled by 2.
   */
  @org.junit.Test
  public void mathPi001() {
    final XQuery query = new XQuery(
      "2*math:pi()",
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
      assertEq("6.283185307179586e0")
    );
  }

  /**
   * function pi() converts an angle of 60 degrees to radians.
   */
  @org.junit.Test
  public void mathPi002() {
    final XQuery query = new XQuery(
      "60 * (math:pi() div 180)",
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
      assertQuery("abs($result - 1.0471975511965976e0) lt 1e-14")
    );
  }

  /**
   * function pi() itself.
   */
  @org.junit.Test
  public void mathPi003() {
    final XQuery query = new XQuery(
      "math:pi()",
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
      assertEq("3.141592653589793e0")
    );
  }

  /**
   * function pi() as a function item.
   */
  @org.junit.Test
  public void mathPi004() {
    final XQuery query = new XQuery(
      "math:pi#0()",
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
      assertEq("3.141592653589793e0")
    );
  }

  /**
   * function pi() via function lookup.
   */
  @org.junit.Test
  public void mathPi005() {
    final XQuery query = new XQuery(
      "function-lookup(xs:QName('math:pi'), 0)()",
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
      assertEq("3.141592653589793e0")
    );
  }
}
