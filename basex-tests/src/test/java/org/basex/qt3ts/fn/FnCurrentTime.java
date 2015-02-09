package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the current-time() function.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnCurrentTime extends QT3TestSet {

  /**
   *  A test whose essence is: `current-time("WRONG PARAM")`. .
   */
  @org.junit.Test
  public void kContextCurrentTimeFunc1() {
    final XQuery query = new XQuery(
      "current-time(\"WRONG PARAM\")",
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
   *  Test that the Dynamic Context property 'current dateTime' when presented as a xs:time is stable during execution. .
   */
  @org.junit.Test
  public void kContextCurrentTimeFunc2() {
    final XQuery query = new XQuery(
      "current-time() eq current-time()",
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
   *  Evaluates a simple call to the fn:current-time" function. Uses a String Value. .
   */
  @org.junit.Test
  public void fnCurrentTime1() {
    final XQuery query = new XQuery(
      "fn:current-time()",
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
   *  Evaluates The "current-time" function as an argument to the xs:string function. .
   */
  @org.junit.Test
  public void fnCurrentTime10() {
    final XQuery query = new XQuery(
      "xs:string(fn:current-time())",
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
   *  Evaluates The "current-time" function as an argument to the timezone-from-time function. .
   */
  @org.junit.Test
  public void fnCurrentTime11() {
    final XQuery query = new XQuery(
      "fn:timezone-from-time(current-time())",
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
   *  Evaluates string value The "current-time" as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnCurrentTime12() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-time()) eq fn:string(fn:current-time())",
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
   *  Evaluates The "current-time" function as part of an equal expression (ne operator) .
   */
  @org.junit.Test
  public void fnCurrentTime13() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-time()) ne fn:string(fn:current-time())",
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
   *  Evaluates The "current-time" function as part of an equal expression (le operator) .
   */
  @org.junit.Test
  public void fnCurrentTime14() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-time()) le fn:string(fn:current-time())",
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
   *  Evaluates The "current-time" function as part of an equal expression (ge operator) .
   */
  @org.junit.Test
  public void fnCurrentTime15() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-time()) ge fn:string(fn:current-time())",
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
   *  Evaluates The "current-time" function as part of a boolean expression ("and" opeartor and fn:true function. .
   */
  @org.junit.Test
  public void fnCurrentTime16() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-time()) and fn:true()",
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
   *  Evaluates The "current-time" function as part of a boolean expression ("and" opeartor and fn:false function. .
   */
  @org.junit.Test
  public void fnCurrentTime17() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-time()) and fn:false()",
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
   *  Evaluates The "current-time" function as part of a boolean expression ("or" opeartor and fn:true function. .
   */
  @org.junit.Test
  public void fnCurrentTime18() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-time()) or fn:true()",
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
   *  Evaluates The "current-time" function as part of a boolean expression ("or" opeartor and fn:false function. .
   */
  @org.junit.Test
  public void fnCurrentTime19() {
    final XQuery query = new XQuery(
      "fn:string(fn:current-time()) or fn:false()",
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
   *  Evaluates The "current-time" function as argument to fn:hours-from-time function. .
   */
  @org.junit.Test
  public void fnCurrentTime2() {
    final XQuery query = new XQuery(
      "fn:hours-from-time(fn:current-time())",
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
   *  Evaluates The "current-time" function (string value)as an argument to the fn:not function. .
   */
  @org.junit.Test
  public void fnCurrentTime20() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(fn:current-time()))",
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
   * Test that the current time has a timezone.
   */
  @org.junit.Test
  public void fnCurrentTime21() {
    final XQuery query = new XQuery(
      "exists(timezone-from-time(current-time()))",
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
   * Test that the current time is the same as the time part of current dateTime.
   */
  @org.junit.Test
  public void fnCurrentTime22() {
    final XQuery query = new XQuery(
      "current-time() = xs:time(current-dateTime())",
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
   *  Evaluates The "current-time" function as argument to fn-minutes-from-time-function. .
   */
  @org.junit.Test
  public void fnCurrentTime3() {
    final XQuery query = new XQuery(
      "fn:minutes-from-time(fn:current-time())",
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
   *  Evaluates The "current-time" function used as as argument to "seconds-from-time" function. .
   */
  @org.junit.Test
  public void fnCurrentTime4() {
    final XQuery query = new XQuery(
      "fn:seconds-from-time(fn:current-time())",
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
   *  Evaluates The "current-time" function as part of a "-" operation. .
   */
  @org.junit.Test
  public void fnCurrentTime5() {
    final XQuery query = new XQuery(
      "fn:current-time() - fn:current-time()",
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
   *  Evaluates The "fn-current-time" function used as part of a "+" expression and a dayTimeDuration. .
   */
  @org.junit.Test
  public void fnCurrentTime6() {
    final XQuery query = new XQuery(
      "fn:current-time() + xs:dayTimeDuration(\"P3DT1H15M\")",
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
   *  Evaluates The "fn-current-time" function used as part of a "-" expression and a dayTimeDuration. .
   */
  @org.junit.Test
  public void fnCurrentTime7() {
    final XQuery query = new XQuery(
      "fn:current-time() - xs:dayTimeDuration(\"P3DT1H15M\")",
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
   *  Evaluates The "current-time" function invoked with incorrect arity. .
   */
  @org.junit.Test
  public void fnCurrentTime8() {
    final XQuery query = new XQuery(
      "fn:current-time(\"Argument 1\")",
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
   *  Evaluates The "current-time" function as part of of a subtraction expression. Both operands are the equal to "current-time". .
   */
  @org.junit.Test
  public void fnCurrentTime9() {
    final XQuery query = new XQuery(
      "fn:current-time() - fn:current-time()",
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
}
