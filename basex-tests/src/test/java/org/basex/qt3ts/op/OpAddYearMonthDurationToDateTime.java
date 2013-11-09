package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the add-yearMonthDuration-to-dateTime operator.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpAddYearMonthDurationToDateTime extends QT3TestSet {

  /**
   *  Simple testing involving operator '+' between xs:dateTime and xs:yearMonthDuration. .
   */
  @org.junit.Test
  public void kYearMonthDurationAddDT1() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-07-19T08:23:01.765\") + xs:yearMonthDuration(\"P3Y35M\") eq xs:dateTime(\"2005-06-19T08:23:01.765\")",
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
   *  Simple testing involving operator '+' between xs:yearMonthDuration and xs:dateTime. .
   */
  @org.junit.Test
  public void kYearMonthDurationAddDT2() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y35M\") + xs:dateTime(\"1999-07-19T08:23:01.765\") eq xs:dateTime(\"2005-06-19T08:23:01.765\")",
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
   *  The '+' operator is not available between xs:date and xs:time. .
   */
  @org.junit.Test
  public void kYearMonthDurationAddDT3() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-10-12\") + xs:time(\"08:12:12\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  The '+' operator is not available between xs:time and xs:date. .
   */
  @org.junit.Test
  public void kYearMonthDurationAddDT4() {
    final XQuery query = new XQuery(
      "xs:time(\"08:12:12\") + xs:date(\"1999-10-12\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  The '+' operator is not available between xs:date and xs:time. .
   */
  @org.junit.Test
  public void kYearMonthDurationAddDT5() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-10-12\") + xs:time(\"08:12:12\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  The '+' operator is not available between xs:time and xs:date. .
   */
  @org.junit.Test
  public void kYearMonthDurationAddDT6() {
    final XQuery query = new XQuery(
      "xs:time(\"08:12:12\") + xs:date(\"1999-10-12\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test addition of zero duration to dateTime .
   */
  @org.junit.Test
  public void cbclAddYearMonthDurationToDateTime001() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare function local:two-digit($number as xs:integer) { \n" +
      "            let $string := string($number) \n" +
      "            return if (string-length($string) lt 2) then concat('0', $string) else $string \n" +
      "         }; \n" +
      "         declare function local:dateTime($year as xs:integer, $month as xs:integer, $day as xs:integer, $hour as xs:integer, $mins as xs:integer) { \n" +
      "            let $m := local:two-digit($month), $d := local:two-digit($day), $h := local:two-digit($hour), $n := local:two-digit($mins) \n" +
      "            return xs:dateTime(concat($year, '-', $m, '-', $d, 'T', $h, ':', $n, ':00')) \n" +
      "         }; \n" +
      "         xs:yearMonthDuration(\"P0Y\") + local:dateTime(2008, 05, 12, 12, 59)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2008-05-12T12:59:00")
    );
  }

  /**
   *  test addition of zero duration to dateTime .
   */
  @org.junit.Test
  public void cbclAddYearMonthDurationToDateTime002() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare function local:two-digit($number as xs:integer) { \n" +
      "            let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string \n" +
      "         }; \n" +
      "         declare function local:dateTime($year as xs:integer, $month as xs:integer, $day as xs:integer, $hour as xs:integer, $mins as xs:integer) { \n" +
      "            let $m := local:two-digit($month), $d := local:two-digit($day), $h := local:two-digit($hour), $n := local:two-digit($mins) \n" +
      "            return xs:dateTime(concat($year, '-', $m, '-', $d, 'T', $h, ':', $n, ':00')) \n" +
      "         }; \n" +
      "         local:dateTime(2008, 05, 12, 12, 59) + xs:yearMonthDuration(\"P0Y\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2008-05-12T12:59:00")
    );
  }

  /**
   *  test addition of large duration to dateTime .
   */
  @org.junit.Test
  public void cbclAddYearMonthDurationToDateTime003() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare function local:two-digit($number as xs:integer) { \n" +
      "            let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string \n" +
      "         }; \n" +
      "         declare function local:dateTime($year as xs:integer, $month as xs:integer, $day as xs:integer, $hour as xs:integer, $mins as xs:integer) { \n" +
      "            let $m := local:two-digit($month), $d := local:two-digit($day), $h := local:two-digit($hour), $n := local:two-digit($mins) \n" +
      "            return xs:dateTime(concat($year, '-', $m, '-', $d, 'T', $h, ':', $n, ':00')) \n" +
      "         }; \n" +
      "         local:dateTime(2008, 05, 12, 12, 59) + xs:yearMonthDuration(\"P4026720960Y\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "4026722968-05-12T12:59:00")
      ||
        error("FODT0002")
      )
    );
  }

  /**
   * date: July 5, 2005  Evaluates The "add-yearMonthDuration-to-dateTime" operator As per example 1 (for this function)of the F&O specs. .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime1() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2000-10-30T11:12:00\") + xs:yearMonthDuration(\"P1Y2M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2001-12-30T11:12:00")
    );
  }

  /**
   * date: July 5, 2005  Evaluates The string value of the "add-yearMonthDuration-to-dateTime" operator used together with an "or" expression. .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime10() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1985-07-05T09:09:09Z\") + xs:yearMonthDuration(\"P02Y02M\"))) or fn:string((xs:dateTime(\"1985-07-05T09:09:09Z\") + xs:yearMonthDuration(\"P02Y02M\")))",
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
   * date: July 5, 2005  Evaluates The string value of the "add-yearMonthDuration-to-dateTime" operator used with a boolean expression and the "fn:true" function. .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime12() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1980-03-02T02:02:02Z\") + xs:yearMonthDuration(\"P05Y05M\"))) and (fn:true())",
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
   * date: July 5, 2005  Evaluates The "add-yearMonthDuration-to-dateTime" operator used together with the numeric-equal-operator "eq". .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime13() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1980-05-05T05:05:05Z\") + xs:yearMonthDuration(\"P23Y11M\")) eq xs:dateTime(\"1980-05-05T05:05:05Z\")",
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
   * date: July 5, 2005  Evaluates The "add-yearMonthDuration-to-dateTime" operator used together with the numeric-equal operator "ne". .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime14() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1979-12-12T09:09:09Z\") + xs:yearMonthDuration(\"P08Y08M\")) ne xs:dateTime(\"1979-12-12T09:09:09Z\")",
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
   * date: July 5, 2005  Evaluates The "add-yearMonthDuration-to-dateTime" operator used together with the numeric-equal operator "le". .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime15() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1978-12-12T07:07:07Z\") + xs:yearMonthDuration(\"P17Y12M\")) le xs:dateTime(\"1978-12-12T07:07:07Z\")",
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
   * date: July 5, 2005  Evaluates The "add-yearMonthDuration-to-dateTime" operator used together with the numeric-equal operator "ge". .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime16() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1977-12-12T03:03:03Z\") + xs:yearMonthDuration(\"P18Y02M\")) ge xs:dateTime(\"1977-12-12T03:03:03Z\")",
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
   * date: July 5, 2005  Evaluates The string value of "add-dayTimeDuration-to-dateTime" operator used as part of a boolean expression (and operator) and the "fn:false" function. .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime2() {
    final XQuery query = new XQuery(
      "fn:string(xs:dateTime(\"2000-12-12T12:12:12Z\") + xs:dayTimeDuration(\"P12DT10H07M\")) and fn:false()",
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
   * date: July 5, 2005  Evaluates The string value of "add-yearMonthDuration-to-dateTime" operator as part of a boolean expression (or operator) and the "fn:boolean" function. .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime3() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1999-10-23T13:45:45Z\") + xs:yearMonthDuration(\"P19Y12M\"))) or fn:false()",
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
   * date: July 5, 2005  Evaluates The string value of the "add-yearMonthDuration-to-dateTime" operator that return true and used together with fn:not. .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:dateTime(\"1998-09-12T13:56:12Z\") + xs:yearMonthDuration(\"P20Y03M\")))",
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
   * date: July 5, 2005  Evaluates The "add-yearMonthDuration-to-dateTime" operator that is used as an argument to the fn:number function. .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime6() {
    final XQuery query = new XQuery(
      "fn:number(xs:dateTime(\"1988-01-28T13:45:23Z\") + xs:yearMonthDuration(\"P09Y02M\"))",
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
   * date: July 5, 2005  Evaluates The "add-yearMonthDuration-to-dateTime" operator used as an argument to the "fn:string" function). .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime7() {
    final XQuery query = new XQuery(
      "fn:string(xs:dateTime(\"1989-07-05T14:34:36Z\") + xs:yearMonthDuration(\"P08Y04M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1997-11-05T14:34:36Z")
    );
  }

  /**
   * date: July 8, 2005  Evaluates The "add-yearMonthDuration-to-dateTime" operator that returns a negative value. .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime8() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"0001-01-01T01:01:01Z\") + xs:yearMonthDuration(\"-P20Y07M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "-0021-06-01T01:01:01Z")
      ||
        assertStringValue(false, "-0020-06-01T01:01:01Z")
      )
    );
  }

  /**
   * date: July 5, 2005  Evaluates The string value of the "add-yearMonthDuration-to-dateTime" operator used together with an "and" expression. .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime9() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1993-12-09T10:10:10Z\") + xs:yearMonthDuration(\"P03Y03M\"))) and fn:string((xs:dateTime(\"1993-12-09T10:10:10Z\") + xs:yearMonthDuration(\"P03Y03M\")))",
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
   *  Evaluates The "op:add-yearMonthDuration-to-dateTime" operator with the arguments set as follows: $arg1 = xs:dateTime(lower bound) $arg2 = xs:yearMonthDuration(lower bound) .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime2args1() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") + xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1970-01-01T00:00:00Z")
    );
  }

  /**
   *  Evaluates The "op:add-yearMonthDuration-to-dateTime" operator with the arguments set as follows: $arg1 = xs:dateTime(mid range) $arg2 = xs:yearMonthDuration(lower bound) .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime2args2() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1996-04-07T01:40:52Z\") + xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1996-04-07T01:40:52Z")
    );
  }

  /**
   *  Evaluates The "op:add-yearMonthDuration-to-dateTime" operator with the arguments set as follows: $arg1 = xs:dateTime(upper bound) $arg2 = xs:yearMonthDuration(lower bound) .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime2args3() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2030-12-31T23:59:59Z\") + xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2030-12-31T23:59:59Z")
    );
  }

  /**
   *  Evaluates The "op:add-yearMonthDuration-to-dateTime" operator with the arguments set as follows: $arg1 = xs:dateTime(lower bound) $arg2 = xs:yearMonthDuration(mid range) .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime2args4() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") + xs:yearMonthDuration(\"P1000Y6M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2970-07-01T00:00:00Z")
    );
  }

  /**
   *  Evaluates The "op:add-yearMonthDuration-to-dateTime" operator with the arguments set as follows: $arg1 = xs:dateTime(lower bound) $arg2 = xs:yearMonthDuration(upper bound) .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTime2args5() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") + xs:yearMonthDuration(\"P2030Y12M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4001-01-01T00:00:00Z")
    );
  }

  /**
   * date: July 5, 2005  Evaluates The string value of the "add-yearMonthDuration-to-dateTime" operator that is used as an argument to the fn:boolean function. .
   */
  @org.junit.Test
  public void opAddYearMonthDurationToDateTimealt5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:dateTime(\"1962-03-12T10:12:34Z\") + xs:yearMonthDuration(\"P10Y01M\")))",
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
}
