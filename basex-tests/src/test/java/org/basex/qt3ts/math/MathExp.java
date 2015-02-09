package org.basex.qt3ts.math;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the math:exp function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MathExp extends QT3TestSet {

  /**
   * Evaluate the function exp() with the argument set to empty sequence.
   */
  @org.junit.Test
  public void mathExp001() {
    final XQuery query = new XQuery(
      "math:exp(())",
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
   * Evaluate the function exp() with the argument set to zero.
   */
  @org.junit.Test
  public void mathExp002() {
    final XQuery query = new XQuery(
      "math:exp(0)",
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
   * Evaluate the function exp() with the argument set to 1.
   */
  @org.junit.Test
  public void mathExp003() {
    final XQuery query = new XQuery(
      "math:exp(1)",
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
      assertQuery("abs($result - 2.7182818284590456e0) lt 1e-15")
    );
  }

  /**
   * Evaluate the function exp() with the argument set to 2.
   */
  @org.junit.Test
  public void mathExp004() {
    final XQuery query = new XQuery(
      "math:exp(2)",
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
      assertEq("7.38905609893065e0")
    );
  }

  /**
   * Evaluate the function exp() with the argument set to -1.
   */
  @org.junit.Test
  public void mathExp005() {
    final XQuery query = new XQuery(
      "math:exp(-1)",
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
      assertEq("0.36787944117144233e0")
    );
  }

  /**
   * Evaluate the function exp() with the argument set to math:pi().
   */
  @org.junit.Test
  public void mathExp006() {
    final XQuery query = new XQuery(
      "math:exp(math:pi())",
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
      assertEq("23.140692632779267e0")
    );
  }

  /**
   * Evaluate the function exp() with the argument set to xs:double('NaN').
   */
  @org.junit.Test
  public void mathExp007() {
    final XQuery query = new XQuery(
      "math:exp(xs:double('NaN'))",
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
   * Evaluate the function exp() with the argument set to xs:double('INF').
   */
  @org.junit.Test
  public void mathExp008() {
    final XQuery query = new XQuery(
      "math:exp(xs:double('INF'))",
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
   * Evaluate the function exp() with the argument set to xs:double('NaN').
   */
  @org.junit.Test
  public void mathExp009() {
    final XQuery query = new XQuery(
      "math:exp(xs:double('-INF'))",
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
}
