package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the seconds-from-dateTime() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnSecondsFromDateTime extends QT3TestSet {

  /**
   *  A test whose essence is: `seconds-from-dateTime()`. .
   */
  @org.junit.Test
  public void kSecondsFromDateTimeFunc1() {
    final XQuery query = new XQuery(
      "seconds-from-dateTime()",
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
   *  A test whose essence is: `seconds-from-dateTime((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kSecondsFromDateTimeFunc2() {
    final XQuery query = new XQuery(
      "seconds-from-dateTime((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(seconds-from-dateTime(()))`. .
   */
  @org.junit.Test
  public void kSecondsFromDateTimeFunc3() {
    final XQuery query = new XQuery(
      "empty(seconds-from-dateTime(()))",
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
   *  A test whose essence is: `seconds-from-dateTime(()) instance of xs:decimal?`. .
   */
  @org.junit.Test
  public void kSecondsFromDateTimeFunc4() {
    final XQuery query = new XQuery(
      "seconds-from-dateTime(()) instance of xs:decimal?",
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
   *  A test whose essence is: `seconds-from-dateTime(xs:dateTime("2001-02-03T08:23:12.43")) eq 12.43`. .
   */
  @org.junit.Test
  public void kSecondsFromDateTimeFunc5() {
    final XQuery query = new XQuery(
      "seconds-from-dateTime(xs:dateTime(\"2001-02-03T08:23:12.43\")) eq 12.43",
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
   *  Evaluates The "seconds-from-dateTime" function As per example 1 of the F&O specs. .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime1() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"1999-05-31T13:20:00-05:00\"))",
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
   *  Evaluates The "secondss-from-dateTime" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime10() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T02:02:02Z\")) * fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T10:03:03Z\"))",
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
   *  Evaluates The "seconds-from-dateTime" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime11() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T02:10:10Z\")) div fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T10:05:05Z\"))",
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
   *  Evaluates The "seconds-from-dateTime" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime12() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T10:10:10Z\")) idiv fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T02:05:05Z\"))",
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
   *  Evaluates The "seconds-from-dateTime" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime13() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T10:10:10Z\")) mod fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T03:10:10Z\"))",
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
   *  Evaluates The "seconds-from-dateTime" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime14() {
    final XQuery query = new XQuery(
      "+fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T10:10:10Z\"))",
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
   *  Evaluates The "seconds-from-dateTime" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime15() {
    final XQuery query = new XQuery(
      "-fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T10:10:10Z\"))",
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
   *  Evaluates The "seconds-from-dateTime" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime16() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T10:10:10Z\")) eq fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T10:10:10Z\"))",
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
   *  Evaluates The "seconds-from-dateTime" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime17() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T10:10:10Z\")) ne fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T10:10:10Z\"))",
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
   *  Evaluates The "seconds-from-dateTime" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime18() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T10:10:10Z\")) le fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T10:10:10Z\"))",
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
   *  Evaluates The "seconds-from-dateTime" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime19() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T10:10:10Z\")) ge fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T10:10:10Z\"))",
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
   *  Evaluates The "seconds-from-dateTime" function as an argument to an "avg" function. .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime2() {
    final XQuery query = new XQuery(
      "fn:avg((fn:seconds-from-dateTime(xs:dateTime(\"1999-05-31T13:30:10Z\")), fn:seconds-from-dateTime(xs:dateTime(\"1999-05-31T13:30:15Z\"))))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "12.5")
    );
  }

  /**
   *  Evaluates The "seconds-from-dateTime" function involving a "numeric-less-than" operation (lt operator) .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime3() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"1999-12-31T21:20:20-05:00\")) lt fn:seconds-from-dateTime(xs:dateTime(\"1999-12-31T21:20:20-05:00\"))",
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
   *  Evaluates The "seconds-from-dateTime" function involving a "numeric-less-than" operation (le operator) .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime4() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"1999-12-31T21:20:20-05:00\")) le fn:seconds-from-dateTime(xs:dateTime(\"1999-12-31T21:20:20-05:00\"))",
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
   *  Evaluates The "seconds-from-dateTime" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime5() {
    final XQuery query = new XQuery(
      "fn:count(fn:seconds-from-dateTime(()))",
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
   *  Evaluates The "seconds-from-dateTime" function that returns 0. .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime6() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"1999-05-31T20:00:00-05:00\"))",
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
   *  Evaluates The "seconds-from-dateTime" function that returns 59. .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime7() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"1999-05-31T23:59:59-05:00\"))",
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
   *  Evaluates The "seconds-from-dateTime" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime8() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T00:00:10Z\")) + fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T10:00:11Z\"))",
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
   *  Evaluates The "seconds-from-dateTime" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime9() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T00:00:10Z\")) - fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "seconds-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(lower bound) .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime1args1() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"1970-01-01T00:00:00Z\"))",
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
   *  Evaluates The "seconds-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(mid range) .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime1args2() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"1996-04-07T01:40:52Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "52")
    );
  }

  /**
   *  Evaluates The "seconds-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(upper bound) .
   */
  @org.junit.Test
  public void fnSecondsFromDateTime1args3() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(xs:dateTime(\"2030-12-31T23:59:59Z\"))",
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
