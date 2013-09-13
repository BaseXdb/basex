package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the years-from-duration() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnYearsFromDuration extends QT3TestSet {

  /**
   *  A test whose essence is: `years-from-duration()`. .
   */
  @org.junit.Test
  public void kYearsFromDurationFunc1() {
    final XQuery query = new XQuery(
      "years-from-duration()",
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
   *  A test whose essence is: `years-from-duration((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kYearsFromDurationFunc2() {
    final XQuery query = new XQuery(
      "years-from-duration((), \"Wrong param\")",
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
   *  A test whose essence is: `empty(years-from-duration(()))`. .
   */
  @org.junit.Test
  public void kYearsFromDurationFunc3() {
    final XQuery query = new XQuery(
      "empty(years-from-duration(()))",
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
   *  A test whose essence is: `years-from-duration(()) instance of xs:integer?`. .
   */
  @org.junit.Test
  public void kYearsFromDurationFunc4() {
    final XQuery query = new XQuery(
      "years-from-duration(()) instance of xs:integer?",
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
   *  A test whose essence is: `years-from-duration(xs:yearMonthDuration("P0003Y2M")) eq 3`. .
   */
  @org.junit.Test
  public void kYearsFromDurationFunc5() {
    final XQuery query = new XQuery(
      "years-from-duration(xs:yearMonthDuration(\"P0003Y2M\")) eq 3",
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
   *  Simple test invoking years-from-duration() on a negative duration. .
   */
  @org.junit.Test
  public void kYearsFromDurationFunc6() {
    final XQuery query = new XQuery(
      "years-from-duration(xs:yearMonthDuration(\"-P0003Y2M\")) eq -3",
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
   *  Simple test invoking years-from-duration() on an negative xs:duration. .
   */
  @org.junit.Test
  public void kYearsFromDurationFunc7() {
    final XQuery query = new XQuery(
      "years-from-duration(xs:duration(\"-P3Y4M4DT1H23M2.34S\")) eq -3",
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
   *  test fn:years-from-duration on xs:dayTimeDuration .
   */
  @org.junit.Test
  public void cbclYearsFromDuration001() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:dayTimeDuration('P1D'))",
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
   *  Evaluates The "years-from-duration" function As per example 1 (for this function)of the F&O specs .
   */
  @org.junit.Test
  public void fnYearsFromDuration1() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"P20Y15M\"))",
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
   *  Evaluates The "years-from-duration" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnYearsFromDuration10() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"P20Y10M\")) * fn:years-from-duration(xs:yearMonthDuration(\"P02Y10M\"))",
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
   *  Evaluates The "years-from-duration" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnYearsFromDuration11() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"P20Y10M\")) div fn:years-from-duration(xs:yearMonthDuration(\"P05Y10M\"))",
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
   *  Evaluates The "years-from-duration" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnYearsFromDuration12() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"P25Y10M\")) idiv fn:years-from-duration(xs:yearMonthDuration(\"P05Y10M\"))",
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
   *  Evaluates The "years-from-duration" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnYearsFromDuration13() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"P10Y10M\")) mod fn:years-from-duration(xs:yearMonthDuration(\"P03Y10M\"))",
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
   *  Evaluates The "years-from-duration" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnYearsFromDuration14() {
    final XQuery query = new XQuery(
      "+fn:years-from-duration(xs:yearMonthDuration(\"P21Y10M\"))",
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
   *  Evaluates The "years-from-duration" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnYearsFromDuration15() {
    final XQuery query = new XQuery(
      "-fn:years-from-duration(xs:yearMonthDuration(\"P25Y10M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-25")
    );
  }

  /**
   *  Evaluates The "years-from-duration" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnYearsFromDuration16() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"P22Y10M\")) eq fn:years-from-duration(xs:yearMonthDuration(\"P22Y10M\"))",
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
   *  Evaluates The "years-from-duration" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnYearsFromDuration17() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"P22Y10M\")) ne fn:years-from-duration(xs:yearMonthDuration(\"P23Y10M\"))",
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
   *  Evaluates The "years-from-duration" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnYearsFromDuration18() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"P21Y01M\")) le fn:years-from-duration(xs:yearMonthDuration(\"P21Y15M\"))",
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
   *  Evaluates The "years-from-duration" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnYearsFromDuration19() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"P20Y10M\")) ge fn:years-from-duration(xs:yearMonthDuration(\"P20Y10M\"))",
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
   *  Evaluates The "years-from-duration" function As per example 2 (for this function) of the F&O specs .
   */
  @org.junit.Test
  public void fnYearsFromDuration2() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"-P15M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Evaluates The "years-from-duration" function with wrong argument type. .
   */
  @org.junit.Test
  public void fnYearsFromDuration20() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:duration(\"P1Y2M3DT10H30M\"))",
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
   *  Evaluates The "years-from-duration" function involving a "numeric-less-than" operation (lt operator) .
   */
  @org.junit.Test
  public void fnYearsFromDuration3() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"P20Y3M\")) lt fn:years-from-duration(xs:yearMonthDuration(\"P21Y2M\"))",
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
   *  Evaluates The "years-from-duration" function involving a "numeric-less-than" operation (le operator) .
   */
  @org.junit.Test
  public void fnYearsFromDuration4() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"P21Y10M\")) le fn:years-from-duration(xs:yearMonthDuration(\"P22Y10M\"))",
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
   *  Evaluates The "years-from-duration" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnYearsFromDuration5() {
    final XQuery query = new XQuery(
      "fn:count(fn:years-from-duration(()))",
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
   *  Evaluates The "years-from-duration" function that returns 1. .
   */
  @org.junit.Test
  public void fnYearsFromDuration6() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"P01Y10M\"))",
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
   *  Evaluates The "years-from-duration" function used as arguments to an avg function. .
   */
  @org.junit.Test
  public void fnYearsFromDuration7() {
    final XQuery query = new XQuery(
      "fn:avg((fn:years-from-duration(xs:yearMonthDuration(\"P23Y10M\")),fn:years-from-duration(xs:yearMonthDuration(\"P21Y10M\"))))",
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
   *  Evaluates The "years-from-duration" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnYearsFromDuration8() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"P21Y10M\")) + fn:years-from-duration(xs:yearMonthDuration(\"P22Y10M\"))",
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
   *  Evaluates The "years-from-duration" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnYearsFromDuration9() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"P30Y10M\")) - fn:years-from-duration(xs:yearMonthDuration(\"P10Y10M\"))",
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
   *  Evaluates The "years-from-duration" function with the arguments set as follows: $arg = xs:yearMonthDuration(lower bound) .
   */
  @org.junit.Test
  public void fnYearsFromDuration1args1() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"P0Y0M\"))",
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
   *  Evaluates The "years-from-duration" function with the arguments set as follows: $arg = xs:yearMonthDuration(mid range) .
   */
  @org.junit.Test
  public void fnYearsFromDuration1args2() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"P1000Y6M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000")
    );
  }

  /**
   *  Evaluates The "years-from-duration" function with the arguments set as follows: $arg = xs:yearMonthDuration(upper bound) .
   */
  @org.junit.Test
  public void fnYearsFromDuration1args3() {
    final XQuery query = new XQuery(
      "fn:years-from-duration(xs:yearMonthDuration(\"P2030Y12M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2031")
    );
  }
}
