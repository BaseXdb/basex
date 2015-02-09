package org.basex.qt3ts.math;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the math:exp10 function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MathExp10 extends QT3TestSet {

  /**
   * Evaluate the function exp10() with the argument set to empty sequence.
   */
  @org.junit.Test
  public void mathExp10001() {
    final XQuery query = new XQuery(
      "math:exp10(())",
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
   * Evaluate the function exp10() with the argument set to 0.
   */
  @org.junit.Test
  public void mathExp10002() {
    final XQuery query = new XQuery(
      "math:exp10(0)",
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
   * Evaluate the function exp10() with the argument set to 1.
   */
  @org.junit.Test
  public void mathExp10003() {
    final XQuery query = new XQuery(
      "math:exp10(1)",
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
      assertEq("1.0e1")
    );
  }

  /**
   * Evaluate the function exp10() with the argument set to 0.5.
   */
  @org.junit.Test
  public void mathExp10004() {
    final XQuery query = new XQuery(
      "math:exp10(0.5)",
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
      (
        assertEq("3.1622776601683795")
      ||
        assertEq("3.162277660168379")
      )
    );
  }

  /**
   * Evaluate the function exp10() with the argument set to -1.
   */
  @org.junit.Test
  public void mathExp10005() {
    final XQuery query = new XQuery(
      "math:exp10(-1)",
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
      assertEq("1.0e-1")
    );
  }

  /**
   * Evaluate the function exp10() with the argument set to xs:double('NaN').
   */
  @org.junit.Test
  public void mathExp10006() {
    final XQuery query = new XQuery(
      "math:exp10(xs:double('NaN'))",
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
   * Evaluate the function exp10() with the argument xs:double('INF').
   */
  @org.junit.Test
  public void mathExp10007() {
    final XQuery query = new XQuery(
      "math:exp10(xs:double('INF'))",
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
   * Evaluate the function exp10() with the argument xs:double('NaN').
   */
  @org.junit.Test
  public void mathExp10008() {
    final XQuery query = new XQuery(
      "math:exp10(xs:double('-INF'))",
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
