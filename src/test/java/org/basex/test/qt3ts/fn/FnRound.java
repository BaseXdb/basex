package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the round() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnRound extends QT3TestSet {

  /**
   *  A test whose essence is: `round()`. .
   */
  @org.junit.Test
  public void kRoundFunc1() {
    final XQuery query = new XQuery(
      "round()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `round(1, 2)`. .
   */
  @org.junit.Test
  public void kRoundFunc2a() {
    final XQuery query = new XQuery(
      "round(1, 2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  A test whose essence is: `empty(round(()))`. .
   */
  @org.junit.Test
  public void kRoundFunc3() {
    final XQuery query = new XQuery(
      "empty(round(()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `round(1) eq 1`. .
   */
  @org.junit.Test
  public void kRoundFunc4() {
    final XQuery query = new XQuery(
      "round(1) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `round(1.1) eq 1`. .
   */
  @org.junit.Test
  public void kRoundFunc5() {
    final XQuery query = new XQuery(
      "round(1.1) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `round(xs:double(1)) eq 1`. .
   */
  @org.junit.Test
  public void kRoundFunc6() {
    final XQuery query = new XQuery(
      "round(xs:double(1)) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `round(xs:float(1)) eq 1`. .
   */
  @org.junit.Test
  public void kRoundFunc7() {
    final XQuery query = new XQuery(
      "round(xs:float(1)) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `round(2.4999) eq 2`. .
   */
  @org.junit.Test
  public void kRoundFunc8() {
    final XQuery query = new XQuery(
      "round(2.4999) eq 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `round(-2.5) eq -2`. .
   */
  @org.junit.Test
  public void kRoundFunc9() {
    final XQuery query = new XQuery(
      "round(-2.5) eq -2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure the return type is properly inferred for unsignedShort. .
   */
  @org.junit.Test
  public void k2RoundFunc1() {
    final XQuery query = new XQuery(
      "round(xs:unsignedShort(.)) instance of xs:unsignedShort",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Ensure the return type is properly inferred for int. .
   */
  @org.junit.Test
  public void k2RoundFunc10() {
    final XQuery query = new XQuery(
      "round(xs:int(.)) instance of xs:int",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Invoke on 0.54, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc100() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.54\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on -0.54, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc101() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.54\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on -0.54, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc102() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.54\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on 0.55, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc103() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.55\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on 0.55, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc104() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.55\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on -0.55, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc105() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.55\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on -0.55, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc106() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.55\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on 0.56, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc107() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.56\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on 0.56, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc108() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.56\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on -0.56, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc109() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.56\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Ensure the return type is properly inferred for short. .
   */
  @org.junit.Test
  public void k2RoundFunc11() {
    final XQuery query = new XQuery(
      "round(xs:short(.)) instance of xs:short",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Invoke on -0.56, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc110() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.56\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on 0.59, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc111() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.59\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on 0.59, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc112() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.59\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on -0.59, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc113() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.59\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on -0.59, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc114() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.59\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on 0.50, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc115() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.50\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on 0.50, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc116() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.50\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on -0.50, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc117() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.50\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.50, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc118() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.50\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.61, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc119() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.61\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Ensure the return type is properly inferred for byte. .
   */
  @org.junit.Test
  public void k2RoundFunc12() {
    final XQuery query = new XQuery(
      "round(xs:byte(.)) instance of xs:byte",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Invoke on 0.61, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc120() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.61\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on -0.61, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc121() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.61\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on -0.61, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc122() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.61\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on 0.64, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc123() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.64\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on 0.64, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc124() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.64\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on -0.64, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc125() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.64\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on -0.64, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc126() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.64\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on 0.65, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc127() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.65\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on 0.65, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc128() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.65\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on -0.65, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc129() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.65\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on xs:double INF. .
   */
  @org.junit.Test
  public void k2RoundFunc13() {
    final XQuery query = new XQuery(
      "round(xs:double(\"INF\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "INF")
    );
  }

  /**
   *  Invoke on -0.65, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc130() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.65\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on 0.66, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc131() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.66\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on 0.66, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc132() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.66\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on -0.66, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc133() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.66\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on -0.66, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc134() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.66\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on 0.69, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc135() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.69\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on 0.69, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc136() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.69\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on -0.69, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc137() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.69\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on -0.69, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc138() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.69\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on 0.60, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc139() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.60\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on xs:double -INF. .
   */
  @org.junit.Test
  public void k2RoundFunc14() {
    final XQuery query = new XQuery(
      "round(xs:double(\"-INF\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-INF")
    );
  }

  /**
   *  Invoke on 0.60, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc140() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.60\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on -0.60, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc141() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.60\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on -0.60, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc142() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.60\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on 0.91, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc143() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.91\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on 0.91, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc144() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.91\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on -0.91, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc145() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.91\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on -0.91, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc146() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.91\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on 0.94, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc147() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.94\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on 0.94, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc148() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.94\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on -0.94, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc149() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.94\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on xs:double 0. .
   */
  @org.junit.Test
  public void k2RoundFunc15() {
    final XQuery query = new XQuery(
      "round(xs:double(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.94, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc150() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.94\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on 0.95, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc151() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.95\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on 0.95, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc152() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.95\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on -0.95, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc153() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.95\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on -0.95, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc154() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.95\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on 0.96, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc155() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.96\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on 0.96, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc156() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.96\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on -0.96, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc157() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.96\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on -0.96, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc158() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.96\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on 0.99, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc159() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.99\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on xs:double -0. .
   */
  @org.junit.Test
  public void k2RoundFunc16() {
    final XQuery query = new XQuery(
      "round(xs:double(\"-0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.99, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc160() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.99\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on -0.99, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc161() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.99\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on -0.99, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc162() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.99\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on 0.90, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc163() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.90\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on 0.90, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc164() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.90\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Invoke on -0.90, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc165() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.90\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on -0.90, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc166() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.90\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on 0.101, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc167() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.101\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Invoke on 0.101, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc168() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.101\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.101, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc169() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.101\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on xs:float INF. .
   */
  @org.junit.Test
  public void k2RoundFunc17() {
    final XQuery query = new XQuery(
      "round(xs:float(\"INF\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "INF")
    );
  }

  /**
   *  Invoke on -0.101, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc170() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.101\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.104, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc171() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.104\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.104, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc172() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.104\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.104, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc173() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.104\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.104, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc174() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.104\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.105, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc175() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.105\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.105, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc176() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.105\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.105, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc177() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.105\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.105, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc178() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.105\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.106, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc179() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.106\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on xs:float -INF. .
   */
  @org.junit.Test
  public void k2RoundFunc18() {
    final XQuery query = new XQuery(
      "round(xs:float(\"-INF\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-INF")
    );
  }

  /**
   *  Invoke on 0.106, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc180() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.106\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.106, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc181() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.106\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.106, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc182() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.106\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.109, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc183() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.109\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.109, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc184() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.109\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.109, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc185() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.109\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.109, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc186() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.109\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.100, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc187() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.100\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.100, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc188() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.100\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.100, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc189() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.100\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on xs:float 0. .
   */
  @org.junit.Test
  public void k2RoundFunc19() {
    final XQuery query = new XQuery(
      "round(xs:float(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.100, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc190() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.100\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Ensure the return type is properly inferred for unsignedLong. .
   */
  @org.junit.Test
  public void k2RoundFunc2() {
    final XQuery query = new XQuery(
      "round(xs:unsignedLong(.)) instance of xs:unsignedLong",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Invoke on xs:float -0. .
   */
  @org.junit.Test
  public void k2RoundFunc20() {
    final XQuery query = new XQuery(
      "round(xs:float(\"-0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on xs:float NaN. .
   */
  @org.junit.Test
  public void k2RoundFunc21() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"NaN\"))",
      ctx);
    try {
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
   *  Invoke on xs:double NaN. .
   */
  @org.junit.Test
  public void k2RoundFunc22() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"NaN\"))",
      ctx);
    try {
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
   *  Invoke on 0.01, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc23() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.01\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Invoke on 0.01, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc24() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.01\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Invoke on -0.01, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc25() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.01\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-0")
    );
  }

  /**
   *  Invoke on -0.01, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc26() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.01\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.04, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc27() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.04\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.04, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc28() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.04\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.04, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc29() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.04\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Ensure the return type is properly inferred for unsignedInt. .
   */
  @org.junit.Test
  public void k2RoundFunc3() {
    final XQuery query = new XQuery(
      "round(xs:unsignedInt(.)) instance of xs:unsignedInt",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Invoke on -0.04, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc30() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.04\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.05, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc31() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.05\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.05, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc32() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.05\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.05, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc33() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.05\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.05, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc34() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.05\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.06, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc35() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.06\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.06, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc36() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.06\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.06, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc37() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.06\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.06, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc38() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.06\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.09, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc39() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.09\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Ensure the return type is properly inferred for unsignedByte. .
   */
  @org.junit.Test
  public void k2RoundFunc4() {
    final XQuery query = new XQuery(
      "round(xs:unsignedByte(.)) instance of xs:unsignedByte",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Invoke on 0.09, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc40() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.09\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.09, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc41() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.09\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.09, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc42() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.09\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.00, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc43() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.00, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc44() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.00, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc45() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.00, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc46() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.11, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc47() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.11\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.11, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc48() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.11\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.11, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc49() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.11\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Ensure the return type is properly inferred for positiveInteger. .
   */
  @org.junit.Test
  public void k2RoundFunc5() {
    final XQuery query = new XQuery(
      "round(xs:positiveInteger(.)) instance of xs:positiveInteger",
      ctx);
    try {
      query.context(node(file("fn/abs/e1.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Invoke on -0.11, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc50() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.11\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.14, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc51() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.14\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.14, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc52() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.14\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.14, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc53() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.14\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.14, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc54() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.14\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertType("xs:double")
      &&
        assertStringValue(false, "-0")
      )
    );
  }

  /**
   *  Invoke on 0.15, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc55() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.15\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.15, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc56() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.15\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.15, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc57() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.15\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.15, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc58() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.15\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.16, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc59() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.16\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Ensure the return type is properly inferred for nonPositiveInteger. .
   */
  @org.junit.Test
  public void k2RoundFunc6() {
    final XQuery query = new XQuery(
      "round(xs:nonPositiveInteger(.)) instance of xs:nonPositiveInteger",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Invoke on 0.16, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc60() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.16\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.16, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc61() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.16\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.16, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc62() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.16\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.19, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc63() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.19\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.19, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc64() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.19\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.19, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc65() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.19\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.19, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc66() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.19\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.10, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc67() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.10\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.10, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc68() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.10\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.10, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc69() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.10\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Ensure the return type is properly inferred for nonNegativeInteger. .
   */
  @org.junit.Test
  public void k2RoundFunc7() {
    final XQuery query = new XQuery(
      "round(xs:nonNegativeInteger(.)) instance of xs:nonNegativeInteger",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Invoke on -0.10, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc70() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.10\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.41, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc71() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.41\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.41, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc72() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.41\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.41, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc73() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.41\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.41, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc74() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.41\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.44, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc75() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.44\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.44, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc76() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.44\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.44, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc77() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.44\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.44, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc78() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.44\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.45, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc79() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.45\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Ensure the return type is properly inferred for negativeInteger. .
   */
  @org.junit.Test
  public void k2RoundFunc8() {
    final XQuery query = new XQuery(
      "round(xs:negativeInteger(.)) instance of xs:negativeInteger",
      ctx);
    try {
      query.context(node(file("fn/abs/e-1.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Invoke on 0.45, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc80() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.45\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.45, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc81() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.45\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.45, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc82() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.45\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.46, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc83() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.46\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.46, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc84() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.46\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.46, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc85() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.46\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.46, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc86() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.46\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.49, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc87() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.49\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.49, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc88() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.49\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.49, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc89() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.49\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Ensure the return type is properly inferred for long. .
   */
  @org.junit.Test
  public void k2RoundFunc9() {
    final XQuery query = new XQuery(
      "round(xs:long(.)) instance of xs:long",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Invoke on -0.49, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc90() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.49\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.40, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc91() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.40\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on 0.40, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc92() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.40\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Invoke on -0.40, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc93() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.40\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on -0.40, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc94() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.40\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-0")
    );
  }

  /**
   *  Invoke on 0.51, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc95() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.51\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Invoke on 0.51, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc96() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0.51\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Invoke on -0.51, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc97() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-0.51\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on -0.51, type xs:double. .
   */
  @org.junit.Test
  public void k2RoundFunc98() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-0.51\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Invoke on 0.54, type xs:float. .
   */
  @org.junit.Test
  public void k2RoundFunc99() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0.54\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  check dynamic type of fn:round on argument of union of numeric types. .
   */
  @org.junit.Test
  public void fnRound1() {
    final XQuery query = new XQuery(
      "for $x in (1, xs:decimal(2), xs:float(3), xs:double(4)) \n" +
      "            return if ((round($x)) instance of xs:integer) then \"integer\" \n" +
      "           else if ((round($x)) instance of xs:decimal) then \"decimal\" \n" +
      "           else if ((round($x)) instance of xs:float) then \"float\"\n" +
      "           else if ((round($x)) instance of xs:double) then \"double\" \n" +
      "           else error()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"integer\", \"decimal\", \"float\", \"double\"")
    );
  }

  /**
   *  round() applied to xs:decimal .
   */
  @org.junit.Test
  public void fnRoundDecimal10() {
    final XQuery query = new XQuery(
      "fn:round(-12.567, 0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-13")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   *  round() applied to xs:decimal .
   */
  @org.junit.Test
  public void fnRoundDecimal11() {
    final XQuery query = new XQuery(
      "fn:round(-1234.567, -2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-1200")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   *  round() applied to xs:decimal .
   */
  @org.junit.Test
  public void fnRoundDecimal12() {
    final XQuery query = new XQuery(
      "fn:round(1.567, -3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   *  round() applied to xs:decimal .
   */
  @org.junit.Test
  public void fnRoundDecimal2() {
    final XQuery query = new XQuery(
      "fn:round(xs:decimal(\"12.1\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("12")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   *  round() applied to xs:decimal .
   */
  @org.junit.Test
  public void fnRoundDecimal3() {
    final XQuery query = new XQuery(
      "fn:round(xs:decimal(\"12.7\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("13")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   *  round() applied to xs:decimal .
   */
  @org.junit.Test
  public void fnRoundDecimal4() {
    final XQuery query = new XQuery(
      "fn:round(xs:decimal(\"12.5\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("13")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   *  round() applied to xs:decimal .
   */
  @org.junit.Test
  public void fnRoundDecimal5() {
    final XQuery query = new XQuery(
      "fn:round(xs:decimal(\"-12.7\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-13")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   *  round() applied to xs:decimal .
   */
  @org.junit.Test
  public void fnRoundDecimal6() {
    final XQuery query = new XQuery(
      "fn:round(xs:decimal(\"-12.1\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-12")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   *  round() applied to xs:decimal .
   */
  @org.junit.Test
  public void fnRoundDecimal7() {
    final XQuery query = new XQuery(
      "fn:round(xs:decimal(\"-12.5\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-12")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   *  round() applied to xs:decimal .
   */
  @org.junit.Test
  public void fnRoundDecimal8() {
    final XQuery query = new XQuery(
      "fn:round(-12.567, 2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-12.57")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   *  round() applied to xs:decimal .
   */
  @org.junit.Test
  public void fnRoundDecimal9() {
    final XQuery query = new XQuery(
      "fn:round(-12.567, 4)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-12.567")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Tests the two-argument form of round().
   */
  @org.junit.Test
  public void fnRound2args1() {
    final XQuery query = new XQuery(
      "fn:round(1.125, 2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("1.13")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Tests the two-argument form of round().
   */
  @org.junit.Test
  public void fnRound2args2() {
    final XQuery query = new XQuery(
      "fn:round(8452, -2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("8500")
    );
  }

  /**
   * Tests the two-argument form of round().
   */
  @org.junit.Test
  public void fnRound2args3() {
    final XQuery query = new XQuery(
      "fn:round(3.1415e0, 2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3.14e0")
    );
  }

  /**
   * Tests the two-argument form of round().
   */
  @org.junit.Test
  public void fnRound2args4() {
    final XQuery query = new XQuery(
      "fn:round(35.425, 2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("35.43")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnRounddbl1args1() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"-1.7976931348623157E308\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnRounddbl1args2() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnRounddbl1args3() {
    final XQuery query = new XQuery(
      "fn:round(xs:double(\"1.7976931348623157E308\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnRounddec1args1() {
    final XQuery query = new XQuery(
      "fn:round(xs:decimal(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnRounddec1args2() {
    final XQuery query = new XQuery(
      "fn:round(xs:decimal(\"617375191608514839\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("617375191608514839")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnRounddec1args3() {
    final XQuery query = new XQuery(
      "fn:round(xs:decimal(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnRoundflt1args1() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"-3.4028235E38\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(\"-3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnRoundflt1args2() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertType("xs:float")
      &&
        assertEq("0")
      )
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnRoundflt1args3() {
    final XQuery query = new XQuery(
      "fn:round(xs:float(\"3.4028235E38\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(\"3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnRoundint1args1() {
    final XQuery query = new XQuery(
      "fn:round(xs:int(\"-2147483648\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-2147483648")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnRoundint1args2() {
    final XQuery query = new XQuery(
      "fn:round(xs:int(\"-1873914410\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1873914410")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnRoundint1args3() {
    final XQuery query = new XQuery(
      "fn:round(xs:int(\"2147483647\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2147483647")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnRoundintg1args1() {
    final XQuery query = new XQuery(
      "fn:round(xs:integer(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnRoundintg1args2() {
    final XQuery query = new XQuery(
      "fn:round(xs:integer(\"830993497117024304\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("830993497117024304")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnRoundintg1args3() {
    final XQuery query = new XQuery(
      "fn:round(xs:integer(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnRoundlng1args1() {
    final XQuery query = new XQuery(
      "fn:round(xs:long(\"-92233720368547758\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnRoundlng1args2() {
    final XQuery query = new XQuery(
      "fn:round(xs:long(\"-47175562203048468\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-47175562203048468")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnRoundlng1args3() {
    final XQuery query = new XQuery(
      "fn:round(xs:long(\"92233720368547758\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("92233720368547758")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnRoundnint1args1() {
    final XQuery query = new XQuery(
      "fn:round(xs:negativeInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnRoundnint1args2() {
    final XQuery query = new XQuery(
      "fn:round(xs:negativeInteger(\"-297014075999096793\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-297014075999096793")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnRoundnint1args3() {
    final XQuery query = new XQuery(
      "fn:round(xs:negativeInteger(\"-1\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnRoundnni1args1() {
    final XQuery query = new XQuery(
      "fn:round(xs:nonNegativeInteger(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnRoundnni1args2() {
    final XQuery query = new XQuery(
      "fn:round(xs:nonNegativeInteger(\"303884545991464527\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("303884545991464527")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnRoundnni1args3() {
    final XQuery query = new XQuery(
      "fn:round(xs:nonNegativeInteger(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnRoundnpi1args1() {
    final XQuery query = new XQuery(
      "fn:round(xs:nonPositiveInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnRoundnpi1args2() {
    final XQuery query = new XQuery(
      "fn:round(xs:nonPositiveInteger(\"-475688437271870490\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-475688437271870490")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnRoundnpi1args3() {
    final XQuery query = new XQuery(
      "fn:round(xs:nonPositiveInteger(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnRoundpint1args1() {
    final XQuery query = new XQuery(
      "fn:round(xs:positiveInteger(\"1\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnRoundpint1args2() {
    final XQuery query = new XQuery(
      "fn:round(xs:positiveInteger(\"52704602390610033\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("52704602390610033")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnRoundpint1args3() {
    final XQuery query = new XQuery(
      "fn:round(xs:positiveInteger(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnRoundsht1args1() {
    final XQuery query = new XQuery(
      "fn:round(xs:short(\"-32768\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnRoundsht1args2() {
    final XQuery query = new XQuery(
      "fn:round(xs:short(\"-5324\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-5324")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnRoundsht1args3() {
    final XQuery query = new XQuery(
      "fn:round(xs:short(\"32767\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("32767")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnRoundulng1args1() {
    final XQuery query = new XQuery(
      "fn:round(xs:unsignedLong(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnRoundulng1args2() {
    final XQuery query = new XQuery(
      "fn:round(xs:unsignedLong(\"130747108607674654\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("130747108607674654")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnRoundulng1args3() {
    final XQuery query = new XQuery(
      "fn:round(xs:unsignedLong(\"184467440737095516\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("184467440737095516")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnRoundusht1args1() {
    final XQuery query = new XQuery(
      "fn:round(xs:unsignedShort(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnRoundusht1args2() {
    final XQuery query = new XQuery(
      "fn:round(xs:unsignedShort(\"44633\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("44633")
    );
  }

  /**
   *  Evaluates The "round" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnRoundusht1args3() {
    final XQuery query = new XQuery(
      "fn:round(xs:unsignedShort(\"65535\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("65535")
    );
  }
}
