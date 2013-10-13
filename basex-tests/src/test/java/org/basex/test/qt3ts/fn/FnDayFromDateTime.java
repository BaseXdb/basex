package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the day-from-dateTime() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnDayFromDateTime extends QT3TestSet {

  /**
   *  A test whose essence is: `day-from-dateTime()`. .
   */
  @org.junit.Test
  public void kDayFromDateTimeFunc1() {
    final XQuery query = new XQuery(
      "day-from-dateTime()",
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
   *  A test whose essence is: `day-from-dateTime((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kDayFromDateTimeFunc2() {
    final XQuery query = new XQuery(
      "day-from-dateTime((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(day-from-dateTime(()))`. .
   */
  @org.junit.Test
  public void kDayFromDateTimeFunc3() {
    final XQuery query = new XQuery(
      "empty(day-from-dateTime(()))",
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
   *  A test whose essence is: `day-from-dateTime(()) instance of xs:integer?`. .
   */
  @org.junit.Test
  public void kDayFromDateTimeFunc4() {
    final XQuery query = new XQuery(
      "day-from-dateTime(()) instance of xs:integer?",
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
   *  A test whose essence is: `day-from-dateTime(xs:dateTime("2001-02-03T08:23:12.43")) eq 3`. .
   */
  @org.junit.Test
  public void kDayFromDateTimeFunc5() {
    final XQuery query = new XQuery(
      "day-from-dateTime(xs:dateTime(\"2001-02-03T08:23:12.43\")) eq 3",
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
   *  Evaluates The "day-from-dateTime" function As per example 1 (of this fucntion) of the F&O specs .
   */
  @org.junit.Test
  public void fnDayFromDateTime1() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(xs:dateTime(\"1999-05-31T13:20:00-05:00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "31")
    );
  }

  /**
   *  Evaluates The "day-from-dateTime" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnDayFromDateTime11() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(xs:dateTime(\"1000-01-10T02:00:00Z\")) div fn:day-from-dateTime(xs:dateTime(\"0050-01-05T10:00:00Z\"))",
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
   *  Evaluates The "day-from-dateTime" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnDayFromDateTime12() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(xs:dateTime(\"1970-01-10T10:00:00Z\")) idiv fn:day-from-dateTime(xs:dateTime(\"1970-01-10T02:00:00Z\"))",
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
   *  Evaluates The "day-from-dateTime" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnDayFromDateTime13() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(xs:dateTime(\"1970-01-10T10:00:00Z\")) mod fn:day-from-dateTime(xs:dateTime(\"1970-01-03T03:00:00Z\"))",
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
   *  Evaluates The "day-from-dateTime" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnDayFromDateTime14() {
    final XQuery query = new XQuery(
      "+fn:day-from-dateTime(xs:dateTime(\"1971-01-01T10:00:00Z\"))",
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
   *  Evaluates The "day-from-dateTime" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnDayFromDateTime15() {
    final XQuery query = new XQuery(
      "-fn:day-from-dateTime(xs:dateTime(\"1970-01-03T10:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-3")
    );
  }

  /**
   *  Evaluates The "day-from-dateTime" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnDayFromDateTime16() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) eq fn:day-from-dateTime(xs:dateTime(\"1970-01-02T10:00:00Z\"))",
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
   *  Evaluates The "day-from-dateTime" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnDayFromDateTime17() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(xs:dateTime(\"1970-01-02T10:00:00Z\")) ne fn:day-from-dateTime(xs:dateTime(\"1970-02-02T10:00:00Z\"))",
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
   *  Evaluates The "day-from-dateTime" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnDayFromDateTime18() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(xs:dateTime(\"1971-01-03T10:00:00Z\")) ge fn:day-from-dateTime(xs:dateTime(\"1970-01-03T10:00:00Z\"))",
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
   *  Evaluates The "day-from-dateTime" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnDayFromDateTime19() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(xs:dateTime(\"1971-01-10T10:00:00Z\")) ge fn:day-from-dateTime(xs:dateTime(\"1970-01-11T10:00:00Z\"))",
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
   *  Evaluates The "day-from-dateTime" function As per example 2 (for this function)of the F&O specs .
   */
  @org.junit.Test
  public void fnDayFromDateTime2() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(xs:dateTime(\"1999-12-31T20:00:00-05:00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "31")
    );
  }

  /**
   *  Evaluates The "day-from-dateTime" function As per example 3 (for this function) of the F&O specs .
   */
  @org.junit.Test
  public void fnDayFromDateTime3() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(fn:adjust-dateTime-to-timezone(xs:dateTime(\"1999-12-31T19:20:00-05:00\"), xs:dayTimeDuration(\"PT0H\")))",
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
   *  Evaluates The "day-from-dateTime" function used as an argument to an "avg" function. .
   */
  @org.junit.Test
  public void fnDayFromDateTime4() {
    final XQuery query = new XQuery(
      "fn:avg((fn:day-from-dateTime(xs:dateTime(\"1996-12-10T12:00:00Z\")),fn:day-from-dateTime(xs:dateTime(\"2000-12-20T12:00:00Z\"))))",
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
   *  Evaluates The "day-from-dateTime" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnDayFromDateTime5() {
    final XQuery query = new XQuery(
      "fn:count(fn:day-from-dateTime(()))",
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
   *  Evaluates The "day-from-dateTime" function that returns 1. .
   */
  @org.junit.Test
  public void fnDayFromDateTime6() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(xs:dateTime(\"1999-01-01T00:20:00-05:00\"))",
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
   *  Evaluates The "day-from-dateTime" function that returns 31. .
   */
  @org.junit.Test
  public void fnDayFromDateTime7() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(xs:dateTime(\"0001-05-31T23:20:00-05:00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "31")
    );
  }

  /**
   *  Evaluates The "day-from-dateTime" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnDayFromDateTime8() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(xs:dateTime(\"1970-01-03T00:00:00Z\")) + fn:day-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "day-from-dateTime" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnDayFromDateTime9() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(xs:dateTime(\"1970-01-10T00:00:00Z\")) - fn:day-from-dateTime(xs:dateTime(\"1969-01-01T10:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9")
    );
  }

  /**
   *  Evaluates The "day-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(lower bound) .
   */
  @org.junit.Test
  public void fnDayFromDateTime1args1() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(xs:dateTime(\"1970-01-01T00:00:00Z\"))",
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
   *  Evaluates The "day-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(mid range) .
   */
  @org.junit.Test
  public void fnDayFromDateTime1args2() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(xs:dateTime(\"1996-04-07T01:40:52Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "7")
    );
  }

  /**
   *  Evaluates The "day-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(upper bound) .
   */
  @org.junit.Test
  public void fnDayFromDateTime1args3() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(xs:dateTime(\"2030-12-31T23:59:59Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "31")
    );
  }

  /**
   *  Evaluates The "day-from-dateTime" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnDayFromDateTimeNew10() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(xs:dateTime(\"1970-01-02T02:00:00Z\")) * fn:day-from-dateTime(xs:dateTime(\"0002-01-04T10:00:00Z\"))",
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
}
