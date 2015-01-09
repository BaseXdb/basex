package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the current-dateTime() function.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnCurrentDateTime extends QT3TestSet {

  /**
   *  A test whose essence is: `current-dateTime("WRONG PARAM")`. .
   */
  @org.junit.Test
  public void kContextCurrentDatetimeFunc1() {
    final XQuery query = new XQuery(
      "current-dateTime(\"WRONG PARAM\")",
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
   *  Test that the Dynamic Context property 'current dateTime' when presented as a xs:dateTime is stable during execution. .
   */
  @org.junit.Test
  public void kContextCurrentDatetimeFunc2() {
    final XQuery query = new XQuery(
      "current-dateTime() eq current-dateTime()",
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
   * dateTime: December 1, 2005  Evaluates a simple call to the fn:current-dateTime" function. Uses a String Value. .
   */
  @org.junit.Test
  public void fnCurrentDateTime1() {
    final XQuery query = new XQuery(
      "fn:current-dateTime()",
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
   * dateTime: December 5, 2005  Evaluates The "current-dateTime" function as an argument to the xs:string function. .
   */
  @org.junit.Test
  public void fnCurrentDateTime10() {
    final XQuery query = new XQuery(
      "xs:string(fn:current-dateTime())",
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
   * dateTime: December 5, 2005  Evaluates The "current-dateTime" function as an argument to the timezone-from-dateTime function. .
   */
  @org.junit.Test
  public void fnCurrentDateTime11() {
    final XQuery query = new XQuery(
      "fn:timezone-from-dateTime(current-dateTime())",
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
   * dateTime: December 5, 2005  Evaluates string value The "current-dateTime" as part of an equal expression (eq operator) .
   */
  @org.junit.Test
  public void fnCurrentDateTime12() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-dateTime()) eq fn:string(fn:current-dateTime())",
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
   * dateTime: December 5, 2005  Evaluates The "current-dateTime" function as part of an equal expression (ne operator) .
   */
  @org.junit.Test
  public void fnCurrentDateTime13() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-dateTime()) ne fn:string(fn:current-dateTime())",
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
   * dateTime: December 5, 2005  Evaluates The "current-dateTime" function as part of an equal expression (le operator) .
   */
  @org.junit.Test
  public void fnCurrentDateTime14() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-dateTime()) le fn:string(fn:current-dateTime())",
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
   * dateTime: December 5, 2005  Evaluates The "current-dateTime" function as part of an equal expression (ge operator) .
   */
  @org.junit.Test
  public void fnCurrentDateTime15() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-dateTime()) ge fn:string(fn:current-dateTime())",
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
   * dateTime: December 5, 2005  Evaluates The "current-dateTime" function as part of a boolean expression ("and" operator and fn:true function. .
   */
  @org.junit.Test
  public void fnCurrentDateTime16() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-dateTime()) and fn:true()",
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
   * dateTime: December 5, 2005  Evaluates The "current-dateTime" function as part of a boolean expression ("and" operator and fn:false function. .
   */
  @org.junit.Test
  public void fnCurrentDateTime17() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-dateTime()) and fn:false()",
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
   * dateTime: December 5, 2005  Evaluates The "current-dateTime" function as part of a boolean expression ("or" operator and fn:true function. .
   */
  @org.junit.Test
  public void fnCurrentDateTime18() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-dateTime()) or fn:true()",
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
   * dateTime: December 5, 2005  Evaluates The "current-dateTime" function as part of a boolean expression ("or" operator and fn:false function. .
   */
  @org.junit.Test
  public void fnCurrentDateTime19() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-dateTime()) or fn:false()",
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
   * dateTime: December 1, 2005  Evaluates The "current-dateTime" function as argument to fn:year-from-dateTime function. .
   */
  @org.junit.Test
  public void fnCurrentDateTime2() {
    final XQuery query = new XQuery(
      "fn:year-from-dateTime(fn:current-dateTime())",
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
   * dateTime: December 5, 2005  Evaluates The "current-dateTime" function (string value)as an argument to the fn:not function. .
   */
  @org.junit.Test
  public void fnCurrentDateTime20() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(fn:current-dateTime()))",
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
   * dateTime: December 5, 2005  Evaluates The "fn-current-dateTime" function used as part of a "-" operation and a yearMonthDuration. .
   */
  @org.junit.Test
  public void fnCurrentDateTime21() {
    final XQuery query = new XQuery(
      "fn:current-dateTime() - xs:yearMonthDuration(\"P1Y2M\")",
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
   * dateTime: December 1, 2005  Evaluates The "current-dateTime" function as argument to fn:hours-from-dateTime function. .
   */
  @org.junit.Test
  public void fnCurrentDateTime22() {
    final XQuery query = new XQuery(
      "fn:hours-from-dateTime(fn:current-dateTime())",
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
   * dateTime: December 1, 2005  Evaluates The "current-dateTime" function as argument to fn-minutes-from-dateTime function. .
   */
  @org.junit.Test
  public void fnCurrentDateTime23() {
    final XQuery query = new XQuery(
      "fn:minutes-from-dateTime(fn:current-dateTime())",
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
   * dateTime: December 1, 2005  Evaluates The "current-dateTime" function used as as argument to "seconds-from-dateTime" function .
   */
  @org.junit.Test
  public void fnCurrentDateTime24() {
    final XQuery query = new XQuery(
      "fn:seconds-from-dateTime(fn:current-dateTime())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:decimal")
    );
  }

  /**
   * Test that the current dateTime has a timezone.
   */
  @org.junit.Test
  public void fnCurrentDateTime25() {
    final XQuery query = new XQuery(
      "exists(timezone-from-dateTime(current-dateTime()))",
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
   * dateTime: December 1, 2005  Evaluates The "current-dateTime" function as argument to fn-month-from-dateTime function. .
   */
  @org.junit.Test
  public void fnCurrentDateTime3() {
    final XQuery query = new XQuery(
      "fn:month-from-dateTime(fn:current-dateTime())",
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
   * dateTime: December 1, 2005  Evaluates The "current-dateTime" function used as as argument to "day-from-dateTime" function .
   */
  @org.junit.Test
  public void fnCurrentDateTime4() {
    final XQuery query = new XQuery(
      "fn:day-from-dateTime(fn:current-dateTime())",
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
   * dateTime: December 1, 2005  Evaluates The "current-dateTime" function as part of a "-" operation. .
   */
  @org.junit.Test
  public void fnCurrentDateTime5() {
    final XQuery query = new XQuery(
      "fn:current-dateTime() - fn:current-dateTime()",
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
   * dateTime: December 5, 2005  Evaluates The "fn-current-dateTime" function used as part of a "+" expression and a dayTimeDuration. .
   */
  @org.junit.Test
  public void fnCurrentDateTime6() {
    final XQuery query = new XQuery(
      "fn:current-dateTime() + xs:dayTimeDuration(\"P3DT1H15M\")",
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
   * dateTime: December 5, 2005  Evaluates The "current-dateTime" function invoked with incorrect arity. .
   */
  @org.junit.Test
  public void fnCurrentDateTime8() {
    final XQuery query = new XQuery(
      "fn:current-dateTime(\"Argument 1\")",
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
   * dateTime: December 5, 2005  Evaluates The "current-dateTime" function as part of of a subtraction expression. Both operands are the equal to "current-dateTime". .
   */
  @org.junit.Test
  public void fnCurrentDateTime9() {
    final XQuery query = new XQuery(
      "fn:current-dateTime() - fn:current-dateTime()",
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
   * dateTime: December 5, 2005  Evaluates The "fn-current-dateTime" function used as part of a "-" expression and a dayTimeDuration. .
   */
  @org.junit.Test
  public void fnCurrentDatetime7() {
    final XQuery query = new XQuery(
      "fn:current-dateTime() - xs:dayTimeDuration(\"P3DT1H15M\")",
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
}
