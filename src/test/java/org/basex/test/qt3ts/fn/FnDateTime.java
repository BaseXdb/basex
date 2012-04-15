package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnDateTime extends QT3TestSet {

  /**
   * Passing too few parameters(none) to fn:dateTime()..
   */
  @org.junit.Test
  public void kDateTimeFunc1() {
    final XQuery query = new XQuery(
      "dateTime()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Invoke fn:dateTime() with the first value having no timezone..
   */
  @org.junit.Test
  public void kDateTimeFunc10() {
    final XQuery query = new XQuery(
      "dateTime(xs:date(\"2004-03-04\"), xs:time(\"08:05:23-05:00\")) eq xs:dateTime(\"2004-03-04T08:05:23-05:00\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Invoke fn:dateTime() with the second value having no timezone..
   */
  @org.junit.Test
  public void kDateTimeFunc11() {
    final XQuery query = new XQuery(
      "dateTime(xs:date(\"2004-03-04+11:00\"), xs:time(\"08:05:23\")) eq xs:dateTime(\"2004-03-04T08:05:23+11:00\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Invoke fn:dateTime() with the time value being 24:00:00. .
   */
  @org.junit.Test
  public void kDateTimeFunc12() {
    final XQuery query = new XQuery(
      "xs:string(dateTime(xs:date(\"1999-12-31\"), xs:time(\"24:00:00\"))) eq \"1999-12-31T00:00:00\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoke fn:dateTime() with timezones being canonical UTC('Z')..
   */
  @org.junit.Test
  public void kDateTimeFunc13() {
    final XQuery query = new XQuery(
      "dateTime(xs:date(\"2004-03-04Z\"), xs:time(\"08:05:23Z\")) eq xs:dateTime(\"2004-03-04T08:05:23Z\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Invoke fn:dateTime() with the first value's timezone being canonical UTC('Z'). .
   */
  @org.junit.Test
  public void kDateTimeFunc14() {
    final XQuery query = new XQuery(
      "dateTime(xs:date(\"2004-03-04Z\"), xs:time(\"08:05:23\")) eq xs:dateTime(\"2004-03-04T08:05:23Z\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Invoke fn:dateTime() with the first value's timezone being an arbitrary value..
   */
  @org.junit.Test
  public void kDateTimeFunc15() {
    final XQuery query = new XQuery(
      "dateTime(xs:date(\"2004-03-04+13:07\"), xs:time(\"08:05:23\")) eq xs:dateTime(\"2004-03-04T08:05:23+13:07\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Invoke fn:dateTime() with the second value's timezone being an arbitrary value..
   */
  @org.junit.Test
  public void kDateTimeFunc16() {
    final XQuery query = new XQuery(
      "dateTime(xs:date(\"2004-03-04\"), xs:time(\"08:05:23+13:07\")) eq xs:dateTime(\"2004-03-04T08:05:23+13:07\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Invoke fn:dateTime() where none of its values has a timezone. .
   */
  @org.junit.Test
  public void kDateTimeFunc17() {
    final XQuery query = new XQuery(
      "dateTime(xs:date(\"2004-03-04\"), xs:time(\"08:05:23\")) eq xs:dateTime(\"2004-03-04T08:05:23\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Invoke fn:dateTime() with the first value's timezone being canonical UTC('Z'). .
   */
  @org.junit.Test
  public void kDateTimeFunc18() {
    final XQuery query = new XQuery(
      "dateTime(xs:date(\"2004-03-04\"), xs:time(\"08:05:23Z\")) eq xs:dateTime(\"2004-03-04T08:05:23Z\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Passing too few parameters(only one) to fn:dateTime()..
   */
  @org.junit.Test
  public void kDateTimeFunc2() {
    final XQuery query = new XQuery(
      "dateTime(xs:date(\"2004-03-04\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Passing too many parameters to fn:dateTime()..
   */
  @org.junit.Test
  public void kDateTimeFunc3() {
    final XQuery query = new XQuery(
      "dateTime(xs:date(\"2004-03-04\"), xs:time(\"08:05:23\"), ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Passing the empty sequence as second argument is allowed(recent change in the specification)..
   */
  @org.junit.Test
  public void kDateTimeFunc4() {
    final XQuery query = new XQuery(
      "empty(dateTime(xs:date(\"2004-03-04\"), ()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Passing the empty sequence as first argument is allowed(recent change in the specification). .
   */
  @org.junit.Test
  public void kDateTimeFunc5() {
    final XQuery query = new XQuery(
      "empty(dateTime((), xs:time(\"08:05:23\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Passing different timezones to fn:dateTime() is an error. .
   */
  @org.junit.Test
  public void kDateTimeFunc6() {
    final XQuery query = new XQuery(
      "dateTime(xs:date(\"2004-03-04-00:01\"), xs:time(\"08:05:23+00:01\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0008")
    );
  }

  /**
   * Passing different timezones to fn:dateTime() is an error. .
   */
  @org.junit.Test
  public void kDateTimeFunc7() {
    final XQuery query = new XQuery(
      "dateTime(xs:date(\"2004-03-04-00:01\"), xs:time(\"08:05:23Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0008")
    );
  }

  /**
   *  Invoke fn:dateTime() with timezones +00:00 and -00:00..
   */
  @org.junit.Test
  public void kDateTimeFunc8() {
    final XQuery query = new XQuery(
      "dateTime(xs:date(\"2004-03-04-00:00\"), xs:time(\"08:05:23+00:00\")) eq xs:dateTime(\"2004-03-04T08:05:23Z\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Invoke fn:dateTime() with identical timezones..
   */
  @org.junit.Test
  public void kDateTimeFunc9() {
    final XQuery query = new XQuery(
      "dateTime(xs:date(\"2004-03-04+11:00\"), xs:time(\"08:05:23+11:00\")) eq xs:dateTime(\"2004-03-04T08:05:23+11:00\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Passing the empty sequence as both arguments is allowed(recent change in the specification). .
   */
  @org.junit.Test
  public void k2DateTimeFunc1() {
    final XQuery query = new XQuery(
      "empty(dateTime((), ()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Evaluation of "fn:dateTime" function as per example 1 of the F & O Specs. for this function. 
   *  .
   */
  @org.junit.Test
  public void fnDateTime1() {
    final XQuery query = new XQuery(
      "fn:dateTime(xs:date(\"1999-12-31\"), xs:time(\"12:00:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1999-12-31T12:00:00")
    );
  }

  /**
   * Evaluation of "fn:dateTime" function together together with the  op:dateTime-equal operator(ge). 
   *  .
   */
  @org.junit.Test
  public void fnDateTime10() {
    final XQuery query = new XQuery(
      "fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")) ge fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Evaluation of "fn:dateTime" function together together with the  op:dateTime-equal operator(lt). 
   *  .
   */
  @org.junit.Test
  public void fnDateTime11() {
    final XQuery query = new XQuery(
      "fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")) lt fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluation of "fn:dateTime" function together together with the  op:dateTime-equal operator(le). 
   *  .
   */
  @org.junit.Test
  public void fnDateTime12() {
    final XQuery query = new XQuery(
      "fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")) le fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Evaluation of "fn:dateTime" function together together with the  op:dateTime-equal operator(gt). 
   *  .
   */
  @org.junit.Test
  public void fnDateTime13() {
    final XQuery query = new XQuery(
      "fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")) gt fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluation of "fn:dateTime" function together together with the  op:dateTime-equal operator(ge). 
   *  .
   */
  @org.junit.Test
  public void fnDateTime14() {
    final XQuery query = new XQuery(
      "fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")) ge fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Evaluation of "fn:dateTime" as an argument to the function "year-from-dateTime". 
   *  .
   */
  @org.junit.Test
  public void fnDateTime15() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1999")
    );
  }

  /**
   * Evaluation of "fn:dateTime" as an argument to the function "month-from-dateTime". 
   *  .
   */
  @org.junit.Test
  public void fnDateTime16() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "12")
    );
  }

  /**
   * Evaluation of "fn:dateTime" as an argument to the function "day-from-dateTime". 
   *  .
   */
  @org.junit.Test
  public void fnDateTime17() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "31")
    );
  }

  /**
   * Evaluation of "fn:dateTime" as an argument to the function "hours-from-dateTime". 
   *  .
   */
  @org.junit.Test
  public void fnDateTime18() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "23")
    );
  }

  /**
   * Evaluation of "fn:dateTime" as an argument to the function "minutes-from-dateTime". 
   *  .
   */
  @org.junit.Test
  public void fnDateTime19() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Evaluation of "fn:dateTime" function as per example 2 of the F & O Specs. for this function. 
   *  .
   */
  @org.junit.Test
  public void fnDateTime2() {
    final XQuery query = new XQuery(
      "fn:dateTime(xs:date(\"1999-12-31\"), xs:time(\"24:00:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1999-12-31T00:00:00")
    );
  }

  /**
   * Evaluation of "fn:dateTime" as an argument to the function "seconds-from-dateTime". 
   *  .
   */
  @org.junit.Test
  public void fnDateTime20() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Evaluation of "fn:dateTime" as an argument to the function "timezone-from-dateTime". 
   *  .
   */
  @org.junit.Test
  public void fnDateTime21() {
    final XQuery query = new XQuery(
      "fn:timezone-from-dateTime(fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "PT10H")
    );
  }

  /**
   * Evaluation of "fn:dateTime" as an argument to the function "adjust-dateTime-to-timezone". 
   *  .
   */
  @org.junit.Test
  public void fnDateTime22() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")),xs:dayTimeDuration(\"PT10H\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1999-12-31T23:00:00+10:00")
    );
  }

  /**
   * Evaluation of "fn:dateTime" as part of a subtraction operation. 
   *  .
   */
  @org.junit.Test
  public void fnDateTime23() {
    final XQuery query = new XQuery(
      "fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")) - fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"22:00:00+10:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "PT1H")
    );
  }

  /**
   * Evaluation of "fn:dateTime" used together with the "op:add-yearMonthDuration-to-dateTime" operator 
   *  .
   */
  @org.junit.Test
  public void fnDateTime24() {
    final XQuery query = new XQuery(
      " fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")) + xs:yearMonthDuration(\"P1Y2M\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2001-02-28T23:00:00+10:00")
    );
  }

  /**
   * Evaluation of "fn:dateTime" used together with the "op:add-dayTimeDuration-to-dateTime" operator 
   *  .
   */
  @org.junit.Test
  public void fnDateTime25() {
    final XQuery query = new XQuery(
      " fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")) + xs:dayTimeDuration(\"P3DT1H15M\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2000-01-04T00:15:00+10:00")
    );
  }

  /**
   * Evaluation of "fn:dateTime" used together with the "subtract-yearMonthDuration-from-dateTime" operator 
   *  .
   */
  @org.junit.Test
  public void fnDateTime26() {
    final XQuery query = new XQuery(
      " fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")) - xs:yearMonthDuration(\"P1Y2M\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1998-10-31T23:00:00+10:00")
    );
  }

  /**
   * Evaluation of "fn:dateTime" used together with the "subtract-dayTimeDuration-from-dateTime" operator 
   *  .
   */
  @org.junit.Test
  public void fnDateTime27() {
    final XQuery query = new XQuery(
      " fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")) - xs:dayTimeDuration(\"P3DT1H15M\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1999-12-28T21:45:00+10:00")
    );
  }

  /**
   * Evaluation of "fn:dateTime" where first argument is empty sequence 
   *  .
   */
  @org.junit.Test
  public void fnDateTime28() {
    final XQuery query = new XQuery(
      " fn:count(fn:dateTime((), xs:time(\"23:00:00+10:00\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Evaluation of "fn:dateTime" where second argument is empty sequence 
   *  .
   */
  @org.junit.Test
  public void fnDateTime29() {
    final XQuery query = new XQuery(
      " fn:count(fn:dateTime(xs:date(\"1999-12-31+10:00\"), ()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Evaluation of "fn:dateTime" function, where only the first argument have a timezone. 
   *  .
   */
  @org.junit.Test
  public void fnDateTime3() {
    final XQuery query = new XQuery(
      "fn:dateTime(xs:date(\"1999-12-31Z\"), xs:time(\"23:00:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1999-12-31T23:00:00Z")
    );
  }

  /**
   * Evaluation of "fn:dateTime" where both arguments are equal to the empty sequence 
   *  .
   */
  @org.junit.Test
  public void fnDateTime30() {
    final XQuery query = new XQuery(
      " fn:count(fn:dateTime((),()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Evaluation of "fn:dateTime" function, where only the second argument have a timezone. 
   *  .
   */
  @org.junit.Test
  public void fnDateTime4() {
    final XQuery query = new XQuery(
      "fn:dateTime(xs:date(\"1999-12-31\"), xs:time(\"23:00:00Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1999-12-31T23:00:00Z")
    );
  }

  /**
   * Evaluation of "fn:dateTime" function, where both arguments have the same timezone (Z). 
   *  .
   */
  @org.junit.Test
  public void fnDateTime5() {
    final XQuery query = new XQuery(
      "fn:dateTime(xs:date(\"1999-12-31Z\"), xs:time(\"23:00:00Z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1999-12-31T23:00:00Z")
    );
  }

  /**
   * Evaluation of "fn:dateTime" function, where both arguments have the same timezone (+10:00). 
   *  .
   */
  @org.junit.Test
  public void fnDateTime6() {
    final XQuery query = new XQuery(
      "fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1999-12-31T23:00:00+10:00")
    );
  }

  /**
   * Evaluation of "fn:dateTime" function together together with the  op:dateTime-equal operator(eq). 
   *  .
   */
  @org.junit.Test
  public void fnDateTime7() {
    final XQuery query = new XQuery(
      "fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")) eq fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Evaluation of "fn:dateTime" function together together with the  op:dateTime-equal operator(ne). 
   *  .
   */
  @org.junit.Test
  public void fnDateTime8() {
    final XQuery query = new XQuery(
      "fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")) ne fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluation of "fn:dateTime" function together together with the  op:dateTime-equal operator(le). 
   *  .
   */
  @org.junit.Test
  public void fnDateTime9() {
    final XQuery query = new XQuery(
      "fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\")) le fn:dateTime(xs:date(\"1999-12-31+10:00\"), xs:time(\"23:00:00+10:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Evaluation of "fn:dateTime" function, where the two arguments have different timezones. 
   *  .
   */
  @org.junit.Test
  public void forg00081() {
    final XQuery query = new XQuery(
      " fn:dateTime(xs:date(\"1999-12-31Z\"), xs:time(\"12:00:00+10:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0008")
    );
  }
}
