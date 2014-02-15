package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the timezone-from-dateTime() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnTimezoneFromDateTime extends QT3TestSet {

  /**
   *  A test whose essence is: `timezone-from-dateTime()`. .
   */
  @org.junit.Test
  public void kTimezoneFromDateTimeFunc1() {
    final XQuery query = new XQuery(
      "timezone-from-dateTime()",
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
   *  A test whose essence is: `timezone-from-dateTime((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kTimezoneFromDateTimeFunc2() {
    final XQuery query = new XQuery(
      "timezone-from-dateTime((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(timezone-from-dateTime(()))`. .
   */
  @org.junit.Test
  public void kTimezoneFromDateTimeFunc3() {
    final XQuery query = new XQuery(
      "empty(timezone-from-dateTime(()))",
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
   *  A test whose essence is: `timezone-from-dateTime(()) instance of xs:dayTimeDuration?`. .
   */
  @org.junit.Test
  public void kTimezoneFromDateTimeFunc4() {
    final XQuery query = new XQuery(
      "timezone-from-dateTime(()) instance of xs:dayTimeDuration?",
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
   *  A test whose essence is: `timezone-from-dateTime(xs:dateTime("2004-10-12T23:43:12Z")) eq xs:dayTimeDuration("PT0S")`. .
   */
  @org.junit.Test
  public void kTimezoneFromDateTimeFunc5() {
    final XQuery query = new XQuery(
      "timezone-from-dateTime(xs:dateTime(\"2004-10-12T23:43:12Z\")) eq xs:dayTimeDuration(\"PT0S\")",
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
   *  A test whose essence is: `timezone-from-dateTime(xs:dateTime("2004-10-12T23:43:12-08:23")) eq xs:dayTimeDuration("-PT8H23M")`. .
   */
  @org.junit.Test
  public void kTimezoneFromDateTimeFunc6() {
    final XQuery query = new XQuery(
      "timezone-from-dateTime(xs:dateTime(\"2004-10-12T23:43:12-08:23\")) eq xs:dayTimeDuration(\"-PT8H23M\")",
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
   *  A test whose essence is: `empty(timezone-from-dateTime(xs:dateTime("2004-12-10T23:43:41.965")))`. .
   */
  @org.junit.Test
  public void kTimezoneFromDateTimeFunc7() {
    final XQuery query = new XQuery(
      "empty(timezone-from-dateTime(xs:dateTime(\"2004-12-10T23:43:41.965\")))",
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
   *  Evaluates The "timezone-from-dateTime" function As per example 1 (for this function) of the F&O specs .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt1() {
    final XQuery query = new XQuery(
      "fn:timezone-from-dateTime(xs:dateTime(\"1999-05-31T13:20:00-05:00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-PT5H")
    );
  }

  /**
   *  Evaluates The "timezone-from-dateTime" function as an argument to the "min" function. .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt10() {
    final XQuery query = new XQuery(
      "fn:min(fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T02:00:00Z\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   *  Evaluates The "timezone-from-dateTime" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt11() {
    final XQuery query = new XQuery(
      "fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T02:00:00+10:00\")) div fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00+05:00\"))",
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
   *  Evaluates The "timezone-from-dateTime" function as an argument to the "fn:number" function. Return NaN. .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt12() {
    final XQuery query = new XQuery(
      "fn:number(fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")))",
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
   *  Evaluates The "timezone-from-dateTime" function as as an argument to an "fn:max" function. .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt13() {
    final XQuery query = new XQuery(
      "fn:max(fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   *  Evaluates The "timezone-from-dateTime" function as an argument to the "fn:string" function .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt14() {
    final XQuery query = new XQuery(
      "fn:string(fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   *  Evaluates The "timezone-from-dateTime" function as part of an "and" expression. Uses the "fn:string" function to account for new EBV rules. .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt15() {
    final XQuery query = new XQuery(
      "fn:string(fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))) and fn:string(fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")))",
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
   *  Evaluates The "timezone-from-dateTime" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt16() {
    final XQuery query = new XQuery(
      "fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) eq fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "timezone-from-dateTime" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt17() {
    final XQuery query = new XQuery(
      "fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) ne fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "timezone-from-dateTime" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt18() {
    final XQuery query = new XQuery(
      "fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) le fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "timezone-from-dateTime" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt19() {
    final XQuery query = new XQuery(
      "fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) ge fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "timezone-from-dateTime" function As per example 2 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt2() {
    final XQuery query = new XQuery(
      "fn:timezone-from-dateTime(xs:dateTime(\"2000-06-12T13:20:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   *  Evaluates The "timezone-from-dateTime" function as part of an "or" expression. Uses the "fn:string" function to account for new EBV rules. .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt20() {
    final XQuery query = new XQuery(
      "fn:string(fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))) or fn:string(fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")))",
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
   *  Evaluates The "timezone-from-dateTime" function As per example 3 (for this function) of the F&O specs. Use the fn:count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt3() {
    final XQuery query = new XQuery(
      "fn:count(fn:timezone-from-dateTime(xs:dateTime(\"2004-08-27T00:00:00\")))",
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
   *  Evaluates The "timezone-from-dateTime" function uses as part of a numeric-less-than expression (le operator). .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt4() {
    final XQuery query = new XQuery(
      "fn:timezone-from-dateTime(xs:dateTime(\"1999-12-31T12:00:00+02:00\")) le fn:timezone-from-dateTime(xs:dateTime(\"1999-12-30T12:00:00+03:00\"))",
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
   *  Evaluates The "timezone-from-dateTime" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt5() {
    final XQuery query = new XQuery(
      "fn:count(fn:timezone-from-dateTime(()))",
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
   *  Evaluates The "timezone-from-dateTime" function that returns dayTimeDuration of 0. .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt6() {
    final XQuery query = new XQuery(
      "fn:timezone-from-dateTime(xs:dateTime(\"1999-05-31T00:20:00+00:00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   *  Evaluates The "timezone-from-dateTime" function that evaluates a timezone of "-00:00". .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt7() {
    final XQuery query = new XQuery(
      "fn:timezone-from-dateTime(xs:dateTime(\"1999-05-31T23:20:00-00:00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   *  Evaluates The "timezone-from-dateTime" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt8() {
    final XQuery query = new XQuery(
      "fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T00:02:00Z\")) + fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:03:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   *  Evaluates The "timezone-from-dateTime" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnTimezoneFromDateTimealt9() {
    final XQuery query = new XQuery(
      "fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T00:00:00+04:00\")) - fn:timezone-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00+02:00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT2H")
    );
  }
}
