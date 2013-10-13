package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the days-from-duration() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnDaysFromDuration extends QT3TestSet {

  /**
   *  A test whose essence is: `days-from-duration()`. .
   */
  @org.junit.Test
  public void kDaysFromDurationFunc1() {
    final XQuery query = new XQuery(
      "days-from-duration()",
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
   *  A test whose essence is: `days-from-duration((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kDaysFromDurationFunc2() {
    final XQuery query = new XQuery(
      "days-from-duration((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(days-from-duration(()))`. .
   */
  @org.junit.Test
  public void kDaysFromDurationFunc3() {
    final XQuery query = new XQuery(
      "empty(days-from-duration(()))",
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
   *  A test whose essence is: `days-from-duration(()) instance of xs:integer?`. .
   */
  @org.junit.Test
  public void kDaysFromDurationFunc4() {
    final XQuery query = new XQuery(
      "days-from-duration(()) instance of xs:integer?",
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
   *  A test whose essence is: `days-from-duration(xs:dayTimeDuration("P45678DT8H2M1.03S")) eq 45678`. .
   */
  @org.junit.Test
  public void kDaysFromDurationFunc5() {
    final XQuery query = new XQuery(
      "days-from-duration(xs:dayTimeDuration(\"P45678DT8H2M1.03S\")) eq 45678",
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
   *  Simple test invoking days-from-duration() on a negative duration. .
   */
  @org.junit.Test
  public void kDaysFromDurationFunc6() {
    final XQuery query = new XQuery(
      "days-from-duration(xs:dayTimeDuration(\"-P45678DT8H2M1.03S\")) eq -45678",
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
   *  Simple test invoking days-from-duration() on an negative xs:duration. .
   */
  @org.junit.Test
  public void kDaysFromDurationFunc7() {
    final XQuery query = new XQuery(
      "days-from-duration(xs:duration(\"-P3Y4M8DT1H23M2.34S\")) eq -8",
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
   *  test fn:days-from-duration on xs:yearMonthDuration .
   */
  @org.junit.Test
  public void cbclDaysFromDuration001() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:yearMonthDuration('P1Y'))",
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
   *  Evaluates The "days-from-duration" function As per example 1 (for this function)of the F&O specs .
   */
  @org.junit.Test
  public void fnDaysFromDuration1() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P3DT10H\"))",
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
   *  Evaluates The "days-from-duration" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnDaysFromDuration10() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P20DT09H\")) * fn:days-from-duration(xs:dayTimeDuration(\"P03DT10H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("60")
    );
  }

  /**
   *  Evaluates The "days-from-duration" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnDaysFromDuration11() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P20DT10H\")) div fn:days-from-duration(xs:dayTimeDuration(\"P05DT05H\"))",
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
   *  Evaluates The "days-from-duration" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnDaysFromDuration12() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P25DT10H\")) idiv fn:days-from-duration(xs:dayTimeDuration(\"P05DT02H\"))",
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
   *  Evaluates The "days-from-duration" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnDaysFromDuration13() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P10DT10H\")) mod fn:days-from-duration(xs:dayTimeDuration(\"P03DT03H\"))",
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
   *  Evaluates The "days-from-duration" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnDaysFromDuration14() {
    final XQuery query = new XQuery(
      "+fn:days-from-duration(xs:dayTimeDuration(\"P21DT10H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("21")
    );
  }

  /**
   *  Evaluates The "days-from-duration" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnDaysFromDuration15() {
    final XQuery query = new XQuery(
      "-fn:days-from-duration(xs:dayTimeDuration(\"P20DT03H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-20")
    );
  }

  /**
   *  Evaluates The "days-from-duration" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnDaysFromDuration16() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P22DT10H\")) eq fn:days-from-duration(xs:dayTimeDuration(\"P22DT09H\"))",
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
   *  Evaluates The "days-from-duration" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnDaysFromDuration17() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P23DT08H\")) ne fn:days-from-duration(xs:dayTimeDuration(\"P12DT05H\"))",
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
   *  Evaluates The "days-from-duration" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnDaysFromDuration18() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P20DT03H\")) le fn:days-from-duration(xs:dayTimeDuration(\"P21DT15H\"))",
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
   *  Evaluates The "days-from-duration" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnDaysFromDuration19() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P21DT07H\")) ge fn:days-from-duration(xs:dayTimeDuration(\"P20DT01H\"))",
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
   *  Evaluates The "days-from-duration" function As per example 2 (for this function) of the F&O specs .
   */
  @org.junit.Test
  public void fnDaysFromDuration2() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P3DT55H\"))",
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
   *  Evaluates The "days-from-duration" function with invalid argument. .
   */
  @org.junit.Test
  public void fnDaysFromDuration20() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:duration(\"P1Y2M3DT10H30M\"))",
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
   *  Evaluates The "days-from-duration" function involving a "numeric-less-than" operation (lt operator) .
   */
  @org.junit.Test
  public void fnDaysFromDuration3() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P20DT20H\")) lt fn:days-from-duration(xs:dayTimeDuration(\"P03DT02H\"))",
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
   *  Evaluates The "days-from-duration" function involving a "numeric-less-than" operation (le operator) .
   */
  @org.junit.Test
  public void fnDaysFromDuration4() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P21DT10H\")) le fn:days-from-duration(xs:dayTimeDuration(\"P22DT10H\"))",
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
   *  Evaluates The "days-from-duration" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnDaysFromDuration5() {
    final XQuery query = new XQuery(
      "fn:count(fn:days-from-duration(()))",
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
   *  Evaluates The "days-from-duration" function that returns 1. .
   */
  @org.junit.Test
  public void fnDaysFromDuration6() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P01DT01H\"))",
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
   *  Evaluates The "days-from-duration" function used as arguments to an avg function. .
   */
  @org.junit.Test
  public void fnDaysFromDuration7() {
    final XQuery query = new XQuery(
      "fn:avg((fn:days-from-duration(xs:dayTimeDuration(\"P23DT10H\")),fn:days-from-duration(xs:dayTimeDuration(\"P21DT10H\"))))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("22")
    );
  }

  /**
   *  Evaluates The "days-from-duration" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnDaysFromDuration8() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P21DT10H\")) + fn:days-from-duration(xs:dayTimeDuration(\"P22DT11H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("43")
    );
  }

  /**
   *  Evaluates The "days-from-duration" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnDaysFromDuration9() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P30DT10H\")) - fn:days-from-duration(xs:dayTimeDuration(\"P10DT09H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("20")
    );
  }

  /**
   *  Evaluates The "days-from-duration" function with the arguments set as follows: $arg = xs:dayTimeDuration(lower bound) .
   */
  @org.junit.Test
  public void fnDaysFromDuration1args1() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P0DT0H0M0S\"))",
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
   *  Evaluates The "days-from-duration" function with the arguments set as follows: $arg = xs:dayTimeDuration(mid range) .
   */
  @org.junit.Test
  public void fnDaysFromDuration1args2() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P15DT11H59M59S\"))",
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
   *  Evaluates The "days-from-duration" function with the arguments set as follows: $arg = xs:dayTimeDuration(upper bound) .
   */
  @org.junit.Test
  public void fnDaysFromDuration1args3() {
    final XQuery query = new XQuery(
      "fn:days-from-duration(xs:dayTimeDuration(\"P31DT23H59M59S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("31")
    );
  }
}
