package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the fn:floor() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnFloor extends QT3TestSet {

  /**
   *  A test whose essence is: `floor()`. .
   */
  @org.junit.Test
  public void kFloorFunc1() {
    final XQuery query = new XQuery(
      "floor()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `floor(1, 2)`. .
   */
  @org.junit.Test
  public void kFloorFunc2() {
    final XQuery query = new XQuery(
      "floor(1, 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `empty(floor(()))`. .
   */
  @org.junit.Test
  public void kFloorFunc3() {
    final XQuery query = new XQuery(
      "empty(floor(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `floor(10.5) eq 10`. .
   */
  @org.junit.Test
  public void kFloorFunc4() {
    final XQuery query = new XQuery(
      "floor(10.5) eq 10",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `floor(-10.5) eq -11`. .
   */
  @org.junit.Test
  public void kFloorFunc5() {
    final XQuery query = new XQuery(
      "floor(-10.5) eq -11",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `floor("a string")`. .
   */
  @org.junit.Test
  public void kFloorFunc6() {
    final XQuery query = new XQuery(
      "floor(\"a string\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Ensure the return type is properly inferred. .
   */
  @org.junit.Test
  public void k2FloorFunc1() {
    final XQuery query = new XQuery(
      "floor(xs:unsignedShort(.)) instance of xs:integer",
      ctx);
    query.context(node(file("fn/abs/e1.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure the return type is properly inferred for long. .
   */
  @org.junit.Test
  public void k2FloorFunc10() {
    final XQuery query = new XQuery(
      "floor(xs:long(.)) instance of xs:integer",
      ctx);
    query.context(node(file("fn/abs/e0.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure the return type is properly inferred for int. .
   */
  @org.junit.Test
  public void k2FloorFunc11() {
    final XQuery query = new XQuery(
      "floor(xs:int(.)) instance of xs:integer",
      ctx);
    query.context(node(file("fn/abs/e0.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure the return type is properly inferred for short. .
   */
  @org.junit.Test
  public void k2FloorFunc12() {
    final XQuery query = new XQuery(
      "floor(xs:short(.)) instance of xs:integer",
      ctx);
    query.context(node(file("fn/abs/e0.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure the return type is properly inferred for byte. .
   */
  @org.junit.Test
  public void k2FloorFunc13() {
    final XQuery query = new XQuery(
      "floor(xs:byte(.)) instance of xs:integer",
      ctx);
    query.context(node(file("fn/abs/e0.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure the return type is properly inferred for unsignedShort. .
   */
  @org.junit.Test
  public void k2FloorFunc2() {
    final XQuery query = new XQuery(
      "floor(xs:unsignedShort(.)) instance of xs:integer",
      ctx);
    query.context(node(file("fn/abs/e0.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure the return type is properly inferred for unsignedLong. .
   */
  @org.junit.Test
  public void k2FloorFunc3() {
    final XQuery query = new XQuery(
      "floor(xs:unsignedLong(.)) instance of xs:integer",
      ctx);
    query.context(node(file("fn/abs/e0.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure the return type is properly inferred for unsignedInt. .
   */
  @org.junit.Test
  public void k2FloorFunc4() {
    final XQuery query = new XQuery(
      "floor(xs:unsignedInt(.)) instance of xs:integer",
      ctx);
    query.context(node(file("fn/abs/e0.xml")));

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
  public void k2FloorFunc5() {
    final XQuery query = new XQuery(
      "floor(xs:unsignedByte(.)) instance of xs:integer",
      ctx);
    query.context(node(file("fn/abs/e0.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure the return type is properly inferred for positiveInteger. .
   */
  @org.junit.Test
  public void k2FloorFunc6() {
    final XQuery query = new XQuery(
      "floor(xs:positiveInteger(.)) instance of xs:integer",
      ctx);
    query.context(node(file("fn/abs/e1.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure the return type is properly inferred for nonPositiveInteger. .
   */
  @org.junit.Test
  public void k2FloorFunc7() {
    final XQuery query = new XQuery(
      "floor(xs:nonPositiveInteger(.)) instance of xs:integer",
      ctx);
    query.context(node(file("fn/abs/e0.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure the return type is properly inferred for nonNegativeInteger. .
   */
  @org.junit.Test
  public void k2FloorFunc8() {
    final XQuery query = new XQuery(
      "floor(xs:nonNegativeInteger(.)) instance of xs:integer",
      ctx);
    query.context(node(file("fn/abs/e0.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure the return type is properly inferred for negativeInteger. .
   */
  @org.junit.Test
  public void k2FloorFunc9() {
    final XQuery query = new XQuery(
      "floor(xs:negativeInteger(.)) instance of xs:integer",
      ctx);
    query.context(node(file("fn/abs/e-1.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  check dynamic type of fn:floor on argument of union of numeric types. .
   */
  @org.junit.Test
  public void fnFloor1() {
    final XQuery query = new XQuery(
      "for $x in (1, xs:decimal(2), xs:float(3), xs:double(4)) return \n" +
      "           if ((floor($x)) instance of xs:integer) then \"integer\" \n" +
      "           else if ((floor($x)) instance of xs:decimal) then \"decimal\" \n" +
      "           else if ((floor($x)) instance of xs:float) then \"float\"\n" +
      "           else if ((floor($x)) instance of xs:double) then \"double\" \n" +
      "           else error()\n" +
      "        ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"integer\", \"decimal\", \"float\", \"double\"")
    );
  }

  /**
   * Basic test of floor(decimal).
   */
  @org.junit.Test
  public void fnFloorDecimal1() {
    final XQuery query = new XQuery(
      "floor(12.5)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("12.0")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Basic test of floor(decimal).
   */
  @org.junit.Test
  public void fnFloorDecimal2() {
    final XQuery query = new XQuery(
      "floor(12.9)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("12.0")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Basic test of floor(decimal).
   */
  @org.junit.Test
  public void fnFloorDecimal3() {
    final XQuery query = new XQuery(
      "floor(0.000000001)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0.0")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Basic test of floor(decimal).
   */
  @org.junit.Test
  public void fnFloorDecimal4() {
    final XQuery query = new XQuery(
      "floor(0.0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0.0")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Basic test of floor(decimal).
   */
  @org.junit.Test
  public void fnFloorDecimal5() {
    final XQuery query = new XQuery(
      "floor(-0.1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-1.0")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Basic test of floor(decimal).
   */
  @org.junit.Test
  public void fnFloorDecimal6() {
    final XQuery query = new XQuery(
      "floor(-12345678.567890)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-12345679")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Basic test of floor(decimal).
   */
  @org.junit.Test
  public void fnFloorDecimal7() {
    final XQuery query = new XQuery(
      "floor(-1234567891234567.2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-1234567891234568.0")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Basic test of floor(double).
   */
  @org.junit.Test
  public void fnFloorDouble1() {
    final XQuery query = new XQuery(
      "floor(12.5e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("12.0e0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of floor(double).
   */
  @org.junit.Test
  public void fnFloorDouble10() {
    final XQuery query = new XQuery(
      "floor(xs:double('-INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "-INF")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of floor(double).
   */
  @org.junit.Test
  public void fnFloorDouble11() {
    final XQuery query = new XQuery(
      "floor(xs:double('-0'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "-0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of floor(double).
   */
  @org.junit.Test
  public void fnFloorDouble2() {
    final XQuery query = new XQuery(
      "floor(12.9e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("12.0e0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of floor(double).
   */
  @org.junit.Test
  public void fnFloorDouble3() {
    final XQuery query = new XQuery(
      "floor(0.000000001e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0.0e0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of floor(double).
   */
  @org.junit.Test
  public void fnFloorDouble4() {
    final XQuery query = new XQuery(
      "floor(0.0e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0.0e0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of floor(double).
   */
  @org.junit.Test
  public void fnFloorDouble5() {
    final XQuery query = new XQuery(
      "floor(-0.1e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-1.0e0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of floor(double).
   */
  @org.junit.Test
  public void fnFloorDouble6() {
    final XQuery query = new XQuery(
      "floor(-12345678.567890e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-12345679e0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of floor(double).
   */
  @org.junit.Test
  public void fnFloorDouble7() {
    final XQuery query = new XQuery(
      "floor(-1234567891234567.2e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-1234567891234568.0e0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of floor(double).
   */
  @org.junit.Test
  public void fnFloorDouble8() {
    final XQuery query = new XQuery(
      "floor(xs:double('NaN'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "NaN")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of floor(double).
   */
  @org.junit.Test
  public void fnFloorDouble9() {
    final XQuery query = new XQuery(
      "floor(xs:double('INF'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "INF")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of floor(float)).
   */
  @org.junit.Test
  public void fnFloorFloat1() {
    final XQuery query = new XQuery(
      "floor(xs:float(12.5e0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("12.0e0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of floor(float)).
   */
  @org.junit.Test
  public void fnFloorFloat10() {
    final XQuery query = new XQuery(
      "floor(xs:float(xs:float('-INF')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "-INF")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of floor(float)).
   */
  @org.junit.Test
  public void fnFloorFloat11() {
    final XQuery query = new XQuery(
      "floor(xs:float(xs:float('-0')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "-0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of floor(float)).
   */
  @org.junit.Test
  public void fnFloorFloat2() {
    final XQuery query = new XQuery(
      "floor(xs:float(12.9e0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("12.0e0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of floor(float)).
   */
  @org.junit.Test
  public void fnFloorFloat3() {
    final XQuery query = new XQuery(
      "floor(xs:float(0.000000001e0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0.0e0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of floor(float)).
   */
  @org.junit.Test
  public void fnFloorFloat4() {
    final XQuery query = new XQuery(
      "floor(xs:float(0.0e0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0.0e0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of floor(float)).
   */
  @org.junit.Test
  public void fnFloorFloat5() {
    final XQuery query = new XQuery(
      "floor(xs:float(-0.1e0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-1.0e0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of floor(float)).
   */
  @org.junit.Test
  public void fnFloorFloat6() {
    final XQuery query = new XQuery(
      "floor(xs:float(-12345678.567890e0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-12345679e0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of floor(float)).
   */
  @org.junit.Test
  public void fnFloorFloat7() {
    final XQuery query = new XQuery(
      "floor(xs:float(-1234567.2e0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("-1234568e0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of floor(float)).
   */
  @org.junit.Test
  public void fnFloorFloat8() {
    final XQuery query = new XQuery(
      "floor(xs:float(xs:float('NaN')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "NaN")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of floor(float)).
   */
  @org.junit.Test
  public void fnFloorFloat9() {
    final XQuery query = new XQuery(
      "floor(xs:float(xs:float('INF')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "INF")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnFloordbl1args1() {
    final XQuery query = new XQuery(
      "fn:floor(xs:double(\"-1.7976931348623157E308\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnFloordbl1args2() {
    final XQuery query = new XQuery(
      "fn:floor(xs:double(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnFloordbl1args3() {
    final XQuery query = new XQuery(
      "fn:floor(xs:double(\"1.7976931348623157E308\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnFloordec1args1() {
    final XQuery query = new XQuery(
      "fn:floor(xs:decimal(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-999999999999999999")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnFloordec1args2() {
    final XQuery query = new XQuery(
      "fn:floor(xs:decimal(\"617375191608514839\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "617375191608514839")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnFloordec1args3() {
    final XQuery query = new XQuery(
      "fn:floor(xs:decimal(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "999999999999999999")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnFloorflt1args1() {
    final XQuery query = new XQuery(
      "fn:floor(xs:float(\"-3.4028235E38\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-3.4028235E38")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnFloorflt1args2() {
    final XQuery query = new XQuery(
      "fn:floor(xs:float(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnFloorflt1args3() {
    final XQuery query = new XQuery(
      "fn:floor(xs:float(\"3.4028235E38\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "3.4028235E38")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnFloorint1args1() {
    final XQuery query = new XQuery(
      "fn:floor(xs:int(\"-2147483648\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-2147483648")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnFloorint1args2() {
    final XQuery query = new XQuery(
      "fn:floor(xs:int(\"-1873914410\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-1873914410")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnFloorint1args3() {
    final XQuery query = new XQuery(
      "fn:floor(xs:int(\"2147483647\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2147483647")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnFloorintg1args1() {
    final XQuery query = new XQuery(
      "fn:floor(xs:integer(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-999999999999999999")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnFloorintg1args2() {
    final XQuery query = new XQuery(
      "fn:floor(xs:integer(\"830993497117024304\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "830993497117024304")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnFloorintg1args3() {
    final XQuery query = new XQuery(
      "fn:floor(xs:integer(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "999999999999999999")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnFloorlng1args1() {
    final XQuery query = new XQuery(
      "fn:floor(xs:long(\"-92233720368547758\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-92233720368547758")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnFloorlng1args2() {
    final XQuery query = new XQuery(
      "fn:floor(xs:long(\"-47175562203048468\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-47175562203048468")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnFloorlng1args3() {
    final XQuery query = new XQuery(
      "fn:floor(xs:long(\"92233720368547758\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "92233720368547758")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnFloornint1args1() {
    final XQuery query = new XQuery(
      "fn:floor(xs:negativeInteger(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-999999999999999999")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnFloornint1args2() {
    final XQuery query = new XQuery(
      "fn:floor(xs:negativeInteger(\"-297014075999096793\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-297014075999096793")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnFloornint1args3() {
    final XQuery query = new XQuery(
      "fn:floor(xs:negativeInteger(\"-1\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-1")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnFloornni1args1() {
    final XQuery query = new XQuery(
      "fn:floor(xs:nonNegativeInteger(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnFloornni1args2() {
    final XQuery query = new XQuery(
      "fn:floor(xs:nonNegativeInteger(\"303884545991464527\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "303884545991464527")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnFloornni1args3() {
    final XQuery query = new XQuery(
      "fn:floor(xs:nonNegativeInteger(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "999999999999999999")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnFloornpi1args1() {
    final XQuery query = new XQuery(
      "fn:floor(xs:nonPositiveInteger(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-999999999999999999")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnFloornpi1args2() {
    final XQuery query = new XQuery(
      "fn:floor(xs:nonPositiveInteger(\"-475688437271870490\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-475688437271870490")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnFloornpi1args3() {
    final XQuery query = new XQuery(
      "fn:floor(xs:nonPositiveInteger(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnFloorpint1args1() {
    final XQuery query = new XQuery(
      "fn:floor(xs:positiveInteger(\"1\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnFloorpint1args2() {
    final XQuery query = new XQuery(
      "fn:floor(xs:positiveInteger(\"52704602390610033\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "52704602390610033")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnFloorpint1args3() {
    final XQuery query = new XQuery(
      "fn:floor(xs:positiveInteger(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "999999999999999999")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnFloorsht1args1() {
    final XQuery query = new XQuery(
      "fn:floor(xs:short(\"-32768\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-32768")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnFloorsht1args2() {
    final XQuery query = new XQuery(
      "fn:floor(xs:short(\"-5324\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-5324")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnFloorsht1args3() {
    final XQuery query = new XQuery(
      "fn:floor(xs:short(\"32767\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "32767")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnFloorulng1args1() {
    final XQuery query = new XQuery(
      "fn:floor(xs:unsignedLong(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnFloorulng1args2() {
    final XQuery query = new XQuery(
      "fn:floor(xs:unsignedLong(\"130747108607674654\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "130747108607674654")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnFloorulng1args3() {
    final XQuery query = new XQuery(
      "fn:floor(xs:unsignedLong(\"184467440737095516\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "184467440737095516")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnFloorusht1args1() {
    final XQuery query = new XQuery(
      "fn:floor(xs:unsignedShort(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnFloorusht1args2() {
    final XQuery query = new XQuery(
      "fn:floor(xs:unsignedShort(\"44633\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "44633")
    );
  }

  /**
   *  Evaluates The "floor" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnFloorusht1args3() {
    final XQuery query = new XQuery(
      "fn:floor(xs:unsignedShort(\"65535\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "65535")
    );
  }
}
