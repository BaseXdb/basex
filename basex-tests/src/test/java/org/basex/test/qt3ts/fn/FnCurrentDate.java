package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the current-date() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnCurrentDate extends QT3TestSet {

  /**
   *  A test whose essence is: `current-date("WRONG PARAM")`. .
   */
  @org.junit.Test
  public void kContextCurrentDateFunc1() {
    final XQuery query = new XQuery(
      "current-date(\"WRONG PARAM\")",
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
   *  Test that the Dynamic Context property 'current dateTime' when presented as a xs:date is stable during execution. .
   */
  @org.junit.Test
  public void kContextCurrentDateFunc2() {
    final XQuery query = new XQuery(
      "current-date() eq current-date()",
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
   *  Evaluates a simple call to the fn:current-date" function. Uses a String Value. .
   */
  @org.junit.Test
  public void fnCurrentDate1() {
    final XQuery query = new XQuery(
      "fn:current-date()",
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
   *  Evaluates The "current-date" function as an argument to the xs:string function. .
   */
  @org.junit.Test
  public void fnCurrentDate10() {
    final XQuery query = new XQuery(
      "xs:string(fn:current-date())",
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
   *  Evaluates The "current-date" function as an argument to the timezone-from-date function. .
   */
  @org.junit.Test
  public void fnCurrentDate11() {
    final XQuery query = new XQuery(
      "fn:timezone-from-date(current-date())",
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
   *  Evaluates string value The "current-date" as part of an equal expression (eq operator) .
   */
  @org.junit.Test
  public void fnCurrentDate12() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-date()) eq fn:string(fn:current-date())",
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
   *  Evaluates The "current-date" function as part of an equal expression (ne operator) .
   */
  @org.junit.Test
  public void fnCurrentDate13() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-date()) ne fn:string(fn:current-date())",
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
   *  Evaluates The "current-date" function as part of an equal expression (le operator) .
   */
  @org.junit.Test
  public void fnCurrentDate14() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-date()) le fn:string(fn:current-date())",
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
   *  Evaluates The "current-date" function as part of an equal expression (ge operator) .
   */
  @org.junit.Test
  public void fnCurrentDate15() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-date()) ge fn:string(fn:current-date())",
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
   *  Evaluates The "current-date" function as part of a boolean expression ("and" operator and fn:true function. .
   */
  @org.junit.Test
  public void fnCurrentDate16() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-date()) and fn:true()",
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
   *  Evaluates The "current-date" function as part of a boolean expression ("and" operator and fn:false function. .
   */
  @org.junit.Test
  public void fnCurrentDate17() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-date()) and fn:false()",
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
   *  Evaluates The "current-date" function as part of a boolean expression ("or" operator and fn:true function. .
   */
  @org.junit.Test
  public void fnCurrentDate18() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-date()) or fn:true()",
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
   *  Evaluates The "current-date" function as part of a boolean expression ("or" operator and fn:false function. .
   */
  @org.junit.Test
  public void fnCurrentDate19() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-date()) or fn:false()",
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
   *  Evaluates The "current-date" function as argument to fn:hours-from-date function. .
   */
  @org.junit.Test
  public void fnCurrentDate2() {
    final XQuery query = new XQuery(
      "fn:year-from-date(fn:current-date())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:integer")
    );
  }

  /**
   *  Evaluates The "current-date" function (string value)as an argument to the fn:not function. .
   */
  @org.junit.Test
  public void fnCurrentDate20() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(fn:current-date()))",
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
   *  Evaluates The "fn-current-date" function used as part of a "-" operation and a yearMonthDuration. .
   */
  @org.junit.Test
  public void fnCurrentDate21() {
    final XQuery query = new XQuery(
      "fn:current-date() - xs:yearMonthDuration(\"P1Y2M\")",
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
   * Test that current date is after 1 Jan 2010.
   */
  @org.junit.Test
  public void fnCurrentDate22() {
    final XQuery query = new XQuery(
      "fn:current-date() gt xs:date('2010-01-01')",
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
   * Test that current date has a timezone.
   */
  @org.junit.Test
  public void fnCurrentDate23() {
    final XQuery query = new XQuery(
      "exists(timezone-from-date(current-date()))",
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
   *  Evaluates The "current-date" function as argument to fn-month-from-date function. .
   */
  @org.junit.Test
  public void fnCurrentDate3() {
    final XQuery query = new XQuery(
      "fn:month-from-date(fn:current-date())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:integer")
    );
  }

  /**
   *  Evaluates The "current-date" function used as as argument to "day-from-date" function. .
   */
  @org.junit.Test
  public void fnCurrentDate4() {
    final XQuery query = new XQuery(
      "fn:day-from-date(fn:current-date())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:integer")
    );
  }

  /**
   *  Evaluates The "current-date" function as part of a "-" operation. .
   */
  @org.junit.Test
  public void fnCurrentDate5() {
    final XQuery query = new XQuery(
      "fn:current-date() - fn:current-date()",
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
   *  Evaluates The "fn-current-date" function used as part of a "+" expression and a dayTimeDuration. .
   */
  @org.junit.Test
  public void fnCurrentDate6() {
    final XQuery query = new XQuery(
      "fn:current-date() + xs:dayTimeDuration(\"P3DT1H15M\")",
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
   *  Evaluates The "fn-current-date" function used as part of a "-" expression and a dayTimeDuration. .
   */
  @org.junit.Test
  public void fnCurrentDate7() {
    final XQuery query = new XQuery(
      "fn:current-date() - xs:dayTimeDuration(\"P3DT1H15M\")",
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
   *  Evaluates The "current-date" function invoked with incorrect arity. .
   */
  @org.junit.Test
  public void fnCurrentDate8() {
    final XQuery query = new XQuery(
      "fn:current-date(\"Argument 1\")",
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
   *  Evaluates The "current-date" function as part of of a subtraction expression. Both operands are the equal to "current-time". .
   */
  @org.junit.Test
  public void fnCurrentDate9() {
    final XQuery query = new XQuery(
      "fn:current-date() - fn:current-date()",
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
   * Test that the current date is the same as the date part of current dateTime.
   */
  @org.junit.Test
  public void fnCurrentTime24() {
    final XQuery query = new XQuery(
      "current-date() = xs:date(current-dateTime())",
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
