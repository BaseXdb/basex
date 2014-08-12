package org.basex.qt3ts.math;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the math:pow function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MathPow extends QT3TestSet {

  /**
   * Evaluate the function math:pow() with the arguments () and 93.7.
   */
  @org.junit.Test
  public void mathPow001() {
    final XQuery query = new XQuery(
      "math:pow((), 93.7)",
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
   * Evaluate the function math:pow() with  the arguments 2 and 3.
   */
  @org.junit.Test
  public void mathPow002() {
    final XQuery query = new XQuery(
      "math:pow(2, 3)",
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
      assertEq("8.0e0")
    );
  }

  /**
   * Evaluate the function math:pow() with  the arguments -2 and 3.
   */
  @org.junit.Test
  public void mathPow003() {
    final XQuery query = new XQuery(
      "math:pow(-2, 3)",
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
      assertEq("-8.0e0")
    );
  }

  /**
   * Evaluate the function math:pow() with  the arguments -2 and -3.
   */
  @org.junit.Test
  public void mathPow004() {
    final XQuery query = new XQuery(
      "math:pow(-2, -3)",
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
      assertEq("-0.125e0")
    );
  }

  /**
   * Evaluate the function math:pow() with  the arguments 2 and 0.
   */
  @org.junit.Test
  public void mathPow005() {
    final XQuery query = new XQuery(
      "math:pow(2, 0)",
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
   * Evaluate the function math:pow() with  the arguments 0 and 0.
   */
  @org.junit.Test
  public void mathPow006() {
    final XQuery query = new XQuery(
      "math:pow(0, 0)",
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
   * Evaluate the function math:pow() with  the arguments xs:double('INF') and 0.
   */
  @org.junit.Test
  public void mathPow007() {
    final XQuery query = new XQuery(
      "math:pow(xs:double('INF'), 0)",
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
   * Evaluate the function math:pow() with  the arguments xs:double('NaN') and 0.
   */
  @org.junit.Test
  public void mathPow008() {
    final XQuery query = new XQuery(
      "math:pow(xs:double('NaN'), 0)",
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
   * Evaluate the function math:pow() with  the arguments -math:pi() and 0.
   */
  @org.junit.Test
  public void mathPow009() {
    final XQuery query = new XQuery(
      "math:pow(-math:pi(), 0)",
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
   * Evaluate the function math:pow() with  the arguments 0e0 and 3.
   */
  @org.junit.Test
  public void mathPow010() {
    final XQuery query = new XQuery(
      "math:pow(0e0, 3)",
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
   * Evaluate the function math:pow() with  the arguments 0e0 and 4.
   */
  @org.junit.Test
  public void mathPow011() {
    final XQuery query = new XQuery(
      "math:pow(0e0, 4)",
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
   * Evaluate the function math:pow() with  the arguments -0e0 and 3.
   */
  @org.junit.Test
  public void mathPow012() {
    final XQuery query = new XQuery(
      "math:pow(-0e0, 3)",
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
   * Evaluate the function math:pow() with  the arguments 0 and 4.
   */
  @org.junit.Test
  public void mathPow013() {
    final XQuery query = new XQuery(
      "math:pow(0, 4)",
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
   * Evaluate the function math:pow() with  the arguments 0e0 and -3.
   */
  @org.junit.Test
  public void mathPow014() {
    final XQuery query = new XQuery(
      "math:pow(0e0, -3)",
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
   * Evaluate the function math:pow() with  the arguments 0e0 and -4.
   */
  @org.junit.Test
  public void mathPow015() {
    final XQuery query = new XQuery(
      "math:pow(0e0, -4)",
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
   * Evaluate the function math:pow() with  the arguments -0e0 and -3.
   */
  @org.junit.Test
  public void mathPow016() {
    final XQuery query = new XQuery(
      "math:pow(-0e0, -3)",
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
      assertEq("xs:double('-INF')")
    );
  }

  /**
   * Evaluate the function math:pow() with  the arguments 0 and -4.
   */
  @org.junit.Test
  public void mathPow017() {
    final XQuery query = new XQuery(
      "math:pow(0, -4)",
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
   * Evaluate the function math:pow() with  the arguments 16 and 0.5e0.
   */
  @org.junit.Test
  public void mathPow018() {
    final XQuery query = new XQuery(
      "math:pow(16, 0.5e0)",
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
      assertEq("4.0e0")
    );
  }

  /**
   * Evaluate the function math:pow() with  the arguments 16 and 0.25e0.
   */
  @org.junit.Test
  public void mathPow019() {
    final XQuery query = new XQuery(
      "math:pow(16, 0.25e0)",
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
      assertEq("2.0e0")
    );
  }

  /**
   * Evaluate the function math:pow() with  the arguments 0e0 and -3.0e0.
   */
  @org.junit.Test
  public void mathPow020() {
    final XQuery query = new XQuery(
      "math:pow(0e0, -3.0e0)",
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
   * Evaluate the function math:pow() with  the arguments -0e0 and -3.0e0.
   */
  @org.junit.Test
  public void mathPow021() {
    final XQuery query = new XQuery(
      "math:pow(-0e0, -3.0e0)",
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
      assertEq("xs:double('-INF')")
    );
  }

  /**
   * Evaluate the function math:pow() with  the arguments 0e0 and -3.1e0.
   */
  @org.junit.Test
  public void mathPow022() {
    final XQuery query = new XQuery(
      "math:pow(0e0, -3.1e0)",
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
   * Evaluate the function math:pow() with  the arguments -0e0 and -3.1e0.
   */
  @org.junit.Test
  public void mathPow023() {
    final XQuery query = new XQuery(
      "math:pow(-0e0, -3.1e0)",
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
   * Evaluate the function math:pow() with  the arguments 0e0 3.0e0.
   */
  @org.junit.Test
  public void mathPow024() {
    final XQuery query = new XQuery(
      "math:pow(0e0, 3.0e0)",
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
   * Evaluate the function math:pow() with  the arguments -0e0 and 3.0e0.
   */
  @org.junit.Test
  public void mathPow025() {
    final XQuery query = new XQuery(
      "math:pow(-0e0, 3.0e0)",
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
   * Evaluate the function math:pow() with  the arguments 0e0 3.1e0.
   */
  @org.junit.Test
  public void mathPow026() {
    final XQuery query = new XQuery(
      "math:pow(0e0, 3.1e0)",
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
   * Evaluate the function math:pow() with  the arguments -0e0 and 3.1e0.
   */
  @org.junit.Test
  public void mathPow027() {
    final XQuery query = new XQuery(
      "math:pow(-0e0, 3.1e0)",
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
   * Evaluate the function math:pow() with  the arguments -1 and xs:double('INF').
   */
  @org.junit.Test
  public void mathPow028() {
    final XQuery query = new XQuery(
      "math:pow(-1, xs:double('INF'))",
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
   * Evaluate the function math:pow() with  the arguments -1 and xs:double('-INF').
   */
  @org.junit.Test
  public void mathPow029() {
    final XQuery query = new XQuery(
      "math:pow(-1, xs:double('-INF'))",
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
   * Evaluate the function math:pow() with  the arguments 1 and xs:double('INF').
   */
  @org.junit.Test
  public void mathPow030() {
    final XQuery query = new XQuery(
      "math:pow(1, xs:double('INF'))",
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
   * Evaluate the function math:pow() with the arguments 1 and xs:double('-INF').
   */
  @org.junit.Test
  public void mathPow031() {
    final XQuery query = new XQuery(
      "math:pow(1, xs:double('-INF'))",
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
   * Evaluate the function math:pow() with the arguments 1 and xs:double('NaN').
   */
  @org.junit.Test
  public void mathPow032() {
    final XQuery query = new XQuery(
      "math:pow(1, xs:double('NaN'))",
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
   * Evaluate the function math:pow() with the arguments -2.5 and 2.0e0.
   */
  @org.junit.Test
  public void mathPow033() {
    final XQuery query = new XQuery(
      "math:pow(-2.5e0, 2.0e0)",
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
      assertEq("6.25e0")
    );
  }

  /**
   * Evaluate the function math:pow() with  the arguments -2.5 and 2.0000001.
   */
  @org.junit.Test
  public void mathPow034() {
    final XQuery query = new XQuery(
      "math:pow(-2.5e0, 2.00000001e0)",
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
