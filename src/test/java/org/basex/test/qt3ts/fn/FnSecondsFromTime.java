package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the seconds-from-time() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnSecondsFromTime extends QT3TestSet {

  /**
   *  A test whose essence is: `seconds-from-time()`. .
   */
  @org.junit.Test
  public void kSecondsFromTimeFunc1() {
    final XQuery query = new XQuery(
      "seconds-from-time()",
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
   *  A test whose essence is: `seconds-from-time((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kSecondsFromTimeFunc2() {
    final XQuery query = new XQuery(
      "seconds-from-time((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(seconds-from-time(()))`. .
   */
  @org.junit.Test
  public void kSecondsFromTimeFunc3() {
    final XQuery query = new XQuery(
      "empty(seconds-from-time(()))",
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
   *  A test whose essence is: `seconds-from-time(()) instance of xs:decimal?`. .
   */
  @org.junit.Test
  public void kSecondsFromTimeFunc4() {
    final XQuery query = new XQuery(
      "seconds-from-time(()) instance of xs:decimal?",
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
   *  Simple test invoking seconds-from-time(). .
   */
  @org.junit.Test
  public void kSecondsFromTimeFunc5() {
    final XQuery query = new XQuery(
      "seconds-from-time(xs:time(\"23:11:12.43\")) eq 12.43",
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
   *  Evaluates The "seconds-from-time" function As per example 1 of the F&O specs (for this function) .
   */
  @org.junit.Test
  public void fnSecondsFromTime1() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(xs:time(\"13:20:10.5\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10.5")
    );
  }

  /**
   *  Evaluates The "seconds-from-time" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromTime10() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(xs:time(\"02:02:03Z\")) * fn:seconds-from-time(xs:time(\"10:08:09Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "27")
    );
  }

  /**
   *  Evaluates The "seconds-from-time" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromTime11() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(xs:time(\"22:33:10Z\")) div fn:seconds-from-time(xs:time(\"02:11:02Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5")
    );
  }

  /**
   *  Evaluates The "seconds-from-time" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromTime12() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(xs:time(\"10:12:15Z\")) idiv fn:seconds-from-time(xs:time(\"02:02:03Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5")
    );
  }

  /**
   *  Evaluates The "seconds-from-time" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromTime13() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(xs:time(\"10:10:20Z\")) mod fn:seconds-from-time(xs:time(\"03:03:02Z\"))",
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
   *  Evaluates The "seconds-from-time" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromTime14() {
    final XQuery query = new XQuery(
      "+fn:seconds-from-time(xs:time(\"10:00:01Z\"))",
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
   *  Evaluates The "seconds-from-time" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromTime15() {
    final XQuery query = new XQuery(
      "-fn:seconds-from-time(xs:time(\"10:10:01Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1")
    );
  }

  /**
   *  Evaluates The "seconds-from-time" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnSecondsFromTime16() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(xs:time(\"10:02:01Z\")) eq fn:seconds-from-time(xs:time(\"10:02:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "seconds-from-time" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnSecondsFromTime17() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(xs:time(\"10:00:01Z\")) ne fn:seconds-from-time(xs:time(\"01:01:00Z\"))",
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
   *  Evaluates The "seconds-from-time" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnSecondsFromTime18() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(xs:time(\"10:00:00Z\")) le fn:seconds-from-time(xs:time(\"10:00:00Z\"))",
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
   *  Evaluates The "seconds-from-time" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnSecondsFromTime19() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(xs:time(\"10:03:01Z\")) ge fn:seconds-from-time(xs:time(\"10:04:02Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "seconds-from-time" function used as part of a numeric less than expression (lt operator) .
   */
  @org.junit.Test
  public void fnSecondsFromTime2() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(xs:time(\"21:23:04Z\")) lt fn:seconds-from-time(xs:time(\"21:24:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "seconds-from-time" function as part of a numeric greater than operation (gt operator) .
   */
  @org.junit.Test
  public void fnSecondsFromTime3() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(xs:time(\"01:23:02Z\")) gt fn:seconds-from-time(xs:time(\"01:23:03Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "seconds-from-time" function used as an argument to an avg function. .
   */
  @org.junit.Test
  public void fnSecondsFromTime4() {
    final XQuery query = new XQuery(
      "fn:avg((fn:seconds-from-time(xs:time(\"01:10:20Z\")), fn:seconds-from-time(xs:time(\"01:20:30Z\"))))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "25")
    );
  }

  /**
   *  Evaluates The "seconds-from-time" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnSecondsFromTime5() {
    final XQuery query = new XQuery(
      "fn:count(fn:seconds-from-time(()))",
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
   *  Evaluates The "seconds-from-time" function that returns 0. .
   */
  @org.junit.Test
  public void fnSecondsFromTime6() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"00:59:00Z\"))",
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
   *  Evaluates The "seconds-from-time" function with the argument set to return 59. .
   */
  @org.junit.Test
  public void fnSecondsFromTime7() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(xs:time(\"23:20:59Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "59")
    );
  }

  /**
   *  Evaluates The "seconds-from-time" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromTime8() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(xs:time(\"02:00:00Z\")) + fn:seconds-from-time(xs:time(\"10:00:10Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10")
    );
  }

  /**
   *  Evaluates The "seconds-from-time" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromTime9() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(xs:time(\"10:10:10Z\")) - fn:seconds-from-time(xs:time(\"09:02:07Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   *  Evaluates The "seconds-from-time" function with the arguments set as follows: $arg = xs:time(lower bound) .
   */
  @org.junit.Test
  public void fnSecondsFromTime1args1() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(xs:time(\"00:00:00Z\"))",
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
   *  Evaluates The "seconds-from-time" function with the arguments set as follows: $arg = xs:time(mid range) .
   */
  @org.junit.Test
  public void fnSecondsFromTime1args2() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(xs:time(\"08:03:35Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "35")
    );
  }

  /**
   *  Evaluates The "seconds-from-time" function with the arguments set as follows: $arg = xs:time(upper bound) .
   */
  @org.junit.Test
  public void fnSecondsFromTime1args3() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(xs:time(\"23:59:59Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "59")
    );
  }
}
