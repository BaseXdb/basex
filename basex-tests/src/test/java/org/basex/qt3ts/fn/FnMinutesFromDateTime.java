package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the minutes-from-dateTime() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnMinutesFromDateTime extends QT3TestSet {

  /**
   *  A test whose essence is: `minutes-from-dateTime()`. .
   */
  @org.junit.Test
  public void kMinutesFromDateTimeFunc1() {
    final XQuery query = new XQuery(
      "minutes-from-dateTime()",
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
   *  A test whose essence is: `minutes-from-dateTime((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kMinutesFromDateTimeFunc2() {
    final XQuery query = new XQuery(
      "minutes-from-dateTime((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(minutes-from-dateTime(()))`. .
   */
  @org.junit.Test
  public void kMinutesFromDateTimeFunc3() {
    final XQuery query = new XQuery(
      "empty(minutes-from-dateTime(()))",
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
   *  A test whose essence is: `minutes-from-dateTime(()) instance of xs:integer?`. .
   */
  @org.junit.Test
  public void kMinutesFromDateTimeFunc4() {
    final XQuery query = new XQuery(
      "minutes-from-dateTime(()) instance of xs:integer?",
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
   *  A test whose essence is: `minutes-from-dateTime(xs:dateTime("2001-02-03T08:23:12.43")) eq 23`. .
   */
  @org.junit.Test
  public void kMinutesFromDateTimeFunc5() {
    final XQuery query = new XQuery(
      "minutes-from-dateTime(xs:dateTime(\"2001-02-03T08:23:12.43\")) eq 23",
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
   *  Evaluates The "minutes-from-dateTime" function As per example 1 of the F&O specs .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime1() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1999-05-31T13:20:00-05:00\"))",
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
   *  Evaluates The "minutes-from-dateTime" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime10() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T02:02:00Z\")) * fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T10:03:00Z\"))",
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
   *  Evaluates The "minutes-from-dateTime" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime11() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T02:10:00Z\")) div fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T10:05:00Z\"))",
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
   *  Evaluates The "minutes-from-dateTime" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime12() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T10:10:00Z\")) idiv fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T02:05:00Z\"))",
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
   *  Evaluates The "minutes-from-dateTime" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime13() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T10:10:00Z\")) mod fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T03:10:00Z\"))",
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
   *  Evaluates The "minutes-from-dateTime" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime14() {
    final XQuery query = new XQuery(
      "+fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T10:10:00Z\"))",
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
   *  Evaluates The "minutes-from-dateTime" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime15() {
    final XQuery query = new XQuery(
      "-fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T10:10:00Z\"))",
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
   *  Evaluates The "minutes-from-dateTime" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime16() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T10:10:00Z\")) eq fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T10:10:00Z\"))",
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
   *  Evaluates The "minutes-from-dateTime" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime17() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T10:10:00Z\")) ne fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T10:10:00Z\"))",
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
   *  Evaluates The "minutes-from-dateTime" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime18() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T10:10:00Z\")) le fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T10:10:00Z\"))",
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
   *  Evaluates The "minutes-from-dateTime" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime19() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T10:10:00Z\")) ge fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T10:10:00Z\"))",
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
   *  Evaluates The "minutes-from-dateTime" function As per example 2 of the F&O specs .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime2() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1999-05-31T13:30:00+05:30\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "30")
    );
  }

  /**
   *  Evaluates The "minutes-from-dateTime" function involving a "numeric-less-than" operation (lt operator) .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime3() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1999-12-31T21:20:00-05:00\")) lt fn:minutes-from-dateTime(xs:dateTime(\"1999-12-31T21:20:00-05:00\"))",
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
   *  Evaluates The "minutes-from-dateTime" function involving a "numeric-less-than" operation (le operator) .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime4() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1999-12-31T21:20:00-05:00\")) le fn:minutes-from-dateTime(xs:dateTime(\"1999-12-31T21:20:00-05:00\"))",
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
   *  Evaluates The "minutes-from-dateTime" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime5() {
    final XQuery query = new XQuery(
      "fn:count(fn:minutes-from-dateTime(()))",
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
   *  Evaluates The "minutes-from-dateTime" function that returns 0. .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime6() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1999-05-31T20:00:00-05:00\"))",
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
   *  Evaluates The "minutes-from-dateTime" function that returns 59. .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime7() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1999-05-31T23:59:00-05:00\"))",
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
   *  Evaluates The "minutes-from-dateTime" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime8() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T00:00:00Z\")) + fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "minutes-from-dateTime" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime9() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T00:00:00Z\")) - fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T10:00:00Z\"))",
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
   *  Evaluates The "minutes-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(lower bound) .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime1args1() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1970-01-01T00:00:00Z\"))",
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
   *  Evaluates The "minutes-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(mid range) .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime1args2() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"1996-04-07T01:40:52Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "40")
    );
  }

  /**
   *  Evaluates The "minutes-from-dateTime" function with the arguments set as follows: $arg = xs:dateTime(upper bound) .
   */
  @org.junit.Test
  public void fnMinutesFromDateTime1args3() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(xs:dateTime(\"2030-12-31T23:59:59Z\"))",
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
