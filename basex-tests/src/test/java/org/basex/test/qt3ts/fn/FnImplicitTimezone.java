package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the implicit-timezone() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnImplicitTimezone extends QT3TestSet {

  /**
   *  A test whose essence is: `implicit-timezone("WRONG PARAM")`. .
   */
  @org.junit.Test
  public void kContextImplicitTimezoneFunc1() {
    final XQuery query = new XQuery(
      "implicit-timezone(\"WRONG PARAM\")",
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
   *  Simple test of implicit-timezone(). .
   */
  @org.junit.Test
  public void kContextImplicitTimezoneFunc2() {
    final XQuery query = new XQuery(
      "seconds-from-duration(implicit-timezone()) le 0 or seconds-from-duration(implicit-timezone()) gt 0",
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
   *  Test that implicit-timezone() do return a value. .
   */
  @org.junit.Test
  public void kContextImplicitTimezoneFunc3() {
    final XQuery query = new XQuery(
      "exists(seconds-from-duration(implicit-timezone()))",
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
   * Description Evaluation of "fn:implicit-timezone" with incorrect arity. .
   */
  @org.junit.Test
  public void fnImplicitTimezone1() {
    final XQuery query = new XQuery(
      "fn:implicit-timezone(\"Argument 1\")",
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
   *  Evaluation of "fn:implicit-timezone" as part of a division operation. Second argument results in NaN .
   */
  @org.junit.Test
  public void fnImplicitTimezone10() {
    final XQuery query = new XQuery(
      "fn:string(fn:implicit-timezone() div ( 0 div 0E0))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0005")
    );
  }

  /**
   *  Evaluation of "fn:implicit-timezone" as part of a division operation. Second argument is 0. .
   */
  @org.junit.Test
  public void fnImplicitTimezone11() {
    final XQuery query = new XQuery(
      "fn:string(fn:implicit-timezone() div 0 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0002")
    );
  }

  /**
   *  Evaluation of "fn:implicit-timezone" as part of a division operation. Second argument is -0. .
   */
  @org.junit.Test
  public void fnImplicitTimezone12() {
    final XQuery query = new XQuery(
      "fn:string(fn:implicit-timezone() div -0 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0002")
    );
  }

  /**
   *  Evaluation of "fn:implicit-timezone" as part of a division operation. Both operands includes the fn:implicit-timezone. .
   */
  @org.junit.Test
  public void fnImplicitTimezone13() {
    final XQuery query = new XQuery(
      "(implicit-timezone() + xs:dayTimeDuration('PT1S')) div (implicit-timezone() + xs:dayTimeDuration('PT1S'))",
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
   *  Evaluation of "fn:implicit-timezone" as part of a division operation. Second operand is a call to xs:dayTimeDuration function. .
   */
  @org.junit.Test
  public void fnImplicitTimezone14() {
    final XQuery query = new XQuery(
      "fn:string(fn:implicit-timezone() div xs:dayTimeDuration(\"P0DT60M00S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:string")
    );
  }

  /**
   *  Evaluation of "fn:implicit-timezone" as part of an addition operation. First operand is a call to xs:time function. .
   */
  @org.junit.Test
  public void fnImplicitTimezone15() {
    final XQuery query = new XQuery(
      "xs:time(\"05:00:00\") + fn:implicit-timezone()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:time")
    );
  }

  /**
   *  Evaluation of "fn:implicit-timezone" as part of a subtraction operation. First operand is a call to xs:time function. .
   */
  @org.junit.Test
  public void fnImplicitTimezone16() {
    final XQuery query = new XQuery(
      "xs:time(\"05:00:00\") - fn:implicit-timezone()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:time")
    );
  }

  /**
   *  Evaluation of "fn:implicit-timezone" as part of a subtraction operation. First operand is a call to xs:date function. .
   */
  @org.junit.Test
  public void fnImplicitTimezone17() {
    final XQuery query = new XQuery(
      "(xs:date(\"2000-10-30\") - fn:implicit-timezone())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:date")
    );
  }

  /**
   *  Evaluation of "fn:implicit-timezone" as part of an addition operation. First operand is a call to xs:date function. .
   */
  @org.junit.Test
  public void fnImplicitTimezone18() {
    final XQuery query = new XQuery(
      "(xs:date(\"2000-10-30\") + fn:implicit-timezone())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:date")
    );
  }

  /**
   *  Evaluation of "fn:implicit-timezone" as part of a subtraction operation. First operand is a call to xs:dateTime function. .
   */
  @org.junit.Test
  public void fnImplicitTimezone19() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"2000-10-30T11:12:00\") - fn:implicit-timezone())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:dateTime")
    );
  }

  /**
   *  Normal call to "fn:implicit-timezone". .
   */
  @org.junit.Test
  public void fnImplicitTimezone2() {
    final XQuery query = new XQuery(
      "fn:implicit-timezone()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:dayTimeDuration")
    );
  }

  /**
   *  Evaluation of "fn:implicit-timezone" as part of an addition operation. First operand is a call to xs:dateTime function. .
   */
  @org.junit.Test
  public void fnImplicitTimezone20() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"2000-10-30T11:12:00\") + fn:implicit-timezone())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:dateTime")
    );
  }

  /**
   *  Evaluation of "fn:implicit-timezone" as an an argument to the adjust-date-to-timezone function. .
   */
  @org.junit.Test
  public void fnImplicitTimezone21() {
    final XQuery query = new XQuery(
      "(fn:adjust-date-to-timezone(xs:date(\"2000-10-30\"),fn:implicit-timezone()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:date")
    );
  }

  /**
   *  Evaluation of "fn:implicit-timezone" as an an argument to the adjust-time-to-timezone function. .
   */
  @org.junit.Test
  public void fnImplicitTimezone22() {
    final XQuery query = new XQuery(
      "(fn:adjust-time-to-timezone(xs:time(\"10:00:00\"),fn:implicit-timezone()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:time")
    );
  }

  /**
   *  Evaluation of "fn:implicit-timezone" as an an argument to the adjust-dateTime-to-timezone function. .
   */
  @org.junit.Test
  public void fnImplicitTimezone23() {
    final XQuery query = new XQuery(
      "(fn:adjust-dateTime-to-timezone(xs:dateTime(\"2002-03-07T10:00:00\"),fn:implicit-timezone()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:dateTime")
    );
  }

  /**
   * Test that implicit timezone is in range -14h to +14h.
   */
  @org.junit.Test
  public void fnImplicitTimezone24() {
    final XQuery query = new XQuery(
      "implicit-timezone() ge xs:dayTimeDuration('-PT14H') and implicit-timezone() le xs:dayTimeDuration('PT14H')",
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
   *  Evaluation of "fn:implicit-timezone" as part of an addition operation. .
   */
  @org.junit.Test
  public void fnImplicitTimezone3() {
    final XQuery query = new XQuery(
      "fn:string(fn:implicit-timezone() + fn:implicit-timezone())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:string")
    );
  }

  /**
   *  Evaluation of "fn:implicit-timezone" as part of a subtraction operation. .
   */
  @org.junit.Test
  public void fnImplicitTimezone4() {
    final XQuery query = new XQuery(
      "fn:string(fn:implicit-timezone() - fn:implicit-timezone())",
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
   *  Evaluation of "fn:implicit-timezone" as part of a multiplication operation. .
   */
  @org.junit.Test
  public void fnImplicitTimezone5() {
    final XQuery query = new XQuery(
      "fn:implicit-timezone() * xs:double(2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:dayTimeDuration")
    );
  }

  /**
   *  Evaluation of "fn:implicit-timezone" as part of a multiplication operation. Second argument is NaN .
   */
  @org.junit.Test
  public void fnImplicitTimezone6() {
    final XQuery query = new XQuery(
      "fn:string(fn:implicit-timezone() * (0 div 0E0))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0005")
    );
  }

  /**
   *  Evaluation of "fn:implicit-timezone" as part of a multiplication operation. Second argument is 0 .
   */
  @org.junit.Test
  public void fnImplicitTimezone7() {
    final XQuery query = new XQuery(
      "fn:string(fn:implicit-timezone() * 0)",
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
   *  Evaluation of "fn:implicit-timezone" as part of a multiplication operation. Second argument is -0 .
   */
  @org.junit.Test
  public void fnImplicitTimezone8() {
    final XQuery query = new XQuery(
      "fn:string(fn:implicit-timezone() * -0)",
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
   *  Evaluation of "fn:implicit-timezone" as part of a division operation. .
   */
  @org.junit.Test
  public void fnImplicitTimezone9() {
    final XQuery query = new XQuery(
      "fn:string(fn:implicit-timezone() div xs:double(2))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:string")
    );
  }
}
