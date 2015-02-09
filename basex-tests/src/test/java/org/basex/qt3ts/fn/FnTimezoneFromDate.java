package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the timezone-from-date() function.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnTimezoneFromDate extends QT3TestSet {

  /**
   *  A test whose essence is: `timezone-from-date()`. .
   */
  @org.junit.Test
  public void kTimezoneFromDateFunc1() {
    final XQuery query = new XQuery(
      "timezone-from-date()",
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
   *  A test whose essence is: `timezone-from-date((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kTimezoneFromDateFunc2() {
    final XQuery query = new XQuery(
      "timezone-from-date((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(timezone-from-date(()))`. .
   */
  @org.junit.Test
  public void kTimezoneFromDateFunc3() {
    final XQuery query = new XQuery(
      "empty(timezone-from-date(()))",
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
   *  A test whose essence is: `timezone-from-date(()) instance of xs:dayTimeDuration?`. .
   */
  @org.junit.Test
  public void kTimezoneFromDateFunc4() {
    final XQuery query = new XQuery(
      "timezone-from-date(()) instance of xs:dayTimeDuration?",
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
   *  A test whose essence is: `timezone-from-date(xs:date("2004-10-12Z")) eq xs:dayTimeDuration("PT0S")`. .
   */
  @org.junit.Test
  public void kTimezoneFromDateFunc5() {
    final XQuery query = new XQuery(
      "timezone-from-date(xs:date(\"2004-10-12Z\")) eq xs:dayTimeDuration(\"PT0S\")",
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
   *  A test whose essence is: `timezone-from-date(xs:date("2004-10-12-08:23")) eq xs:dayTimeDuration("-PT8H23M")`. .
   */
  @org.junit.Test
  public void kTimezoneFromDateFunc6() {
    final XQuery query = new XQuery(
      "timezone-from-date(xs:date(\"2004-10-12-08:23\")) eq xs:dayTimeDuration(\"-PT8H23M\")",
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
   *  A test whose essence is: `empty(timezone-from-date(xs:date("2004-10-12")))`. .
   */
  @org.junit.Test
  public void kTimezoneFromDateFunc7() {
    final XQuery query = new XQuery(
      "empty(timezone-from-date(xs:date(\"2004-10-12\")))",
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
   *  test fn:timezone-from-date on fn:adjust-date-to-timezone .
   */
  @org.junit.Test
  public void cbclTimezoneFromDate001() {
    final XQuery query = new XQuery(
      "\n" +
      "      timezone-from-date(adjust-date-to-timezone(xs:date(\"1997-01-01\"))) = implicit-timezone()\n" +
      "   ",
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
   *  test fn:timezone-from-date .
   */
  @org.junit.Test
  public void cbclTimezoneFromDate002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:timezone($arg as xs:integer) { if ($arg gt 0) then xs:dayTimeDuration(concat('PT', $arg, 'H')) else if ($arg lt 0) then xs:dayTimeDuration(concat('-PT', -$arg, 'H')) else xs:dayTimeDuration('PT0H') };\n" +
      "        timezone-from-date( adjust-date-to-timezone( fn:current-date(), local:timezone(15)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0003")
    );
  }

  /**
   *  test fn:timezone-from-date .
   */
  @org.junit.Test
  public void cbclTimezoneFromDate003() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:timezone($arg as xs:integer) { if ($arg gt 0) then xs:dayTimeDuration(concat('PT', $arg, 'H')) else if ($arg lt 0) then xs:dayTimeDuration(concat('-PT', -$arg, 'H')) else xs:dayTimeDuration('PT0H') };\n" +
      "        timezone-from-date( adjust-date-to-timezone( fn:current-date(), local:timezone(14)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT14H")
    );
  }

  /**
   *  test fn:timezone-from-date .
   */
  @org.junit.Test
  public void cbclTimezoneFromDate004() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:timezone($arg as xs:integer) { if ($arg gt 0) then xs:dayTimeDuration(concat('PT', $arg, 'H')) else if ($arg lt 0) then xs:dayTimeDuration(concat('-PT', -$arg, 'H')) else xs:dayTimeDuration('PT0H') };\n" +
      "        timezone-from-date( adjust-date-to-timezone( fn:current-date(), timezone-from-date( adjust-date-to-timezone( xs:date('1970-01-01'), local:timezone(-12)))))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-PT12H")
    );
  }

  /**
   *  test fn:timezone-from-date .
   */
  @org.junit.Test
  public void cbclTimezoneFromDate005() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:date($arg as xs:boolean) { if ($arg) then xs:date('1970-01-01Z') else fn:current-date() };\n" +
      "        timezone-from-date( adjust-date-to-timezone( xs:date('2008-08-01'), timezone-from-date(local:date(true()))))",
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
   *  test fn:timezone-from-date .
   */
  @org.junit.Test
  public void cbclTimezoneFromDate006() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:dateTime($arg as xs:boolean) { if ($arg) then xs:dateTime('1970-01-01T00:00:00Z') else fn:current-dateTime() };\n" +
      "        timezone-from-date( adjust-date-to-timezone( xs:date('2008-08-01'), timezone-from-dateTime(local:dateTime(true()))))",
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
   *  test fn:timezone-from-date .
   */
  @org.junit.Test
  public void cbclTimezoneFromDate007() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:time($arg as xs:boolean) { if ($arg) then xs:time('12:00:00Z') else fn:current-time() };\n" +
      "        timezone-from-date( adjust-date-to-timezone( xs:date('2008-08-01'), timezone-from-time(local:time(true()))))",
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
   *  Evaluates The "timezone-from-date" function As per example 1 of the F&O specs .
   */
  @org.junit.Test
  public void fnTimezoneFromDate1() {
    final XQuery query = new XQuery(
      "fn:timezone-from-date(xs:date(\"1999-05-31-05:00\"))",
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
   *  Evaluates The "timezone-from-date" function as as an argument to the "not" function. Uses the "fn:string" function to account for new EBV rules. .
   */
  @org.junit.Test
  public void fnTimezoneFromDate10() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(fn:timezone-from-date(xs:date(\"1970-01-03+02:00\"))))",
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
   *  Evaluates The "timezone-from-date" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnTimezoneFromDate11() {
    final XQuery query = new XQuery(
      "fn:timezone-from-date(xs:date(\"1970-01-02+10:00\")) div fn:timezone-from-date(xs:date(\"1970-01-01+05:00\"))",
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
   *  Evaluates The "timezone-from-date" function as an argument to the fn:number function. returns "NaN". .
   */
  @org.junit.Test
  public void fnTimezoneFromDate12() {
    final XQuery query = new XQuery(
      "fn:number(fn:timezone-from-date(xs:date(\"1970-01-02+10:00\")))",
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
   *  Evaluates The "timezone-from-date" function as an argument to the "fn:boolean" function. Uses the "fn:string" function to account for new EBV rules. .
   */
  @org.junit.Test
  public void fnTimezoneFromDate13() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(fn:timezone-from-date(xs:date(\"1970-01-02+10:00\"))))",
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
   *  Evaluates The "timezone-from-date" function as argument to the "fn:string" function. .
   */
  @org.junit.Test
  public void fnTimezoneFromDate14() {
    final XQuery query = new XQuery(
      "fn:string(fn:timezone-from-date(xs:date(\"1970-01-01Z\")))",
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
   *  Evaluates The "timezone-from-date" function as part of an "and" expression. Uses the "fn:string" function to account for new EBV rules. .
   */
  @org.junit.Test
  public void fnTimezoneFromDate15() {
    final XQuery query = new XQuery(
      "fn:string(fn:timezone-from-date(xs:date(\"1970-01-01Z\"))) and fn:string(fn:timezone-from-date(xs:date(\"1970-01-01Z\")))",
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
   *  Evaluates The "timezone-from-date" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnTimezoneFromDate16() {
    final XQuery query = new XQuery(
      "fn:timezone-from-date(xs:date(\"1970-01-02+10:00\")) eq fn:timezone-from-date(xs:date(\"1970-01-01+10:00\"))",
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
   *  Evaluates The "month-from-date" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnTimezoneFromDate17() {
    final XQuery query = new XQuery(
      "fn:month-from-date(xs:date(\"1970-01-01+05:00\")) ne fn:month-from-date(xs:date(\"1970-01-03+03:00\"))",
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
   *  Evaluates The "timezone-from-date" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnTimezoneFromDate18() {
    final XQuery query = new XQuery(
      "fn:timezone-from-date(xs:date(\"1970-01-01+04:00\")) le fn:timezone-from-date(xs:date(\"1970-01-02+02:00\"))",
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
   *  Evaluates The "timezone-from-date" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnTimezoneFromDate19() {
    final XQuery query = new XQuery(
      "fn:timezone-from-date(xs:date(\"1970-01-03+02:00\")) ge fn:timezone-from-date(xs:date(\"1970-01-01+01:00\"))",
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
   *  Evaluates The "timezone-from-date" function As per example 2 of the F&O specs. .
   */
  @org.junit.Test
  public void fnTimezoneFromDate2() {
    final XQuery query = new XQuery(
      "fn:timezone-from-date(xs:date(\"2000-06-12Z\"))",
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
   *  Evaluates The "timezone-from-date" function as part of an "or" expression. Uses the "fn:string" function to account for new EBV rules. .
   */
  @org.junit.Test
  public void fnTimezoneFromDate20() {
    final XQuery query = new XQuery(
      "fn:string(fn:timezone-from-date(xs:date(\"1970-01-01Z\"))) or fn:string(fn:timezone-from-date(xs:date(\"1970-01-01Z\")))",
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
   *  Evaluates The "timezone-from-date" function involving a "numeric-less-than" operation (lt operator) .
   */
  @org.junit.Test
  public void fnTimezoneFromDate3() {
    final XQuery query = new XQuery(
      "fn:timezone-from-date(xs:date(\"1999-12-31+05:00\")) lt fn:timezone-from-date(xs:date(\"1999-12-31+06:00\"))",
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
   *  Evaluates The "timezone-from-date" function involving a "numeric-less-than" operation (le operator) .
   */
  @org.junit.Test
  public void fnTimezoneFromDate4() {
    final XQuery query = new XQuery(
      "fn:timezone-from-date(xs:date(\"1999-12-31+01:00\")) le fn:timezone-from-date(xs:date(\"1999-12-31+01:00\"))",
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
   *  Evaluates The "timezone-from-date" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnTimezoneFromDate5() {
    final XQuery query = new XQuery(
      "fn:count(fn:timezone-from-date(()))",
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
   *  Evaluates The "timezone-from-date" function that returns a dayTimeDuration of 0. .
   */
  @org.junit.Test
  public void fnTimezoneFromDate6() {
    final XQuery query = new XQuery(
      "fn:timezone-from-date(xs:date(\"1999-05-31+00:00\"))",
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
   *  Evaluates The "timezone-from-date" function that uses a timezone of -0. .
   */
  @org.junit.Test
  public void fnTimezoneFromDate7() {
    final XQuery query = new XQuery(
      "fn:timezone-from-date(xs:date(\"1999-12-31-00:00\"))",
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
   *  Evaluates The "timezone-from-date" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnTimezoneFromDate8() {
    final XQuery query = new XQuery(
      "fn:timezone-from-date(xs:date(\"1970-01-01+08:00\")) + fn:timezone-from-date(xs:date(\"1970-01-01+03:00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT11H")
    );
  }

  /**
   *  Evaluates The "timezone-from-date" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnTimezoneFromDate9() {
    final XQuery query = new XQuery(
      "fn:timezone-from-date(xs:date(\"1970-01-01+09:00\")) - fn:timezone-from-date(xs:date(\"1970-01-01+10:00\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-PT1H")
    );
  }
}
