package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the months-from-duration() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnMonthsFromDuration extends QT3TestSet {

  /**
   *  A test whose essence is: `months-from-duration()`. .
   */
  @org.junit.Test
  public void kMonthsFromDurationFunc1() {
    final XQuery query = new XQuery(
      "months-from-duration()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `months-from-duration((), "Wrong param")`. .
   */
  @org.junit.Test
  public void kMonthsFromDurationFunc2() {
    final XQuery query = new XQuery(
      "months-from-duration((), \"Wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `empty(months-from-duration(()))`. .
   */
  @org.junit.Test
  public void kMonthsFromDurationFunc3() {
    final XQuery query = new XQuery(
      "empty(months-from-duration(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `months-from-duration(()) instance of xs:integer?`. .
   */
  @org.junit.Test
  public void kMonthsFromDurationFunc4() {
    final XQuery query = new XQuery(
      "months-from-duration(()) instance of xs:integer?",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `months-from-duration(xs:yearMonthDuration("P0003Y2M")) eq 2`. .
   */
  @org.junit.Test
  public void kMonthsFromDurationFunc5() {
    final XQuery query = new XQuery(
      "months-from-duration(xs:yearMonthDuration(\"P0003Y2M\")) eq 2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test invoking months-from-duration() on a negative duration. .
   */
  @org.junit.Test
  public void kMonthsFromDurationFunc6() {
    final XQuery query = new XQuery(
      "months-from-duration(xs:yearMonthDuration(\"-P0003Y2M\")) eq -2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test invoking months-from-duration() on an negative xs:duration. .
   */
  @org.junit.Test
  public void kMonthsFromDurationFunc7() {
    final XQuery query = new XQuery(
      "months-from-duration(xs:duration(\"-P3Y4M4DT1H23M2.34S\")) eq -4",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "months-from-duration" function As per example 1 (for this function)of the F&O specs .
   */
  @org.junit.Test
  public void fnMonthsFromDuration1() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"P20Y15M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("3")
    );
  }

  /**
   *  Evaluates The "months-from-duration" function as part of a "*" expression. .
   */
  @org.junit.Test
  public void fnMonthsFromDuration10() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"P20Y09M\")) * fn:months-from-duration(xs:yearMonthDuration(\"P02Y10M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("90")
    );
  }

  /**
   *  Evaluates The "months-from-duration" function as part of a "div" expression. .
   */
  @org.junit.Test
  public void fnMonthsFromDuration11() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"P20Y10M\")) div fn:months-from-duration(xs:yearMonthDuration(\"P05Y05M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("2")
    );
  }

  /**
   *  Evaluates The "months-from-duration" function as part of a "idiv" expression. .
   */
  @org.junit.Test
  public void fnMonthsFromDuration12() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"P25Y10M\")) idiv fn:months-from-duration(xs:yearMonthDuration(\"P05Y02M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("5")
    );
  }

  /**
   *  Evaluates The "months-from-duration" function as part of a "mod" expression. .
   */
  @org.junit.Test
  public void fnMonthsFromDuration13() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"P10Y10M\")) mod fn:months-from-duration(xs:yearMonthDuration(\"P03Y03M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "months-from-duration" function as part of a "numeric-unary-plus" expression. .
   */
  @org.junit.Test
  public void fnMonthsFromDuration14() {
    final XQuery query = new XQuery(
      "+fn:months-from-duration(xs:yearMonthDuration(\"P21Y10M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("10")
    );
  }

  /**
   *  Evaluates The "months-from-duration" function as part of a "numeric-unary-minus" expression. .
   */
  @org.junit.Test
  public void fnMonthsFromDuration15() {
    final XQuery query = new XQuery(
      "-fn:months-from-duration(xs:yearMonthDuration(\"P25Y03M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-3")
    );
  }

  /**
   *  Evaluates The "months-from-duration" function as part of a "numeric-equal" expression (eq operator) .
   */
  @org.junit.Test
  public void fnMonthsFromDuration16() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"P22Y10M\")) eq fn:months-from-duration(xs:yearMonthDuration(\"P22Y09M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "months-from-duration" function as part of a "numeric-equal" expression (ne operator) .
   */
  @org.junit.Test
  public void fnMonthsFromDuration17() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"P22Y10M\")) ne fn:months-from-duration(xs:yearMonthDuration(\"P23Y10M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "months-from-duration" function as part of a "numeric-equal" expression (le operator) .
   */
  @org.junit.Test
  public void fnMonthsFromDuration18() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"P21Y01M\")) le fn:months-from-duration(xs:yearMonthDuration(\"P21Y15M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "months-from-duration" function as part of a "numeric-equal" expression (ge operator) .
   */
  @org.junit.Test
  public void fnMonthsFromDuration19() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"P20Y09M\")) ge fn:months-from-duration(xs:yearMonthDuration(\"P20Y01M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "months-from-duration" function As per example 2 (for this function) of the F&O specs .
   */
  @org.junit.Test
  public void fnMonthsFromDuration2() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"-P20Y18M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-6")
    );
  }

  /**
   *  Evaluates The "months-from-duration" function with wrong argument type. .
   */
  @org.junit.Test
  public void fnMonthsFromDuration20() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:duration(\"P1Y2M3DT10H30M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("2")
    );
  }

  /**
   *  Evaluates The "months-from-duration" function involving a "numeric-less-than" operation (lt operator) .
   */
  @org.junit.Test
  public void fnMonthsFromDuration3() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"P20Y3M\")) lt fn:months-from-duration(xs:yearMonthDuration(\"P21Y2M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "months-from-duration" function involving a "numeric-less-than" operation (le operator) .
   */
  @org.junit.Test
  public void fnMonthsFromDuration4() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"P21Y10M\")) le fn:months-from-duration(xs:yearMonthDuration(\"P22Y10M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "months-from-duration" function using the empty sequence as an argument. Use count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnMonthsFromDuration5() {
    final XQuery query = new XQuery(
      "fn:count(fn:months-from-duration(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "months-from-duration" function that returns 1. .
   */
  @org.junit.Test
  public void fnMonthsFromDuration6() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"P01Y01M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "months-from-duration" function used as arguments to an avg function. .
   */
  @org.junit.Test
  public void fnMonthsFromDuration7() {
    final XQuery query = new XQuery(
      "fn:avg((fn:months-from-duration(xs:yearMonthDuration(\"P23Y10M\")),fn:months-from-duration(xs:yearMonthDuration(\"P21Y10M\"))))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("10")
    );
  }

  /**
   *  Evaluates The "months-from-duration" function as part of a "+" expression. .
   */
  @org.junit.Test
  public void fnMonthsFromDuration8() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"P21Y10M\")) + fn:months-from-duration(xs:yearMonthDuration(\"P22Y11M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("21")
    );
  }

  /**
   *  Evaluates The "months-from-duration" function as part of a "-" expression. .
   */
  @org.junit.Test
  public void fnMonthsFromDuration9() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"P30Y10M\")) - fn:months-from-duration(xs:yearMonthDuration(\"P10Y09M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "months-from-duration" function with the arguments set as follows: $arg = xs:yearMonthDuration(lower bound) .
   */
  @org.junit.Test
  public void fnMonthsFromDuration1args1() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"P0Y0M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "months-from-duration" function with the arguments set as follows: $arg = xs:yearMonthDuration(mid range) .
   */
  @org.junit.Test
  public void fnMonthsFromDuration1args2() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"P1000Y6M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("6")
    );
  }

  /**
   *  Evaluates The "months-from-duration" function with the arguments set as follows: $arg = xs:yearMonthDuration(upper bound) .
   */
  @org.junit.Test
  public void fnMonthsFromDuration1args3() {
    final XQuery query = new XQuery(
      "fn:months-from-duration(xs:yearMonthDuration(\"P2030Y12M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }
}
