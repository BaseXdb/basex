package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the adjust-time-to-timezone() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnAdjustTimeToTimezone extends QT3TestSet {

  /**
   *  A test whose essence is: `adjust-time-to-timezone()`. .
   */
  @org.junit.Test
  public void kAdjTimeToTimezoneFunc1() {
    final XQuery query = new XQuery(
      "adjust-time-to-timezone()",
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
   *  Example from F&O. .
   */
  @org.junit.Test
  public void kAdjTimeToTimezoneFunc10() {
    final XQuery query = new XQuery(
      "adjust-time-to-timezone(xs:time(\"10:00:00\"), xs:dayTimeDuration(\"-PT5H0M\")) eq xs:time(\"10:00:00-05:00\")",
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
   *  Example from F&O. .
   */
  @org.junit.Test
  public void kAdjTimeToTimezoneFunc11() {
    final XQuery query = new XQuery(
      "adjust-time-to-timezone(xs:time(\"10:00:00-07:00\"), xs:dayTimeDuration(\"-PT5H0M\")) eq xs:time(\"12:00:00-05:00\")",
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
   *  Example from F&O. .
   */
  @org.junit.Test
  public void kAdjTimeToTimezoneFunc12() {
    final XQuery query = new XQuery(
      "adjust-time-to-timezone(xs:time(\"10:00:00\"), xs:dayTimeDuration(\"-PT10H\")) eq xs:time(\"10:00:00-10:00\")",
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
   *  Example from F&O. .
   */
  @org.junit.Test
  public void kAdjTimeToTimezoneFunc13() {
    final XQuery query = new XQuery(
      "adjust-time-to-timezone(xs:time(\"10:00:00-07:00\"), xs:dayTimeDuration(\"-PT10H\")) eq xs:time(\"07:00:00-10:00\")",
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
   *  Example from F&O. .
   */
  @org.junit.Test
  public void kAdjTimeToTimezoneFunc14() {
    final XQuery query = new XQuery(
      "adjust-time-to-timezone(xs:time(\"10:00:00\"), ()) eq xs:time(\"10:00:00\")",
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
   *  Example from F&O. .
   */
  @org.junit.Test
  public void kAdjTimeToTimezoneFunc15() {
    final XQuery query = new XQuery(
      "adjust-time-to-timezone(xs:time(\"10:00:00-07:00\"), ()) eq xs:time(\"10:00:00\")",
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
   *  Example from F&O. .
   */
  @org.junit.Test
  public void kAdjTimeToTimezoneFunc16() {
    final XQuery query = new XQuery(
      "adjust-time-to-timezone(xs:time(\"10:00:00-07:00\"), xs:dayTimeDuration(\"PT10H\")) eq xs:time(\"03:00:00+10:00\")",
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
   *  A test whose essence is: `adjust-time-to-timezone((), (), "WRONG PARAM")`. .
   */
  @org.junit.Test
  public void kAdjTimeToTimezoneFunc2() {
    final XQuery query = new XQuery(
      "adjust-time-to-timezone((), (), \"WRONG PARAM\")",
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
   *  A test whose essence is: `empty(adjust-time-to-timezone(()))`. .
   */
  @org.junit.Test
  public void kAdjTimeToTimezoneFunc3() {
    final XQuery query = new XQuery(
      "empty(adjust-time-to-timezone(()))",
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
   *  A test whose essence is: `empty(adjust-time-to-timezone((), ()))`. .
   */
  @org.junit.Test
  public void kAdjTimeToTimezoneFunc4() {
    final XQuery query = new XQuery(
      "empty(adjust-time-to-timezone((), ()))",
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
   *  A test whose essence is: `adjust-time-to-timezone(()) instance of xs:time?`. .
   */
  @org.junit.Test
  public void kAdjTimeToTimezoneFunc5() {
    final XQuery query = new XQuery(
      "adjust-time-to-timezone(()) instance of xs:time?",
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
   *  Passing a too large xs:dayTimeDuration as timezone to adjust-time-to-timezone(). .
   */
  @org.junit.Test
  public void kAdjTimeToTimezoneFunc6() {
    final XQuery query = new XQuery(
      "adjust-time-to-timezone(xs:time(\"08:02:00\"), xs:dayTimeDuration(\"PT14H1M\"))",
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
   *  Passing a too small xs:dayTimeDuration as timezone to adjust-time-to-timezone(). .
   */
  @org.junit.Test
  public void kAdjTimeToTimezoneFunc7() {
    final XQuery query = new XQuery(
      "adjust-time-to-timezone(xs:time(\"08:02:00\"), xs:dayTimeDuration(\"-PT14H1M\"))",
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
   *  Passing a xs:dayTimeDuration as timezone to adjust-time-to-timezone() which isn't an integral number of minutes. .
   */
  @org.junit.Test
  public void kAdjTimeToTimezoneFunc8() {
    final XQuery query = new XQuery(
      "adjust-time-to-timezone(xs:time(\"08:02:00\"), xs:dayTimeDuration(\"PT14H0M0.001S\"))",
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
   *  Test that the implicit timezone in the dynamic context is used if $timezone is empty; indirectly also tests context stability. .
   */
  @org.junit.Test
  public void kAdjTimeToTimezoneFunc9() {
    final XQuery query = new XQuery(
      "timezone-from-time(adjust-time-to-timezone(xs:time(\"00:00:00\"))) eq implicit-timezone()",
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
   * Check optimization codepath in adjust-time-to-timezone .
   */
  @org.junit.Test
  public void cbclAdjustTimeToTimezone001() {
    final XQuery query = new XQuery(
      "adjust-time-to-timezone(current-time(), implicit-timezone()) eq current-time()",
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
   * Trigger EvaluateToItem in adjust-time-to-timezone .
   */
  @org.junit.Test
  public void cbclAdjustTimeToTimezone002() {
    final XQuery query = new XQuery(
      "adjust-time-to-timezone(xs:time(\"12:00:00Z\")) eq adjust-time-to-timezone(xs:time(\"13:00:00+01:00\"))",
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
   * Test Description: Evaluates The "adjust-time-to-timezone" function as per example 1 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone1() {
    final XQuery query = new XQuery(
      "fn:adjust-time-to-timezone(xs:time(\"10:00:00-05:00\"),xs:dayTimeDuration(\"-PT5H0M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10:00:00-05:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-time-to-timezone" function as part of an subtraction expression. Use zulu timezone and empty sequence for 2nd argument. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone10() {
    final XQuery query = new XQuery(
      "fn:adjust-time-to-timezone(xs:time(\"10:00:00Z\"),()) - fn:adjust-time-to-timezone(xs:time(\"10:00:00Z\"),())",
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
   * Test Description: Evaluates The "adjust-time-to-timezone" function as an argument to a string function. Use zulu timezone and empty sequence for 2nd argument. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone11() {
    final XQuery query = new XQuery(
      "fn:string(fn:adjust-time-to-timezone(xs:time(\"10:00:00Z\"),()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10:00:00")
    );
  }

  /**
   * Test Description: Evaluates The string value "adjust-time-to-timezone" function as an argument to a boolean function. Use zulu timezone and empty sequence for 2nd argument. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone12() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(fn:adjust-time-to-timezone(xs:time(\"10:00:00Z\"),())))",
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
   * Test Description: Evaluates The string value of "adjust-time-to-timezone" function as an argument to the "fn:not" function. Use zulu timezone and empty sequence for 2nd argument. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone13() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(fn:adjust-time-to-timezone(xs:time(\"10:00:00Z\"),())))",
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
   * Test Description: Evaluates The string value of "adjust-time-to-timezone" function part of a boolean (or) expression and the fn:true function. Use zulu timezone and empty sequence for 2nd argument. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone14() {
    final XQuery query = new XQuery(
      "fn:string(fn:adjust-time-to-timezone(xs:time(\"10:00:00Z\"),())) or fn:true()",
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
   * Test Description: Evaluates The string value of "adjust-time-to-timezone" function as part of a boolean (or) expression and the fn:false function. Use zulu timezone and empty sequence for 2nd argument. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone15() {
    final XQuery query = new XQuery(
      "fn:string(fn:adjust-time-to-timezone(xs:time(\"10:00:00Z\"),())) or fn:false()",
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
   * Test Description: Evaluates The string value of "adjust-time-to-timezone" function as part of a boolean (and) expression and the fn:true function. Use zulu timezone and empty sequence for 2nd argument. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone16() {
    final XQuery query = new XQuery(
      "fn:string(fn:adjust-time-to-timezone(xs:time(\"10:00:00Z\"),())) and fn:true()",
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
   * Test Description: Evaluates The string value of "adjust-time-to-timezone" function as part of a boolean (and) expression and the fn:false function. Use zulu timezone and empty sequence for 2nd argument. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone17() {
    final XQuery query = new XQuery(
      "fn:string(fn:adjust-time-to-timezone(xs:time(\"10:00:00Z\"),())) and fn:false()",
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
   * Test Description: Evaluates The "adjust-time-to-timezone" function as part of a subtraction expression, which results on a negative number. Uses one adjust-time-to-timezone function and one xs:time constructor. Use zulu timezone and empty sequence for 2nd argument. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone18() {
    final XQuery query = new XQuery(
      "let $tz := xs:dayTimeDuration(\"PT10H\") \n" +
      "         return fn:adjust-time-to-timezone(xs:time(\"01:00:00Z\"),$tz) - xs:time(\"10:00:00Z\")\n" +
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
      assertStringValue(false, "-PT9H")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-time-to-timezone" function where an xs:time value is subtracted. Use zulu timezone and empty sequence for 2nd argument. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone19() {
    final XQuery query = new XQuery(
      "let $tz := xs:dayTimeDuration(\"PT10H\") \n" +
      "         return fn:adjust-time-to-timezone(xs:time(\"10:00:00Z\"),$tz) - xs:time(\"09:00:00Z\")\n" +
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
      assertStringValue(false, "PT1H")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-time-to-timezone" function as per example 2 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone2() {
    final XQuery query = new XQuery(
      "fn:adjust-time-to-timezone(xs:time(\"10:00:00-07:00\"),xs:dayTimeDuration(\"-PT5H0M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "12:00:00-05:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-time-to-timezone" function as part of a comparisson expression (ge operator). Use zulu timezone and empty sequence for 2nd argument. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone20() {
    final XQuery query = new XQuery(
      "fn:adjust-time-to-timezone(xs:time(\"10:00:00Z\"),()) ge fn:adjust-time-to-timezone(xs:time(\"11:00:00Z\"),())",
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
   * Test Description: Evaluates The "adjust-time-to-timezone" function as per example 3 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone3() {
    final XQuery query = new XQuery(
      "let $tz := xs:dayTimeDuration(\"-PT10H\") \n" +
      "         return fn:adjust-time-to-timezone(xs:time(\"10:00:00\"), $tz)\n" +
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
      assertStringValue(false, "10:00:00-10:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-time-to-timezone" function as per example 4 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone4() {
    final XQuery query = new XQuery(
      "let $tz := xs:dayTimeDuration(\"-PT10H\") \n" +
      "         return fn:adjust-time-to-timezone(xs:time(\"10:00:00-07:00\"), $tz)\n" +
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
      assertStringValue(false, "07:00:00-10:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-time-to-timezone" function as per example 5 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone5() {
    final XQuery query = new XQuery(
      "fn:adjust-time-to-timezone(xs:time(\"10:00:00-05:00\"),())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10:00:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-time-to-timezone" function as per example 6 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone6() {
    final XQuery query = new XQuery(
      "fn:adjust-time-to-timezone(xs:time(\"10:00:00-07:00\"), ())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10:00:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-time-to-timezone" function as per example 7 (for this function) of the F&O specs. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone7() {
    final XQuery query = new XQuery(
      "fn:adjust-time-to-timezone(xs:time(\"10:00:00-07:00\"), xs:dayTimeDuration(\"PT10H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "03:00:00+10:00")
    );
  }

  /**
   * Test Description: Evaluates The "adjust-time-to-timezone" function using the empty sequence as the first argument, Uses the count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone8() {
    final XQuery query = new XQuery(
      "fn:count(fn:adjust-time-to-timezone((),()))",
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
   * Test Description: Evaluates The "adjust-time-to-timezone" function as part of a subtraction expression, whicg results on a negative number. Uses two adjust-time-to-timezone functions. .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone9() {
    final XQuery query = new XQuery(
      "fn:adjust-time-to-timezone(xs:time(\"09:00:00Z\"),()) - fn:adjust-time-to-timezone(xs:time(\"10:00:00Z\"),())",
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

  /**
   *  Evaluates The "adjust-time-to-timezone" function with the arguments set as follows: $arg = xs:time(lower bound) .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone1args1() {
    final XQuery query = new XQuery(
      "fn:adjust-time-to-timezone(xs:time(\"00:00:00Z\"),xs:dayTimeDuration(\"-PT10H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "14:00:00-10:00")
    );
  }

  /**
   *  Evaluates The "adjust-time-to-timezone" function with the arguments set as follows: $arg = xs:time(mid range) .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone1args2() {
    final XQuery query = new XQuery(
      "fn:adjust-time-to-timezone(xs:time(\"08:03:35Z\"),xs:dayTimeDuration(\"-PT10H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "22:03:35-10:00")
    );
  }

  /**
   *  Evaluates The "adjust-time-to-timezone" function with the arguments set as follows: $arg = xs:time(upper bound) .
   */
  @org.junit.Test
  public void fnAdjustTimeToTimezone1args3() {
    final XQuery query = new XQuery(
      "fn:adjust-time-to-timezone(xs:time(\"23:59:59Z\"),xs:dayTimeDuration(\"-PT10H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "13:59:59-10:00")
    );
  }
}
