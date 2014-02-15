package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnCeiling extends QT3TestSet {

  /**
   *  A test whose essence is: `ceiling()`. .
   */
  @org.junit.Test
  public void kCeilingFunc1() {
    final XQuery query = new XQuery(
      "ceiling()",
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
   *  A test whose essence is: `ceiling(1, 2)`. .
   */
  @org.junit.Test
  public void kCeilingFunc2() {
    final XQuery query = new XQuery(
      "ceiling(1, 2)",
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
   *  A test whose essence is: `empty(ceiling(()))`. .
   */
  @org.junit.Test
  public void kCeilingFunc3() {
    final XQuery query = new XQuery(
      "empty(ceiling(()))",
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
   *  A test whose essence is: `ceiling(10.5) eq 11`. .
   */
  @org.junit.Test
  public void kCeilingFunc4() {
    final XQuery query = new XQuery(
      "ceiling(10.5) eq 11",
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
   *  A test whose essence is: `ceiling(-10.5) eq -10`. .
   */
  @org.junit.Test
  public void kCeilingFunc5() {
    final XQuery query = new XQuery(
      "ceiling(-10.5) eq -10",
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
   *  A test whose essence is: `ceiling("a string")`. .
   */
  @org.junit.Test
  public void kCeilingFunc6() {
    final XQuery query = new XQuery(
      "ceiling(\"a string\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Ensure the return type is properly inferred for unsignedShort. .
   */
  @org.junit.Test
  public void k2CeilingFunc1() {
    final XQuery query = new XQuery(
      "ceiling(xs:unsignedShort(.)) instance of xs:integer",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
   *  Ensure the return type is properly inferred for int. .
   */
  @org.junit.Test
  public void k2CeilingFunc10() {
    final XQuery query = new XQuery(
      "ceiling(xs:int(.)) instance of xs:integer",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
   *  Ensure the return type is properly inferred for short. .
   */
  @org.junit.Test
  public void k2CeilingFunc11() {
    final XQuery query = new XQuery(
      "ceiling(xs:short(.)) instance of xs:integer",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
   *  Ensure the return type is properly inferred for byte. .
   */
  @org.junit.Test
  public void k2CeilingFunc12() {
    final XQuery query = new XQuery(
      "ceiling(xs:byte(.)) instance of xs:integer",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
   *  Ensure the return type is properly inferred for unsignedLong. .
   */
  @org.junit.Test
  public void k2CeilingFunc2() {
    final XQuery query = new XQuery(
      "ceiling(xs:unsignedLong(.)) instance of xs:integer",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
   *  Ensure the return type is properly inferred for unsignedInt. .
   */
  @org.junit.Test
  public void k2CeilingFunc3() {
    final XQuery query = new XQuery(
      "ceiling(xs:unsignedInt(.)) instance of xs:integer",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
   *  Ensure the return type is properly inferred for unsignedByte. .
   */
  @org.junit.Test
  public void k2CeilingFunc4() {
    final XQuery query = new XQuery(
      "ceiling(xs:unsignedByte(.)) instance of xs:integer",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
   *  Ensure the return type is properly inferred for positiveInteger. .
   */
  @org.junit.Test
  public void k2CeilingFunc5() {
    final XQuery query = new XQuery(
      "ceiling(xs:positiveInteger(.)) instance of xs:integer",
      ctx);
    try {
      query.context(node(file("fn/abs/e1.xml")));
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
   *  Ensure the return type is properly inferred for nonPositiveInteger. .
   */
  @org.junit.Test
  public void k2CeilingFunc6() {
    final XQuery query = new XQuery(
      "ceiling(xs:nonPositiveInteger(.)) instance of xs:integer",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
   *  Ensure the return type is properly inferred for nonNegativeInteger. .
   */
  @org.junit.Test
  public void k2CeilingFunc7() {
    final XQuery query = new XQuery(
      "ceiling(xs:nonNegativeInteger(.)) instance of xs:integer",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
   *  Ensure the return type is properly inferred for negativeInteger. .
   */
  @org.junit.Test
  public void k2CeilingFunc8() {
    final XQuery query = new XQuery(
      "ceiling(xs:negativeInteger(.)) instance of xs:integer",
      ctx);
    try {
      query.context(node(file("fn/abs/e-1.xml")));
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
   *  Ensure the return type is properly inferred for long. .
   */
  @org.junit.Test
  public void k2CeilingFunc9() {
    final XQuery query = new XQuery(
      "ceiling(xs:long(.)) instance of xs:integer",
      ctx);
    try {
      query.context(node(file("fn/abs/e0.xml")));
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
   *  check dynamic type of fn:ceiling on argument of union of numeric types. .
   */
  @org.junit.Test
  public void fnCeiling1() {
    final XQuery query = new XQuery(
      "for $x in (1, xs:decimal(2), xs:float(3), xs:double(4)) return \n" +
      "           if ((ceiling($x)) instance of xs:integer) then \"integer\" \n" +
      "           else if ((ceiling($x)) instance of xs:decimal) then \"decimal\" \n" +
      "           else if ((ceiling($x)) instance of xs:float) then \"float\"\n" +
      "           else if ((ceiling($x)) instance of xs:double) then \"double\" \n" +
      "           else error()\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"integer\", \"decimal\", \"float\", \"double\"")
    );
  }

  /**
   * Basic test of ceiling(decimal).
   */
  @org.junit.Test
  public void fnCeilingDecimal1() {
    final XQuery query = new XQuery(
      "ceiling(12.5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("13.0")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Basic test of ceiling(decimal).
   */
  @org.junit.Test
  public void fnCeilingDecimal2() {
    final XQuery query = new XQuery(
      "ceiling(12.9)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("13.0")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Basic test of ceiling(decimal).
   */
  @org.junit.Test
  public void fnCeilingDecimal3() {
    final XQuery query = new XQuery(
      "ceiling(0.000000001)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("1.0")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Basic test of ceiling(decimal).
   */
  @org.junit.Test
  public void fnCeilingDecimal4() {
    final XQuery query = new XQuery(
      "ceiling(0.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0.0")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Basic test of ceiling(decimal).
   */
  @org.junit.Test
  public void fnCeilingDecimal5() {
    final XQuery query = new XQuery(
      "ceiling(-0.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0.0")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Basic test of ceiling(decimal).
   */
  @org.junit.Test
  public void fnCeilingDecimal6() {
    final XQuery query = new XQuery(
      "ceiling(-12345678.567890)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-12345678")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Basic test of ceiling(decimal).
   */
  @org.junit.Test
  public void fnCeilingDecimal7() {
    final XQuery query = new XQuery(
      "ceiling(-1234567891234567.2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-1234567891234567.0")
      &&
        assertType("xs:decimal")
      )
    );
  }

  /**
   * Basic test of ceiling(double).
   */
  @org.junit.Test
  public void fnCeilingDouble1() {
    final XQuery query = new XQuery(
      "ceiling(12.5e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("13.0e0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of ceiling(double).
   */
  @org.junit.Test
  public void fnCeilingDouble10() {
    final XQuery query = new XQuery(
      "ceiling(xs:double('-INF'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "-INF")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of ceiling(double).
   */
  @org.junit.Test
  public void fnCeilingDouble11() {
    final XQuery query = new XQuery(
      "ceiling(xs:double('-0'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of ceiling(double).
   */
  @org.junit.Test
  public void fnCeilingDouble2() {
    final XQuery query = new XQuery(
      "ceiling(12.9e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("13.0e0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of ceiling(double).
   */
  @org.junit.Test
  public void fnCeilingDouble3() {
    final XQuery query = new XQuery(
      "ceiling(0.000000001e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("1.0e0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of ceiling(double).
   */
  @org.junit.Test
  public void fnCeilingDouble4() {
    final XQuery query = new XQuery(
      "ceiling(0.0e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0.0e0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of ceiling(double).
   */
  @org.junit.Test
  public void fnCeilingDouble5() {
    final XQuery query = new XQuery(
      "ceiling(-0.1e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-0.0e0")
      &&
        assertEq("-0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of ceiling(double).
   */
  @org.junit.Test
  public void fnCeilingDouble6() {
    final XQuery query = new XQuery(
      "ceiling(-12345678.567890e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-12345678e0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of ceiling(double).
   */
  @org.junit.Test
  public void fnCeilingDouble7() {
    final XQuery query = new XQuery(
      "ceiling(-1234567891234567.2e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-1234567891234567.0e0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of ceiling(double).
   */
  @org.junit.Test
  public void fnCeilingDouble8() {
    final XQuery query = new XQuery(
      "ceiling(xs:double('NaN'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "NaN")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of ceiling(double).
   */
  @org.junit.Test
  public void fnCeilingDouble9() {
    final XQuery query = new XQuery(
      "ceiling(xs:double('INF'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "INF")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Basic test of ceiling(float)).
   */
  @org.junit.Test
  public void fnCeilingFloat1() {
    final XQuery query = new XQuery(
      "ceiling(xs:float(12.5e0))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("13.0e0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of ceiling(float)).
   */
  @org.junit.Test
  public void fnCeilingFloat10() {
    final XQuery query = new XQuery(
      "ceiling(xs:float(xs:float('-INF')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "-INF")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of ceiling(float)).
   */
  @org.junit.Test
  public void fnCeilingFloat11() {
    final XQuery query = new XQuery(
      "ceiling(xs:float(xs:float('-0')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of ceiling(float)).
   */
  @org.junit.Test
  public void fnCeilingFloat2() {
    final XQuery query = new XQuery(
      "ceiling(xs:float(12.9e0))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("13.0e0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of ceiling(float)).
   */
  @org.junit.Test
  public void fnCeilingFloat3() {
    final XQuery query = new XQuery(
      "ceiling(xs:float(0.000000001e0))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("1.0e0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of ceiling(float)).
   */
  @org.junit.Test
  public void fnCeilingFloat4() {
    final XQuery query = new XQuery(
      "ceiling(xs:float(0.0e0))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0.0e0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of ceiling(float)).
   */
  @org.junit.Test
  public void fnCeilingFloat5() {
    final XQuery query = new XQuery(
      "ceiling(xs:float(-0.1e0))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0.0e0")
      &&
        assertEq("-0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of ceiling(float)).
   */
  @org.junit.Test
  public void fnCeilingFloat6() {
    final XQuery query = new XQuery(
      "ceiling(xs:float(-12345678.1e0))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-12345678e0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of ceiling(float)).
   */
  @org.junit.Test
  public void fnCeilingFloat7() {
    final XQuery query = new XQuery(
      "ceiling(xs:float(-1234567.2e0))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("-1234567e0")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of ceiling(float)).
   */
  @org.junit.Test
  public void fnCeilingFloat8() {
    final XQuery query = new XQuery(
      "ceiling(xs:float(xs:float('NaN')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "NaN")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   * Basic test of ceiling(float)).
   */
  @org.junit.Test
  public void fnCeilingFloat9() {
    final XQuery query = new XQuery(
      "ceiling(xs:float(xs:float('INF')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "INF")
      &&
        assertType("xs:float")
      )
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnCeilingdbl1args1() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:double(\"-1.7976931348623157E308\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnCeilingdbl1args2() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:double(\"0\"))",
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
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnCeilingdbl1args3() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:double(\"1.7976931348623157E308\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnCeilingdec1args1() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:decimal(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnCeilingdec1args2() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:decimal(\"617375191608514839\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("617375191608514839")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnCeilingdec1args3() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:decimal(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnCeilingflt1args1() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:float(\"-3.4028235E38\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-3.4028235E38")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnCeilingflt1args2() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:float(\"0\"))",
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
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnCeilingflt1args3() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:float(\"3.4028235E38\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3.4028235E38")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnCeilingint1args1() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:int(\"-2147483648\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-2147483648")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnCeilingint1args2() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:int(\"-1873914410\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1873914410")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnCeilingint1args3() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:int(\"2147483647\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2147483647")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnCeilingintg1args1() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:integer(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnCeilingintg1args2() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:integer(\"830993497117024304\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("830993497117024304")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnCeilingintg1args3() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:integer(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnCeilinglng1args1() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:long(\"-92233720368547758\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnCeilinglng1args2() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:long(\"-47175562203048468\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-47175562203048468")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnCeilinglng1args3() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:long(\"92233720368547758\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("92233720368547758")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnCeilingnint1args1() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:negativeInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnCeilingnint1args2() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:negativeInteger(\"-297014075999096793\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-297014075999096793")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnCeilingnint1args3() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:negativeInteger(\"-1\"))",
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
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnCeilingnni1args1() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:nonNegativeInteger(\"0\"))",
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
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnCeilingnni1args2() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:nonNegativeInteger(\"303884545991464527\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("303884545991464527")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnCeilingnni1args3() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:nonNegativeInteger(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnCeilingnpi1args1() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:nonPositiveInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnCeilingnpi1args2() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:nonPositiveInteger(\"-475688437271870490\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-475688437271870490")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnCeilingnpi1args3() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:nonPositiveInteger(\"0\"))",
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
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnCeilingpint1args1() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:positiveInteger(\"1\"))",
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
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnCeilingpint1args2() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:positiveInteger(\"52704602390610033\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("52704602390610033")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnCeilingpint1args3() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:positiveInteger(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnCeilingsht1args1() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:short(\"-32768\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnCeilingsht1args2() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:short(\"-5324\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-5324")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnCeilingsht1args3() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:short(\"32767\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("32767")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnCeilingulng1args1() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:unsignedLong(\"0\"))",
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
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnCeilingulng1args2() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:unsignedLong(\"130747108607674654\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("130747108607674654")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnCeilingulng1args3() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:unsignedLong(\"184467440737095516\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("184467440737095516")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnCeilingusht1args1() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:unsignedShort(\"0\"))",
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
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnCeilingusht1args2() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:unsignedShort(\"44633\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("44633")
    );
  }

  /**
   *  Evaluates The "ceiling" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnCeilingusht1args3() {
    final XQuery query = new XQuery(
      "fn:ceiling(xs:unsignedShort(\"65535\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("65535")
    );
  }
}
