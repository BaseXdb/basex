package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the hours-from-duration() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnHoursFromDuration extends QT3TestSet {

  /**
   *  A test whose essence is: `hours-from-duration()`. .
   */
  @org.junit.Test
  public void kHoursFromDurationFunc1() {
    final XQuery query = new XQuery(
      "hours-from-duration()",
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
   *  A test whose essence is: `hours-from-duration((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kHoursFromDurationFunc2() {
    final XQuery query = new XQuery(
      "hours-from-duration((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(hours-from-duration(()))`. .
   */
  @org.junit.Test
  public void kHoursFromDurationFunc3() {
    final XQuery query = new XQuery(
      "empty(hours-from-duration(()))",
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
   *  A test whose essence is: `hours-from-duration(()) instance of xs:integer?`. .
   */
  @org.junit.Test
  public void kHoursFromDurationFunc4() {
    final XQuery query = new XQuery(
      "hours-from-duration(()) instance of xs:integer?",
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
   *  A test whose essence is: `hours-from-duration(xs:dayTimeDuration("P3DT8H2M1.03S")) eq 8`. .
   */
  @org.junit.Test
  public void kHoursFromDurationFunc5() {
    final XQuery query = new XQuery(
      "hours-from-duration(xs:dayTimeDuration(\"P3DT8H2M1.03S\")) eq 8",
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
   *  Simple test invoking hours-from-duration() on a negative duration. .
   */
  @org.junit.Test
  public void kHoursFromDurationFunc6() {
    final XQuery query = new XQuery(
      "hours-from-duration(xs:dayTimeDuration(\"-P3DT8H2M1.03S\")) eq -8",
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
   *  Simple test invoking days-from-hours() on an negative xs:duration. .
   */
  @org.junit.Test
  public void kHoursFromDurationFunc7() {
    final XQuery query = new XQuery(
      "hours-from-duration(xs:duration(\"-P3Y4M8DT1H23M2.34S\")) eq -1",
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
   *  test fn:hours-from-duration on xs:yearMonthDuration .
   */
  @org.junit.Test
  public void cbclHoursFromDuration001() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:yearMonthDuration('P1Y'))",
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
   *  Evaluates The "hours-from-duration" function As per example 1 (for this function)of the F&O specs .
   */
  @org.junit.Test
  public void fnHoursFromDuration1() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"P3DT10H\"))",
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
   *  Evaluates The "hours-from-duration" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnHoursFromDuration10() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"P20DT05H\")) * fn:hours-from-duration(xs:dayTimeDuration(\"P03DT08H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("40")
    );
  }

  /**
   *  Evaluates The "hours-from-duration" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnHoursFromDuration11() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"P20DT10H\")) div fn:hours-from-duration(xs:dayTimeDuration(\"P05DT05H\"))",
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
   *  Evaluates The "hours-from-duration" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnHoursFromDuration12() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"P25DT10H\")) idiv fn:hours-from-duration(xs:dayTimeDuration(\"P05DT02H\"))",
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
   *  Evaluates The "hours-from-duration" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnHoursFromDuration13() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"P10DT10H\")) mod fn:hours-from-duration(xs:dayTimeDuration(\"P03DT02H\"))",
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
   *  Evaluates The "hours-from-duration" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnHoursFromDuration14() {
    final XQuery query = new XQuery(
      "+fn:hours-from-duration(xs:dayTimeDuration(\"P21DT10H\"))",
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
   *  Evaluates The "hours-from-duration" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnHoursFromDuration15() {
    final XQuery query = new XQuery(
      "-fn:hours-from-duration(xs:dayTimeDuration(\"P20DT02H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-2")
    );
  }

  /**
   *  Evaluates The "hours-from-duration" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnHoursFromDuration16() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"P22DT09H\")) eq fn:hours-from-duration(xs:dayTimeDuration(\"P22DT09H\"))",
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
   *  Evaluates The "hours-from-duration" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnHoursFromDuration17() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"P23DT07H\")) ne fn:hours-from-duration(xs:dayTimeDuration(\"P12DT05H\"))",
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
   *  Evaluates The "hours-from-duration" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnHoursFromDuration18() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"P20DT03H\")) le fn:hours-from-duration(xs:dayTimeDuration(\"P21DT01H\"))",
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
   *  Evaluates The "hours-from-duration" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnHoursFromDuration19() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"P21DT07H\")) ge fn:hours-from-duration(xs:dayTimeDuration(\"P20DT08H\"))",
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
   *  Evaluates The "hours-from-duration" function As per example 2 (for this function) of the F&O specs .
   */
  @org.junit.Test
  public void fnHoursFromDuration2() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"P3DT12H32M12S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("12")
    );
  }

  /**
   *  Evaluates The "hours-from-duration" function with wrong argument type. .
   */
  @org.junit.Test
  public void fnHoursFromDuration20() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:duration(\"P1Y2M3DT10H30M\"))",
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
   *  Evaluates The "hours-from-duration" function as per example 3 of this function on the F&O specs. .
   */
  @org.junit.Test
  public void fnHoursFromDuration3() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"PT123H\"))",
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
   *  Evaluates The "hours-from-duration" function as per example 4 (for this function) in the F&O specs. .
   */
  @org.junit.Test
  public void fnHoursFromDuration4() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"-P3DT10H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-10")
    );
  }

  /**
   *  Evaluates The "hours-from-duration" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnHoursFromDuration5() {
    final XQuery query = new XQuery(
      "fn:count(fn:hours-from-duration(()))",
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
   *  Evaluates The "hours-from-duration" function that returns 1. .
   */
  @org.junit.Test
  public void fnHoursFromDuration6() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"P01DT01H\"))",
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
   *  Evaluates The "hours-from-duration" function used as arguments to an avg function. .
   */
  @org.junit.Test
  public void fnHoursFromDuration7() {
    final XQuery query = new XQuery(
      "fn:avg((fn:hours-from-duration(xs:dayTimeDuration(\"P23DT10H\")),fn:hours-from-duration(xs:dayTimeDuration(\"P21DT08H\"))))",
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
   *  Evaluates The "hours-from-duration" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnHoursFromDuration8() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"P21DT10H\")) + fn:hours-from-duration(xs:dayTimeDuration(\"P22DT20H\"))",
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
   *  Evaluates The "hours-from-duration" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnHoursFromDuration9() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"P30DT10H\")) - fn:hours-from-duration(xs:dayTimeDuration(\"P10DT02H\"))",
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
   *  Evaluates The "hours-from-duration" function with the arguments set as follows: $arg = xs:dayTimeDuration(lower bound) .
   */
  @org.junit.Test
  public void fnHoursFromDuration1args1() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"P0DT0H0M0S\"))",
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
   *  Evaluates The "hours-from-duration" function with the arguments set as follows: $arg = xs:dayTimeDuration(mid range) .
   */
  @org.junit.Test
  public void fnHoursFromDuration1args2() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"P15DT11H59M59S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("11")
    );
  }

  /**
   *  Evaluates The "hours-from-duration" function with the arguments set as follows: $arg = xs:dayTimeDuration(upper bound) .
   */
  @org.junit.Test
  public void fnHoursFromDuration1args3() {
    final XQuery query = new XQuery(
      "fn:hours-from-duration(xs:dayTimeDuration(\"P31DT23H59M59S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("23")
    );
  }
}
