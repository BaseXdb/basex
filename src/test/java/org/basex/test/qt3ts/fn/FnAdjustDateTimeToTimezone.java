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
public class FnAdjustDateTimeToTimezone extends QT3TestSet {

  /**
   *  A test whose essence is: `adjust-dateTime-to-timezone()`. .
   */
  @org.junit.Test
  public void kAdjDateTimeToTimezoneFunc1() {
    final XQuery query = new XQuery(
      "adjust-dateTime-to-timezone()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  Example from F&O. .
   */
  @org.junit.Test
  public void kAdjDateTimeToTimezoneFunc10() {
    final XQuery query = new XQuery(
      "adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00\"), xs:dayTimeDuration(\"-PT5H0M\")) eq xs:dateTime(\"2002-03-07T10:00:00-05:00\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Example from F&O. .
   */
  @org.junit.Test
  public void kAdjDateTimeToTimezoneFunc11() {
    final XQuery query = new XQuery(
      "adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-07:00\"), xs:dayTimeDuration(\"-PT5H0M\")) eq xs:dateTime(\"2002-03-07T12:00:00-05:00\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Example from F&O. .
   */
  @org.junit.Test
  public void kAdjDateTimeToTimezoneFunc12() {
    final XQuery query = new XQuery(
      "adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00\"), xs:dayTimeDuration(\"-PT10H\")) eq xs:dateTime(\"2002-03-07T10:00:00-10:00\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Example from F&O. .
   */
  @org.junit.Test
  public void kAdjDateTimeToTimezoneFunc13() {
    final XQuery query = new XQuery(
      "adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-07:00\"), xs:dayTimeDuration(\"-PT10H\")) eq xs:dateTime(\"2002-03-07T07:00:00-10:00\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Example from F&O. .
   */
  @org.junit.Test
  public void kAdjDateTimeToTimezoneFunc14() {
    final XQuery query = new XQuery(
      "adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-07:00\"), xs:dayTimeDuration(\"PT10H\")) eq xs:dateTime(\"2002-03-08T03:00:00+10:00\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Example from F&O. .
   */
  @org.junit.Test
  public void kAdjDateTimeToTimezoneFunc15() {
    final XQuery query = new XQuery(
      "adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T00:00:00+01:00\"), xs:dayTimeDuration(\"-PT8H\")) eq xs:dateTime(\"2002-03-06T15:00:00-08:00\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Example from F&O. .
   */
  @org.junit.Test
  public void kAdjDateTimeToTimezoneFunc16() {
    final XQuery query = new XQuery(
      "adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00\"), ()) eq xs:dateTime(\"2002-03-07T10:00:00\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `adjust-dateTime-to-timezone((), (), "WRONG PARAM")`. .
   */
  @org.junit.Test
  public void kAdjDateTimeToTimezoneFunc2() {
    final XQuery query = new XQuery(
      "adjust-dateTime-to-timezone((), (), \"WRONG PARAM\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `empty(adjust-dateTime-to-timezone(()))`. .
   */
  @org.junit.Test
  public void kAdjDateTimeToTimezoneFunc3() {
    final XQuery query = new XQuery(
      "empty(adjust-dateTime-to-timezone(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(adjust-dateTime-to-timezone((), ()))`. .
   */
  @org.junit.Test
  public void kAdjDateTimeToTimezoneFunc4() {
    final XQuery query = new XQuery(
      "empty(adjust-dateTime-to-timezone((), ()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `adjust-dateTime-to-timezone(()) instance of xs:dateTime?`. .
   */
  @org.junit.Test
  public void kAdjDateTimeToTimezoneFunc5() {
    final XQuery query = new XQuery(
      "adjust-dateTime-to-timezone(()) instance of xs:dateTime?",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test that the implicit timezone in the dynamic context is used if $timezone is empty; indirectly also tests context stability. .
   */
  @org.junit.Test
  public void kAdjDateTimeToTimezoneFunc6() {
    final XQuery query = new XQuery(
      "timezone-from-dateTime(adjust-dateTime-to-timezone(xs:dateTime(\"2001-02-03T00:00:00\"))) eq implicit-timezone()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Passing a too large xs:dayTimeDuration as timezone to adjust-dateTime-to-timezone(). .
   */
  @org.junit.Test
  public void kAdjDateTimeToTimezoneFunc7() {
    final XQuery query = new XQuery(
      "adjust-dateTime-to-timezone(xs:dateTime(\"2001-02-03T08:02:00\"), xs:dayTimeDuration(\"PT14H1M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODT0003")
    );
  }

  /**
   *  Passing a too small xs:dayTimeDuration as timezone to adjust-dateTime-to-timezone(). .
   */
  @org.junit.Test
  public void kAdjDateTimeToTimezoneFunc8() {
    final XQuery query = new XQuery(
      "adjust-dateTime-to-timezone(xs:dateTime(\"2001-02-03T08:02:00\"), xs:dayTimeDuration(\"-PT14H1M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODT0003")
    );
  }

  /**
   *  Passing a xs:dayTimeDuration as timezone to adjust-dateTime-to-timezone() which isn't an integral number of minutes. .
   */
  @org.junit.Test
  public void kAdjDateTimeToTimezoneFunc9() {
    final XQuery query = new XQuery(
      "adjust-dateTime-to-timezone(xs:dateTime(\"2001-02-03T08:02:00\"), xs:dayTimeDuration(\"PT14H0M0.001S\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODT0003")
    );
  }

  /**
   *  Ensure dateTime values are properly normalized. .
   */
  @org.junit.Test
  public void k2AdjDateTimeToTimezoneFunc1() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(xs:dateTime(\"1999-12-31T24:00:00\"), ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2000-01-01T00:00:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-dateTime-to-timezone" function as per example 1 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone1() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-05:00\"),xs:dayTimeDuration(\"-PT5H0M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2002-03-07T10:00:00-05:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-dateTime-to-timezone" function as part of an subtraction expression. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone10() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-07:00\")) - fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-07:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-dateTime-to-timezone" function as an argument to a string function. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone11() {
    final XQuery query = new XQuery(
      "fn:string(fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-04:00\"),()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2002-03-07T10:00:00")
    );
  }

  /**
   * Test Description: Evaluates string value The "adjust-dateTime-to-timezone" function as an argument to a boolean function. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone12() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-04:00\"),())))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Test Description: Evaluates The string value of "adjust-dateTime-to-timezone" function as an argument to the "fn:not" function. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone13() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-04:00\"),())))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Test Description: Evaluates The string value "adjust-dateTime-to-timezone" function as part of a boolean (or) expression and the fn:true function. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone14() {
    final XQuery query = new XQuery(
      "fn:string(fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-04:00\"),())) or fn:true()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Test Description: Evaluates The string value "adjust-dateTime-to-timezone" function as part of a boolean (or) expression and the fn:false function. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone15() {
    final XQuery query = new XQuery(
      "fn:string(fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-04:00\"),())) or fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Test Description: Evaluates string value The "adjust-dateTime-to-timezone" function as part of a boolean (and) expression and the fn:true function. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone16() {
    final XQuery query = new XQuery(
      "fn:string(fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-04:00\"),())) and fn:true()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Test Description: Evaluates The string value of "adjust-dateTime-to-timezone" function as part of a boolean (and) expression and the fn:false function. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone17() {
    final XQuery query = new XQuery(
      "fn:string(fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-04:00\"),())) and fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Test Description: Evaluates The "adjust-dateTime-to-timezone" function as part of a subtraction expression, whicg results on a negative number. Uses one adjust-dateTime-to-timezone function and one xs:dateTime constructor. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone18() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-04:00\")) - xs:dateTime(\"2006-03-07T10:00:00-05:00\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-P1461DT1H")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-dateTime-to-timezone" function where an xs:dateTime value is subtracted. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone19() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-04:00\")) - xs:dateTime(\"2001-03-07T10:00:00-05:00\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "P364DT23H")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-dateTime-to-timezone" function as per example 2 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone2() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-07:00\"),xs:dayTimeDuration(\"-PT5H0M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2002-03-07T12:00:00-05:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-dateTime-to-timezone" function as part of a comparisson expression (ge operator). .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone20() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-04:00\")) ge fn:adjust-dateTime-to-timezone(xs:dateTime(\"2005-03-07T10:00:00-04:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Test Description: Evaluates The "adjust-dateTime-to-timezone" function using the empty sequence as a value to the first argument. Uses "fn:count" to avoid empty file. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone21() {
    final XQuery query = new XQuery(
      "fn:count(fn:adjust-dateTime-to-timezone(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-dateTime-to-timezone" function with value of $timezone less than -PT14H. Should raise error. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone22() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-04:00\"),xs:dayTimeDuration(\"-PT15H\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODT0003")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-dateTime-to-timezone" function with value of $timezone greater than PT14H. Should raise error. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone23() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-04:00\"),xs:dayTimeDuration(\"PT15H\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODT0003")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-dateTime-to-timezone" function as per example 3 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone3() {
    final XQuery query = new XQuery(
      "let $tz := xs:dayTimeDuration(\"-PT10H\") return fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00\"), $tz)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2002-03-07T10:00:00-10:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-dateTime-to-timezone" function as per example 4 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone4() {
    final XQuery query = new XQuery(
      "let $tz := xs:dayTimeDuration(\"-PT10H\") return fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-07:00\"), $tz)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2002-03-07T07:00:00-10:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-dateTime-to-timezone" function as per example 5 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone5() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-07:00\"), xs:dayTimeDuration(\"PT10H\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2002-03-08T03:00:00+10:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-dateTime-to-timezone" function as per example 6 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone6() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T00:00:00+01:00\"), xs:dayTimeDuration(\"-PT8H\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2002-03-06T15:00:00-08:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-dateTime-to-timezone" function as per example 7 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone7() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00\"), ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2002-03-07T10:00:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-dateTime-to-timezone" function as per example 8 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone8() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-07:00\"), ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2002-03-07T10:00:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-dateTime-to-timezone" function as part of a subtraction expression, whicg results on a negative number. Uses two adjust-dateTime-to-timezone functions. .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone9() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00-07:00\")) - fn:adjust-dateTime-to-timezone(xs:dateTime(\"2006-03-07T10:00:00-07:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-P1461D")
    );
  }

  /**
   *  Evaluates The "adjust-dateTime-to-timezone" function with the arguments set as follows: $arg = xs:dateTime(lower bound) .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone1args1() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(xs:dateTime(\"1970-01-01T00:00:00Z\"),xs:dayTimeDuration(\"-PT10H\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1969-12-31T14:00:00-10:00")
    );
  }

  /**
   *  Evaluates The "adjust-dateTime-to-timezone" function with the arguments set as follows: $arg = xs:dateTime(mid range) .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone1args2() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(xs:dateTime(\"1996-04-07T01:40:52Z\"),xs:dayTimeDuration(\"-PT10H\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1996-04-06T15:40:52-10:00")
    );
  }

  /**
   *  Evaluates The "adjust-dateTime-to-timezone" function with the arguments set as follows: $arg = xs:dateTime(upper bound) .
   */
  @org.junit.Test
  public void fnAdjustDateTimeToTimezone1args3() {
    final XQuery query = new XQuery(
      "fn:adjust-dateTime-to-timezone(xs:dateTime(\"2030-12-31T23:59:59Z\"),xs:dayTimeDuration(\"-PT10H\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2030-12-31T13:59:59-10:00")
    );
  }
}
