package org.basex.test.qt3ts.math;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the math:log function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MathLog extends QT3TestSet {

  /**
   * Evaluate the function log() with the argument set to empty sequence.
   */
  @org.junit.Test
  public void mathLog001() {
    final XQuery query = new XQuery(
      "math:log(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Evaluate the function log() with the argument set to 0.
   */
  @org.junit.Test
  public void mathLog002() {
    final XQuery query = new XQuery(
      "math:log(0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("xs:double('-INF')")
    );
  }

  /**
   * Evaluate the function log() with the argument set to math:exp(1).
   */
  @org.junit.Test
  public void mathLog003() {
    final XQuery query = new XQuery(
      "math:log(math:exp(1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1.0e0")
    );
  }

  /**
   * Evaluate the function log() with the argument set to 1.0e-3.
   */
  @org.junit.Test
  public void mathLog004() {
    final XQuery query = new XQuery(
      "math:log(1.0e-3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-6.907755278982137e0")
    );
  }

  /**
   * Evaluate the function log() with the argument set to 2.
   */
  @org.junit.Test
  public void mathLog005() {
    final XQuery query = new XQuery(
      "math:log(2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0.6931471805599453e0")
    );
  }

  /**
   * Evaluate the function log() with the argument set to xs:double('-1').
   */
  @org.junit.Test
  public void mathLog006() {
    final XQuery query = new XQuery(
      "math:log(-1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function log() with the argument set to xs:double('NaN').
   */
  @org.junit.Test
  public void mathLog007() {
    final XQuery query = new XQuery(
      "math:log(xs:double('NaN'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function log() with the argument set to xs:double('INF').
   */
  @org.junit.Test
  public void mathLog008() {
    final XQuery query = new XQuery(
      "math:log(xs:double('INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("xs:double('INF')")
    );
  }

  /**
   * Evaluate the function log() with the argument set to xs:double('-INF').
   */
  @org.junit.Test
  public void mathLog009() {
    final XQuery query = new XQuery(
      "math:log(xs:double('-INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }
}
