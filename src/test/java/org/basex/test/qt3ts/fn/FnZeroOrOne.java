package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the zero-or-one() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnZeroOrOne extends QT3TestSet {

  /**
   *  A test whose essence is: `zero-or-one(1, 2)`. .
   */
  @org.junit.Test
  public void kSeqZeroOrOneFunc1() {
    final XQuery query = new XQuery(
      "zero-or-one(1, 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `zero-or-one()`. .
   */
  @org.junit.Test
  public void kSeqZeroOrOneFunc2() {
    final XQuery query = new XQuery(
      "zero-or-one()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `zero-or-one(true())`. .
   */
  @org.junit.Test
  public void kSeqZeroOrOneFunc3() {
    final XQuery query = new XQuery(
      "zero-or-one(true())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(zero-or-one(()))`. .
   */
  @org.junit.Test
  public void kSeqZeroOrOneFunc4() {
    final XQuery query = new XQuery(
      "empty(zero-or-one(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(zero-or-one( "one" )) eq 1`. .
   */
  @org.junit.Test
  public void kSeqZeroOrOneFunc5() {
    final XQuery query = new XQuery(
      "count(zero-or-one( \"one\" )) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(zero-or-one( () )) eq 0`. .
   */
  @org.junit.Test
  public void kSeqZeroOrOneFunc6() {
    final XQuery query = new XQuery(
      "count(zero-or-one( () )) eq 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `zero-or-one(error())`. .
   */
  @org.junit.Test
  public void kSeqZeroOrOneFunc7() {
    final XQuery query = new XQuery(
      "zero-or-one(error())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOER0000")
    );
  }

  /**
   *  A test whose essence is: `zero-or-one( (1, 2, 3) )`. .
   */
  @org.junit.Test
  public void kSeqZeroOrOneFunc8() {
    final XQuery query = new XQuery(
      "zero-or-one( (1, 2, 3) )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0003")
    );
  }

  /**
   *  Evaluation of the fn:zero-or-one function with argument sequence containing more than one item .
   */
  @org.junit.Test
  public void fnZeroOrOne1() {
    final XQuery query = new XQuery(
      "fn:zero-or-one((1,2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0003")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnZeroOrOnedbl1args1() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:double(\"-1.7976931348623157E308\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnZeroOrOnedbl1args2() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:double(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnZeroOrOnedbl1args3() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:double(\"1.7976931348623157E308\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnZeroOrOnedec1args1() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:decimal(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-999999999999999999")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnZeroOrOnedec1args2() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:decimal(\"617375191608514839\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "617375191608514839")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnZeroOrOnedec1args3() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:decimal(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "999999999999999999")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnZeroOrOneflt1args1() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:float(\"-3.4028235E38\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-3.4028235E38")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnZeroOrOneflt1args2() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:float(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnZeroOrOneflt1args3() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:float(\"3.4028235E38\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "3.4028235E38")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnZeroOrOneint1args1() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:int(\"-2147483648\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-2147483648")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnZeroOrOneint1args2() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:int(\"-1873914410\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-1873914410")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnZeroOrOneint1args3() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:int(\"2147483647\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2147483647")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnZeroOrOneintg1args1() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:integer(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-999999999999999999")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnZeroOrOneintg1args2() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:integer(\"830993497117024304\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "830993497117024304")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnZeroOrOneintg1args3() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:integer(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "999999999999999999")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnZeroOrOnelng1args1() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:long(\"-92233720368547758\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-92233720368547758")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnZeroOrOnelng1args2() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:long(\"-47175562203048468\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-47175562203048468")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnZeroOrOnelng1args3() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:long(\"92233720368547758\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "92233720368547758")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnZeroOrOnenint1args1() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:negativeInteger(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-999999999999999999")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnZeroOrOnenint1args2() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:negativeInteger(\"-297014075999096793\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-297014075999096793")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnZeroOrOnenint1args3() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:negativeInteger(\"-1\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-1")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnZeroOrOnenni1args1() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:nonNegativeInteger(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnZeroOrOnenni1args2() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:nonNegativeInteger(\"303884545991464527\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "303884545991464527")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnZeroOrOnenni1args3() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:nonNegativeInteger(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "999999999999999999")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnZeroOrOnenpi1args1() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:nonPositiveInteger(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-999999999999999999")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnZeroOrOnenpi1args2() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:nonPositiveInteger(\"-475688437271870490\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-475688437271870490")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnZeroOrOnenpi1args3() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:nonPositiveInteger(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnZeroOrOnepint1args1() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:positiveInteger(\"1\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnZeroOrOnepint1args2() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:positiveInteger(\"52704602390610033\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "52704602390610033")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnZeroOrOnepint1args3() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:positiveInteger(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "999999999999999999")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnZeroOrOnesht1args1() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:short(\"-32768\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-32768")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnZeroOrOnesht1args2() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:short(\"-5324\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-5324")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnZeroOrOnesht1args3() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:short(\"32767\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "32767")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnZeroOrOneulng1args1() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:unsignedLong(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnZeroOrOneulng1args2() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:unsignedLong(\"130747108607674654\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "130747108607674654")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnZeroOrOneulng1args3() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:unsignedLong(\"184467440737095516\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "184467440737095516")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnZeroOrOneusht1args1() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:unsignedShort(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnZeroOrOneusht1args2() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:unsignedShort(\"44633\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "44633")
    );
  }

  /**
   *  Evaluates The "zero-or-one" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnZeroOrOneusht1args3() {
    final XQuery query = new XQuery(
      "fn:zero-or-one(xs:unsignedShort(\"65535\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "65535")
    );
  }
}
