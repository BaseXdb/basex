package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the numeric-unary-minus() function.
 *
 * @author BaseX Team 2005-12, BSD License
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-65535")
    );
  }
}
