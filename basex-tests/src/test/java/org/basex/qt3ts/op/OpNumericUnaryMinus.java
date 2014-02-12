package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the numeric-unary-minus() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpNumericUnaryMinus extends QT3TestSet {

  /**
   *  No unary operator is available for xs:string. .
   */
  @org.junit.Test
  public void kNumericUnaryMinus1() {
    final XQuery query = new XQuery(
      "-\"a string\"",
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
   *  A test whose essence is: `---------3 eq -3`. .
   */
  @org.junit.Test
  public void kNumericUnaryMinus10() {
    final XQuery query = new XQuery(
      "---------3 eq -3",
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
   *  A test whose essence is: `+-+-+-+-+-+-+-+3 eq -3`. .
   */
  @org.junit.Test
  public void kNumericUnaryMinus11() {
    final XQuery query = new XQuery(
      "+-+-+-+-+-+-+-+3 eq -3",
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
   *  A test whose essence is: `----+-+-++-+-+-+-+++-+--+--3 eq -3`. .
   */
  @org.junit.Test
  public void kNumericUnaryMinus12() {
    final XQuery query = new XQuery(
      "----+-+-++-+-+-+-+++-+--+--3 eq -3",
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
   *  A test whose essence is: `---3 eq -3`. .
   */
  @org.junit.Test
  public void kNumericUnaryMinus13() {
    final XQuery query = new XQuery(
      "---3 eq -3",
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
   *  A test whose essence is: `+(-3) eq -3`. .
   */
  @org.junit.Test
  public void kNumericUnaryMinus2() {
    final XQuery query = new XQuery(
      "+(-3) eq -3",
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
   *  A test whose essence is: `(-3) eq -3`. .
   */
  @org.junit.Test
  public void kNumericUnaryMinus3() {
    final XQuery query = new XQuery(
      "(-3) eq -3",
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
   *  A test whose essence is: `(+3) ne -3`. .
   */
  @org.junit.Test
  public void kNumericUnaryMinus4() {
    final XQuery query = new XQuery(
      "(+3) ne -3",
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
   *  A test whose essence is: `-3 eq -3`. .
   */
  @org.junit.Test
  public void kNumericUnaryMinus5() {
    final XQuery query = new XQuery(
      "-3 eq -3",
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
   *  A test whose essence is: `+(-3) eq -3`. .
   */
  @org.junit.Test
  public void kNumericUnaryMinus6() {
    final XQuery query = new XQuery(
      "+(-3) eq -3",
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
   *  A test whose essence is: `-(+3) eq -3`. .
   */
  @org.junit.Test
  public void kNumericUnaryMinus7() {
    final XQuery query = new XQuery(
      "-(+3) eq -3",
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
   *  A test whose essence is: `-(3) eq -3`. .
   */
  @org.junit.Test
  public void kNumericUnaryMinus8() {
    final XQuery query = new XQuery(
      "-(3) eq -3",
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
   *  A test whose essence is: `-(3) eq -3`. .
   */
  @org.junit.Test
  public void kNumericUnaryMinus9() {
    final XQuery query = new XQuery(
      "-(3) eq -3",
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
   *  Operand is constructor function for xs:double. .
   */
  @org.junit.Test
  public void k2NumericUnaryMinus1() {
    final XQuery query = new XQuery(
      "-xs:double(0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-0")
    );
  }

  /**
   *  A tricky operand, which requires various forms of argument conversion. .
   */
  @org.junit.Test
  public void k2NumericUnaryMinus10() {
    final XQuery query = new XQuery(
      "-((<n>1</n> | ())[1])",
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
   *  Operand is constructor function for xs:float. .
   */
  @org.junit.Test
  public void k2NumericUnaryMinus2() {
    final XQuery query = new XQuery(
      "-xs:float(0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-0")
    );
  }

  /**
   *  Operand is constructor function for xs:decimal. .
   */
  @org.junit.Test
  public void k2NumericUnaryMinus3() {
    final XQuery query = new XQuery(
      "-xs:decimal(0)",
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
   *  Operand is constructor function for xs:integer. .
   */
  @org.junit.Test
  public void k2NumericUnaryMinus4() {
    final XQuery query = new XQuery(
      "-xs:integer(0)",
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
   *  -0.0. .
   */
  @org.junit.Test
  public void k2NumericUnaryMinus5() {
    final XQuery query = new XQuery(
      "-0.0",
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
   *  Cast -0.0 to xs:float. .
   */
  @org.junit.Test
  public void k2NumericUnaryMinus6() {
    final XQuery query = new XQuery(
      "xs:float(-0.0)",
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
   *  Cast -0.0 to xs:double. .
   */
  @org.junit.Test
  public void k2NumericUnaryMinus7() {
    final XQuery query = new XQuery(
      "xs:double(-0.0)",
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
   *  Unary combined with path expressions. .
   */
  @org.junit.Test
  public void k2NumericUnaryMinus8() {
    final XQuery query = new XQuery(
      "empty(document{()}/(-element()))",
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
   *  Unary with an empty sequence. .
   */
  @org.junit.Test
  public void k2NumericUnaryMinus9() {
    final XQuery query = new XQuery(
      "empty(-())",
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
   *  Test unary minus with a potential type check error .
   */
  @org.junit.Test
  public void cbclNumericUnaryMinus001() {
    final XQuery query = new XQuery(
      "declare function local:value($number as xs:boolean) { if ($number) then 1 else xs:string('1') }; -(local:value(true()))",
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
        assertStringValue(false, "-1")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Check dynamic type of unary plus on argument of union of numeric types and untypedAtomic. .
   */
  @org.junit.Test
  public void opNumericUnaryMinus1() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $x in (1, xs:decimal(2), xs:float(3), xs:double(4), xs:untypedAtomic(5)) \n" +
      "        return typeswitch (-$x) \n" +
      "        case xs:integer return \"integer\" \n" +
      "        case xs:decimal return \"decimal\" \n" +
      "        case xs:float return \"float\" \n" +
      "        case xs:double return \"double\" \n" +
      "        default return error()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "integer decimal float double double")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusdbl1args1() {
    final XQuery query = new XQuery(
      "-(xs:double(\"-1.7976931348623157E308\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusdbl1args2() {
    final XQuery query = new XQuery(
      "-(xs:double(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-0")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusdbl1args3() {
    final XQuery query = new XQuery(
      "-(xs:double(\"1.7976931348623157E308\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusdec1args1() {
    final XQuery query = new XQuery(
      "-(xs:decimal(\"-999999999999999999\"))",
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
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusdec1args2() {
    final XQuery query = new XQuery(
      "-(xs:decimal(\"617375191608514839\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-617375191608514839")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusdec1args3() {
    final XQuery query = new XQuery(
      "-(xs:decimal(\"999999999999999999\"))",
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
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusflt1args1() {
    final XQuery query = new XQuery(
      "-(xs:float(\"-3.4028235E38\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(3.4028235E38)")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusflt1args2() {
    final XQuery query = new XQuery(
      "-(xs:float(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-0")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusflt1args3() {
    final XQuery query = new XQuery(
      "-(xs:float(\"3.4028235E38\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(-3.4028235E38)")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusint1args1() {
    final XQuery query = new XQuery(
      "-(xs:int(\"-2147483647\"))",
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
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusint1args2() {
    final XQuery query = new XQuery(
      "-(xs:int(\"-1873914410\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1873914410")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusint1args3() {
    final XQuery query = new XQuery(
      "-(xs:int(\"2147483647\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-2147483647")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusintg1args1() {
    final XQuery query = new XQuery(
      "-(xs:integer(\"-999999999999999999\"))",
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
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusintg1args2() {
    final XQuery query = new XQuery(
      "-(xs:integer(\"830993497117024304\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-830993497117024304")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusintg1args3() {
    final XQuery query = new XQuery(
      "-(xs:integer(\"999999999999999999\"))",
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
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinuslng1args1() {
    final XQuery query = new XQuery(
      "-(xs:long(\"-92233720368547758\"))",
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
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void opNumericUnaryMinuslng1args2() {
    final XQuery query = new XQuery(
      "-(xs:long(\"-47175562203048468\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("47175562203048468")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinuslng1args3() {
    final XQuery query = new XQuery(
      "-(xs:long(\"92233720368547758\"))",
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
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusnint1args1() {
    final XQuery query = new XQuery(
      "-(xs:negativeInteger(\"-999999999999999999\"))",
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
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusnint1args2() {
    final XQuery query = new XQuery(
      "-(xs:negativeInteger(\"-297014075999096793\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("297014075999096793")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusnint1args3() {
    final XQuery query = new XQuery(
      "-(xs:negativeInteger(\"-1\"))",
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
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusnni1args1() {
    final XQuery query = new XQuery(
      "-(xs:nonNegativeInteger(\"0\"))",
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
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusnni1args2() {
    final XQuery query = new XQuery(
      "-(xs:nonNegativeInteger(\"303884545991464527\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-303884545991464527")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusnni1args3() {
    final XQuery query = new XQuery(
      "-(xs:nonNegativeInteger(\"999999999999999999\"))",
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
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusnpi1args1() {
    final XQuery query = new XQuery(
      "-(xs:nonPositiveInteger(\"-999999999999999999\"))",
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
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusnpi1args2() {
    final XQuery query = new XQuery(
      "-(xs:nonPositiveInteger(\"-475688437271870490\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("475688437271870490")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusnpi1args3() {
    final XQuery query = new XQuery(
      "-(xs:nonPositiveInteger(\"0\"))",
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
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinuspint1args1() {
    final XQuery query = new XQuery(
      "-(xs:positiveInteger(\"1\"))",
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
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void opNumericUnaryMinuspint1args2() {
    final XQuery query = new XQuery(
      "-(xs:positiveInteger(\"52704602390610033\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-52704602390610033")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinuspint1args3() {
    final XQuery query = new XQuery(
      "-(xs:positiveInteger(\"999999999999999999\"))",
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
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinussht1args1() {
    final XQuery query = new XQuery(
      "-(xs:short(\"-32768\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("32768")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void opNumericUnaryMinussht1args2() {
    final XQuery query = new XQuery(
      "-(xs:short(\"-5324\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5324")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinussht1args3() {
    final XQuery query = new XQuery(
      "-(xs:short(\"32767\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-32767")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusulng1args1() {
    final XQuery query = new XQuery(
      "-(xs:unsignedLong(\"0\"))",
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
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusulng1args2() {
    final XQuery query = new XQuery(
      "-(xs:unsignedLong(\"130747108607674654\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-130747108607674654")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinusulng1args3() {
    final XQuery query = new XQuery(
      "-(xs:unsignedLong(\"184467440737095516\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-184467440737095516")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinususht1args1() {
    final XQuery query = new XQuery(
      "-(xs:unsignedShort(\"0\"))",
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
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void opNumericUnaryMinususht1args2() {
    final XQuery query = new XQuery(
      "-(xs:unsignedShort(\"44633\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-44633")
    );
  }

  /**
   *  Evaluates The "op:numeric-unary-minus" operator with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void opNumericUnaryMinususht1args3() {
    final XQuery query = new XQuery(
      "-(xs:unsignedShort(\"65535\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-65535")
    );
  }
}
