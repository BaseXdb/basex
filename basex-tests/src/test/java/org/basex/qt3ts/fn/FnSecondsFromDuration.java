package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the seconds-from-duration() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnSecondsFromDuration extends QT3TestSet {

  /**
   *  A test whose essence is: `seconds-from-duration()`. .
   */
  @org.junit.Test
  public void kSecondsFromDurationFunc1() {
    final XQuery query = new XQuery(
      "seconds-from-duration()",
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
   *  A test whose essence is: `seconds-from-duration((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kSecondsFromDurationFunc2() {
    final XQuery query = new XQuery(
      "seconds-from-duration((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(seconds-from-duration(()))`. .
   */
  @org.junit.Test
  public void kSecondsFromDurationFunc3() {
    final XQuery query = new XQuery(
      "empty(seconds-from-duration(()))",
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
   *  A test whose essence is: `seconds-from-duration(()) instance of xs:decimal?`. .
   */
  @org.junit.Test
  public void kSecondsFromDurationFunc4() {
    final XQuery query = new XQuery(
      "seconds-from-duration(()) instance of xs:decimal?",
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
   *  Simple test invoking seconds-from-duration() on a positive duration. .
   */
  @org.junit.Test
  public void kSecondsFromDurationFunc5() {
    final XQuery query = new XQuery(
      "seconds-from-duration(xs:dayTimeDuration(\"P3DT8H2M1.03S\")) eq 1.03",
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
   *  Simple test invoking seconds-from-duration() on a negative duration. .
   */
  @org.junit.Test
  public void kSecondsFromDurationFunc6() {
    final XQuery query = new XQuery(
      "seconds-from-duration(xs:dayTimeDuration(\"-P3DT8H2M1.03S\")) eq -1.03",
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
   *  Simple test invoking minutes-from-seconds() on an negative xs:duration. .
   */
  @org.junit.Test
  public void kSecondsFromDurationFunc7() {
    final XQuery query = new XQuery(
      "seconds-from-duration(xs:duration(\"-P3Y4M8DT1H23M2.34S\")) eq -2.34",
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
   *  test fn:seconds-from-duration on xs:dayTimeDuration .
   */
  @org.junit.Test
  public void cbclSecondsFromDuration001() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:yearMonthDuration('P1Y'))",
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
   *  Evaluates The "seconds-from-duration" function As per example 1 (for this function)of the F&O specs .
   */
  @org.junit.Test
  public void fnSecondsFromDuration1() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P3DT10H12.5S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("12.5")
    );
  }

  /**
   *  Evaluates The "seconds-from-duration" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromDuration10() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P20DT09H04M20S\")) * fn:seconds-from-duration(xs:dayTimeDuration(\"P03DT10H10M03S\"))",
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
   *  Evaluates The "seconds-from-duration" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromDuration11() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P20DT10H10M30S\")) div fn:seconds-from-duration(xs:dayTimeDuration(\"P05DT05H02M02S\"))",
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
   *  Evaluates The "seconds-from-duration" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromDuration12() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P25DT10H20M40S\")) idiv fn:seconds-from-duration(xs:dayTimeDuration(\"P05DT02H04M20S\"))",
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
   *  Evaluates The "seconds-from-duration" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromDuration13() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P10DT10H20M10S\")) mod fn:seconds-from-duration(xs:dayTimeDuration(\"P03DT03H03M03S\"))",
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
   *  Evaluates The "seconds-from-duration" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromDuration14() {
    final XQuery query = new XQuery(
      "+fn:seconds-from-duration(xs:dayTimeDuration(\"P21DT10H10M20S\"))",
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
   *  Evaluates The "seconds-from-duration" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromDuration15() {
    final XQuery query = new XQuery(
      "-fn:seconds-from-duration(xs:dayTimeDuration(\"P20DT03H20M30S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-30")
    );
  }

  /**
   *  Evaluates The "seconds-from-duration" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnSecondsFromDuration16() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P22DT10H10M01S\")) eq fn:seconds-from-duration(xs:dayTimeDuration(\"P22DT09H10M01S\"))",
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
   *  Evaluates The "seconds-from-duration" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnSecondsFromDuration17() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P23DT08H20M02S\")) ne fn:seconds-from-duration(xs:dayTimeDuration(\"P12DT05H22M03S\"))",
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
   *  Evaluates The "seconds-from-duration" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnSecondsFromDuration18() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P20DT03H09M20S\")) le fn:seconds-from-duration(xs:dayTimeDuration(\"P21DT15H21M31S\"))",
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
   *  Evaluates The "seconds-from-duration" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnSecondsFromDuration19() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P21DT07H12M59S\")) ge fn:seconds-from-duration(xs:dayTimeDuration(\"P20DT01H13M01S\"))",
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
   *  Evaluates The "seconds-from-duration" function As per example 2 (for this function) of the F&O specs .
   */
  @org.junit.Test
  public void fnSecondsFromDuration2() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"-PT256S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-16")
    );
  }

  /**
   *  Evaluates The "seconds-from-duration" function with wrong argument type. .
   */
  @org.junit.Test
  public void fnSecondsFromDuration20() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:duration(\"P1Y2M3DT10H30M911S\"))",
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
   *  Evaluates The "seconds-from-duration" function to evaluate normalization of dayTimeDuration .
   */
  @org.junit.Test
  public void fnSecondsFromDuration21() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P21DT10H10M90S\"))",
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
   *  Evaluates The "seconds-from-duration" function involving a "numeric-less-than" operation (lt operator) .
   */
  @org.junit.Test
  public void fnSecondsFromDuration3() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P20DT20H20M10S\")) lt fn:seconds-from-duration(xs:dayTimeDuration(\"P03DT02H10M20S\"))",
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
   *  Evaluates The "seconds-from-duration" function involving a "numeric-less-than" operation (le operator) .
   */
  @org.junit.Test
  public void fnSecondsFromDuration4() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P21DT10H10M09S\")) le fn:seconds-from-duration(xs:dayTimeDuration(\"P22DT10H09M31S\"))",
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
   *  Evaluates The "seconds-from-duration" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnSecondsFromDuration5() {
    final XQuery query = new XQuery(
      "fn:count(fn:seconds-from-duration(()))",
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
   *  Evaluates The "seconds-from-duration" function that returns 1. .
   */
  @org.junit.Test
  public void fnSecondsFromDuration6() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P01DT01H01M01S\"))",
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
   *  Evaluates The "seconds-from-duration" function used as arguments to an avg function. .
   */
  @org.junit.Test
  public void fnSecondsFromDuration7() {
    final XQuery query = new XQuery(
      "fn:avg((fn:seconds-from-duration(xs:dayTimeDuration(\"P23DT10H20M30S\")),fn:seconds-from-duration(xs:dayTimeDuration(\"P21DT10H10M32S\"))))",
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

  /**
   *  Evaluates The "seconds-from-duration" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromDuration8() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P21DT10H10M09S\")) + fn:seconds-from-duration(xs:dayTimeDuration(\"P22DT11H30M21S\"))",
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
   *  Evaluates The "seconds-from-duration" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnSecondsFromDuration9() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P30DT10H20M10S\")) - fn:seconds-from-duration(xs:dayTimeDuration(\"P10DT09H10M02S\"))",
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
   *  Evaluates The "seconds-from-duration" function with the arguments set as follows: $arg = xs:dayTimeDuration(lower bound) .
   */
  @org.junit.Test
  public void fnSecondsFromDuration1args1() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P0DT0H0M0S\"))",
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
   *  Evaluates The "seconds-from-duration" function with the arguments set as follows: $arg = xs:dayTimeDuration(mid range) .
   */
  @org.junit.Test
  public void fnSecondsFromDuration1args2() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P15DT11H59M59S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("59")
    );
  }

  /**
   *  Evaluates The "seconds-from-duration" function with the arguments set as follows: $arg = xs:dayTimeDuration(upper bound) .
   */
  @org.junit.Test
  public void fnSecondsFromDuration1args3() {
    final XQuery query = new XQuery(
      "fn:seconds-from-duration(xs:dayTimeDuration(\"P31DT23H59M59S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("59")
    );
  }
}
