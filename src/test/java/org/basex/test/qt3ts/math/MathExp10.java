package org.basex.test.qt3ts.math;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the math:exp10 function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("3.1622776601683795e0")
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0.0e0")
    );
  }
}
