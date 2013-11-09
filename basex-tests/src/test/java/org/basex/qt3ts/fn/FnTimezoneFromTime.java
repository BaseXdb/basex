package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the timezone-from-time() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnTimezoneFromTime extends QT3TestSet {

  /**
   *  A test whose essence is: `timezone-from-time()`. .
   */
  @org.junit.Test
  public void kTimezoneFromTimeFunc1() {
    final XQuery query = new XQuery(
      "timezone-from-time()",
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
   *  A test whose essence is: `timezone-from-time((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kTimezoneFromTimeFunc2() {
    final XQuery query = new XQuery(
      "timezone-from-time((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(timezone-from-time(()))`. .
   */
  @org.junit.Test
  public void kTimezoneFromTimeFunc3() {
    final XQuery query = new XQuery(
      "empty(timezone-from-time(()))",
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
   *  A test whose essence is: `timezone-from-time(()) instance of xs:dayTimeDuration?`. .
   */
  @org.junit.Test
  public void kTimezoneFromTimeFunc4() {
    final XQuery query = new XQuery(
      "timezone-from-time(()) instance of xs:dayTimeDuration?",
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
   *  A test whose essence is: `timezone-from-time(xs:time("23:43:12.765Z")) eq xs:dayTimeDuration("PT0S")`. .
   */
  @org.junit.Test
  public void kTimezoneFromTimeFunc5() {
    final XQuery query = new XQuery(
      "timezone-from-time(xs:time(\"23:43:12.765Z\")) eq xs:dayTimeDuration(\"PT0S\")",
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
   *  A test whose essence is: `timezone-from-time(xs:time("23:43:12.765-08:23")) eq xs:dayTimeDuration("-PT8H23M")`. .
   */
  @org.junit.Test
  public void kTimezoneFromTimeFunc6() {
    final XQuery query = new XQuery(
      "timezone-from-time(xs:time(\"23:43:12.765-08:23\")) eq xs:dayTimeDuration(\"-PT8H23M\")",
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
   *  A test whose essence is: `empty(timezone-from-time(xs:time("23:43:12.765")))`. .
   */
  @org.junit.Test
  public void kTimezoneFromTimeFunc7() {
    final XQuery query = new XQuery(
      "empty(timezone-from-time(xs:time(\"23:43:12.765\")))",
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
   *  Evaluates The "timezone-from-time" function As per example 1 of the F&O specs (for this function) .
   */
  @org.junit.Test
  public void fnTimezoneFromTime1() {
    final XQuery query = new XQuery(
      "fn:timezone-from-time(xs:time(\"13:20:00-05:00\"))",
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
   *  Evaluates The "timezone-from-time" function as an argument to the "fn:not" function. Uses the "fn:string" function to account for new EBV rules. .
   */
  @org.junit.Test
  public void fnTimezoneFromTime10() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(fn:timezone-from-time(xs:time(\"02:02:03Z\"))))",
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
   *  Evaluates The "timezone-from-time" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnTimezoneFromTime11() {
    final XQuery query = new XQuery(
      "fn:timezone-from-time(xs:time(\"22:33:10+10:00\")) div fn:timezone-from-time(xs:time(\"02:11:02+05:00\"))",
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
   *  Evaluates The "timezone-from-time" function as an argument to the "fn:number" function. Return NaN. .
   */
  @org.junit.Test
  public void fnTimezoneFromTime12() {
    final XQuery query = new XQuery(
      "fn:number(fn:timezone-from-time(xs:time(\"10:12:15Z\")))",
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
   *  Evaluates The "timezone-from-time" function as an argument to the "fn:boolean" function. Uses the "fn:string" function to account for new EBV rules. .
   */
  @org.junit.Test
  public void fnTimezoneFromTime13() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(fn:timezone-from-time(xs:time(\"10:10:20Z\"))))",
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
   *  Evaluates The "timezone-from-time" function as an argument to the "fn:string" function. .
   */
  @org.junit.Test
  public void fnTimezoneFromTime14() {
    final XQuery query = new XQuery(
      "fn:string(fn:timezone-from-time(xs:time(\"10:00:01Z\")))",
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
   *  Evaluates The "timezone-from-time" function as part of an "and" expression. Uses the "fn:string" function to account for new EBV rules. .
   */
  @org.junit.Test
  public void fnTimezoneFromTime15() {
    final XQuery query = new XQuery(
      "fn:string(fn:timezone-from-time(xs:time(\"10:10:01Z\"))) and fn:string(fn:timezone-from-time(xs:time(\"10:10:01Z\")))",
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
   *  Evaluates The "timezone-from-time" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnTimezoneFromTime16() {
    final XQuery query = new XQuery(
      "fn:timezone-from-time(xs:time(\"10:02:01Z\")) eq fn:timezone-from-time(xs:time(\"10:02:00Z\"))",
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
   *  Evaluates The "timezone-from-time" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnTimezoneFromTime17() {
    final XQuery query = new XQuery(
      "fn:timezone-from-time(xs:time(\"10:00:01Z\")) ne fn:timezone-from-time(xs:time(\"01:01:00Z\"))",
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
   *  Evaluates The "timezone-from-time" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnTimezoneFromTime18() {
    final XQuery query = new XQuery(
      "fn:timezone-from-time(xs:time(\"10:00:00Z\")) le fn:timezone-from-time(xs:time(\"10:00:00Z\"))",
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
   *  Evaluates The "timezone-from-time" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnTimezoneFromTime19() {
    final XQuery query = new XQuery(
      "fn:timezone-from-time(xs:time(\"10:03:01Z\")) ge fn:timezone-from-time(xs:time(\"10:04:02Z\"))",
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
   *  Evaluates The "timezone-from-time" function as per example 2 (for this function) from the F&O. specs. Use fn:count to aoid empty file. .
   */
  @org.junit.Test
  public void fnTimezoneFromTime2() {
    final XQuery query = new XQuery(
      "fn:count(fn:timezone-from-time(xs:time(\"13:20:00\")))",
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
   *  Evaluates The "timezone-from-time" function as part of an "or" expression. Uses the "fn:string" function to account for new EBV rules. .
   */
  @org.junit.Test
  public void fnTimezoneFromTime20() {
    final XQuery query = new XQuery(
      "fn:string(fn:timezone-from-time(xs:time(\"10:03:01Z\"))) or fn:string(fn:timezone-from-time(xs:time(\"10:04:02Z\")))",
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
   *  Evaluates The "timezone-from-time" function as part of a numeric greater than operation (gt operator) .
   */
  @org.junit.Test
  public void fnTimezoneFromTime3() {
    final XQuery query = new XQuery(
      "fn:timezone-from-time(xs:time(\"01:23:02Z\")) gt fn:timezone-from-time(xs:time(\"01:23:03Z\"))",
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
   *  Evaluates The "timezone-from-time" function as part of a numeric-less-than expression (le operator) .
   */
  @org.junit.Test
  public void fnTimezoneFromTime4() {
    final XQuery query = new XQuery(
      "fn:timezone-from-time(xs:time(\"01:10:20Z\")) le fn:timezone-from-time(xs:time(\"01:20:30Z\"))",
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
   *  Evaluates The "timezone-from-time" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnTimezoneFromTime5() {
    final XQuery query = new XQuery(
      "fn:count(fn:timezone-from-time(()))",
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
   *  Evaluates The "timezone-from-time" function that returns a dayTimeDuration of 0. .
   */
  @org.junit.Test
  public void fnTimezoneFromTime6() {
    final XQuery query = new XQuery(
      "fn:timezone-from-time(xs:time(\"00:59:00+00:00\"))",
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
   *  Evaluates The "timezone-from-time" function with a timezone component of "-00:00". .
   */
  @org.junit.Test
  public void fnTimezoneFromTime7() {
    final XQuery query = new XQuery(
      "fn:timezone-from-time(xs:time(\"23:20:59-00:00\"))",
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
   *  Evaluates The "timezone-from-time" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnTimezoneFromTime8() {
    final XQuery query = new XQuery(
      "fn:timezone-from-time(xs:time(\"02:00:00Z\")) + fn:timezone-from-time(xs:time(\"10:00:10Z\"))",
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
   *  Evaluates The "timezone-from-time" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnTimezoneFromTime9() {
    final XQuery query = new XQuery(
      "fn:timezone-from-time(xs:time(\"10:10:10Z\")) - fn:timezone-from-time(xs:time(\"09:02:07Z\"))",
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
}
