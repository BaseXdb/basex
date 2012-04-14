package org.basex.test.qt3ts.math;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the math:log10 function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MathLog10 extends QT3TestSet {

  /**
   * Evaluate the function log10() with the argument set to empty sequence.
   */
  @org.junit.Test
  public void mathLog10001() {
    final XQuery query = new XQuery(
      "math:log10(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Evaluate the function log10() with the argument set to 0.
   */
  @org.junit.Test
  public void mathLog10002() {
    final XQuery query = new XQuery(
      "math:log10(0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("xs:double('-INF')")
    );
  }

  /**
   * Evaluate the function log10() with the argument set to 1.0e3.
   */
  @org.junit.Test
  public void mathLog10003() {
    final XQuery query = new XQuery(
      "math:log10(1.0e3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("3.0e0")
    );
  }

  /**
   * Evaluate the function log10() with the argument set to 1.0e-3.
   */
  @org.junit.Test
  public void mathLog10004() {
    final XQuery query = new XQuery(
      "math:log10(1.0e-3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-3.0e0")
    );
  }

  /**
   * Evaluate the function log10() with the argument set to 2.
   */
  @org.junit.Test
  public void mathLog10005() {
    final XQuery query = new XQuery(
      "math:log10(2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0.3010299956639812e0")
    );
  }

  /**
   * Evaluate the function log10() with the argument set to -1.
   */
  @org.junit.Test
  public void mathLog10006() {
    final XQuery query = new XQuery(
      "math:log10(-1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function log10() with the argument set to xs:double('NaN').
   */
  @org.junit.Test
  public void mathLog10007() {
    final XQuery query = new XQuery(
      "math:log10(xs:double('NaN'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Evaluate the function log10() with the argument set to xs:double('INF').
   */
  @org.junit.Test
  public void mathLog10008() {
    final XQuery query = new XQuery(
      "math:log10(xs:double('INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("xs:double('INF')")
    );
  }

  /**
   * Evaluate the function log10() with the argument set to xs:double('-INF').
   */
  @org.junit.Test
  public void mathLog10009() {
    final XQuery query = new XQuery(
      "math:log10(xs:double('-INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }
}
