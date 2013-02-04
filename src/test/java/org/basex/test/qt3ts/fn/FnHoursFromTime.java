package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the hours-from-time() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnHoursFromTime extends QT3TestSet {

  /**
   *  A test whose essence is: `hours-from-time()`. .
   */
  @org.junit.Test
  public void kHoursFromTimeFunc1() {
    final XQuery query = new XQuery(
      "hours-from-time()",
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
   *  A test whose essence is: `hours-from-time((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kHoursFromTimeFunc2() {
    final XQuery query = new XQuery(
      "hours-from-time((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(hours-from-time(()))`. .
   */
  @org.junit.Test
  public void kHoursFromTimeFunc3() {
    final XQuery query = new XQuery(
      "empty(hours-from-time(()))",
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
   *  A test whose essence is: `hours-from-time(()) instance of xs:integer?`. .
   */
  @org.junit.Test
  public void kHoursFromTimeFunc4() {
    final XQuery query = new XQuery(
      "hours-from-time(()) instance of xs:integer?",
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
   *  A test whose essence is: `hours-from-time(xs:time("23:11:12.43")) eq 23`. .
   */
  @org.junit.Test
  public void kHoursFromTimeFunc5() {
    final XQuery query = new XQuery(
      "hours-from-time(xs:time(\"23:11:12.43\")) eq 23",
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
   *  Evaluates The "hours-from-time" function As per example 1 of the F&O specs .
   */
  @org.junit.Test
  public void fnHoursFromTime1() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"11:23:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "11")
    );
  }

  /**
   *  Evaluates The "hours-from-time" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnHoursFromTime10() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"02:00:00Z\")) * fn:hours-from-time(xs:time(\"10:00:00Z\"))",
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
   *  Evaluates The "hours-from-time" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnHoursFromTime11() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"22:00:00Z\")) div fn:hours-from-time(xs:time(\"02:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "11")
    );
  }

  /**
   *  Evaluates The "hours-from-time" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnHoursFromTime12() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"10:00:00Z\")) idiv fn:hours-from-time(xs:time(\"02:00:00Z\"))",
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
   *  Evaluates The "hours-from-time" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnHoursFromTime13() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"10:00:00Z\")) mod fn:hours-from-time(xs:time(\"03:00:00Z\"))",
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
   *  Evaluates The "hours-from-time" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnHoursFromTime14() {
    final XQuery query = new XQuery(
      "+fn:hours-from-time(xs:time(\"10:00:00Z\"))",
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
   *  Evaluates The "hours-from-time" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnHoursFromTime15() {
    final XQuery query = new XQuery(
      "-fn:hours-from-time(xs:time(\"10:00:00Z\"))",
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
   *  Evaluates The "hours-from-time" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnHoursFromTime16() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"10:00:00Z\")) eq fn:hours-from-time(xs:time(\"10:00:00Z\"))",
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
   *  Evaluates The "hours-from-time" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnHoursFromTime17() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"10:00:00Z\")) ne fn:hours-from-time(xs:time(\"01:00:00Z\"))",
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
   *  Evaluates The "hours-from-time" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnHoursFromTime18() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"10:00:00Z\")) le fn:hours-from-time(xs:time(\"10:00:00Z\"))",
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
   *  Evaluates The "hours-from-time" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnHoursFromTime19() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"10:00:00Z\")) ge fn:hours-from-time(xs:time(\"10:00:00Z\"))",
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
   *  Evaluates The "hours-from-time" function As per example 2 of the F&O specs .
   */
  @org.junit.Test
  public void fnHoursFromTime2() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"21:23:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "21")
    );
  }

  /**
   *  Evaluates The "hours-from-time" function As per example 3 of the F&O specs .
   */
  @org.junit.Test
  public void fnHoursFromTime3() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"01:23:00+05:00\"))",
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
   *  Evaluates The "hours-from-time" function As per example 4 of the F&O specs .
   */
  @org.junit.Test
  public void fnHoursFromTime4() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(fn:adjust-time-to-timezone(xs:time(\"01:23:00+05:00\"), xs:dayTimeDuration(\"PT0H\")))",
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
   *  Evaluates The "hours-from-time" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnHoursFromTime5() {
    final XQuery query = new XQuery(
      "fn:count(fn:hours-from-time(()))",
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
   *  Evaluates The "hours-from-time" function that returns 0. .
   */
  @org.junit.Test
  public void fnHoursFromTime6() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"00:20:00Z\"))",
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
   *  Evaluates The "hours-from-time" function that returns 23. .
   */
  @org.junit.Test
  public void fnHoursFromTime7() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"23:20:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "23")
    );
  }

  /**
   *  Evaluates The "hours-from-time" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnHoursFromTime8() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"02:00:00Z\")) + fn:hours-from-time(xs:time(\"10:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "12")
    );
  }

  /**
   *  Evaluates The "hours-from-time" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnHoursFromTime9() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"10:00:00Z\")) - fn:hours-from-time(xs:time(\"09:00:00Z\"))",
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
   *  Evaluates The "hours-from-time" function with the arguments set as follows: $arg = xs:time(lower bound) .
   */
  @org.junit.Test
  public void fnHoursFromTime1args1() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"00:00:00Z\"))",
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
   *  Evaluates The "hours-from-time" function with the arguments set as follows: $arg = xs:time(mid range) .
   */
  @org.junit.Test
  public void fnHoursFromTime1args2() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"08:03:35Z\"))",
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
   *  Evaluates The "hours-from-time" function with the arguments set as follows: $arg = xs:time(upper bound) .
   */
  @org.junit.Test
  public void fnHoursFromTime1args3() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(xs:time(\"23:59:59Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "23")
    );
  }
}
