package org.basex.qt3ts.app;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests derived from the functx library .
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class AppFunctxFn extends QT3TestSet {

  /**
.
   */
  @org.junit.Test
  public void functxFnQName1() {
    final XQuery query = new XQuery(
      "(QName('http://datypic.com/prod','product'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "product")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnQName2() {
    final XQuery query = new XQuery(
      "(QName('http://datypic.com/prod', 'pre:product'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "pre:product")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnQName3() {
    final XQuery query = new XQuery(
      "(QName('', 'product'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "product")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnQNameAll() {
    final XQuery query = new XQuery(
      "(QName('http://datypic.com/prod','product'), QName('http://datypic.com/prod', 'pre:product'), QName('', 'product'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "product pre:product product")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAbs1() {
    final XQuery query = new XQuery(
      "(abs(3.5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3.5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAbs2() {
    final XQuery query = new XQuery(
      "(abs(-4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("4")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAbs3() {
    final XQuery query = new XQuery(
      "(abs(xs:float('-INF')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "INF")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAbsAll() {
    final XQuery query = new XQuery(
      "(abs(3.5), abs(-4), abs(xs:float('-INF')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3.5 4 INF")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAdjustDateToTimezone1() {
    final XQuery query = new XQuery(
      "(adjust-date-to-timezone( xs:date('2006-02-15'), xs:dayTimeDuration('-PT8H')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2006-02-15-08:00")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAdjustDateToTimezone2() {
    final XQuery query = new XQuery(
      "(adjust-date-to-timezone( xs:date('2006-02-15-03:00'), xs:dayTimeDuration('-PT8H')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2006-02-14-08:00")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAdjustDateToTimezone3() {
    final XQuery query = new XQuery(
      "(adjust-date-to-timezone( xs:date('2006-02-15'), ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2006-02-15")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAdjustDateToTimezone4() {
    final XQuery query = new XQuery(
      "(adjust-date-to-timezone( xs:date('2006-02-15-03:00'), ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2006-02-15")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAdjustDateToTimezoneAll() {
    final XQuery query = new XQuery(
      "(adjust-date-to-timezone( xs:date('2006-02-15'), xs:dayTimeDuration('-PT8H')), adjust-date-to-timezone( xs:date('2006-02-15-03:00'), xs:dayTimeDuration('-PT8H')), adjust-date-to-timezone( xs:date('2006-02-15'), ()), adjust-date-to-timezone( xs:date('2006-02-15-03:00'), ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2006-02-15-08:00 2006-02-14-08:00 2006-02-15 2006-02-15")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAdjustDateTimeToTimezone1() {
    final XQuery query = new XQuery(
      "(adjust-dateTime-to-timezone( xs:dateTime('2006-02-15T17:00:00'), xs:dayTimeDuration('-PT7H')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2006-02-15T17:00:00-07:00")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAdjustDateTimeToTimezone2() {
    final XQuery query = new XQuery(
      "(adjust-dateTime-to-timezone( xs:dateTime('2006-02-15T17:00:00-03:00'), xs:dayTimeDuration('-PT7H')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2006-02-15T13:00:00-07:00")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAdjustDateTimeToTimezone3() {
    final XQuery query = new XQuery(
      "(adjust-dateTime-to-timezone( xs:dateTime('2006-02-15T17:00:00'), ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2006-02-15T17:00:00")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAdjustDateTimeToTimezone4() {
    final XQuery query = new XQuery(
      "(adjust-dateTime-to-timezone( xs:dateTime('2006-02-15T17:00:00-03:00'), ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2006-02-15T17:00:00")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAdjustDateTimeToTimezone5() {
    final XQuery query = new XQuery(
      "(adjust-dateTime-to-timezone( xs:dateTime('2006-02-15T01:00:00-03:00'), xs:dayTimeDuration('-PT7H')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2006-02-14T21:00:00-07:00")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAdjustDateTimeToTimezoneAll() {
    final XQuery query = new XQuery(
      "(adjust-dateTime-to-timezone( xs:dateTime('2006-02-15T17:00:00'), xs:dayTimeDuration('-PT7H')), adjust-dateTime-to-timezone( xs:dateTime('2006-02-15T17:00:00-03:00'), xs:dayTimeDuration('-PT7H')), adjust-dateTime-to-timezone( xs:dateTime('2006-02-15T17:00:00'), ()), adjust-dateTime-to-timezone( xs:dateTime('2006-02-15T17:00:00-03:00'), ()), adjust-dateTime-to-timezone( xs:dateTime('2006-02-15T01:00:00-03:00'), xs:dayTimeDuration('-PT7H')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2006-02-15T17:00:00-07:00 2006-02-15T13:00:00-07:00 2006-02-15T17:00:00 2006-02-15T17:00:00 2006-02-14T21:00:00-07:00")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAdjustTimeToTimezone1() {
    final XQuery query = new XQuery(
      "(adjust-time-to-timezone( xs:time('17:00:00'), xs:dayTimeDuration('-PT7H')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "17:00:00-07:00")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAdjustTimeToTimezone2() {
    final XQuery query = new XQuery(
      "(adjust-time-to-timezone( xs:time('17:00:00-03:00'), xs:dayTimeDuration('-PT7H')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "13:00:00-07:00")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAdjustTimeToTimezone3() {
    final XQuery query = new XQuery(
      "(adjust-time-to-timezone( xs:time('17:00:00'), ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "17:00:00")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAdjustTimeToTimezone4() {
    final XQuery query = new XQuery(
      "(adjust-time-to-timezone( xs:time('17:00:00-03:00'), ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "17:00:00")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAdjustTimeToTimezoneAll() {
    final XQuery query = new XQuery(
      "(adjust-time-to-timezone( xs:time('17:00:00'), xs:dayTimeDuration('-PT7H')), adjust-time-to-timezone( xs:time('17:00:00-03:00'), xs:dayTimeDuration('-PT7H')), adjust-time-to-timezone( xs:time('17:00:00'), ()), adjust-time-to-timezone( xs:time('17:00:00-03:00'), ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "17:00:00-07:00 13:00:00-07:00 17:00:00 17:00:00")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAvg1() {
    final XQuery query = new XQuery(
      "(avg( (1, 2, 3, 4, 5) ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAvg2() {
    final XQuery query = new XQuery(
      "(avg( (1, 2, 3, (), 4, 5) ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAvg3() {
    final XQuery query = new XQuery(
      "(avg( (xs:yearMonthDuration('P4M'), xs:yearMonthDuration('P6M') ) ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P5M")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAvg4() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (avg($ordDoc//item/@quantity))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1.1666666666666667")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAvg5() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (avg( () ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnAvgAll() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (avg( (1, 2, 3, 4, 5) ), avg( (1, 2, 3, (), 4, 5) ), avg( (xs:yearMonthDuration('P4M'), xs:yearMonthDuration('P6M') ) ), avg($ordDoc//item/@quantity), avg( () ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("3, 3, xs:yearMonthDuration('P5M'), 1.1666666666666667")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnBaseUri1() {
    final XQuery query = new XQuery(
      "let $cats := (/) return (base-uri($cats//catalog[1]))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_cats.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.org/ACC/")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnBaseUri2() {
    final XQuery query = new XQuery(
      "let $cats := (/) return (base-uri($cats//catalog[2]/product))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_cats.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.org/WMN/")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnBaseUri3() {
    final XQuery query = new XQuery(
      "let $cats := (/) return (base-uri($cats//catalog[2]/product/@href))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_cats.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.org/WMN/")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnBaseUriAll() {
    final XQuery query = new XQuery(
      "let $cats := (/) return (base-uri($cats//catalog[1]), base-uri($cats//catalog[2]/product), base-uri($cats//catalog[2]/product/@href))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_cats.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.org/ACC/ http://example.org/WMN/ http://example.org/WMN/")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnBoolean1() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (boolean( () ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
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
.
   */
  @org.junit.Test
  public void functxFnBoolean2() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (boolean(''))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
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
.
   */
  @org.junit.Test
  public void functxFnBoolean3() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (boolean(0))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
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
.
   */
  @org.junit.Test
  public void functxFnBoolean4() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (boolean('0'))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
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
.
   */
  @org.junit.Test
  public void functxFnBoolean5() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (boolean('false'))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
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
.
   */
  @org.junit.Test
  public void functxFnBoolean6() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (boolean(xs:float('NaN')))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
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
.
   */
  @org.junit.Test
  public void functxFnBoolean7() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (boolean($ordDoc/order[1]))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
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
.
   */
  @org.junit.Test
  public void functxFnBoolean8() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (boolean($ordDoc/noSuchChild))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
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
.
   */
  @org.junit.Test
  public void functxFnBoolean9() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (boolean(<a>false</a>))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
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
.
   */
  @org.junit.Test
  public void functxFnBooleanAll() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (boolean( () ), boolean(''), boolean(0), boolean('0'), boolean('false'), boolean(xs:float('NaN')), boolean($ordDoc/order[1]), boolean($ordDoc/noSuchChild), boolean(<a>false</a>))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "false false false true true false true false true")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCeiling1() {
    final XQuery query = new XQuery(
      "(ceiling(5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCeiling2() {
    final XQuery query = new XQuery(
      "(ceiling(5.1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("6")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCeiling3() {
    final XQuery query = new XQuery(
      "(ceiling(5.5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("6")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCeiling4() {
    final XQuery query = new XQuery(
      "(ceiling(-5.5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCeiling5() {
    final XQuery query = new XQuery(
      "(ceiling(-5.51))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCeiling6() {
    final XQuery query = new XQuery(
      "(ceiling( () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCeilingAll() {
    final XQuery query = new XQuery(
      "(ceiling(5), ceiling(5.1), ceiling(5.5), ceiling(-5.5), ceiling(-5.51), ceiling( () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5 6 6 -5 -5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCodepointEqual1() {
    final XQuery query = new XQuery(
      "(codepoint-equal('abc', 'abc'))",
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
.
   */
  @org.junit.Test
  public void functxFnCodepointEqual2() {
    final XQuery query = new XQuery(
      "(codepoint-equal('abc', 'ab c'))",
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
.
   */
  @org.junit.Test
  public void functxFnCodepointEqual3() {
    final XQuery query = new XQuery(
      "(codepoint-equal('abc', ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCodepointEqualAll() {
    final XQuery query = new XQuery(
      "(codepoint-equal('abc', 'abc'), codepoint-equal('abc', 'ab c'), codepoint-equal('abc', ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true false")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCodepointsToString1() {
    final XQuery query = new XQuery(
      "(codepoints-to-string((97, 32, 98, 32, 99)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCodepointsToString2() {
    final XQuery query = new XQuery(
      "(codepoints-to-string(97))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCodepointsToString3() {
    final XQuery query = new XQuery(
      "(codepoints-to-string(()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCodepointsToStringAll() {
    final XQuery query = new XQuery(
      "(codepoints-to-string((97, 32, 98, 32, 99)), codepoints-to-string(97), codepoints-to-string(()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b c a ")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCompare1() {
    final XQuery query = new XQuery(
      "(compare('a', 'b'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCompare2() {
    final XQuery query = new XQuery(
      "(compare('a', 'a'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCompare3() {
    final XQuery query = new XQuery(
      "(compare('b', 'a'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCompare4() {
    final XQuery query = new XQuery(
      "(compare('ab', 'abc'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCompare5() {
    final XQuery query = new XQuery(
      "(compare('a', 'B'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCompare6() {
    final XQuery query = new XQuery(
      "(compare(upper-case('a'), upper-case('B')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCompare7() {
    final XQuery query = new XQuery(
      "(compare('a', ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCompareAll() {
    final XQuery query = new XQuery(
      "(compare('a', 'b'), compare('a', 'a'), compare('b', 'a'), compare('ab', 'abc'), compare('a', 'B'), compare(upper-case('a'), upper-case('B')), compare('a', ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1 0 1 -1 1 -1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnConcat1() {
    final XQuery query = new XQuery(
      "(concat('a', 'b'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ab")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnConcat2() {
    final XQuery query = new XQuery(
      "(concat('a', 'b', 'c'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnConcat3() {
    final XQuery query = new XQuery(
      "(concat('a', (), 'b', '', 'c'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnConcat4() {
    final XQuery query = new XQuery(
      "(concat('a', <x>b</x>, 'c'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnConcatAll() {
    final XQuery query = new XQuery(
      "(concat('a', 'b'), concat('a', 'b', 'c'), concat('a', (), 'b', '', 'c'), concat('a', <x>b</x>, 'c'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ab abc abc abc")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnContains1() {
    final XQuery query = new XQuery(
      "(contains('query', 'e'))",
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
.
   */
  @org.junit.Test
  public void functxFnContains2() {
    final XQuery query = new XQuery(
      "(contains('query', 'ery'))",
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
.
   */
  @org.junit.Test
  public void functxFnContains3() {
    final XQuery query = new XQuery(
      "(contains('query', 'query'))",
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
.
   */
  @org.junit.Test
  public void functxFnContains4() {
    final XQuery query = new XQuery(
      "(contains('query', 'x'))",
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
.
   */
  @org.junit.Test
  public void functxFnContains5() {
    final XQuery query = new XQuery(
      "(contains('query', ''))",
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
.
   */
  @org.junit.Test
  public void functxFnContains6() {
    final XQuery query = new XQuery(
      "(contains('query', ()))",
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
.
   */
  @org.junit.Test
  public void functxFnContains7() {
    final XQuery query = new XQuery(
      "(contains( (), 'q'))",
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
.
   */
  @org.junit.Test
  public void functxFnContainsAll() {
    final XQuery query = new XQuery(
      "(contains('query', 'e'), contains('query', 'ery'), contains('query', 'query'), contains('query', 'x'), contains('query', ''), contains('query', ()), contains( (), 'q'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true true false true true false")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCount1() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (count( (1, 2, 3) ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCount2() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (count($ordDoc//item))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("6")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCount3() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (count( distinct-values($ordDoc//item/@num)))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("4")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCount4() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (count( (1, 2, 3, () ) ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCount5() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (count( () ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnCountAll() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (count( (1, 2, 3) ), count($ordDoc//item), count( distinct-values($ordDoc//item/@num)), count( (1, 2, 3, () ) ), count( () ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3 6 4 3 0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnData1() {
    final XQuery query = new XQuery(
      "let $cat := (/) return (data($cat//product[1]/number))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_catalog.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "557")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnData2() {
    final XQuery query = new XQuery(
      "let $cat := (/) return (data($cat//number))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_catalog.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "557 563 443 784")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnData3() {
    final XQuery query = new XQuery(
      "let $cat := (/) return (data($cat//product[1]/@dept))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_catalog.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "WMN")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnData4() {
    final XQuery query = new XQuery(
      "let $cat := (/) return (data($cat//product[1]/colorChoices))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_catalog.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "navy black")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnData5() {
    final XQuery query = new XQuery(
      "let $cat := (/) return (data($cat//product[1]))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_catalog.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "557 Fleece Pullover navy black")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnData6() {
    final XQuery query = new XQuery(
      "let $cat := (/) return (data($cat//product[4]/desc))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_catalog.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Our favorite shirt!")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDataAll() {
    final XQuery query = new XQuery(
      "let $cat := (/) return (data($cat//product[1]/number), data($cat//number), data($cat//product[1]/@dept), data($cat//product[1]/colorChoices), data($cat//product[1]), data($cat//product[4]/desc))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_catalog.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "557 557 563 443 784 WMN navy black 557 Fleece Pullover navy black Our favorite shirt!")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDateTime1() {
    final XQuery query = new XQuery(
      "(dateTime(xs:date('2006-08-15'), xs:time('12:30:45-05:00')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2006-08-15T12:30:45-05:00")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDayFromDate1() {
    final XQuery query = new XQuery(
      "(day-from-date(xs:date('2006-08-15')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("15")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDayFromDateTime1() {
    final XQuery query = new XQuery(
      "(day-from-dateTime( xs:dateTime('2006-08-15T10:30:23')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("15")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDaysFromDuration1() {
    final XQuery query = new XQuery(
      "(days-from-duration( xs:dayTimeDuration('P5D')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDaysFromDuration2() {
    final XQuery query = new XQuery(
      "(days-from-duration( xs:dayTimeDuration('-PT24H')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDaysFromDuration3() {
    final XQuery query = new XQuery(
      "(days-from-duration( xs:dayTimeDuration('PT23H')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDaysFromDuration4() {
    final XQuery query = new XQuery(
      "(days-from-duration( xs:dayTimeDuration('P1DT36H')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDaysFromDuration5() {
    final XQuery query = new XQuery(
      "(days-from-duration( xs:dayTimeDuration('PT1440M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDaysFromDurationAll() {
    final XQuery query = new XQuery(
      "(days-from-duration( xs:dayTimeDuration('P5D')), days-from-duration( xs:dayTimeDuration('-PT24H')), days-from-duration( xs:dayTimeDuration('PT23H')), days-from-duration( xs:dayTimeDuration('P1DT36H')), days-from-duration( xs:dayTimeDuration('PT1440M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5 -1 0 2 1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDeepEqual1() {
    final XQuery query = new XQuery(
      "let $prod1 := <product dept='MEN' id='P123'> <number>784</number> </product> return let $prod2 := <product id='P123' dept='MEN'><!--comment--> <number>784</number> </product> return (deep-equal( 1, 1 ))",
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
.
   */
  @org.junit.Test
  public void functxFnDeepEqual2() {
    final XQuery query = new XQuery(
      "let $prod1 := <product dept='MEN' id='P123'> <number>784</number> </product> return let $prod2 := <product id='P123' dept='MEN'><!--comment--> <number>784</number> </product> return (deep-equal( (1, 1), (1, 1) ))",
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
.
   */
  @org.junit.Test
  public void functxFnDeepEqual3() {
    final XQuery query = new XQuery(
      "let $prod1 := <product dept='MEN' id='P123'> <number>784</number> </product> return let $prod2 := <product id='P123' dept='MEN'><!--comment--> <number>784</number> </product> return (deep-equal( (1, 2), (1.0, 2.0) ))",
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
.
   */
  @org.junit.Test
  public void functxFnDeepEqual4() {
    final XQuery query = new XQuery(
      "let $prod1 := <product dept='MEN' id='P123'> <number>784</number> </product> return let $prod2 := <product id='P123' dept='MEN'><!--comment--> <number>784</number> </product> return (deep-equal( (1, 2), (2, 1) ))",
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
.
   */
  @org.junit.Test
  public void functxFnDeepEqual5() {
    final XQuery query = new XQuery(
      "let $prod1 := <product dept='MEN' id='P123'> <number>784</number> </product> return let $prod2 := <product id='P123' dept='MEN'><!--comment--> <number>784</number> </product> return (deep-equal( $prod1, $prod2 ))",
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
.
   */
  @org.junit.Test
  public void functxFnDeepEqual6() {
    final XQuery query = new XQuery(
      "let $prod1 := <product dept='MEN' id='P123'> <number>784</number> </product> return let $prod2 := <product id='P123' dept='MEN'><!--comment--> <number>784</number> </product> return (deep-equal( $prod1/number, $prod2/number ))",
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
.
   */
  @org.junit.Test
  public void functxFnDeepEqualAll() {
    final XQuery query = new XQuery(
      "let $prod1 := <product dept='MEN' id='P123'> <number>784</number> </product> return let $prod2 := <product id='P123' dept='MEN'><!--comment--> <number>784</number> </product> return (deep-equal( 1, 1 ), deep-equal( (1, 1), (1, 1) ), deep-equal( (1, 2), (1.0, 2.0) ), deep-equal( (1, 2), (2, 1) ), deep-equal( $prod1, $prod2 ), deep-equal( $prod1/number, $prod2/number ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true true false true true")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDefaultCollation1() {
    final XQuery query = new XQuery(
      "(default-collation())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.w3.org/2005/xpath-functions/collation/codepoint")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDistinctValues1() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a>3</a> <b>5</b> <b>3</b> </in-xml> return (distinct-values( ('a', 'b', 'a') ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDistinctValues2() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a>3</a> <b>5</b> <b>3</b> </in-xml> return (distinct-values( (1, 2, 3) ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDistinctValues3() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a>3</a> <b>5</b> <b>3</b> </in-xml> return (distinct-values( ('a', 2, 3) ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a 2 3")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDistinctValues4() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a>3</a> <b>5</b> <b>3</b> </in-xml> return (distinct-values( (xs:integer('1'), xs:decimal('1.0'), xs:float('1.0E0') ) ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDistinctValues5() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a>3</a> <b>5</b> <b>3</b> </in-xml> return (distinct-values($in-xml/*))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3 5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDistinctValues6() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a>3</a> <b>5</b> <b>3</b> </in-xml> return (distinct-values( () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDistinctValuesAll() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a>3</a> <b>5</b> <b>3</b> </in-xml> return (distinct-values( ('a', 'b', 'a') ), distinct-values( (1, 2, 3) ), distinct-values( ('a', 2, 3) ), distinct-values( (xs:integer('1'), xs:decimal('1.0'), xs:float('1.0E0') ) ), distinct-values($in-xml/*), distinct-values( () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b 1 2 3 a 2 3 1 3 5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnDocAvailable1() {
    final XQuery query = new XQuery(
      "(doc-available( document-uri(/) ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
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
.
   */
  @org.junit.Test
  public void functxFnEmpty1() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a></a> <b/> <c>xyz</c> </in-xml> return (empty( ('a', 'b', 'c') ))",
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
.
   */
  @org.junit.Test
  public void functxFnEmpty2() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a></a> <b/> <c>xyz</c> </in-xml> return (empty( () ))",
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
.
   */
  @org.junit.Test
  public void functxFnEmpty3() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a></a> <b/> <c>xyz</c> </in-xml> return (empty(0))",
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
.
   */
  @org.junit.Test
  public void functxFnEmpty4() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a></a> <b/> <c>xyz</c> </in-xml> return (empty($in-xml/a))",
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
.
   */
  @org.junit.Test
  public void functxFnEmpty5() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a></a> <b/> <c>xyz</c> </in-xml> return (empty($in-xml/b))",
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
.
   */
  @org.junit.Test
  public void functxFnEmpty6() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a></a> <b/> <c>xyz</c> </in-xml> return (empty($in-xml/c))",
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
.
   */
  @org.junit.Test
  public void functxFnEmpty7() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a></a> <b/> <c>xyz</c> </in-xml> return (empty($in-xml/foo))",
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
.
   */
  @org.junit.Test
  public void functxFnEmptyAll() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a></a> <b/> <c>xyz</c> </in-xml> return (empty( ('a', 'b', 'c') ), empty( () ), empty(0), empty($in-xml/a), empty($in-xml/b), empty($in-xml/c), empty($in-xml/foo))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "false true false false false false true")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnEncodeForUri1() {
    final XQuery query = new XQuery(
      "(encode-for-uri( 'Sales % Numbers.pdf'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Sales%20%25%20Numbers.pdf")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnEncodeForUri2() {
    final XQuery query = new XQuery(
      "(encode-for-uri( 'http://datypic.com/a%20URI#frag'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http%3A%2F%2Fdatypic.com%2Fa%2520URI%23frag")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnEncodeForUriAll() {
    final XQuery query = new XQuery(
      "(encode-for-uri( 'Sales % Numbers.pdf'), encode-for-uri( 'http://datypic.com/a%20URI#frag'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Sales%20%25%20Numbers.pdf http%3A%2F%2Fdatypic.com%2Fa%2520URI%23frag")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnEndsWith1() {
    final XQuery query = new XQuery(
      "(ends-with('query', 'y'))",
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
.
   */
  @org.junit.Test
  public void functxFnEndsWith2() {
    final XQuery query = new XQuery(
      "(ends-with('query', 'query'))",
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
.
   */
  @org.junit.Test
  public void functxFnEndsWith3() {
    final XQuery query = new XQuery(
      "(ends-with('query', ''))",
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
.
   */
  @org.junit.Test
  public void functxFnEndsWith4() {
    final XQuery query = new XQuery(
      "(ends-with('query ', 'y'))",
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
.
   */
  @org.junit.Test
  public void functxFnEndsWith5() {
    final XQuery query = new XQuery(
      "(ends-with('', 'y'))",
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
.
   */
  @org.junit.Test
  public void functxFnEndsWithAll() {
    final XQuery query = new XQuery(
      "(ends-with('query', 'y'), ends-with('query', 'query'), ends-with('query', ''), ends-with('query ', 'y'), ends-with('', 'y'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true true false false")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnEscapeHtmlUri1() {
    final XQuery query = new XQuery(
      "(escape-html-uri( 'http://datypic.com/a%20URI#frag'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://datypic.com/a%20URI#frag")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnEscapeHtmlUri2() {
    final XQuery query = new XQuery(
      "(escape-html-uri('http://datypic.com'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://datypic.com")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnEscapeHtmlUriAll() {
    final XQuery query = new XQuery(
      "(escape-html-uri( 'http://datypic.com/a%20URI#frag'), escape-html-uri('http://datypic.com'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://datypic.com/a%20URI#frag http://datypic.com")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnExactlyOne1() {
    final XQuery query = new XQuery(
      "(exactly-one('a'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnExists1() {
    final XQuery query = new XQuery(
      "(exists( ('a', 'b', 'c') ))",
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
.
   */
  @org.junit.Test
  public void functxFnExists2() {
    final XQuery query = new XQuery(
      "(exists( '' ))",
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
.
   */
  @org.junit.Test
  public void functxFnExists3() {
    final XQuery query = new XQuery(
      "(exists( () ))",
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
.
   */
  @org.junit.Test
  public void functxFnExists4() {
    final XQuery query = new XQuery(
      "(exists( false() ))",
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
.
   */
  @org.junit.Test
  public void functxFnExistsAll() {
    final XQuery query = new XQuery(
      "(exists( ('a', 'b', 'c') ), exists( '' ), exists( () ), exists( false() ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true false true")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnFalse1() {
    final XQuery query = new XQuery(
      "(false())",
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
.
   */
  @org.junit.Test
  public void functxFnFloor1() {
    final XQuery query = new XQuery(
      "(floor(5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnFloor2() {
    final XQuery query = new XQuery(
      "(floor(5.1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnFloor3() {
    final XQuery query = new XQuery(
      "(floor(5.7))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnFloor4() {
    final XQuery query = new XQuery(
      "(floor(-5.1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-6")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnFloor5() {
    final XQuery query = new XQuery(
      "(floor(-5.7))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-6")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnFloor6() {
    final XQuery query = new XQuery(
      "(floor( () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnFloorAll() {
    final XQuery query = new XQuery(
      "(floor(5), floor(5.1), floor(5.7), floor(-5.1), floor(-5.7), floor( () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5 5 5 -6 -6")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnHoursFromDateTime1() {
    final XQuery query = new XQuery(
      "(hours-from-dateTime( xs:dateTime('2006-08-15T10:30:23')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("10")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnHoursFromDateTime2() {
    final XQuery query = new XQuery(
      "(hours-from-dateTime( xs:dateTime('2006-08-15T10:30:23-05:00')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("10")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnHoursFromDateTimeAll() {
    final XQuery query = new XQuery(
      "(hours-from-dateTime( xs:dateTime('2006-08-15T10:30:23')), hours-from-dateTime( xs:dateTime('2006-08-15T10:30:23-05:00')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10 10")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnHoursFromDuration1() {
    final XQuery query = new XQuery(
      "(hours-from-duration( xs:dayTimeDuration('P1DT5H')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnHoursFromDuration2() {
    final XQuery query = new XQuery(
      "(hours-from-duration( xs:dayTimeDuration('-PT36H')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-12")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnHoursFromDuration3() {
    final XQuery query = new XQuery(
      "(hours-from-duration( xs:dayTimeDuration('PT1H90M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnHoursFromDuration4() {
    final XQuery query = new XQuery(
      "(hours-from-duration( xs:dayTimeDuration('PT2H59M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnHoursFromDuration5() {
    final XQuery query = new XQuery(
      "(hours-from-duration( xs:dayTimeDuration('PT3600S')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnHoursFromDurationAll() {
    final XQuery query = new XQuery(
      "(hours-from-duration( xs:dayTimeDuration('P1DT5H')), hours-from-duration( xs:dayTimeDuration('-PT36H')), hours-from-duration( xs:dayTimeDuration('PT1H90M')), hours-from-duration( xs:dayTimeDuration('PT2H59M')), hours-from-duration( xs:dayTimeDuration('PT3600S')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5 -12 2 2 1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnHoursFromTime1() {
    final XQuery query = new XQuery(
      "(hours-from-time( xs:time('10:30:23')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("10")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnHoursFromTime2() {
    final XQuery query = new XQuery(
      "(hours-from-time( xs:time('10:30:23-05:00')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("10")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnHoursFromTimeAll() {
    final XQuery query = new XQuery(
      "(hours-from-time( xs:time('10:30:23')), hours-from-time( xs:time('10:30:23-05:00')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10 10")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnIndexOf1() {
    final XQuery query = new XQuery(
      "(index-of( ('a', 'b', 'c'), 'a'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnIndexOf2() {
    final XQuery query = new XQuery(
      "(index-of( ('a', 'b', 'c'), 'd'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnIndexOf3() {
    final XQuery query = new XQuery(
      "(index-of( (4, 5, 6, 4), 4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 4")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnIndexOf4() {
    final XQuery query = new XQuery(
      "(index-of( (4, 5, 6, 4), 04.0))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 4")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnIndexOf5() {
    final XQuery query = new XQuery(
      "(index-of( ('a', 5, 6), 'a'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnIndexOf6() {
    final XQuery query = new XQuery(
      "(index-of( (), 'a'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnIndexOf7() {
    final XQuery query = new XQuery(
      "(index-of( (<a>1</a>, <b>1</b>), <c>1</c> ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnIndexOfAll() {
    final XQuery query = new XQuery(
      "(index-of( ('a', 'b', 'c'), 'a'), index-of( ('a', 'b', 'c'), 'd'), index-of( (4, 5, 6, 4), 4), index-of( (4, 5, 6, 4), 04.0), index-of( ('a', 5, 6), 'a'), index-of( (), 'a'), index-of( (<a>1</a>, <b>1</b>), <c>1</c> ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 1 4 1 4 1 1 2")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnInsertBefore1() {
    final XQuery query = new XQuery(
      "(insert-before( ('a', 'b', 'c'), 1, ('x', 'y')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "x y a b c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnInsertBefore2() {
    final XQuery query = new XQuery(
      "(insert-before( ('a', 'b', 'c'), 2, ('x', 'y')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a x y b c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnInsertBefore3() {
    final XQuery query = new XQuery(
      "(insert-before( ('a', 'b', 'c'), 10, ('x', 'y')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b c x y")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnInsertBefore4() {
    final XQuery query = new XQuery(
      "(insert-before( ('a', 'b', 'c'), 0, ('x', 'y')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "x y a b c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnInsertBefore5() {
    final XQuery query = new XQuery(
      "(insert-before( ('a', 'b', 'c'), 2, ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnInsertBefore6() {
    final XQuery query = new XQuery(
      "(insert-before( (), 3, ('a', 'b', 'c') ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnInsertBeforeAll() {
    final XQuery query = new XQuery(
      "(insert-before( ('a', 'b', 'c'), 1, ('x', 'y')), insert-before( ('a', 'b', 'c'), 2, ('x', 'y')), insert-before( ('a', 'b', 'c'), 10, ('x', 'y')), insert-before( ('a', 'b', 'c'), 0, ('x', 'y')), insert-before( ('a', 'b', 'c'), 2, ()), insert-before( (), 3, ('a', 'b', 'c') ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "x y a b c a x y b c a b c x y x y a b c a b c a b c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnIriToUri1() {
    final XQuery query = new XQuery(
      "(iri-to-uri( 'http://datypic.com/Sales Numbers.pdf'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://datypic.com/Sales%20Numbers.pdf")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLang1() {
    final XQuery query = new XQuery(
      "let $in-xml := <desclist xml:lang=\"en\"> <desc xml:lang=\"en-US\"> <line>A line of text.</line> </desc> <desc xml:lang=\"fr\"> <line>Une ligne de texte.</line> </desc> </desclist> return ($in-xml// desc[lang('en')])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<desc xml:lang=\"en-US\"><line>A line of text.</line></desc>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLang2() {
    final XQuery query = new XQuery(
      "let $in-xml := <desclist xml:lang=\"en\"> <desc xml:lang=\"en-US\"> <line>A line of text.</line> </desc> <desc xml:lang=\"fr\"> <line>Une ligne de texte.</line> </desc> </desclist> return ($in-xml// desc[lang('en-US')])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<desc xml:lang=\"en-US\"><line>A line of text.</line></desc>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLang3() {
    final XQuery query = new XQuery(
      "let $in-xml := <desclist xml:lang=\"en\"> <desc xml:lang=\"en-US\"> <line>A line of text.</line> </desc> <desc xml:lang=\"fr\"> <line>Une ligne de texte.</line> </desc> </desclist> return ($in-xml// desc[lang('fr')])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<desc xml:lang=\"fr\"><line>Une ligne de texte.</line></desc>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLang4() {
    final XQuery query = new XQuery(
      "let $in-xml := <desclist xml:lang=\"en\"> <desc xml:lang=\"en-US\"> <line>A line of text.</line> </desc> <desc xml:lang=\"fr\"> <line>Une ligne de texte.</line> </desc> </desclist> return ($in-xml// desc/line[lang('en')])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<line>A line of text.</line>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLang5() {
    final XQuery query = new XQuery(
      "let $in-xml := <desclist xml:lang=\"en\"> <desc xml:lang=\"en-US\"> <line>A line of text.</line> </desc> <desc xml:lang=\"fr\"> <line>Une ligne de texte.</line> </desc> </desclist> return ($in-xml[lang('en-US')])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLang6() {
    final XQuery query = new XQuery(
      "let $in-xml := <desclist xml:lang=\"en\"> <desc xml:lang=\"en-US\"> <line>A line of text.</line> </desc> <desc xml:lang=\"fr\"> <line>Une ligne de texte.</line> </desc> </desclist> return ($in-xml// desc[lang('FR')])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<desc xml:lang=\"fr\"><line>Une ligne de texte.</line></desc>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLangAll() {
    final XQuery query = new XQuery(
      "let $in-xml := <desclist xml:lang=\"en\"> <desc xml:lang=\"en-US\"> <line>A line of text.</line> </desc> <desc xml:lang=\"fr\"> <line>Une ligne de texte.</line> </desc> </desclist> return ($in-xml// desc[lang('en')], $in-xml// desc[lang('en-US')], $in-xml// desc[lang('fr')], $in-xml// desc/line[lang('en')], $in-xml[lang('en-US')], $in-xml// desc[lang('FR')])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<desc xml:lang=\"en-US\"><line>A line of text.</line></desc><desc xml:lang=\"en-US\"><line>A line of text.</line></desc><desc xml:lang=\"fr\"><line>Une ligne de texte.</line></desc><line>A line of text.</line><desc xml:lang=\"fr\"><line>Une ligne de texte.</line></desc>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLast1() {
    final XQuery query = new XQuery(
      "(/ catalog/product[last()])",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_catalog.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<product dept=\"MEN\">\n  <number>784</number>\n  <name language=\"en\">Cotton Dress Shirt</name>\n  <colorChoices>white gray</colorChoices>\n  <desc>Our <i>favorite</i> shirt!</desc>\n </product>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLocalName1() {
    final XQuery query = new XQuery(
      "declare namespace pre = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (local-name($in-xml))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "noNamespace")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLocalName2() {
    final XQuery query = new XQuery(
      "declare namespace pre = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (local-name($in-xml//pre:prefixed))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "prefixed")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLocalName3() {
    final XQuery query = new XQuery(
      "declare namespace pre = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (local-name($in-xml//unpre:unprefixed))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "unprefixed")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLocalName4() {
    final XQuery query = new XQuery(
      "declare namespace pre = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (local-name($in-xml//@pre:prefAttr))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "prefAttr")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLocalName5() {
    final XQuery query = new XQuery(
      "declare namespace pre = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (local-name($in-xml//@noNSAttr))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "noNSAttr")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLocalNameAll() {
    final XQuery query = new XQuery(
      "declare namespace pre = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (local-name($in-xml), local-name($in-xml//pre:prefixed), local-name($in-xml//unpre:unprefixed), local-name($in-xml//@pre:prefAttr), local-name($in-xml//@noNSAttr))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "noNamespace prefixed unprefixed prefAttr noNSAttr")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLocalNameFromQName1() {
    final XQuery query = new XQuery(
      "(local-name-from-QName( QName('http://datypic.com/prod', 'number')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "number")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLocalNameFromQName2() {
    final XQuery query = new XQuery(
      "(local-name-from-QName(QName ('', 'number')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "number")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLocalNameFromQName3() {
    final XQuery query = new XQuery(
      "(local-name-from-QName( () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLocalNameFromQNameAll() {
    final XQuery query = new XQuery(
      "(local-name-from-QName( QName('http://datypic.com/prod', 'number')), local-name-from-QName(QName ('', 'number')), local-name-from-QName( () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "number number")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLowerCase1() {
    final XQuery query = new XQuery(
      "(lower-case('QUERY'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLowerCase2() {
    final XQuery query = new XQuery(
      "(lower-case('Query'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLowerCase3() {
    final XQuery query = new XQuery(
      "(lower-case('QUERY123'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "query123")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnLowerCaseAll() {
    final XQuery query = new XQuery(
      "(lower-case('QUERY'), lower-case('Query'), lower-case('QUERY123'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "query query query123")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMatches1() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street Traverse City, MI 49684' return (matches('query', 'q'))",
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
.
   */
  @org.junit.Test
  public void functxFnMatches10() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street Traverse City, MI 49684' return (matches($address, 'Street.*City', 's'))",
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
.
   */
  @org.junit.Test
  public void functxFnMatches11() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street\n" +
      "Traverse City, MI 49684' return (matches($address, 'Street$'))",
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
.
   */
  @org.junit.Test
  public void functxFnMatches12() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street\n" +
      "Traverse City, MI 49684' return (matches($address, 'Street$', 'm'))",
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
.
   */
  @org.junit.Test
  public void functxFnMatches13() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street\n" +
      "Traverse City, MI 49684' return (matches($address, 'street'))",
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
.
   */
  @org.junit.Test
  public void functxFnMatches14() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street\n" +
      "Traverse City, MI 49684' return (matches($address, 'street', 'i'))",
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
.
   */
  @org.junit.Test
  public void functxFnMatches15() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street\n" +
      "Traverse City, MI 49684' return (matches($address, 'Main Street'))",
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
.
   */
  @org.junit.Test
  public void functxFnMatches16() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street\n" +
      "Traverse City, MI 49684' return (matches($address, 'Main Street', 'x'))",
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
.
   */
  @org.junit.Test
  public void functxFnMatches17() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street\n" +
      "Traverse City, MI 49684' return (matches($address, 'Main \\s Street', 'x'))",
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
.
   */
  @org.junit.Test
  public void functxFnMatches18() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street\n" +
      "Traverse City, MI 49684' return (matches($address, 'street$', 'im'))",
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
.
   */
  @org.junit.Test
  public void functxFnMatches2() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street Traverse City, MI 49684' return (matches('query', 'ue'))",
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
.
   */
  @org.junit.Test
  public void functxFnMatches3() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street Traverse City, MI 49684' return (matches('query', '^qu'))",
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
.
   */
  @org.junit.Test
  public void functxFnMatches4() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street Traverse City, MI 49684' return (matches('query', 'qu$'))",
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
.
   */
  @org.junit.Test
  public void functxFnMatches5() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street Traverse City, MI 49684' return (matches('query', '[ux]'))",
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
.
   */
  @org.junit.Test
  public void functxFnMatches6() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street Traverse City, MI 49684' return (matches('query', 'q.*'))",
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
.
   */
  @org.junit.Test
  public void functxFnMatches7() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street Traverse City, MI 49684' return (matches('query', '[a-z]{5}'))",
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
.
   */
  @org.junit.Test
  public void functxFnMatches8() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street Traverse City, MI 49684' return (matches((), 'q' ))",
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
.
   */
  @org.junit.Test
  public void functxFnMatches9() {
    final XQuery query = new XQuery(
      "let $address := '123 Main Street\n" +
      "Traverse City, MI 49684' return (matches($address, 'Street.*City'))",
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
.
   */
  @org.junit.Test
  public void functxFnMatchesAll() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $address := '123 Main Street\n" +
      "Traverse City, MI 49684' \n" +
      "        return (matches('query', 'q'), matches('query', 'ue'), matches('query', '^qu'), \n" +
      "            matches('query', 'qu$'), matches('query', '[ux]'), matches('query', 'q.*'), \n" +
      "            matches('query', '[a-z]{5}'), matches((), 'q' ), matches($address, 'Street.*City'), \n" +
      "            matches($address, 'Street.*City', 's'), matches($address, 'Street$'), \n" +
      "            matches($address, 'Street$', 'm'), matches($address, 'street'), \n" +
      "            matches($address, 'street', 'i'), matches($address, 'Main Street'), \n" +
      "            matches($address, 'Main Street', 'x'), matches($address, 'Main \\s Street', 'x'), \n" +
      "            matches($address, 'street$', 'im'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true true false true true true false false true false true false true true false true true")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMax1() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (max( (2, 1, 5, 4, 3) ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMax2() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (max( ('a', 'b', 'c') ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMax3() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (max( 2 ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMax4() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (max($ordDoc//item/string(@dept)))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "WMN")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMaxAll() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (max( (2, 1, 5, 4, 3) ), max( ('a', 'b', 'c') ), max( 2 ), max($ordDoc//item/string(@dept)))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5 c 2 WMN")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMin1() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (min( (2.0, 1, 3.5, 4) ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMin2() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (min( ('a', 'b', 'c') ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMin3() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (min($ordDoc//item//string(@color)))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMin4() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (min($ordDoc//item/@color/string(.)))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "beige")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMinAll() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (min( (2.0, 1, 3.5, 4) ), min( ('a', 'b', 'c') ), min($ordDoc//item//string(@color)), min($ordDoc//item/@color/string(.)))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 a  beige")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMinutesFromDateTime1() {
    final XQuery query = new XQuery(
      "(minutes-from-dateTime( xs:dateTime('2006-08-15T10:30:23')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("30")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMinutesFromDuration1() {
    final XQuery query = new XQuery(
      "(minutes-from-duration( xs:dayTimeDuration('PT30M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("30")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMinutesFromDuration2() {
    final XQuery query = new XQuery(
      "(minutes-from-duration( xs:dayTimeDuration('-PT90M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-30")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMinutesFromDuration3() {
    final XQuery query = new XQuery(
      "(minutes-from-duration( xs:dayTimeDuration('PT1M90S')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMinutesFromDuration4() {
    final XQuery query = new XQuery(
      "(minutes-from-duration( xs:dayTimeDuration('PT3H')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMinutesFromDuration5() {
    final XQuery query = new XQuery(
      "(minutes-from-duration( xs:dayTimeDuration('PT60M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMinutesFromDurationAll() {
    final XQuery query = new XQuery(
      "(minutes-from-duration( xs:dayTimeDuration('PT30M')), minutes-from-duration( xs:dayTimeDuration('-PT90M')), minutes-from-duration( xs:dayTimeDuration('PT1M90S')), minutes-from-duration( xs:dayTimeDuration('PT3H')), minutes-from-duration( xs:dayTimeDuration('PT60M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "30 -30 2 0 0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMinutesFromTime1() {
    final XQuery query = new XQuery(
      "(minutes-from-time(xs:time('10:30:23')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("30")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMonthFromDate1() {
    final XQuery query = new XQuery(
      "(month-from-date(xs:date('2006-08-15')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("8")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMonthFromDateTime1() {
    final XQuery query = new XQuery(
      "(month-from-dateTime( xs:dateTime('2006-08-15T10:30:23')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("8")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMonthsFromDuration1() {
    final XQuery query = new XQuery(
      "(months-from-duration( xs:yearMonthDuration('P3M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMonthsFromDuration2() {
    final XQuery query = new XQuery(
      "(months-from-duration( xs:yearMonthDuration('-P18M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-6")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMonthsFromDuration3() {
    final XQuery query = new XQuery(
      "(months-from-duration( xs:yearMonthDuration('P1Y')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMonthsFromDuration4() {
    final XQuery query = new XQuery(
      "(months-from-duration( xs:yearMonthDuration('P12M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnMonthsFromDurationAll() {
    final XQuery query = new XQuery(
      "(months-from-duration( xs:yearMonthDuration('P3M')), months-from-duration( xs:yearMonthDuration('-P18M')), months-from-duration( xs:yearMonthDuration('P1Y')), months-from-duration( xs:yearMonthDuration('P12M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3 -6 0 0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnName1() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre2 = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (name($in-xml))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "noNamespace")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnName2() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre2 = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (name($in-xml//pre2:prefixed))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "pre:prefixed")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnName3() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre2 = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (name($in-xml//unpre2:unprefixed))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "unprefixed")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnName4() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre2 = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (name($in-xml//@pre2:prefAttr))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "pre:prefAttr")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnName5() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre2 = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (name($in-xml//@noNSAttr))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "noNSAttr")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNameAll() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre2 = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (name($in-xml), name($in-xml//pre2:prefixed), name($in-xml//unpre2:unprefixed), name($in-xml//@pre2:prefAttr), name($in-xml//@noNSAttr))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "noNamespace pre:prefixed unprefixed pre:prefAttr noNSAttr")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNamespaceUri1() {
    final XQuery query = new XQuery(
      "declare namespace pre = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (namespace-uri($in-xml))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNamespaceUri2() {
    final XQuery query = new XQuery(
      "declare namespace pre = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (namespace-uri($in-xml//pre:prefixed))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://datypic.com/pre")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNamespaceUri3() {
    final XQuery query = new XQuery(
      "declare namespace pre = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (namespace-uri($in-xml//unpre:unprefixed))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://datypic.com/unpre")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNamespaceUri4() {
    final XQuery query = new XQuery(
      "declare namespace pre = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (namespace-uri($in-xml//@pre:prefAttr))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://datypic.com/pre")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNamespaceUri5() {
    final XQuery query = new XQuery(
      "declare namespace pre = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (namespace-uri($in-xml//@noNSAttr))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNamespaceUriAll() {
    final XQuery query = new XQuery(
      "declare namespace pre = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (namespace-uri($in-xml), namespace-uri($in-xml//pre:prefixed), namespace-uri($in-xml//unpre:unprefixed), namespace-uri($in-xml//@pre:prefAttr), namespace-uri($in-xml//@noNSAttr))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, " http://datypic.com/pre http://datypic.com/unpre http://datypic.com/pre ")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNamespaceUriForPrefix1() {
    final XQuery query = new XQuery(
      "declare namespace pre = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (namespace-uri-for-prefix( '', $in-xml))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNamespaceUriForPrefix2() {
    final XQuery query = new XQuery(
      "declare namespace pre = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (namespace-uri-for-prefix( 'pre',$in-xml//pre:prefixed))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://datypic.com/pre")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNamespaceUriForPrefix3() {
    final XQuery query = new XQuery(
      "declare namespace pre = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (namespace-uri-for-prefix( '',$in-xml//unpre:unprefixed))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://datypic.com/unpre")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNamespaceUriForPrefix4() {
    final XQuery query = new XQuery(
      "declare namespace pre = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (namespace-uri-for-prefix( 'pre',$in-xml//unpre:unprefixed))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://datypic.com/pre")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNamespaceUriForPrefixAll() {
    final XQuery query = new XQuery(
      "declare namespace pre = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := \n" +
      "         <noNamespace> \n" +
      "            <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> \n" +
      "                <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> \n" +
      "            </pre:prefixed> \n" +
      "        </noNamespace> \n" +
      "        return (namespace-uri-for-prefix('', $in-xml),\n" +
      "                namespace-uri-for-prefix('pre',$in-xml//pre:prefixed),\n" +
      "                namespace-uri-for-prefix('',$in-xml//unpre:unprefixed),\n" +
      "                namespace-uri-for-prefix('pre',$in-xml//unpre:unprefixed))\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://datypic.com/pre http://datypic.com/unpre http://datypic.com/pre")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNamespaceUriFromQName1() {
    final XQuery query = new XQuery(
      "(namespace-uri-from-QName( QName ('http://datypic.com/pre', 'prefixed')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://datypic.com/pre")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNamespaceUriFromQName2() {
    final XQuery query = new XQuery(
      "(namespace-uri-from-QName( QName ('', 'unprefixed')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNamespaceUriFromQName3() {
    final XQuery query = new XQuery(
      "(namespace-uri-from-QName( () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNamespaceUriFromQNameAll() {
    final XQuery query = new XQuery(
      "(namespace-uri-from-QName( QName ('http://datypic.com/pre', 'prefixed')), namespace-uri-from-QName( QName ('', 'unprefixed')), namespace-uri-from-QName( () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://datypic.com/pre ")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNilled1() {
    final XQuery query = new XQuery(
      "let $in-xml := <root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> <child>12</child> <child xsi:nil=\"true\"></child> <child></child> <child/> <child xsi:nil=\"false\"></child> </root> return (nilled($in-xml//child[1]))",
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
.
   */
  @org.junit.Test
  public void functxFnNilled2() {
    final XQuery query = new XQuery(
      "let $in-xml := <root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> <child>12</child> <child xsi:nil=\"true\"></child> <child></child> <child/> <child xsi:nil=\"false\"></child> </root> return (nilled($in-xml//child[3]))",
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
.
   */
  @org.junit.Test
  public void functxFnNilled3() {
    final XQuery query = new XQuery(
      "let $in-xml := <root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> <child>12</child> <child xsi:nil=\"true\"></child> <child></child> <child/> <child xsi:nil=\"false\"></child> </root> return (nilled($in-xml//child[4]))",
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
.
   */
  @org.junit.Test
  public void functxFnNilled4() {
    final XQuery query = new XQuery(
      "let $in-xml := <root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> <child>12</child> <child xsi:nil=\"true\"></child> <child></child> <child/> <child xsi:nil=\"false\"></child> </root> return (nilled($in-xml//child[5]))",
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
.
   */
  @org.junit.Test
  public void functxFnNilledAll() {
    final XQuery query = new XQuery(
      "let $in-xml := <root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> <child>12</child> <child xsi:nil=\"true\"></child> <child></child> <child/> <child xsi:nil=\"false\"></child> </root> return (nilled($in-xml//child[1]), nilled($in-xml//child[3]), nilled($in-xml//child[4]), nilled($in-xml//child[5]))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "false false false false")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNodeName1() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre2 = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (node-name($in-xml))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "noNamespace")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNodeName2() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre2 = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (node-name($in-xml/pre2:prefixed))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "pre:prefixed")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNodeName3() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre2 = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (node-name($in-xml//unpre2:unprefixed))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "unprefixed")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNodeName4() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre2 = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (node-name($in-xml//@pre2:prefAttr))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "pre:prefAttr")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNodeName5() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre2 = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (node-name($in-xml//@noNSAttr))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "noNSAttr")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNodeNameAll() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre2 = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (node-name($in-xml), node-name($in-xml/pre2:prefixed), node-name($in-xml//unpre2:unprefixed), node-name($in-xml//@pre2:prefAttr), node-name($in-xml//@noNSAttr))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "noNamespace pre:prefixed unprefixed pre:prefAttr noNSAttr")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNormalizeSpace1() {
    final XQuery query = new XQuery(
      "(normalize-space('query'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNormalizeSpace2() {
    final XQuery query = new XQuery(
      "(normalize-space(' query '))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNormalizeSpace3() {
    final XQuery query = new XQuery(
      "(normalize-space('xml query'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xml query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNormalizeSpace4() {
    final XQuery query = new XQuery(
      "(normalize-space('xml query'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xml query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNormalizeSpace5() {
    final XQuery query = new XQuery(
      "(normalize-space('xml query'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xml query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNormalizeSpace6() {
    final XQuery query = new XQuery(
      "(normalize-space(''))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNormalizeSpace7() {
    final XQuery query = new XQuery(
      "(normalize-space(' '))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNormalizeSpace8() {
    final XQuery query = new XQuery(
      "(normalize-space(()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNormalizeSpace9() {
    final XQuery query = new XQuery(
      "(normalize-space( <element> query </element>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNormalizeSpaceAll() {
    final XQuery query = new XQuery(
      "(normalize-space('query'), normalize-space(' query '), normalize-space('xml query'), normalize-space('xml query'), normalize-space('xml query'), normalize-space(''), normalize-space(' '), normalize-space(()), normalize-space( <element> query </element>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "query query xml query xml query xml query    query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNormalizeUnicode1() {
    final XQuery query = new XQuery(
      "(normalize-unicode('query'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNormalizeUnicode2() {
    final XQuery query = new XQuery(
      "(normalize-unicode('query', ''))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNormalizeUnicodeAll() {
    final XQuery query = new XQuery(
      "(normalize-unicode('query'), normalize-unicode('query', ''))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "query query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNot1() {
    final XQuery query = new XQuery(
      "(not(32 >\n" +
      "         20))",
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
.
   */
  @org.junit.Test
  public void functxFnNot2() {
    final XQuery query = new XQuery(
      "(not((/) //product))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_catalog.xml")));
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
.
   */
  @org.junit.Test
  public void functxFnNot3() {
    final XQuery query = new XQuery(
      "(not(true()))",
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
.
   */
  @org.junit.Test
  public void functxFnNot4() {
    final XQuery query = new XQuery(
      "(not(()))",
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
.
   */
  @org.junit.Test
  public void functxFnNot5() {
    final XQuery query = new XQuery(
      "(not(''))",
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
.
   */
  @org.junit.Test
  public void functxFnNot6() {
    final XQuery query = new XQuery(
      "(not(0))",
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
.
   */
  @org.junit.Test
  public void functxFnNot7() {
    final XQuery query = new XQuery(
      "(not(<e>false</e>))",
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
.
   */
  @org.junit.Test
  public void functxFnNotAll() {
    final XQuery query = new XQuery(
      "(not(32 > 20), not((/) //product), not(true()), not(()), not(''), not(0), not(<e>false</e>))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_catalog.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "false false false true true true false")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNumber1() {
    final XQuery query = new XQuery(
      "let $priceDoc := (/) return (number( $priceDoc//prod[1]/price))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_prices.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "29.99")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNumber2() {
    final XQuery query = new XQuery(
      "let $priceDoc := (/) return (number( $priceDoc//prod[1]/@currency))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_prices.xml")));
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
.
   */
  @org.junit.Test
  public void functxFnNumber3() {
    final XQuery query = new XQuery(
      "let $priceDoc := (/) return (number('29.99'))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_prices.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "29.99")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNumber4() {
    final XQuery query = new XQuery(
      "let $priceDoc := (/) return (number('ABC'))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_prices.xml")));
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
.
   */
  @org.junit.Test
  public void functxFnNumber5() {
    final XQuery query = new XQuery(
      "let $priceDoc := (/) return (number( () ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_prices.xml")));
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
.
   */
  @org.junit.Test
  public void functxFnNumber6() {
    final XQuery query = new XQuery(
      "let $priceDoc := (/) return ($priceDoc// prod/price[number() >\n" +
      "         35])",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_prices.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<price currency=\"USD\">69.99</price><price currency=\"USD\">39.99</price>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnNumberAll() {
    final XQuery query = new XQuery(
      "let $priceDoc := (/) return (number( $priceDoc//prod[1]/price), number( $priceDoc//prod[1]/@currency), number('29.99'), number('ABC'), number( () ), $priceDoc// prod/price[number() >\n" +
      "         35])",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_prices.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("29.99 NaN 29.99 NaN NaN<price currency=\"USD\">69.99</price><price currency=\"USD\">39.99</price>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnOneOrMore1() {
    final XQuery query = new XQuery(
      "(one-or-more('a'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnOneOrMore2() {
    final XQuery query = new XQuery(
      "(one-or-more( ('a', 'b') ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnOneOrMoreAll() {
    final XQuery query = new XQuery(
      "(one-or-more('a'), one-or-more( ('a', 'b') ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a a b")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnPosition1() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a>1</a> <c>2</c> <a>3</a> <a>4</a> <a>5</a> </in-xml> return ($in-xml/*[position() > 2])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>3</a><a>4</a><a>5</a>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnPosition2() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a>1</a> <c>2</c> <a>3</a> <a>4</a> <a>5</a> </in-xml> return ($in-xml/a[position() > 2])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>4</a><a>5</a>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnPosition3() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a>1</a> <c>2</c> <a>3</a> <a>4</a> <a>5</a> </in-xml> return ($in-xml/a[position() = 3])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>4</a>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnPositionAll() {
    final XQuery query = new XQuery(
      "let $in-xml := <in-xml> <a>1</a> <c>2</c> <a>3</a> <a>4</a> <a>5</a> </in-xml> return ($in-xml/*[position() > 2], $in-xml/a[position() > 2], $in-xml/a[position() = 3])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>3</a><a>4</a><a>5</a><a>4</a><a>5</a><a>4</a>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnPrefixFromQName1() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (prefix-from-QName( node-name($in-xml)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnPrefixFromQName2() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (prefix-from-QName( node-name($in-xml//pre2:prefixed)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "pre")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnPrefixFromQName3() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (prefix-from-QName( node-name($in-xml//unpre:unprefixed)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnPrefixFromQName4() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (prefix-from-QName( node-name($in-xml//@pre2:prefAttr)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "pre")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnPrefixFromQName5() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (prefix-from-QName( node-name($in-xml//@noNSAttr)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnPrefixFromQName6() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (prefix-from-QName( node-name(<pre2:new>xyz</pre2:new>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "pre2")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnPrefixFromQNameAll() {
    final XQuery query = new XQuery(
      "declare namespace pre2 = \"http://datypic.com/pre\";\n" +
      "         declare namespace unpre = \"http://datypic.com/unpre\";\n" +
      "         let $in-xml := <noNamespace> <pre:prefixed xmlns=\"http://datypic.com/unpre\" xmlns:pre=\"http://datypic.com/pre\"> <unprefixed pre:prefAttr=\"a\" noNSAttr=\"b\">123</unprefixed> </pre:prefixed> </noNamespace> return (prefix-from-QName( node-name($in-xml)), prefix-from-QName( node-name($in-xml//pre2:prefixed)), prefix-from-QName( node-name($in-xml//unpre:unprefixed)), prefix-from-QName( node-name($in-xml//@pre2:prefAttr)), prefix-from-QName( node-name($in-xml//@noNSAttr)), prefix-from-QName( node-name(<pre2:new>xyz</pre2:new>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "pre pre pre2")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRemove1() {
    final XQuery query = new XQuery(
      "(remove( ('a', 'b', 'c'), 2) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRemove2() {
    final XQuery query = new XQuery(
      "(remove( ('a', 'b', 'c'), 10))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRemove3() {
    final XQuery query = new XQuery(
      "(remove( ('a', 'b', 'c'), 0))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRemoveAll() {
    final XQuery query = new XQuery(
      "(remove( ('a', 'b', 'c'), 2) , remove( ('a', 'b', 'c'), 10), remove( ('a', 'b', 'c'), 0))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a c a b c a b c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace1() {
    final XQuery query = new XQuery(
      "(replace('query', 'r', 'as'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "queasy")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace10() {
    final XQuery query = new XQuery(
      "(replace('reluctant', 'r.*?t', 'X'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Xant")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace11() {
    final XQuery query = new XQuery(
      "(replace('aaah', 'a{2,3}', 'X'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Xh")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace12() {
    final XQuery query = new XQuery(
      "(replace('aaah', 'a{2,3}?', 'X'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Xah")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace13() {
    final XQuery query = new XQuery(
      "(replace('aaaah', 'a{2,3}', 'X'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Xah")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace14() {
    final XQuery query = new XQuery(
      "(replace('aaaah', 'a{2,3}?', 'X'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "XXh")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace15() {
    final XQuery query = new XQuery(
      "(replace('Chap 2...Chap 3...Chap 4...', 'Chap (\\d)', 'Sec $1.0'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Sec 2.0...Sec 3.0...Sec 4.0...")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace16() {
    final XQuery query = new XQuery(
      "(replace('abc123', '([a-z])', '$1x'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "axbxcx123")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace17() {
    final XQuery query = new XQuery(
      "(replace('2315551212', '(\\d{3})(\\d{3})(\\d{4})', '($1) $2-$3'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "(231) 555-1212")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace18() {
    final XQuery query = new XQuery(
      "(replace('2006-10-18', '\\d{2}(\\d{2})-(\\d{2})-(\\d{2})', '$2/$3/$1'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10/18/06")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace19() {
    final XQuery query = new XQuery(
      "(replace('25', '(\\d+)', '\\$$1.00'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "$25.00")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace2() {
    final XQuery query = new XQuery(
      "(replace('query', 'qu', 'quack'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "quackery")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace3() {
    final XQuery query = new XQuery(
      "(replace('query', '[ry]', 'l'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "quell")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace4() {
    final XQuery query = new XQuery(
      "(replace('query', '[ry]+', 'l'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "quel")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace5() {
    final XQuery query = new XQuery(
      "(replace('query', 'z', 'a'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace6() {
    final XQuery query = new XQuery(
      "(replace('query', 'query', ''))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace7() {
    final XQuery query = new XQuery(
      "(replace( (), 'r', 'as'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace8() {
    final XQuery query = new XQuery(
      "(replace('Chapter', '(Chap)|(Chapter)', 'x'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xter")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplace9() {
    final XQuery query = new XQuery(
      "(replace('reluctant', 'r.*t', 'X'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "X")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReplaceAll() {
    final XQuery query = new XQuery(
      "(replace('query', 'r', 'as'), replace('query', 'qu', 'quack'), replace('query', '[ry]', 'l'), replace('query', '[ry]+', 'l'), replace('query', 'z', 'a'), replace('query', 'query', ''), replace( (), 'r', 'as'), replace('Chapter', '(Chap)|(Chapter)', 'x'), replace('reluctant', 'r.*t', 'X'), replace('reluctant', 'r.*?t', 'X'), replace('aaah', 'a{2,3}', 'X'), replace('aaah', 'a{2,3}?', 'X'), replace('aaaah', 'a{2,3}', 'X'), replace('aaaah', 'a{2,3}?', 'X'), replace('Chap 2...Chap 3...Chap 4...', 'Chap (\\d)', 'Sec $1.0'), replace('abc123', '([a-z])', '$1x'), replace('2315551212', '(\\d{3})(\\d{3})(\\d{4})', '($1) $2-$3'), replace('2006-10-18', '\\d{2}(\\d{2})-(\\d{2})-(\\d{2})', '$2/$3/$1'), replace('25', '(\\d+)', '\\$$1.00'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "queasy quackery quell quel query   xter X Xant Xh Xah Xah XXh Sec 2.0...Sec 3.0...Sec 4.0... axbxcx123 (231) 555-1212 10/18/06 $25.00")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnResolveQName1() {
    final XQuery query = new XQuery(
      "declare namespace ord = \"http://datypic.com/ord\";\n" +
      "         declare namespace dty = \"http://datypic.com\";\n" +
      "         declare namespace dty2 = \"http://datypic.com/ns2\";\n" +
      "         let $root := <root> <order xmlns:ord=\"http://datypic.com/ord\" xmlns=\"http://datypic.com\"> <!-- ... --> </order> </root> return (resolve-QName('myName', $root))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "myName")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnResolveQName2() {
    final XQuery query = new XQuery(
      "declare namespace ord = \"http://datypic.com/ord\";\n" +
      "         declare namespace dty = \"http://datypic.com\";\n" +
      "         declare namespace dty2 = \"http://datypic.com/ns2\";\n" +
      "         let $root := <root> <order xmlns:ord=\"http://datypic.com/ord\" xmlns=\"http://datypic.com\"> <!-- ... --> </order> </root> return (resolve-QName('myName', $root/dty:order))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "myName")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnResolveQName3() {
    final XQuery query = new XQuery(
      "declare namespace ord = \"http://datypic.com/ord\";\n" +
      "         declare namespace dty = \"http://datypic.com\";\n" +
      "         declare namespace dty2 = \"http://datypic.com/ns2\";\n" +
      "         let $root := <root> <order xmlns:ord=\"http://datypic.com/ord\" xmlns=\"http://datypic.com\"> <!-- ... --> </order> </root> return (resolve-QName( 'ord:myName', $root/dty:order))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ord:myName")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnResolveQNameAll() {
    final XQuery query = new XQuery(
      "declare namespace ord = \"http://datypic.com/ord\";\n" +
      "         declare namespace dty = \"http://datypic.com\";\n" +
      "         declare namespace dty2 = \"http://datypic.com/ns2\";\n" +
      "         let $root := <root> <order xmlns:ord=\"http://datypic.com/ord\" xmlns=\"http://datypic.com\"> <!-- ... --> </order> </root> return (resolve-QName('myName', $root), resolve-QName('myName', $root/dty:order), resolve-QName( 'ord:myName', $root/dty:order))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "myName myName ord:myName")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnResolveUri1() {
    final XQuery query = new XQuery(
      "(resolve-uri('prod', 'http://datypic.com/'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://datypic.com/prod")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnResolveUri2() {
    final XQuery query = new XQuery(
      "(resolve-uri('prod2', 'http://datypic.com/prod1'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://datypic.com/prod2")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnResolveUri3() {
    final XQuery query = new XQuery(
      "(resolve-uri( 'http://example.org','http://datypic.com'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.org")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnResolveUri4() {
    final XQuery query = new XQuery(
      "(resolve-uri( 'http://datypic.com', '../base'))",
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
        assertStringValue(false, "http://datypic.com")
      ||
        error("FORG0002")
      )
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnResolveUri5() {
    final XQuery query = new XQuery(
      "(resolve-uri( '', 'http://datypic.com'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://datypic.com")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnResolveUriAll() {
    final XQuery query = new XQuery(
      "(resolve-uri('prod', 'http://datypic.com/'), resolve-uri('prod2', 'http://datypic.com/prod1'), resolve-uri( 'http://example.org','http://datypic.com'), resolve-uri( 'http://datypic.com', '../base'), resolve-uri( '', 'http://datypic.com'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://datypic.com/prod http://datypic.com/prod2 http://example.org http://datypic.com http://datypic.com")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReverse1() {
    final XQuery query = new XQuery(
      "(reverse( (1, 2, 3, 4, 5) ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5 4 3 2 1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReverse2() {
    final XQuery query = new XQuery(
      "(reverse( (6, 2, 4) ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4 2 6")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReverse3() {
    final XQuery query = new XQuery(
      "(reverse( () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnReverseAll() {
    final XQuery query = new XQuery(
      "(reverse( (1, 2, 3, 4, 5) ), reverse( (6, 2, 4) ), reverse( () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5 4 3 2 1 4 2 6")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRoot1() {
    final XQuery query = new XQuery(
      "let $in-xml := <a><x>123</x></a> return (root( (/) //item[1]))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<order num=\"00299432\" date=\"2006-09-15\" cust=\"0221A\">\n  <item dept=\"WMN\" num=\"557\" quantity=\"1\" color=\"beige\"/>\n  <item dept=\"ACC\" num=\"563\" quantity=\"1\"/>\n  <item dept=\"ACC\" num=\"443\" quantity=\"2\"/>\n  <item dept=\"MEN\" num=\"784\" quantity=\"1\" color=\"blue/white\"/>\n  <item dept=\"MEN\" num=\"784\" quantity=\"1\" color=\"blue/red\"/>\n  <item dept=\"WMN\" num=\"557\" quantity=\"1\" color=\"sage\"/>\n</order>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRoot2() {
    final XQuery query = new XQuery(
      "let $in-xml := <a><x>123</x></a> return (root($in-xml/x))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a><x>123</x></a>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRootAll() {
    final XQuery query = new XQuery(
      "let $in-xml := <a><x>123</x></a> return (root( (/) //item[1]), root($in-xml/x))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<order num=\"00299432\" date=\"2006-09-15\" cust=\"0221A\">\n  <item dept=\"WMN\" num=\"557\" quantity=\"1\" color=\"beige\"/>\n  <item dept=\"ACC\" num=\"563\" quantity=\"1\"/>\n  <item dept=\"ACC\" num=\"443\" quantity=\"2\"/>\n  <item dept=\"MEN\" num=\"784\" quantity=\"1\" color=\"blue/white\"/>\n  <item dept=\"MEN\" num=\"784\" quantity=\"1\" color=\"blue/red\"/>\n  <item dept=\"WMN\" num=\"557\" quantity=\"1\" color=\"sage\"/>\n</order><a><x>123</x></a>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRound1() {
    final XQuery query = new XQuery(
      "(round(5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRound2() {
    final XQuery query = new XQuery(
      "(round(5.1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRound3() {
    final XQuery query = new XQuery(
      "(round(5.5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("6")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRound4() {
    final XQuery query = new XQuery(
      "(round(-5.5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRound5() {
    final XQuery query = new XQuery(
      "(round(-5.51))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-6")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRoundAll() {
    final XQuery query = new XQuery(
      "(round(5), round(5.1), round(5.5), round(-5.5), round(-5.51))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5 5 6 -5 -6")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRoundHalfToEven1() {
    final XQuery query = new XQuery(
      "(round-half-to-even(5.5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("6")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRoundHalfToEven2() {
    final XQuery query = new XQuery(
      "(round-half-to-even(6.5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("6")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRoundHalfToEven3() {
    final XQuery query = new XQuery(
      "(round-half-to-even(9372.253, 2))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9372.25")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRoundHalfToEven4() {
    final XQuery query = new XQuery(
      "(round-half-to-even(9372.253, 0))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("9372")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRoundHalfToEven5() {
    final XQuery query = new XQuery(
      "(round-half-to-even(9372.253, -3))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("9000")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnRoundHalfToEvenAll() {
    final XQuery query = new XQuery(
      "(round-half-to-even(5.5), round-half-to-even(6.5), round-half-to-even(9372.253, 2), round-half-to-even(9372.253, 0), round-half-to-even(9372.253, -3))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "6 6 9372.25 9372 9000")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSecondsFromDateTime1() {
    final XQuery query = new XQuery(
      "(seconds-from-dateTime( xs:dateTime('2006-08-15T10:30:23.5')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "23.5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSecondsFromDuration1() {
    final XQuery query = new XQuery(
      "(seconds-from-duration( xs:dayTimeDuration('PT30.5S')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "30.5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSecondsFromDuration2() {
    final XQuery query = new XQuery(
      "(seconds-from-duration( xs:dayTimeDuration('-PT90.5S')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-30.5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSecondsFromDuration3() {
    final XQuery query = new XQuery(
      "(seconds-from-duration( xs:dayTimeDuration('PT1M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSecondsFromDuration4() {
    final XQuery query = new XQuery(
      "(seconds-from-duration( xs:dayTimeDuration('PT60S')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSecondsFromDurationAll() {
    final XQuery query = new XQuery(
      "(seconds-from-duration( xs:dayTimeDuration('PT30.5S')), seconds-from-duration( xs:dayTimeDuration('-PT90.5S')), seconds-from-duration( xs:dayTimeDuration('PT1M')), seconds-from-duration( xs:dayTimeDuration('PT60S')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "30.5 -30.5 0 0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSecondsFromTime1() {
    final XQuery query = new XQuery(
      "(seconds-from-time(xs:time('10:30:23.5')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "23.5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStartsWith1() {
    final XQuery query = new XQuery(
      "(starts-with('query', 'que'))",
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
.
   */
  @org.junit.Test
  public void functxFnStartsWith2() {
    final XQuery query = new XQuery(
      "(starts-with('query', 'query'))",
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
.
   */
  @org.junit.Test
  public void functxFnStartsWith3() {
    final XQuery query = new XQuery(
      "(starts-with('query', 'u'))",
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
.
   */
  @org.junit.Test
  public void functxFnStartsWith4() {
    final XQuery query = new XQuery(
      "(starts-with('query', ''))",
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
.
   */
  @org.junit.Test
  public void functxFnStartsWith5() {
    final XQuery query = new XQuery(
      "(starts-with('', 'query'))",
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
.
   */
  @org.junit.Test
  public void functxFnStartsWith6() {
    final XQuery query = new XQuery(
      "(starts-with('', ''))",
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
.
   */
  @org.junit.Test
  public void functxFnStartsWith7() {
    final XQuery query = new XQuery(
      "(starts-with('query', ()))",
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
.
   */
  @org.junit.Test
  public void functxFnStartsWith8() {
    final XQuery query = new XQuery(
      "(starts-with(' query', 'q'))",
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
.
   */
  @org.junit.Test
  public void functxFnStartsWithAll() {
    final XQuery query = new XQuery(
      "(starts-with('query', 'que'), starts-with('query', 'query'), starts-with('query', 'u'), starts-with('query', ''), starts-with('', 'query'), starts-with('', ''), starts-with('query', ()), starts-with(' query', 'q'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true false true false true true false")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnString1() {
    final XQuery query = new XQuery(
      "let $in-xml-2 := <product dept=\"MEN\"> <number>784</number> <name language=\"en\">Cotton Dress Shirt</name> <colorChoices>white gray</colorChoices> <desc>Our <i>favorite</i> shirt!</desc> </product> return (string($in-xml-2/number))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "784")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnString2() {
    final XQuery query = new XQuery(
      "let $in-xml-2 := <product dept=\"MEN\"> <number>784</number> <name language=\"en\">Cotton Dress Shirt</name> <colorChoices>white gray</colorChoices> <desc>Our <i>favorite</i> shirt!</desc> </product> return (string($in-xml-2/desc))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Our favorite shirt!")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnString3() {
    final XQuery query = new XQuery(
      "let $in-xml-2 := <product dept=\"MEN\"> <number>784</number> <name language=\"en\">Cotton Dress Shirt</name> <colorChoices>white gray</colorChoices> <desc>Our <i>favorite</i> shirt!</desc> </product> return (string($in-xml-2/@dept))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "MEN")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringAll() {
    final XQuery query = new XQuery(
      "let $in-xml-2 := <product dept=\"MEN\"> <number>784</number> <name language=\"en\">Cotton Dress Shirt</name> <colorChoices>white gray</colorChoices> <desc>Our <i>favorite</i> shirt!</desc> </product> return (string($in-xml-2/number), string($in-xml-2/desc), string($in-xml-2/@dept))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "784 Our favorite shirt! MEN")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringJoin1() {
    final XQuery query = new XQuery(
      "(string-join( ('a', 'b', 'c'), ''))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringJoin2() {
    final XQuery query = new XQuery(
      "(string-join( ('a', 'b', 'c'), '/*'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a/*b/*c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringJoin3() {
    final XQuery query = new XQuery(
      "(string-join( ('a', '', 'c'), '/*'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a/*/*c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringJoin4() {
    final XQuery query = new XQuery(
      "(string-join( 'a', '/*'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringJoin5() {
    final XQuery query = new XQuery(
      "(string-join((), '/*'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringJoinAll() {
    final XQuery query = new XQuery(
      "(string-join( ('a', 'b', 'c'), ''), string-join( ('a', 'b', 'c'), '/*'), string-join( ('a', '', 'c'), '/*'), string-join( 'a', '/*'), string-join((), '/*'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc a/*b/*c a/*/*c a ")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringLength1() {
    final XQuery query = new XQuery(
      "(string-length('query'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringLength2() {
    final XQuery query = new XQuery(
      "(string-length(' \n" +
      "query\n" +
      " '))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("9")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringLength3() {
    final XQuery query = new XQuery(
      "(string-length(normalize-space(' query ')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringLength4() {
    final XQuery query = new XQuery(
      "(string-length('xml query'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("9")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringLength5() {
    final XQuery query = new XQuery(
      "(string-length(''))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringLength6() {
    final XQuery query = new XQuery(
      "(string-length(()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringLengthAll() {
    final XQuery query = new XQuery(
      "(string-length('query'), string-length(' \n" +
      "query \n" +
      " '), string-length(normalize-space('  \n" +
      "query \n" +
      " ')), string-length('xml query'), string-length(''), string-length(()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5 10 5 9 0 0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringToCodepoints1() {
    final XQuery query = new XQuery(
      "(string-to-codepoints('abc'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "97 98 99")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringToCodepoints2() {
    final XQuery query = new XQuery(
      "(string-to-codepoints('a'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("97")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringToCodepoints3() {
    final XQuery query = new XQuery(
      "(string-to-codepoints(''))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnStringToCodepointsAll() {
    final XQuery query = new XQuery(
      "(string-to-codepoints('abc'), string-to-codepoints('a'), string-to-codepoints(''))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "97 98 99 97")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubsequence1() {
    final XQuery query = new XQuery(
      "(subsequence( ('a', 'b', 'c', 'd', 'e'), 3) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "c d e")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubsequence2() {
    final XQuery query = new XQuery(
      "(subsequence( ('a', 'b', 'c', 'd', 'e'), 3, 2))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "c d")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubsequence3() {
    final XQuery query = new XQuery(
      "(subsequence( ('a', 'b', 'c', 'd', 'e'), 3, 10))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "c d e")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubsequence4() {
    final XQuery query = new XQuery(
      "(subsequence( ('a', 'b', 'c', 'd', 'e'), 10))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubsequence5() {
    final XQuery query = new XQuery(
      "(subsequence( ('a', 'b', 'c', 'd', 'e'), -2, 5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubsequence6() {
    final XQuery query = new XQuery(
      "(subsequence( (), 3))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubsequenceAll() {
    final XQuery query = new XQuery(
      "(subsequence( ('a', 'b', 'c', 'd', 'e'), 3) , subsequence( ('a', 'b', 'c', 'd', 'e'), 3, 2), subsequence( ('a', 'b', 'c', 'd', 'e'), 3, 10), subsequence( ('a', 'b', 'c', 'd', 'e'), 10), subsequence( ('a', 'b', 'c', 'd', 'e'), -2, 5), subsequence( (), 3))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "c d e c d c d e a b")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstring1() {
    final XQuery query = new XQuery(
      "(substring('query', 1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstring10() {
    final XQuery query = new XQuery(
      "(substring('', 1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstring11() {
    final XQuery query = new XQuery(
      "(substring((), 1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstring2() {
    final XQuery query = new XQuery(
      "(substring('query', 3))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ery")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstring3() {
    final XQuery query = new XQuery(
      "(substring('query', 1, 1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "q")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstring4() {
    final XQuery query = new XQuery(
      "(substring('query', 2, 3))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "uer")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstring5() {
    final XQuery query = new XQuery(
      "(substring('query', 2, 850))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "uery")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstring6() {
    final XQuery query = new XQuery(
      "(substring('query', 6, 2))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstring7() {
    final XQuery query = new XQuery(
      "(substring('query', -2))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstring8() {
    final XQuery query = new XQuery(
      "(substring('query', -2, 5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "qu")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstring9() {
    final XQuery query = new XQuery(
      "(substring('query', 1, 0))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstringAfter1() {
    final XQuery query = new XQuery(
      "(substring-after('query', 'u'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ery")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstringAfter2() {
    final XQuery query = new XQuery(
      "(substring-after('queryquery', 'ue'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ryquery")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstringAfter3() {
    final XQuery query = new XQuery(
      "(substring-after('query', 'y'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstringAfter4() {
    final XQuery query = new XQuery(
      "(substring-after('query', 'x'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstringAfter5() {
    final XQuery query = new XQuery(
      "(substring-after('query', ''))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstringAfter6() {
    final XQuery query = new XQuery(
      "(substring-after('', 'x'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstringAfterAll() {
    final XQuery query = new XQuery(
      "(substring-after('query', 'u'), substring-after('queryquery', 'ue'), substring-after('query', 'y'), substring-after('query', 'x'), substring-after('query', ''), substring-after('', 'x'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ery ryquery   query ")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstringAll() {
    final XQuery query = new XQuery(
      "(substring('query', 1), substring('query', 3), substring('query', 1, 1), substring('query', 2, 3), substring('query', 2, 850), substring('query', 6, 2), substring('query', -2), substring('query', -2, 5), substring('query', 1, 0), substring('', 1), substring((), 1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "query ery q uer uery  query qu   ")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstringBefore1() {
    final XQuery query = new XQuery(
      "(substring-before('query', 'r'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "que")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstringBefore2() {
    final XQuery query = new XQuery(
      "(substring-before('query', 'ery'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "qu")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstringBefore3() {
    final XQuery query = new XQuery(
      "(substring-before('queryquery', 'ery'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "qu")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstringBefore4() {
    final XQuery query = new XQuery(
      "(substring-before('query', 'query'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstringBefore5() {
    final XQuery query = new XQuery(
      "(substring-before('query', 'x'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstringBefore6() {
    final XQuery query = new XQuery(
      "(substring-before('query', ''))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstringBefore7() {
    final XQuery query = new XQuery(
      "(substring-before('query', ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSubstringBeforeAll() {
    final XQuery query = new XQuery(
      "(substring-before('query', 'r'), substring-before('query', 'ery'), substring-before('queryquery', 'ery'), substring-before('query', 'query'), substring-before('query', 'x'), substring-before('query', ''), substring-before('query', ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "que qu qu    ")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSum1() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (sum( (1, 2, 3) ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("6")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSum2() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (sum($ordDoc//item/@quantity))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("7")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSum3() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (sum( (xs:yearMonthDuration('P1Y2M'), xs:yearMonthDuration('P2Y3M')) ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P3Y5M")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSum4() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (sum( (1, 2, 3, () ) ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("6")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSum5() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (sum( () ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSum6() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (sum( (), () ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnSumAll() {
    final XQuery query = new XQuery(
      "let $ordDoc := (/) return (sum( (1, 2, 3) ), sum($ordDoc//item/@quantity), sum( (xs:yearMonthDuration('P1Y2M'), xs:yearMonthDuration('P2Y3M')) ), sum( (1, 2, 3, () ) ), sum( () ), sum( (), () ))",
      ctx);
    try {
      query.context(node(file("app/FunctxFn/functx_order.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "6 7 P3Y5M 6 0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTimezoneFromDate1() {
    final XQuery query = new XQuery(
      "(timezone-from-date( xs:date('2006-08-15-05:00')))",
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
.
   */
  @org.junit.Test
  public void functxFnTimezoneFromDate2() {
    final XQuery query = new XQuery(
      "(timezone-from-date( xs:date('2006-08-15')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTimezoneFromDateAll() {
    final XQuery query = new XQuery(
      "(timezone-from-date( xs:date('2006-08-15-05:00')), timezone-from-date( xs:date('2006-08-15')))",
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
.
   */
  @org.junit.Test
  public void functxFnTimezoneFromDateTime1() {
    final XQuery query = new XQuery(
      "(timezone-from-dateTime( xs:dateTime('2006-08-15T10:30:23-05:00')))",
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
.
   */
  @org.junit.Test
  public void functxFnTimezoneFromDateTime2() {
    final XQuery query = new XQuery(
      "(timezone-from-dateTime( xs:dateTime('2006-08-15T10:30:23')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTimezoneFromDateTimeAll() {
    final XQuery query = new XQuery(
      "(timezone-from-dateTime( xs:dateTime('2006-08-15T10:30:23-05:00')), timezone-from-dateTime( xs:dateTime('2006-08-15T10:30:23')))",
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
.
   */
  @org.junit.Test
  public void functxFnTimezoneFromTime1() {
    final XQuery query = new XQuery(
      "(timezone-from-time( xs:time('09:54:00-05:00')))",
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
.
   */
  @org.junit.Test
  public void functxFnTimezoneFromTime2() {
    final XQuery query = new XQuery(
      "(timezone-from-time( xs:time('09:54:00+05:00')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT5H")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTimezoneFromTime3() {
    final XQuery query = new XQuery(
      "(timezone-from-time( xs:time('09:54:00Z')))",
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
.
   */
  @org.junit.Test
  public void functxFnTimezoneFromTime4() {
    final XQuery query = new XQuery(
      "(timezone-from-time( xs:time('09:54:00')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTimezoneFromTimeAll() {
    final XQuery query = new XQuery(
      "(timezone-from-time( xs:time('09:54:00-05:00')), timezone-from-time( xs:time('09:54:00+05:00')), timezone-from-time( xs:time('09:54:00Z')), timezone-from-time( xs:time('09:54:00')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-PT5H PT5H PT0S")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTokenize1() {
    final XQuery query = new XQuery(
      "(tokenize( 'a b c', '\\s'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTokenize10() {
    final XQuery query = new XQuery(
      "(tokenize( (), '\\s+'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTokenize11() {
    final XQuery query = new XQuery(
      "(tokenize( 'abc', '\\s'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTokenize12() {
    final XQuery query = new XQuery(
      "(tokenize( 'a,xb,xc', ',|,x'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a xb xc")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTokenize2() {
    final XQuery query = new XQuery(
      "string-join(tokenize( 'a    b c', '\\s'), '|')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a||||b|c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTokenize3() {
    final XQuery query = new XQuery(
      "(tokenize( 'a b c', '\\s+'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTokenize4() {
    final XQuery query = new XQuery(
      "(tokenize( ' b c', '\\s'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, " b c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTokenize5() {
    final XQuery query = new XQuery(
      "(tokenize( 'a,b,c', ','))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTokenize6() {
    final XQuery query = new XQuery(
      "(tokenize( 'a,b,,c', ','))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b  c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTokenize7() {
    final XQuery query = new XQuery(
      "(tokenize( 'a, b, c', '[,\\s]+'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b c")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTokenize8() {
    final XQuery query = new XQuery(
      "(tokenize( '2006-12-25T12:15:00', '[\\-T:]'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2006 12 25 12 15 00")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTokenize9() {
    final XQuery query = new XQuery(
      "(tokenize( 'Hello, there.', '\\W+'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Hello there ")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTokenizeAll() {
    final XQuery query = new XQuery(
      "(tokenize( 'a b c', '\\s'), tokenize( 'a b c', '\\s'), tokenize( 'a b c', '\\s+'), tokenize( ' b c', '\\s'), tokenize( 'a,b,c', ','), tokenize( 'a,b,,c', ','), tokenize( 'a, b, c', '[,\\s]+'), tokenize( '2006-12-25T12:15:00', '[\\-T:]'), tokenize( 'Hello, there.', '\\W+'), tokenize( (), '\\s+'), tokenize( 'abc', '\\s'), tokenize( 'a,xb,xc', ',|,x'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b c a b c a b c  b c a b c a b  c a b c 2006 12 25 12 15 00 Hello there  abc a xb xc")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTranslate1() {
    final XQuery query = new XQuery(
      "(translate('1999/01/02', '/', '-'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1999-01-02")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTranslate2() {
    final XQuery query = new XQuery(
      "(translate('xml query', 'qlmx', 'QLMX'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "XML Query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTranslate3() {
    final XQuery query = new XQuery(
      "(translate('xml query', 'qlmx ', 'Q'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTranslate4() {
    final XQuery query = new XQuery(
      "(translate('xml query', 'qlmx ', ''))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "uery")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTranslate5() {
    final XQuery query = new XQuery(
      "(translate('xml query', 'abcd', 'ABCD'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xml query")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTranslate6() {
    final XQuery query = new XQuery(
      "(translate('', 'qlmx ', 'Q'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTranslate7() {
    final XQuery query = new XQuery(
      "(translate((), 'qlmx ', 'Q'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTranslateAll() {
    final XQuery query = new XQuery(
      "(translate('1999/01/02', '/', '-'), translate('xml query', 'qlmx', 'QLMX'), translate('xml query', 'qlmx ', 'Q'), translate('xml query', 'qlmx ', ''), translate('xml query', 'abcd', 'ABCD'), translate('', 'qlmx ', 'Q'), translate((), 'qlmx ', 'Q'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1999-01-02 XML Query Query uery xml query  ")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnTrue1() {
    final XQuery query = new XQuery(
      "(true())",
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
.
   */
  @org.junit.Test
  public void functxFnUpperCase1() {
    final XQuery query = new XQuery(
      "(upper-case('query'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "QUERY")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnUpperCase2() {
    final XQuery query = new XQuery(
      "(upper-case('QUERY'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "QUERY")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnUpperCase3() {
    final XQuery query = new XQuery(
      "(upper-case('Query'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "QUERY")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnUpperCase4() {
    final XQuery query = new XQuery(
      "(upper-case('query-123'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "QUERY-123")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnUpperCaseAll() {
    final XQuery query = new XQuery(
      "(upper-case('query'), upper-case('QUERY'), upper-case('Query'), upper-case('query-123'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "QUERY QUERY QUERY QUERY-123")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnYearFromDate1() {
    final XQuery query = new XQuery(
      "(year-from-date(xs:date('2006-08-15')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2006")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnYearFromDateTime1() {
    final XQuery query = new XQuery(
      "(year-from-dateTime( xs:dateTime('2006-08-15T10:30:23')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2006")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnYearsFromDuration1() {
    final XQuery query = new XQuery(
      "(years-from-duration( xs:yearMonthDuration('P3Y')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnYearsFromDuration2() {
    final XQuery query = new XQuery(
      "(years-from-duration( xs:yearMonthDuration('P3Y11M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnYearsFromDuration3() {
    final XQuery query = new XQuery(
      "(years-from-duration( xs:yearMonthDuration('-P18M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnYearsFromDuration4() {
    final XQuery query = new XQuery(
      "(years-from-duration( xs:yearMonthDuration('P1Y18M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnYearsFromDuration5() {
    final XQuery query = new XQuery(
      "(years-from-duration( xs:yearMonthDuration('P12M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnYearsFromDurationAll() {
    final XQuery query = new XQuery(
      "(years-from-duration( xs:yearMonthDuration('P3Y')), years-from-duration( xs:yearMonthDuration('P3Y11M')), years-from-duration( xs:yearMonthDuration('-P18M')), years-from-duration( xs:yearMonthDuration('P1Y18M')), years-from-duration( xs:yearMonthDuration('P12M')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3 3 -1 2 1")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnZeroOrOne1() {
    final XQuery query = new XQuery(
      "(zero-or-one( () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnZeroOrOne2() {
    final XQuery query = new XQuery(
      "(zero-or-one('a'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void functxFnZeroOrOneAll() {
    final XQuery query = new XQuery(
      "(zero-or-one( () ), zero-or-one('a'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a")
    );
  }
}
