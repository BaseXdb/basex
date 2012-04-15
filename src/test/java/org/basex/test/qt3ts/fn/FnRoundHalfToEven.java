package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the round-half-to-even() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnRoundHalfToEven extends QT3TestSet {

  /**
   *  A test whose essence is: `round-half-to-even()`. .
   */
  @org.junit.Test
  public void kRoundEvenFunc1() {
    final XQuery query = new XQuery(
      "round-half-to-even()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `round-half-to-even(1.1, 3, "wrong param")`. .
   */
  @org.junit.Test
  public void kRoundEvenFunc2() {
    final XQuery query = new XQuery(
      "round-half-to-even(1.1, 3, \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `empty(round-half-to-even(()))`. .
   */
  @org.junit.Test
  public void kRoundEvenFunc3() {
    final XQuery query = new XQuery(
      "empty(round-half-to-even(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(round-half-to-even((), 3))`. .
   */
  @org.junit.Test
  public void kRoundEvenFunc4() {
    final XQuery query = new XQuery(
      "empty(round-half-to-even((), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `round-half-to-even("a string")`. .
   */
  @org.junit.Test
  public void kRoundEvenFunc5() {
    final XQuery query = new XQuery(
      "round-half-to-even(\"a string\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Invoked fn:round-half-to-even() on 1. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc1() {
    final XQuery query = new XQuery(
      "round-half-to-even(1) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoked fn:round-half-to-even() on xs:float -0. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc10() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:float(\"-0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-0")
    );
  }

  /**
   *  Invoked fn:round-half-to-even() on xs:double NaN. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc11() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:double(\"NaN\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  Invoked fn:round-half-to-even() on xs:float NaN. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc12() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:float(\"NaN\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  Invoked fn:round-half-to-even() on xs:double -INF. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc13() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:double(\"-INF\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-INF")
    );
  }

  /**
   *  Invoked fn:round-half-to-even() on xs:float -INF. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc14() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:float(\"-INF\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-INF")
    );
  }

  /**
   *  Invoked fn:round-half-to-even() on xs:double INF. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc15() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:double(\"INF\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "INF")
    );
  }

  /**
   *  Invoked fn:round-half-to-even() on xs:float INF. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc16() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:float(\"INF\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "INF")
    );
  }

  /**
   *  Ensure the return type is properly inferred for unsignedShort. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc17() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:unsignedShort(\"0\")) instance of xs:unsignedShort",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Ensure the return type is properly inferred for unsignedLong. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc18() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:unsignedLong(\"0\")) instance of xs:unsignedLong",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Ensure the return type is properly inferred for unsignedInt. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc19() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:unsignedInt(\"0\")) instance of xs:unsignedInt",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Invoked fn:round-half-to-even() on 1.0. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc2() {
    final XQuery query = new XQuery(
      "round-half-to-even(1.0) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure the return type is properly inferred for unsignedByte. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc20() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:unsignedByte(\"0\")) instance of xs:unsignedByte",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Ensure the return type is properly inferred for positiveInteger. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc21() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:positiveInteger(\"1\")) instance of xs:positiveInteger",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Ensure the return type is properly inferred for nonPositiveInteger. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc22() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:nonPositiveInteger(\"0\")) instance of xs:nonPositiveInteger",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Ensure the return type is properly inferred for nonNegativeInteger. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc23() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:nonNegativeInteger(\"0\")) instance of xs:nonNegativeInteger",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Ensure the return type is properly inferred for negativeInteger. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc24() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:negativeInteger(\"-1\")) instance of xs:negativeInteger",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Ensure the return type is properly inferred for long. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc25() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:long(\"0\")) instance of xs:long",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Ensure the return type is properly inferred for int. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc26() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:int(\"0\")) instance of xs:int",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Ensure the return type is properly inferred for short. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc27() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:short(\"0\")) instance of xs:short",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Ensure the return type is properly inferred for byte. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc28() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:byte(\"0\")) instance of xs:byte",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Invoked fn:round-half-to-even() on 0.5. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc3() {
    final XQuery query = new XQuery(
      "round-half-to-even(0.5) eq 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoked fn:round-half-to-even() on 1.5. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc4() {
    final XQuery query = new XQuery(
      "round-half-to-even(1.5) eq 2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoked fn:round-half-to-even() on 2.5. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc5() {
    final XQuery query = new XQuery(
      "round-half-to-even(2.5) eq 2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoked fn:round-half-to-even() on a large double. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc6() {
    final XQuery query = new XQuery(
      "round-half-to-even(3.567812E+3, 2) eq 3567.81E0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoked fn:round-half-to-even() on a large double with 2 in precision. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc7() {
    final XQuery query = new XQuery(
      "round-half-to-even(4.7564E-3, 2) eq 0.0E0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoked fn:round-half-to-even() on a large double with -2 in precision. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc8() {
    final XQuery query = new XQuery(
      "round-half-to-even(35612.25, -2) eq 35600",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoked fn:round-half-to-even() on xs:double -0. .
   */
  @org.junit.Test
  public void k2RoundEvenFunc9() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:double(\"-0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-0")
    );
  }

  /**
   *  check dynamic type of fn:round-half-to-even on argument of union of numeric types. .
   */
  @org.junit.Test
  public void fnRoundHalfToEven1() {
    final XQuery query = new XQuery(
      "for $x in (1, xs:decimal(2), xs:float(3), xs:double(4)) \n" +
      "            return if ((round-half-to-even($x)) instance of xs:integer) then \"integer\" \n" +
      "           else if ((round-half-to-even($x)) instance of xs:decimal) then \"decimal\" \n" +
      "           else if ((round-half-to-even($x)) instance of xs:float) then \"float\"\n" +
      "           else if ((round-half-to-even($x)) instance of xs:double) then \"double\" \n" +
      "           else error()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"integer\", \"decimal\", \"float\", \"double\"")
    );
  }

  /**
   * Zero second argument - decimal.
   */
  @org.junit.Test
  public void fnRoundHalfToEven10() {
    final XQuery query = new XQuery(
      "round-half-to-even(4561.234567, 0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("4561")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Zero second argument - double.
   */
  @org.junit.Test
  public void fnRoundHalfToEven11() {
    final XQuery query = new XQuery(
      "round-half-to-even(4561.000005e0, 0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("4561")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Zero second argument - float.
   */
  @org.junit.Test
  public void fnRoundHalfToEven12() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:float('4561.000005e0'), 0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("4561")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Zero second argument - integer.
   */
  @org.junit.Test
  public void fnRoundHalfToEven13() {
    final XQuery query = new XQuery(
      "round-half-to-even(4561234567, 0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("4561234567")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Positive second argument - halfway case - float. The expected result is surprising; it arises because 0.05 is not exactly representable as an xs:float.
   */
  @org.junit.Test
  public void fnRoundHalfToEven14() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:float('0.05'), 1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0.1")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Positive second argument - halfway case - float. The expected result is surprising; it arises because -0.05 is not exactly representable as an xs:float.
   */
  @org.junit.Test
  public void fnRoundHalfToEven15() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:float('-0.05'), 1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-0.1")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Positive second argument - halfway case - float.
   */
  @org.junit.Test
  public void fnRoundHalfToEven16() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:float('3.75'), 1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("3.8")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Positive second argument - halfway case - decimal.
   */
  @org.junit.Test
  public void fnRoundHalfToEven17() {
    final XQuery query = new XQuery(
      "round-half-to-even(123.355, 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("123.36")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Positive second argument - halfway case - decimal.
   */
  @org.junit.Test
  public void fnRoundHalfToEven18() {
    final XQuery query = new XQuery(
      "round-half-to-even(123.365, 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("123.36")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Positive second argument - halfway case - decimal.
   */
  @org.junit.Test
  public void fnRoundHalfToEven19() {
    final XQuery query = new XQuery(
      "round-half-to-even(123.375, 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("123.38")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   *  check dynamic type of fn:round-half-to-even on argument of union of numeric types. .
   */
  @org.junit.Test
  public void fnRoundHalfToEven2() {
    final XQuery query = new XQuery(
      "for $x in (1, xs:decimal(2), xs:float(3), xs:double(4)) \n" +
      "            return if ((round-half-to-even($x,1)) instance of xs:integer) then \"integer\" \n" +
      "           else if ((round-half-to-even($x,1)) instance of xs:decimal) then \"decimal\" \n" +
      "           else if ((round-half-to-even($x,1)) instance of xs:float) then \"float\"\n" +
      "           else if ((round-half-to-even($x,1)) instance of xs:double) then \"double\" \n" +
      "           else error()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"integer\", \"decimal\", \"float\", \"double\"")
    );
  }

  /**
   * Positive second argument - halfway case - decimal.
   */
  @org.junit.Test
  public void fnRoundHalfToEven20() {
    final XQuery query = new XQuery(
      "round-half-to-even(123.385, 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("123.38")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Positive second argument - halfway case - decimal.
   */
  @org.junit.Test
  public void fnRoundHalfToEven21() {
    final XQuery query = new XQuery(
      "round-half-to-even(-123.355, 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-123.36")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Positive second argument - halfway case - decimal.
   */
  @org.junit.Test
  public void fnRoundHalfToEven22() {
    final XQuery query = new XQuery(
      "round-half-to-even(-123.365, 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-123.36")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Positive second argument - halfway case - decimal.
   */
  @org.junit.Test
  public void fnRoundHalfToEven23() {
    final XQuery query = new XQuery(
      "round-half-to-even(-123.375, 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-123.38")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Positive second argument - halfway case - decimal.
   */
  @org.junit.Test
  public void fnRoundHalfToEven24() {
    final XQuery query = new XQuery(
      "round-half-to-even(-123.385, 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-123.38")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Negative second argument - halfway case - decimal.
   */
  @org.junit.Test
  public void fnRoundHalfToEven24a() {
    final XQuery query = new XQuery(
      "round-half-to-even(12350.00, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("12400")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Negative second argument - halfway case - decimal.
   */
  @org.junit.Test
  public void fnRoundHalfToEven25() {
    final XQuery query = new XQuery(
      "round-half-to-even(-12350.00, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-12400")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Negative second argument - halfway case - decimal.
   */
  @org.junit.Test
  public void fnRoundHalfToEven26() {
    final XQuery query = new XQuery(
      "round-half-to-even(12450.00, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("12400")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Negative second argument - halfway case - decimal.
   */
  @org.junit.Test
  public void fnRoundHalfToEven27() {
    final XQuery query = new XQuery(
      "round-half-to-even(-12450.00, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-12400")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Negative second argument - halfway case - decimal.
   */
  @org.junit.Test
  public void fnRoundHalfToEven28() {
    final XQuery query = new XQuery(
      "round-half-to-even(12550.00, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("12600")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Negative second argument - halfway case - decimal.
   */
  @org.junit.Test
  public void fnRoundHalfToEven29() {
    final XQuery query = new XQuery(
      "round-half-to-even(-12550.00, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-12600")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Positive second argument - decimal.
   */
  @org.junit.Test
  public void fnRoundHalfToEven3() {
    final XQuery query = new XQuery(
      "round-half-to-even(1.234567, 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("1.23")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Negative second argument - halfway case - integer.
   */
  @org.junit.Test
  public void fnRoundHalfToEven30() {
    final XQuery query = new XQuery(
      "round-half-to-even(12350, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("12400")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Negative second argument - halfway case - integer.
   */
  @org.junit.Test
  public void fnRoundHalfToEven31() {
    final XQuery query = new XQuery(
      "round-half-to-even(-12350, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-12400")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Negative second argument - halfway case - integer.
   */
  @org.junit.Test
  public void fnRoundHalfToEven32() {
    final XQuery query = new XQuery(
      "round-half-to-even(12450, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("12400")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Negative second argument - halfway case - integer.
   */
  @org.junit.Test
  public void fnRoundHalfToEven33() {
    final XQuery query = new XQuery(
      "round-half-to-even(-12450, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-12400")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Negative second argument - halfway case - integer.
   */
  @org.junit.Test
  public void fnRoundHalfToEven34() {
    final XQuery query = new XQuery(
      "round-half-to-even(12550, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("12600")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Negative second argument - halfway case - integer.
   */
  @org.junit.Test
  public void fnRoundHalfToEven35() {
    final XQuery query = new XQuery(
      "round-half-to-even(-12550, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-12600")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Negative second argument - halfway case - double.
   */
  @org.junit.Test
  public void fnRoundHalfToEven36() {
    final XQuery query = new XQuery(
      "round-half-to-even(12350e0, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("12400")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Negative second argument - halfway case - double.
   */
  @org.junit.Test
  public void fnRoundHalfToEven37() {
    final XQuery query = new XQuery(
      "round-half-to-even(-12350e0, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-12400")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Negative second argument - halfway case - double.
   */
  @org.junit.Test
  public void fnRoundHalfToEven38() {
    final XQuery query = new XQuery(
      "round-half-to-even(12450e0, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("12400")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Negative second argument - halfway case - double.
   */
  @org.junit.Test
  public void fnRoundHalfToEven39() {
    final XQuery query = new XQuery(
      "round-half-to-even(-12450e0, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-12400")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Positive second argument - double.
   */
  @org.junit.Test
  public void fnRoundHalfToEven4() {
    final XQuery query = new XQuery(
      "round-half-to-even(1.000005e0, 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("1")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Negative second argument - halfway case - double.
   */
  @org.junit.Test
  public void fnRoundHalfToEven40() {
    final XQuery query = new XQuery(
      "round-half-to-even(12550e0, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("12600")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Negative second argument - halfway case - double.
   */
  @org.junit.Test
  public void fnRoundHalfToEven41() {
    final XQuery query = new XQuery(
      "round-half-to-even(-12550e0, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-12600")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Positive second argument - float.
   */
  @org.junit.Test
  public void fnRoundHalfToEven5() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:float('1.000005e0'), 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("1")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Negative second argument - decimal.
   */
  @org.junit.Test
  public void fnRoundHalfToEven6() {
    final XQuery query = new XQuery(
      "round-half-to-even(4561.234567, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("4600")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Negative second argument - double.
   */
  @org.junit.Test
  public void fnRoundHalfToEven7() {
    final XQuery query = new XQuery(
      "round-half-to-even(4561.000005e0, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("4600")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Negative second argument - float.
   */
  @org.junit.Test
  public void fnRoundHalfToEven8() {
    final XQuery query = new XQuery(
      "round-half-to-even(xs:float('4561.000005e0'), -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("4600")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Negative second argument - integer.
   */
  @org.junit.Test
  public void fnRoundHalfToEven9() {
    final XQuery query = new XQuery(
      "round-half-to-even(4561234567, -2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("4561234600")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvendbl1args1() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:double(\"-1.7976931348623157E308\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-1.7976931348623157E308")
      ||
        error("FOCA0001")
      )
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvendbl1args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:double(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvendbl1args3() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:double(\"1.7976931348623157E308\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("1.7976931348623157E308")
      ||
        error("FOCA0001")
      )
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvendec1args1() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:decimal(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvendec1args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:decimal(\"617375191608514839\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("617375191608514839")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvendec1args3() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:decimal(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenflt1args1() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:float(\"-3.4028235E38\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("xs:float(\"-3.4028235E38\")")
      ||
        error("FOCA0001")
      )
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenflt1args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:float(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenflt1args3() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:float(\"3.4028235E38\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("xs:float(\"3.4028235E38\")")
      ||
        error("FOCA0001")
      )
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenint1args1() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:int(\"-2147483648\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-2147483648")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenint1args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:int(\"-1873914410\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1873914410")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenint1args3() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:int(\"2147483647\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("2147483647")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenintg1args1() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:integer(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenintg1args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:integer(\"830993497117024304\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("830993497117024304")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenintg1args3() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:integer(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenlng1args1() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:long(\"-92233720368547758\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenlng1args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:long(\"-47175562203048468\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-47175562203048468")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenlng1args3() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:long(\"92233720368547758\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("92233720368547758")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvennint1args1() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:negativeInteger(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvennint1args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:negativeInteger(\"-297014075999096793\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-297014075999096793")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvennint1args3() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:negativeInteger(\"-1\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-1")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvennni1args1() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:nonNegativeInteger(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvennni1args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:nonNegativeInteger(\"303884545991464527\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("303884545991464527")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvennni1args3() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:nonNegativeInteger(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvennpi1args1() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:nonPositiveInteger(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvennpi1args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:nonPositiveInteger(\"-475688437271870490\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-475688437271870490")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvennpi1args3() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:nonPositiveInteger(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenpint1args1() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:positiveInteger(\"1\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenpint1args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:positiveInteger(\"52704602390610033\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("52704602390610033")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenpint1args3() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:positiveInteger(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvensht1args1() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:short(\"-32768\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvensht1args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:short(\"-5324\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-5324")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvensht1args3() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:short(\"32767\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("32767")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenulng1args1() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:unsignedLong(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenulng1args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:unsignedLong(\"130747108607674654\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("130747108607674654")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenulng1args3() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:unsignedLong(\"184467440737095516\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("184467440737095516")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenusht1args1() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:unsignedShort(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenusht1args2() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:unsignedShort(\"44633\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("44633")
    );
  }

  /**
   *  Evaluates The "round-half-to-even" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnRoundHalfToEvenusht1args3() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even(xs:unsignedShort(\"65535\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("65535")
    );
  }
}
