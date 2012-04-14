package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the adjust-date-to-timezone() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnAdjustDateToTimezone extends QT3TestSet {

  /**
   *  A test whose essence is: `adjust-date-to-timezone()`. .
   */
  @org.junit.Test
  public void kAdjDateToTimezoneFunc1() {
    final XQuery query = new XQuery(
      "adjust-date-to-timezone()",
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
  public void kAdjDateToTimezoneFunc10() {
    final XQuery query = new XQuery(
      "adjust-date-to-timezone(xs:date(\"2002-03-07-07:00\"), xs:dayTimeDuration(\"-PT5H0M\")) eq xs:date(\"2002-03-07-05:00\")",
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
  public void kAdjDateToTimezoneFunc11() {
    final XQuery query = new XQuery(
      "adjust-date-to-timezone(xs:date(\"2002-03-07\"), xs:dayTimeDuration(\"-PT10H\")) eq xs:date(\"2002-03-07-10:00\")",
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
  public void kAdjDateToTimezoneFunc12() {
    final XQuery query = new XQuery(
      "adjust-date-to-timezone(xs:date(\"2002-03-07-07:00\"), xs:dayTimeDuration(\"-PT10H\")) eq xs:date(\"2002-03-06-10:00\")",
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
  public void kAdjDateToTimezoneFunc13() {
    final XQuery query = new XQuery(
      "adjust-date-to-timezone(xs:date(\"2002-03-07\"), ()) eq xs:date(\"2002-03-07\")",
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
  public void kAdjDateToTimezoneFunc14() {
    final XQuery query = new XQuery(
      "adjust-date-to-timezone(xs:date(\"2002-03-07-07:00\"), ()) eq xs:date(\"2002-03-07\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `adjust-date-to-timezone((), (), "WRONG PARAM")`. .
   */
  @org.junit.Test
  public void kAdjDateToTimezoneFunc2() {
    final XQuery query = new XQuery(
      "adjust-date-to-timezone((), (), \"WRONG PARAM\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `empty(adjust-date-to-timezone(()))`. .
   */
  @org.junit.Test
  public void kAdjDateToTimezoneFunc3() {
    final XQuery query = new XQuery(
      "empty(adjust-date-to-timezone(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(adjust-date-to-timezone((), ()))`. .
   */
  @org.junit.Test
  public void kAdjDateToTimezoneFunc4() {
    final XQuery query = new XQuery(
      "empty(adjust-date-to-timezone((), ()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `adjust-date-to-timezone(()) instance of xs:date?`. .
   */
  @org.junit.Test
  public void kAdjDateToTimezoneFunc5() {
    final XQuery query = new XQuery(
      "adjust-date-to-timezone(()) instance of xs:date?",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Passing a too large xs:dayTimeDuration as timezone to adjust-date-to-timezone(). .
   */
  @org.junit.Test
  public void kAdjDateToTimezoneFunc6() {
    final XQuery query = new XQuery(
      "adjust-date-to-timezone(xs:date(\"2001-02-03\"), xs:dayTimeDuration(\"PT14H1M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODT0003")
    );
  }

  /**
   *  Passing a too small xs:dayTimeDuration as timezone to adjust-date-to-timezone(). .
   */
  @org.junit.Test
  public void kAdjDateToTimezoneFunc7() {
    final XQuery query = new XQuery(
      "adjust-date-to-timezone(xs:date(\"2001-02-03\"), xs:dayTimeDuration(\"-PT14H1M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODT0003")
    );
  }

  /**
   *  Passing a xs:dayTimeDuration as timezone to adjust-date-to-timezone() which isn't an integral number of minutes. .
   */
  @org.junit.Test
  public void kAdjDateToTimezoneFunc8() {
    final XQuery query = new XQuery(
      "adjust-date-to-timezone(xs:date(\"2001-02-03\"), xs:dayTimeDuration(\"PT14H0M0.001S\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODT0003")
    );
  }

  /**
   *  Example from F&O. .
   */
  @org.junit.Test
  public void kAdjDateToTimezoneFunc9() {
    final XQuery query = new XQuery(
      "adjust-date-to-timezone(xs:date(\"2002-03-07\"), xs:dayTimeDuration(\"-PT5H0M\")) eq xs:date(\"2002-03-07-05:00\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Test Description: Evaluates The "adjust-date-to-timezone" function as per example 1 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone1() {
    final XQuery query = new XQuery(
      "fn:adjust-date-to-timezone(xs:date(\"2002-03-07-05:00\"),xs:dayTimeDuration(\"-PT5H0M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2002-03-07-05:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-date-to-timezone" function as part of an subtraction expression. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone10() {
    final XQuery query = new XQuery(
      "fn:adjust-date-to-timezone(xs:date(\"2002-03-07-07:00\")) - fn:adjust-date-to-timezone(xs:date(\"2002-03-07-07:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-date-to-timezone" function as an argument to a string function. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone11() {
    final XQuery query = new XQuery(
      "fn:string(fn:adjust-date-to-timezone(xs:date(\"2002-03-07-04:00\"),()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2002-03-07")
    );
  }

  /**
   * Test Description: Evaluates the string value The "adjust-date-to-timezone" function as an argument to a boolean function. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone12() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(fn:adjust-date-to-timezone(xs:date(\"2002-03-07-04:00\"),())))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Test Description: Evaluates The string value of "adjust-date-to-timezone" function as an argument to the "fn:not" function. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone13() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(fn:adjust-date-to-timezone(xs:date(\"2002-03-07-04:00\"),())))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Test Description: Evaluates The string value of "adjust-date-to-timezone" function as part of a boolean (or) expression and the fn:true function. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone14() {
    final XQuery query = new XQuery(
      "fn:string(fn:adjust-date-to-timezone(xs:date(\"2002-03-07-04:00\"),())) or fn:true()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Test Description: Evaluates The string value of "adjust-date-to-timezone" function as part of a boolean (or) expression and the fn:false function. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone15() {
    final XQuery query = new XQuery(
      "fn:string(fn:adjust-date-to-timezone(xs:date(\"2002-03-07-04:00\"),())) or fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Test Description: Evaluates The string value of "adjust-date-to-timezone" function as part of a boolean (and) expression and the fn:true function. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone16() {
    final XQuery query = new XQuery(
      "fn:string(fn:adjust-date-to-timezone(xs:date(\"2002-03-07-04:00\"),())) and fn:true()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Test Description: Evaluates The string value of "adjust-date-to-timezone" function as part of a boolean (and) expression and the fn:false function. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone17() {
    final XQuery query = new XQuery(
      "fn:string(fn:adjust-date-to-timezone(xs:date(\"2002-03-07-04:00\"),())) and fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Test Description: Evaluates The "adjust-date-to-timezone" function as part of a subtraction expression, which results on a negative number. Uses one adjust-date-to-timezone function and one xs:date constructor. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone18() {
    final XQuery query = new XQuery(
      "let $tz := xs:dayTimeDuration(\"PT10H\") \n" +
      "         return fn:adjust-date-to-timezone(xs:date(\"2002-03-07Z\"),$tz) - xs:date(\"2006-03-07Z\")\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-P1461DT10H")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-date-to-timezone" function where an xs:date value is subtracted. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone19() {
    final XQuery query = new XQuery(
      "let $tz := xs:dayTimeDuration(\"PT10H\") \n" +
      "         return fn:adjust-date-to-timezone(xs:date(\"2004-03-07Z\"),$tz) - xs:date(\"2001-03-07Z\")\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "P1095DT14H")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-date-to-timezone" function as per example 2 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone2() {
    final XQuery query = new XQuery(
      "fn:adjust-date-to-timezone(xs:date(\"2002-03-07-07:00\"),xs:dayTimeDuration(\"-PT5H0M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2002-03-07-05:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-date-to-timezone" function as part of a comparisson expression (ge operator). .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone20() {
    final XQuery query = new XQuery(
      "fn:adjust-date-to-timezone(xs:date(\"2002-03-07-04:00\")) ge fn:adjust-date-to-timezone(xs:date(\"2005-03-07-04:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Test Description: Evaluates The "adjust-date-to-timezone" function as per example 3 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone3() {
    final XQuery query = new XQuery(
      "let $tz := xs:dayTimeDuration(\"-PT10H\") \n" +
      "         return fn:adjust-date-to-timezone(xs:date(\"2002-03-07\"), $tz)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2002-03-07-10:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-date-to-timezone" function as per example 4 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone4() {
    final XQuery query = new XQuery(
      "let $tz := xs:dayTimeDuration(\"-PT10H\") \n" +
      "         return fn:adjust-date-to-timezone(xs:date(\"2002-03-07-07:00\"), $tz)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2002-03-06-10:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-date-to-timezone" function as per example 5 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone5() {
    final XQuery query = new XQuery(
      "fn:adjust-date-to-timezone(xs:date(\"2002-03-07\"), ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2002-03-07")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-date-to-timezone" function as per example 6 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone6() {
    final XQuery query = new XQuery(
      "fn:adjust-date-to-timezone(xs:date(\"2002-03-07-07:00\"), ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2002-03-07")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-date-to-timezone" function using the empty sequence as a value to the first argument. Uses "fn:count" to avoid empty file. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone7() {
    final XQuery query = new XQuery(
      "fn:count(fn:adjust-date-to-timezone(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-date-to-timezone" function as part of a subtraction expression, whicg results on a negative number. Uses two adjust-date-to-timezone functions. .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone9() {
    final XQuery query = new XQuery(
      "fn:adjust-date-to-timezone(xs:date(\"2002-03-07-07:00\")) - fn:adjust-date-to-timezone(xs:date(\"2006-03-07-07:00\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-P1461D")
    );
  }

  /**
   *  Evaluates The "adjust-date-to-timezone" function with the arguments set as follows: $arg = xs:date(lower bound) .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone1args1() {
    final XQuery query = new XQuery(
      "fn:adjust-date-to-timezone(xs:date(\"1970-01-01Z\"),xs:dayTimeDuration(\"-PT10H\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1969-12-31-10:00")
    );
  }

  /**
   *  Evaluates The "adjust-date-to-timezone" function with the arguments set as follows: $arg = xs:date(mid range) .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone1args2() {
    final XQuery query = new XQuery(
      "fn:adjust-date-to-timezone(xs:date(\"1983-11-17Z\"),xs:dayTimeDuration(\"-PT10H\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1983-11-16-10:00")
    );
  }

  /**
   *  Evaluates The "adjust-date-to-timezone" function with the arguments set as follows: $arg = xs:date(upper bound) .
   */
  @org.junit.Test
  public void fnAdjustDateToTimezone1args3() {
    final XQuery query = new XQuery(
      "fn:adjust-date-to-timezone(xs:date(\"2030-12-31Z\"),xs:dayTimeDuration(\"-PT10H\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2030-12-30-10:00")
    );
  }
}
