package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the month-from-dateTime() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnMonthFromDateTime extends QT3TestSet {

  /**
   *  A test whose essence is: `month-from-dateTime()`. .
   */
  @org.junit.Test
  public void kMonthFromDateTimeFunc1() {
    final XQuery query = new XQuery(
      "month-from-dateTime()",
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
   *  A test whose essence is: `month-from-dateTime((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kMonthFromDateTimeFunc2() {
    final XQuery query = new XQuery(
      "month-from-dateTime((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(month-from-dateTime(()))`. .
   */
  @org.junit.Test
  public void kMonthFromDateTimeFunc3() {
    final XQuery query = new XQuery(
      "empty(month-from-dateTime(()))",
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
   *  A test whose essence is: `month-from-dateTime(()) instance of xs:integer?`. .
   */
  @org.junit.Test
  public void kMonthFromDateTimeFunc4() {
    final XQuery query = new XQuery(
      "month-from-dateTime(()) instance of xs:integer?",
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
   *  A test whose essence is: `month-from-dateTime(xs:dateTime("2001-02-03T08:23:12.43")) eq 2`. .
   */
  @org.junit.Test
  public void kMonthFromDateTimeFunc5() {
    final XQuery query = new XQuery(
      "month-from-dateTime(xs:dateTime(\"2001-02-03T08:23:12.43\")) eq 2",
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
   *  Evaluates The "month-from-dateTime" function As per example 1 (of this fucntion) of the F&O specs .
   */
  @org.junit.Test
  public void fnMonthFromDateTime1() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(xs:dateTime(\"1999-05-31T13:20:00-05:00\"))",
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
   *  Evaluates The "month-from-dateTime" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnMonthFromDateTime11() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(xs:dateTime(\"1000-10-01T02:00:00Z\")) div fn:month-from-dateTime(xs:dateTime(\"0050-05-01T10:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   *  Evaluates The "month-from-dateTime" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnMonthFromDateTime12() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(xs:dateTime(\"1970-01-12T10:00:00Z\")) idiv fn:month-from-dateTime(xs:dateTime(\"1970-02-01T02:00:00Z\"))",
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
   *  Evaluates The "month-from-dateTime" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnMonthFromDateTime13() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(xs:dateTime(\"1970-10-01T10:00:00Z\")) mod fn:month-from-dateTime(xs:dateTime(\"1970-03-01T03:00:00Z\"))",
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
   *  Evaluates The "month-from-dateTime" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnMonthFromDateTime14() {
    final XQuery query = new XQuery(
      "+fn:month-from-dateTime(xs:dateTime(\"1971-01-01T10:00:00Z\"))",
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
   *  Evaluates The "month-from-dateTime" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnMonthFromDateTime15() {
    final XQuery query = new XQuery(
      "-fn:month-from-dateTime(xs:dateTime(\"1970-02-01T10:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-2")
    );
  }

  /**
   *  Evaluates The "month-from-dateTime" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnMonthFromDateTime16() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) eq fn:month-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "month-from-dateTime" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnMonthFromDateTime17() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) ne fn:month-from-dateTime(xs:dateTime(\"1970-02-01T10:00:00Z\"))",
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
   *  Evaluates The "month-from-dateTime" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnMonthFromDateTime18() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(xs:dateTime(\"1970-02-01T10:00:00Z\")) le fn:month-from-dateTime(xs:dateTime(\"1971-01-01T10:00:00Z\"))",
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
   *  Evaluates The "month-from-dateTime" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnMonthFromDateTime19() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(xs:dateTime(\"1971-01-01T10:00:00Z\")) ge fn:month-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "month-from-dateTime" function As per example 2 (for this function)of the F&O specs .
   */
  @org.junit.Test
  public void fnMonthFromDateTime2() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(xs:dateTime(\"1999-12-31T19:20:00-05:00\"))",
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
   *  Evaluates The "month-from-dateTime" function As per example 3 (for this function) of the F&O specs .
   */
  @org.junit.Test
  public void fnMonthFromDateTime3() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(fn:adjust-dateTime-to-timezone(xs:dateTime(\"1999-12-31T19:20:00-05:00\"), xs:dayTimeDuration(\"PT0H\")))",
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
   *  Evaluates The "month-from-dateTime" function used as an argument to an "avg" function. .
   */
  @org.junit.Test
  public void fnMonthFromDateTime4() {
    final XQuery query = new XQuery(
      "fn:avg((fn:month-from-dateTime(xs:dateTime(\"1996-12-31T12:00:00Z\")),fn:month-from-dateTime(xs:dateTime(\"2000-10-31T12:00:00Z\"))))",
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
   *  Evaluates The "month-from-dateTime" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnMonthFromDateTime5() {
    final XQuery query = new XQuery(
      "fn:count(fn:month-from-dateTime(()))",
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
   *  Evaluates The "month-from-dateTime" function that returns a 1. .
   */
  @org.junit.Test
  public void fnMonthFromDateTime6() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(xs:dateTime(\"-1999-01-31T00:20:00-05:00\"))",
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
   *  Evaluates The "month-from-dateTime" function that returns 12. Uses Zulu. .
   */
  @org.junit.Test
  public void fnMonthFromDateTime7() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(xs:dateTime(\"0001-12-31T23:20:00Z\"))",
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
   *  Evaluates The "month-from-dateTime" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnMonthFromDateTime8() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(xs:dateTime(\"1970-01-01T00:00:00Z\")) + fn:month-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   *  Evaluates The "month-from-dateTime" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnMonthFromDateTime9() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(xs:dateTime(\"1970-02-01T00:00:00Z\")) - fn:month-from-dateTime(xs:dateTime(\"1969-01-01T10:00:00Z\"))",
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
   *  Evaluates The "month-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(lower bound) .
   */
  @org.junit.Test
  public void fnMonthFromDateTime1args1() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(xs:dateTime(\"1970-01-01T00:00:00Z\"))",
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
   *  Evaluates The "month-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(mid range) .
   */
  @org.junit.Test
  public void fnMonthFromDateTime1args2() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(xs:dateTime(\"1996-04-07T01:40:52Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   *  Evaluates The "month-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(upper bound) .
   */
  @org.junit.Test
  public void fnMonthFromDateTime1args3() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(xs:dateTime(\"2030-12-31T23:59:59Z\"))",
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
   *  Evaluates The "month-from-dateTime" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnMonthFromDateTimeNew10() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(xs:dateTime(\"1970-03-01T02:00:00Z\")) * fn:month-from-dateTime(xs:dateTime(\"0002-02-01T10:00:00Z\"))",
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
}
