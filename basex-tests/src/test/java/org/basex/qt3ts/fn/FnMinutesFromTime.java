package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the minutes-from-time() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnMinutesFromTime extends QT3TestSet {

  /**
   *  A test whose essence is: `minutes-from-time()`. .
   */
  @org.junit.Test
  public void kMinutesFromTimeFunc1() {
    final XQuery query = new XQuery(
      "minutes-from-time()",
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
   *  A test whose essence is: `minutes-from-time((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kMinutesFromTimeFunc2() {
    final XQuery query = new XQuery(
      "minutes-from-time((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(minutes-from-time(()))`. .
   */
  @org.junit.Test
  public void kMinutesFromTimeFunc3() {
    final XQuery query = new XQuery(
      "empty(minutes-from-time(()))",
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
   *  A test whose essence is: `minutes-from-time(()) instance of xs:integer?`. .
   */
  @org.junit.Test
  public void kMinutesFromTimeFunc4() {
    final XQuery query = new XQuery(
      "minutes-from-time(()) instance of xs:integer?",
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
   *  A test whose essence is: `minutes-from-time(xs:time("23:11:12.43")) eq 11`. .
   */
  @org.junit.Test
  public void kMinutesFromTimeFunc5() {
    final XQuery query = new XQuery(
      "minutes-from-time(xs:time(\"23:11:12.43\")) eq 11",
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
   *  Evaluates The "minutes-from-time" function As per example 1 of the F&O specs (for this function) .
   */
  @org.junit.Test
  public void fnMinutesFromTime1() {
    final XQuery query = new XQuery(
      "fn:minutes-from-time(xs:time(\"13:00:00Z\"))",
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
   *  Evaluates The "minutes-from-time" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromTime10() {
    final XQuery query = new XQuery(
      "fn:minutes-from-time(xs:time(\"02:02:00Z\")) * fn:minutes-from-time(xs:time(\"10:08:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "16")
    );
  }

  /**
   *  Evaluates The "minutes-from-time" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromTime11() {
    final XQuery query = new XQuery(
      "fn:minutes-from-time(xs:time(\"22:33:00Z\")) div fn:minutes-from-time(xs:time(\"02:11:00Z\"))",
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
   *  Evaluates The "minutes-from-time" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromTime12() {
    final XQuery query = new XQuery(
      "fn:minutes-from-time(xs:time(\"10:12:00Z\")) idiv fn:minutes-from-time(xs:time(\"02:02:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "6")
    );
  }

  /**
   *  Evaluates The "minutes-from-time" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromTime13() {
    final XQuery query = new XQuery(
      "fn:minutes-from-time(xs:time(\"10:10:00Z\")) mod fn:minutes-from-time(xs:time(\"03:03:00Z\"))",
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
   *  Evaluates The "minutes-from-time" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromTime14() {
    final XQuery query = new XQuery(
      "+fn:minutes-from-time(xs:time(\"10:00:00Z\"))",
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
   *  Evaluates The "minutes-from-time" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromTime15() {
    final XQuery query = new XQuery(
      "-fn:minutes-from-time(xs:time(\"10:10:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-10")
    );
  }

  /**
   *  Evaluates The "minutes-from-time" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnMinutesFromTime16() {
    final XQuery query = new XQuery(
      "fn:minutes-from-time(xs:time(\"10:02:00Z\")) eq fn:minutes-from-time(xs:time(\"10:02:00Z\"))",
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
   *  Evaluates The "minutes-from-time" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnMinutesFromTime17() {
    final XQuery query = new XQuery(
      "fn:minutes-from-time(xs:time(\"10:00:00Z\")) ne fn:minutes-from-time(xs:time(\"01:01:00Z\"))",
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
   *  Evaluates The "minutes-from-time" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnMinutesFromTime18() {
    final XQuery query = new XQuery(
      "fn:minutes-from-time(xs:time(\"10:00:00Z\")) le fn:minutes-from-time(xs:time(\"10:00:00Z\"))",
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
   *  Evaluates The "minutes-from-time" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnMinutesFromTime19() {
    final XQuery query = new XQuery(
      "fn:minutes-from-time(xs:time(\"10:03:00Z\")) ge fn:minutes-from-time(xs:time(\"10:04:00Z\"))",
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
   *  Evaluates The "minutes-from-time" function used as part of a numeric less than expression (lt operator) .
   */
  @org.junit.Test
  public void fnMinutesFromTime2() {
    final XQuery query = new XQuery(
      "fn:minutes-from-time(xs:time(\"21:23:00Z\")) lt fn:minutes-from-time(xs:time(\"21:24:00Z\"))",
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
   *  Evaluates The "minutes-from-time" function as part of a numeric greater than operation (gt operator) .
   */
  @org.junit.Test
  public void fnMinutesFromTime3() {
    final XQuery query = new XQuery(
      "fn:minutes-from-time(xs:time(\"01:23:00Z\")) gt fn:minutes-from-time(xs:time(\"01:23:00Z\"))",
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
   *  Evaluates The "minutes-from-time" function used as an argument to an avg function. .
   */
  @org.junit.Test
  public void fnMinutesFromTime4() {
    final XQuery query = new XQuery(
      "fn:avg((fn:minutes-from-time(xs:time(\"01:10:00Z\")), fn:minutes-from-time(xs:time(\"01:20:00Z\"))))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "15")
    );
  }

  /**
   *  Evaluates The "minutes-from-time" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnMinutesFromTime5() {
    final XQuery query = new XQuery(
      "fn:count(fn:minutes-from-time(()))",
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
   *  Evaluates The "hours-from-time" function that returns 59. .
   */
  @org.junit.Test
  public void fnMinutesFromTime6() {
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
   *  Evaluates The "minutes-from-time" function used as part of an abs function. .
   */
  @org.junit.Test
  public void fnMinutesFromTime7() {
    final XQuery query = new XQuery(
      "fn:abs(fn:minutes-from-time(xs:time(\"23:20:00Z\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "20")
    );
  }

  /**
   *  Evaluates The "minutes-from-time" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromTime8() {
    final XQuery query = new XQuery(
      "fn:minutes-from-time(xs:time(\"02:00:00Z\")) + fn:minutes-from-time(xs:time(\"10:00:00Z\"))",
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
   *  Evaluates The "minutes-from-time" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromTime9() {
    final XQuery query = new XQuery(
      "fn:minutes-from-time(xs:time(\"10:10:00Z\")) - fn:minutes-from-time(xs:time(\"09:02:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "8")
    );
  }

  /**
   *  Evaluates The "minutes-from-time" function with the arguments set as follows: $arg = xs:time(lower bound) .
   */
  @org.junit.Test
  public void fnMinutesFromTime1args1() {
    final XQuery query = new XQuery(
      "fn:minutes-from-time(xs:time(\"00:00:00Z\"))",
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
   *  Evaluates The "minutes-from-time" function with the arguments set as follows: $arg = xs:time(mid range) .
   */
  @org.junit.Test
  public void fnMinutesFromTime1args2() {
    final XQuery query = new XQuery(
      "fn:minutes-from-time(xs:time(\"08:03:35Z\"))",
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
   *  Evaluates The "minutes-from-time" function with the arguments set as follows: $arg = xs:time(upper bound) .
   */
  @org.junit.Test
  public void fnMinutesFromTime1args3() {
    final XQuery query = new XQuery(
      "fn:minutes-from-time(xs:time(\"23:59:59Z\"))",
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
