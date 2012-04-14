package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the empty() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnEmpty extends QT3TestSet {

  /**
   *  A test whose essence is: `empty(1, 2)`. .
   */
  @org.junit.Test
  public void kSeqEmptyFunc1() {
    final XQuery query = new XQuery(
      "empty(1, 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `empty()`. .
   */
  @org.junit.Test
  public void kSeqEmptyFunc2() {
    final XQuery query = new XQuery(
      "empty()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `not(empty("string"))`. .
   */
  @org.junit.Test
  public void kSeqEmptyFunc3() {
    final XQuery query = new XQuery(
      "not(empty(\"string\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(empty((1, (), "string")))`. .
   */
  @org.junit.Test
  public void kSeqEmptyFunc4() {
    final XQuery query = new XQuery(
      "not(empty((1, (), \"string\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(empty((1, "string")))`. .
   */
  @org.junit.Test
  public void kSeqEmptyFunc5() {
    final XQuery query = new XQuery(
      "not(empty((1, \"string\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(empty( ((), 1, "string") ))`. .
   */
  @org.junit.Test
  public void kSeqEmptyFunc6() {
    final XQuery query = new XQuery(
      "not(empty( ((), 1, \"string\") ))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(())`. .
   */
  @org.junit.Test
  public void kSeqEmptyFunc7() {
    final XQuery query = new XQuery(
      "empty(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty( ((), (), ()) )`. .
   */
  @org.junit.Test
  public void kSeqEmptyFunc8() {
    final XQuery query = new XQuery(
      "empty( ((), (), ()) )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:empty combined with fn:remove and fn:not. .
   */
  @org.junit.Test
  public void kSeqEmptyFunc9() {
    final XQuery query = new XQuery(
      "not(exists(remove(remove((current-time(), 1), 1), 1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnEmptydbl1args1() {
    final XQuery query = new XQuery(
      "fn:empty((xs:double(\"-1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnEmptydbl1args2() {
    final XQuery query = new XQuery(
      "fn:empty((xs:double(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnEmptydbl1args3() {
    final XQuery query = new XQuery(
      "fn:empty((xs:double(\"1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnEmptydec1args1() {
    final XQuery query = new XQuery(
      "fn:empty((xs:decimal(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnEmptydec1args2() {
    final XQuery query = new XQuery(
      "fn:empty((xs:decimal(\"617375191608514839\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnEmptydec1args3() {
    final XQuery query = new XQuery(
      "fn:empty((xs:decimal(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnEmptyflt1args1() {
    final XQuery query = new XQuery(
      "fn:empty((xs:float(\"-3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnEmptyflt1args2() {
    final XQuery query = new XQuery(
      "fn:empty((xs:float(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnEmptyflt1args3() {
    final XQuery query = new XQuery(
      "fn:empty((xs:float(\"3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnEmptyint1args1() {
    final XQuery query = new XQuery(
      "fn:empty((xs:int(\"-2147483648\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnEmptyint1args2() {
    final XQuery query = new XQuery(
      "fn:empty((xs:int(\"-1873914410\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnEmptyint1args3() {
    final XQuery query = new XQuery(
      "fn:empty((xs:int(\"2147483647\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnEmptyintg1args1() {
    final XQuery query = new XQuery(
      "fn:empty((xs:integer(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnEmptyintg1args2() {
    final XQuery query = new XQuery(
      "fn:empty((xs:integer(\"830993497117024304\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnEmptyintg1args3() {
    final XQuery query = new XQuery(
      "fn:empty((xs:integer(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnEmptylng1args1() {
    final XQuery query = new XQuery(
      "fn:empty((xs:long(\"-92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnEmptylng1args2() {
    final XQuery query = new XQuery(
      "fn:empty((xs:long(\"-47175562203048468\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnEmptylng1args3() {
    final XQuery query = new XQuery(
      "fn:empty((xs:long(\"92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnEmptynint1args1() {
    final XQuery query = new XQuery(
      "fn:empty((xs:negativeInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnEmptynint1args2() {
    final XQuery query = new XQuery(
      "fn:empty((xs:negativeInteger(\"-297014075999096793\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnEmptynint1args3() {
    final XQuery query = new XQuery(
      "fn:empty((xs:negativeInteger(\"-1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnEmptynni1args1() {
    final XQuery query = new XQuery(
      "fn:empty((xs:nonNegativeInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnEmptynni1args2() {
    final XQuery query = new XQuery(
      "fn:empty((xs:nonNegativeInteger(\"303884545991464527\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnEmptynni1args3() {
    final XQuery query = new XQuery(
      "fn:empty((xs:nonNegativeInteger(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnEmptynpi1args1() {
    final XQuery query = new XQuery(
      "fn:empty((xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnEmptynpi1args2() {
    final XQuery query = new XQuery(
      "fn:empty((xs:nonPositiveInteger(\"-475688437271870490\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnEmptynpi1args3() {
    final XQuery query = new XQuery(
      "fn:empty((xs:nonPositiveInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnEmptypint1args1() {
    final XQuery query = new XQuery(
      "fn:empty((xs:positiveInteger(\"1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnEmptypint1args2() {
    final XQuery query = new XQuery(
      "fn:empty((xs:positiveInteger(\"52704602390610033\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnEmptypint1args3() {
    final XQuery query = new XQuery(
      "fn:empty((xs:positiveInteger(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnEmptysht1args1() {
    final XQuery query = new XQuery(
      "fn:empty((xs:short(\"-32768\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnEmptysht1args2() {
    final XQuery query = new XQuery(
      "fn:empty((xs:short(\"-5324\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnEmptysht1args3() {
    final XQuery query = new XQuery(
      "fn:empty((xs:short(\"32767\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnEmptyulng1args1() {
    final XQuery query = new XQuery(
      "fn:empty((xs:unsignedLong(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnEmptyulng1args2() {
    final XQuery query = new XQuery(
      "fn:empty((xs:unsignedLong(\"130747108607674654\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnEmptyulng1args3() {
    final XQuery query = new XQuery(
      "fn:empty((xs:unsignedLong(\"184467440737095516\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnEmptyusht1args1() {
    final XQuery query = new XQuery(
      "fn:empty((xs:unsignedShort(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnEmptyusht1args2() {
    final XQuery query = new XQuery(
      "fn:empty((xs:unsignedShort(\"44633\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "empty" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnEmptyusht1args3() {
    final XQuery query = new XQuery(
      "fn:empty((xs:unsignedShort(\"65535\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }
}
