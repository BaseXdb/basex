package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the minutes-from-duration() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnMinutesFromDuration extends QT3TestSet {

  /**
   *  A test whose essence is: `minutes-from-duration()`. .
   */
  @org.junit.Test
  public void kMinutesFromDurationFunc1() {
    final XQuery query = new XQuery(
      "minutes-from-duration()",
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
   *  A test whose essence is: `minutes-from-duration((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kMinutesFromDurationFunc2() {
    final XQuery query = new XQuery(
      "minutes-from-duration((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(minutes-from-duration(()))`. .
   */
  @org.junit.Test
  public void kMinutesFromDurationFunc3() {
    final XQuery query = new XQuery(
      "empty(minutes-from-duration(()))",
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
   *  A test whose essence is: `minutes-from-duration(()) instance of xs:integer?`. .
   */
  @org.junit.Test
  public void kMinutesFromDurationFunc4() {
    final XQuery query = new XQuery(
      "minutes-from-duration(()) instance of xs:integer?",
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
   *  A test whose essence is: `minutes-from-duration(xs:dayTimeDuration("P3DT8H2M1.03S")) eq 2`. .
   */
  @org.junit.Test
  public void kMinutesFromDurationFunc5() {
    final XQuery query = new XQuery(
      "minutes-from-duration(xs:dayTimeDuration(\"P3DT8H2M1.03S\")) eq 2",
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
   *  Simple test invoking minutes-from-duration() on a negative duration. .
   */
  @org.junit.Test
  public void kMinutesFromDurationFunc6() {
    final XQuery query = new XQuery(
      "minutes-from-duration(xs:dayTimeDuration(\"-P3DT8H2M1.03S\")) eq -2",
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
   *  Simple test invoking minutes-from-hours() on an negative xs:duration. .
   */
  @org.junit.Test
  public void kMinutesFromDurationFunc7() {
    final XQuery query = new XQuery(
      "minutes-from-duration(xs:duration(\"-P3Y4M8DT1H23M2.34S\")) eq -23",
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
   *  test fn:minutes-from-duration on xs:dayTimeDuration .
   */
  @org.junit.Test
  public void cbclMinutesFromDuration001() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:yearMonthDuration('P1Y'))",
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
   *  Evaluates The "minutes-from-duration" function As per example 1 (for this function)of the F&O specs .
   */
  @org.junit.Test
  public void fnMinutesFromDuration1() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P3DT10H\"))",
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
   *  Evaluates The "minutes-from-duration" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromDuration10() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P20DT09H04M\")) * fn:minutes-from-duration(xs:dayTimeDuration(\"P03DT10H10M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "40")
    );
  }

  /**
   *  Evaluates The "minutes-from-duration" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromDuration11() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P20DT10H10M\")) div fn:minutes-from-duration(xs:dayTimeDuration(\"P05DT05H02M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5")
    );
  }

  /**
   *  Evaluates The "minutes-from-duration" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromDuration12() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P25DT10H20M\")) idiv fn:minutes-from-duration(xs:dayTimeDuration(\"P05DT02H04M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5")
    );
  }

  /**
   *  Evaluates The "minutes-from-duration" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromDuration13() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P10DT10H20M\")) mod fn:minutes-from-duration(xs:dayTimeDuration(\"P03DT03H03M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   *  Evaluates The "minutes-from-duration" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromDuration14() {
    final XQuery query = new XQuery(
      "+fn:minutes-from-duration(xs:dayTimeDuration(\"P21DT10H10M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10")
    );
  }

  /**
   *  Evaluates The "minutes-from-duration" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromDuration15() {
    final XQuery query = new XQuery(
      "-fn:minutes-from-duration(xs:dayTimeDuration(\"P20DT03H20M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-20")
    );
  }

  /**
   *  Evaluates The "minutes-from-duration" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnMinutesFromDuration16() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P22DT10H10M\")) eq fn:minutes-from-duration(xs:dayTimeDuration(\"P22DT09H10M\"))",
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
   *  Evaluates The "minutes-from-duration" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnMinutesFromDuration17() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P23DT08H20M\")) ne fn:minutes-from-duration(xs:dayTimeDuration(\"P12DT05H22M\"))",
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
   *  Evaluates The "minutes-from-duration" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnMinutesFromDuration18() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P20DT03H09M\")) le fn:minutes-from-duration(xs:dayTimeDuration(\"P21DT15H21M\"))",
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
   *  Evaluates The "minutes-from-duration" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnMinutesFromDuration19() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P21DT07H12M\")) ge fn:minutes-from-duration(xs:dayTimeDuration(\"P20DT01H13M\"))",
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
   *  Evaluates The "minutes-from-duration" function As per example 2 (for this function) of the F&O specs .
   */
  @org.junit.Test
  public void fnMinutesFromDuration2() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"-P5DT12H30M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-30")
    );
  }

  /**
   *  Evaluates The "minutes-from-duration" function with wrong argument type. .
   */
  @org.junit.Test
  public void fnMinutesFromDuration20() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:duration(\"P1Y2M3DT10H30M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "30")
    );
  }

  /**
   *  Evaluates The "minutes-from-duration" function to evaluate normalization of duration. .
   */
  @org.junit.Test
  public void fnMinutesFromDuration21() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P21DT10H65M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5")
    );
  }

  /**
   *  Evaluates The "minutes-from-duration" function involving a "numeric-less-than" operation (lt operator) .
   */
  @org.junit.Test
  public void fnMinutesFromDuration3() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P20DT20H20M\")) lt fn:minutes-from-duration(xs:dayTimeDuration(\"P03DT02H10M\"))",
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
   *  Evaluates The "minutes-from-duration" function involving a "numeric-less-than" operation (le operator) .
   */
  @org.junit.Test
  public void fnMinutesFromDuration4() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P21DT10H10M\")) le fn:minutes-from-duration(xs:dayTimeDuration(\"P22DT10H09M\"))",
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
   *  Evaluates The "minutes-from-duration" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnMinutesFromDuration5() {
    final XQuery query = new XQuery(
      "fn:count(fn:minutes-from-duration(()))",
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
   *  Evaluates The "minutes-from-duration" function that returns 1. .
   */
  @org.junit.Test
  public void fnMinutesFromDuration6() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P01DT01H01M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "minutes-from-duration" function used as arguments to an avg function. .
   */
  @org.junit.Test
  public void fnMinutesFromDuration7() {
    final XQuery query = new XQuery(
      "fn:avg((fn:minutes-from-duration(xs:dayTimeDuration(\"P23DT10H20M\")),fn:minutes-from-duration(xs:dayTimeDuration(\"P21DT10H10M\"))))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "15")
    );
  }

  /**
   *  Evaluates The "minutes-from-duration" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromDuration8() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P21DT10H10M\")) + fn:minutes-from-duration(xs:dayTimeDuration(\"P22DT11H30M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "40")
    );
  }

  /**
   *  Evaluates The "minutes-from-duration" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnMinutesFromDuration9() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P30DT10H20M\")) - fn:minutes-from-duration(xs:dayTimeDuration(\"P10DT09H10M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10")
    );
  }

  /**
   *  Evaluates The "minutes-from-duration" function with the arguments set as follows: $arg = xs:dayTimeDuration(lower bound) .
   */
  @org.junit.Test
  public void fnMinutesFromDuration1args1() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P0DT0H0M0S\"))",
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
   *  Evaluates The "minutes-from-duration" function with the arguments set as follows: $arg = xs:dayTimeDuration(mid range) .
   */
  @org.junit.Test
  public void fnMinutesFromDuration1args2() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P15DT11H59M59S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "59")
    );
  }

  /**
   *  Evaluates The "minutes-from-duration" function with the arguments set as follows: $arg = xs:dayTimeDuration(upper bound) .
   */
  @org.junit.Test
  public void fnMinutesFromDuration1args3() {
    final XQuery query = new XQuery(
      "fn:minutes-from-duration(xs:dayTimeDuration(\"P31DT23H59M59S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "59")
    );
  }
}
