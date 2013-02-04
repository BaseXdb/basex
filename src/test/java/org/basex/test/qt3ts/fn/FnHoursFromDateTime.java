package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the hours-from-dateTime() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnHoursFromDateTime extends QT3TestSet {

  /**
   *  A test whose essence is: `hours-from-dateTime()`. .
   */
  @org.junit.Test
  public void kHoursFromDateTimeFunc1() {
    final XQuery query = new XQuery(
      "hours-from-dateTime()",
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
   *  A test whose essence is: `hours-from-dateTime((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kHoursFromDateTimeFunc2() {
    final XQuery query = new XQuery(
      "hours-from-dateTime((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(hours-from-dateTime(()))`. .
   */
  @org.junit.Test
  public void kHoursFromDateTimeFunc3() {
    final XQuery query = new XQuery(
      "empty(hours-from-dateTime(()))",
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
   *  A test whose essence is: `hours-from-dateTime(()) instance of xs:integer?`. .
   */
  @org.junit.Test
  public void kHoursFromDateTimeFunc4() {
    final XQuery query = new XQuery(
      "hours-from-dateTime(()) instance of xs:integer?",
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
   *  A test whose essence is: `hours-from-dateTime(xs:dateTime("2001-02-03T08:23:12.43")) eq 8`. .
   */
  @org.junit.Test
  public void kHoursFromDateTimeFunc5() {
    final XQuery query = new XQuery(
      "hours-from-dateTime(xs:dateTime(\"2001-02-03T08:23:12.43\")) eq 8",
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
   *  Evaluates The "hours-from-dateTime" function As per example 1 of the F&O specs .
   */
  @org.junit.Test
  public void fnHoursFromDateTime1() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"1999-05-31T08:20:00-05:00\"))",
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
   *  Evaluates The "hours-from-dateTime" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnHoursFromDateTime10() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T02:00:00Z\")) * fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "hours-from-dateTime" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnHoursFromDateTime11() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T02:00:00Z\")) div fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0.2")
    );
  }

  /**
   *  Evaluates The "hours-from-dateTime" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnHoursFromDateTime12() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) idiv fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T02:00:00Z\"))",
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
   *  Evaluates The "hours-from-dateTime" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnHoursFromDateTime13() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) mod fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T03:00:00Z\"))",
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
   *  Evaluates The "hours-from-dateTime" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnHoursFromDateTime14() {
    final XQuery query = new XQuery(
      "+fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "hours-from-dateTime" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnHoursFromDateTime15() {
    final XQuery query = new XQuery(
      "-fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "hours-from-dateTime" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnHoursFromDateTime16() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) eq fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "hours-from-dateTime" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnHoursFromDateTime17() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) ne fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "hours-from-dateTime" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnHoursFromDateTime18() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) le fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "hours-from-dateTime" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnHoursFromDateTime19() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) ge fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "hours-from-dateTime" function As per example 2 of the F&O specs .
   */
  @org.junit.Test
  public void fnHoursFromDateTime2() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"1999-12-31T21:20:00-05:00\"))",
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
   *  Evaluates The "hours-from-dateTime" function As per example 3 of the F&O specs .
   */
  @org.junit.Test
  public void fnHoursFromDateTime3() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(fn:adjust-dateTime-to-timezone(xs:dateTime(\"1999-12-31T21:20:00-05:00\"), xs:dayTimeDuration(\"PT0H\")))",
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
   *  Evaluates The "hours-from-dateTime" function As per example 4 of the F&O specs .
   */
  @org.junit.Test
  public void fnHoursFromDateTime4() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"1999-12-31T12:00:00\"))",
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
   *  Evaluates The "hours-from-dateTime" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnHoursFromDateTime5() {
    final XQuery query = new XQuery(
      "fn:count(fn:hours-from-dateTime(()))",
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
   *  Evaluates The "hours-from-dateTime" function that returns 0. .
   */
  @org.junit.Test
  public void fnHoursFromDateTime6() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"1999-05-31T00:20:00-05:00\"))",
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
   *  Evaluates The "hours-from-dateTime" function that returns 23. .
   */
  @org.junit.Test
  public void fnHoursFromDateTime7() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"1999-05-31T23:20:00-05:00\"))",
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
   *  Evaluates The "hours-from-dateTime" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnHoursFromDateTime8() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T00:00:00Z\")) + fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "hours-from-dateTime" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnHoursFromDateTime9() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T00:00:00Z\")) - fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "hours-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(lower bound) .
   */
  @org.junit.Test
  public void fnHoursFromDateTime1args1() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"1970-01-01T00:00:00\"))",
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
   *  Evaluates The "hours-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(mid range) .
   */
  @org.junit.Test
  public void fnHoursFromDateTime1args2() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"1996-04-07T01:40:52\"))",
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
   *  Evaluates The "hours-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(upper bound) .
   */
  @org.junit.Test
  public void fnHoursFromDateTime1args3() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(xs:dateTime(\"2030-12-31T23:59:59\"))",
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
