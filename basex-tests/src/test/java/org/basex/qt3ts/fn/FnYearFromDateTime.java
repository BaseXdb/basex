package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the year-from-dateTime() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnYearFromDateTime extends QT3TestSet {

  /**
   *  A test whose essence is: `year-from-dateTime()`. .
   */
  @org.junit.Test
  public void kYearFromDateTimeFunc1() {
    final XQuery query = new XQuery(
      "year-from-dateTime()",
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
   *  A test whose essence is: `year-from-dateTime((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kYearFromDateTimeFunc2() {
    final XQuery query = new XQuery(
      "year-from-dateTime((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(year-from-dateTime(()))`. .
   */
  @org.junit.Test
  public void kYearFromDateTimeFunc3() {
    final XQuery query = new XQuery(
      "empty(year-from-dateTime(()))",
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
   *  A test whose essence is: `year-from-dateTime(()) instance of xs:integer?`. .
   */
  @org.junit.Test
  public void kYearFromDateTimeFunc4() {
    final XQuery query = new XQuery(
      "year-from-dateTime(()) instance of xs:integer?",
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
   *  A test whose essence is: `year-from-dateTime(xs:dateTime("2001-02-03T08:23:12.43")) eq 2001`. .
   */
  @org.junit.Test
  public void kYearFromDateTimeFunc5() {
    final XQuery query = new XQuery(
      "year-from-dateTime(xs:dateTime(\"2001-02-03T08:23:12.43\")) eq 2001",
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
   *  Evaluates The "year-from-dateTime" function As per example 1 (of this fucntion) of the F&O specs .
   */
  @org.junit.Test
  public void fnYearFromDateTime1() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"1999-05-31T13:20:00-05:00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1999")
    );
  }

  /**
   *  Evaluates The "year-from-dateTime" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnYearFromDateTime10() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"1970-01-01T02:00:00Z\")) * fn:year-from-dateTime(xs:dateTime(\"0002-01-01T10:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3940")
    );
  }

  /**
   *  Evaluates The "year-from-dateTime" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnYearFromDateTime11() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"1000-01-01T02:00:00Z\")) div fn:year-from-dateTime(xs:dateTime(\"0050-01-01T10:00:00Z\"))",
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
   *  Evaluates The "year-from-dateTime" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnYearFromDateTime12() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) idiv fn:year-from-dateTime(xs:dateTime(\"1970-01-01T02:00:00Z\"))",
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
   *  Evaluates The "year-from-dateTime" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnYearFromDateTime13() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) mod fn:year-from-dateTime(xs:dateTime(\"1970-01-01T03:00:00Z\"))",
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
   *  Evaluates The "year-from-dateTime" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnYearFromDateTime16() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) eq fn:year-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "year-from-dateTime" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnYearFromDateTime17() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) ne fn:year-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "year-from-dateTime" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnYearFromDateTime18() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\")) le fn:year-from-dateTime(xs:dateTime(\"1971-01-01T10:00:00Z\"))",
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
   *  Evaluates The "year-from-dateTime" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnYearFromDateTime19() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"1971-01-01T10:00:00Z\")) ge fn:year-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "year-from-dateTime" function As per example 2 (for this function)of the F&O specs .
   */
  @org.junit.Test
  public void fnYearFromDateTime2() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"1999-05-31T21:30:00-05:00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1999")
    );
  }

  /**
   *  Evaluates The "year-from-dateTime" function As per example 3 (for this function) of the F&O specs .
   */
  @org.junit.Test
  public void fnYearFromDateTime3() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"1999-12-31T19:20:00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1999")
    );
  }

  /**
   *  Evaluates The "year-from-dateTime" function used as an argument to an "avg" function. .
   */
  @org.junit.Test
  public void fnYearFromDateTime4() {
    final XQuery query = new XQuery(
      "fn:avg((fn:year-from-dateTime(xs:dateTime(\"1996-12-31T12:00:00Z\")),fn:year-from-dateTime(xs:dateTime(\"2000-12-31T12:00:00Z\"))))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1998")
    );
  }

  /**
   *  Evaluates The "year-from-dateTime" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnYearFromDateTime5() {
    final XQuery query = new XQuery(
      "fn:count(fn:year-from-dateTime(()))",
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
   *  Evaluates The "year-from-dateTime" function that returns a negative number. .
   */
  @org.junit.Test
  public void fnYearFromDateTime6() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"-1999-05-31T00:20:00-05:00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1999")
    );
  }

  /**
   *  Evaluates The "year-from-dateTime" function that returns 0001. .
   */
  @org.junit.Test
  public void fnYearFromDateTime7() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"0001-05-31T23:20:00-05:00\"))",
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
   *  Evaluates The "year-from-dateTime" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnYearFromDateTime8() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"1970-01-01T00:00:00Z\")) + fn:year-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3940")
    );
  }

  /**
   *  Evaluates The "year-from-dateTime" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnYearFromDateTime9() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"1970-01-01T00:00:00Z\")) - fn:year-from-dateTime(xs:dateTime(\"1969-01-01T10:00:00Z\"))",
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
   *  Evaluates The "year-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(lower bound) .
   */
  @org.junit.Test
  public void fnYearFromDateTime1args1() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"1970-01-01T00:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1970")
    );
  }

  /**
   *  Evaluates The "year-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(mid range) .
   */
  @org.junit.Test
  public void fnYearFromDateTime1args2() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"1996-04-07T01:40:52Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1996")
    );
  }

  /**
   *  Evaluates The "year-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(upper bound) .
   */
  @org.junit.Test
  public void fnYearFromDateTime1args3() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(xs:dateTime(\"2030-12-31T23:59:59Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2030")
    );
  }

  /**
   *  Evaluates The "year-from-dateTime" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnYearFromDateTimeNew14() {
    final XQuery query = new XQuery(
      "+fn:year-from-dateTime(xs:dateTime(\"1971-01-01T10:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1971")
    );
  }

  /**
   *  Evaluates The "year-from-dateTime" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnYearFromDateTimeNew15() {
    final XQuery query = new XQuery(
      "-fn:year-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1970")
    );
  }
}
