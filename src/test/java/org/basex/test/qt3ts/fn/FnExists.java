package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the iexists() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnExists extends QT3TestSet {

  /**
   *  A test whose essence is: `exists(1, 2)`. .
   */
  @org.junit.Test
  public void kSeqExistsFunc1() {
    final XQuery query = new XQuery(
      "exists(1, 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  The Dynamic Context property 'current dateTime' must have an explicit timezone when presented as a xs:dateTime. .
   */
  @org.junit.Test
  public void kSeqExistsFunc10() {
    final XQuery query = new XQuery(
      "exists(timezone-from-dateTime(current-dateTime()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The Dynamic Context property 'current dateTime' must have an explicit timezone when presented as a xs:date. .
   */
  @org.junit.Test
  public void kSeqExistsFunc11() {
    final XQuery query = new XQuery(
      "exists(timezone-from-date(current-date()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The Dynamic Context property 'current dateTime' must have an explicit timezone when presented as a xs:time. .
   */
  @org.junit.Test
  public void kSeqExistsFunc12() {
    final XQuery query = new XQuery(
      "exists(timezone-from-time(current-time()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:empty combined with fn:remove. .
   */
  @org.junit.Test
  public void kSeqExistsFunc13() {
    final XQuery query = new XQuery(
      "empty(remove(current-time(), 1))",
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
  public void kSeqExistsFunc14() {
    final XQuery query = new XQuery(
      "not(empty(remove((current-time(), 1), 1)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `exists()`. .
   */
  @org.junit.Test
  public void kSeqExistsFunc2() {
    final XQuery query = new XQuery(
      "exists()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `not(exists(()))`. .
   */
  @org.junit.Test
  public void kSeqExistsFunc3() {
    final XQuery query = new XQuery(
      "not(exists(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `exists(1)`. .
   */
  @org.junit.Test
  public void kSeqExistsFunc4() {
    final XQuery query = new XQuery(
      "exists(1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `exists((1))`. .
   */
  @org.junit.Test
  public void kSeqExistsFunc5() {
    final XQuery query = new XQuery(
      "exists((1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `exists((1, 2, 3))`. .
   */
  @org.junit.Test
  public void kSeqExistsFunc6() {
    final XQuery query = new XQuery(
      "exists((1, 2, 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(exists(()))`. .
   */
  @org.junit.Test
  public void kSeqExistsFunc7() {
    final XQuery query = new XQuery(
      "not(exists(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(exists( ((), (), (), ()) ))`. .
   */
  @org.junit.Test
  public void kSeqExistsFunc8() {
    final XQuery query = new XQuery(
      "not(exists( ((), (), (), ()) ))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `exists(reverse((1, 2, 3)))`. .
   */
  @org.junit.Test
  public void kSeqExistsFunc9() {
    final XQuery query = new XQuery(
      "exists(reverse((1, 2, 3)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnExistsdbl1args1() {
    final XQuery query = new XQuery(
      "fn:exists((xs:double(\"-1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnExistsdbl1args2() {
    final XQuery query = new XQuery(
      "fn:exists((xs:double(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnExistsdbl1args3() {
    final XQuery query = new XQuery(
      "fn:exists((xs:double(\"1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnExistsdec1args1() {
    final XQuery query = new XQuery(
      "fn:exists((xs:decimal(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnExistsdec1args2() {
    final XQuery query = new XQuery(
      "fn:exists((xs:decimal(\"617375191608514839\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnExistsdec1args3() {
    final XQuery query = new XQuery(
      "fn:exists((xs:decimal(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnExistsflt1args1() {
    final XQuery query = new XQuery(
      "fn:exists((xs:float(\"-3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnExistsflt1args2() {
    final XQuery query = new XQuery(
      "fn:exists((xs:float(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnExistsflt1args3() {
    final XQuery query = new XQuery(
      "fn:exists((xs:float(\"3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnExistsint1args1() {
    final XQuery query = new XQuery(
      "fn:exists((xs:int(\"-2147483648\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnExistsint1args2() {
    final XQuery query = new XQuery(
      "fn:exists((xs:int(\"-1873914410\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnExistsint1args3() {
    final XQuery query = new XQuery(
      "fn:exists((xs:int(\"2147483647\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnExistsintg1args1() {
    final XQuery query = new XQuery(
      "fn:exists((xs:integer(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnExistsintg1args2() {
    final XQuery query = new XQuery(
      "fn:exists((xs:integer(\"830993497117024304\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnExistsintg1args3() {
    final XQuery query = new XQuery(
      "fn:exists((xs:integer(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnExistslng1args1() {
    final XQuery query = new XQuery(
      "fn:exists((xs:long(\"-92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnExistslng1args2() {
    final XQuery query = new XQuery(
      "fn:exists((xs:long(\"-47175562203048468\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnExistslng1args3() {
    final XQuery query = new XQuery(
      "fn:exists((xs:long(\"92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnExistsnint1args1() {
    final XQuery query = new XQuery(
      "fn:exists((xs:negativeInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnExistsnint1args2() {
    final XQuery query = new XQuery(
      "fn:exists((xs:negativeInteger(\"-297014075999096793\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnExistsnint1args3() {
    final XQuery query = new XQuery(
      "fn:exists((xs:negativeInteger(\"-1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnExistsnni1args1() {
    final XQuery query = new XQuery(
      "fn:exists((xs:nonNegativeInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnExistsnni1args2() {
    final XQuery query = new XQuery(
      "fn:exists((xs:nonNegativeInteger(\"303884545991464527\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnExistsnni1args3() {
    final XQuery query = new XQuery(
      "fn:exists((xs:nonNegativeInteger(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnExistsnpi1args1() {
    final XQuery query = new XQuery(
      "fn:exists((xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnExistsnpi1args2() {
    final XQuery query = new XQuery(
      "fn:exists((xs:nonPositiveInteger(\"-475688437271870490\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnExistsnpi1args3() {
    final XQuery query = new XQuery(
      "fn:exists((xs:nonPositiveInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnExistspint1args1() {
    final XQuery query = new XQuery(
      "fn:exists((xs:positiveInteger(\"1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnExistspint1args2() {
    final XQuery query = new XQuery(
      "fn:exists((xs:positiveInteger(\"52704602390610033\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnExistspint1args3() {
    final XQuery query = new XQuery(
      "fn:exists((xs:positiveInteger(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnExistssht1args1() {
    final XQuery query = new XQuery(
      "fn:exists((xs:short(\"-32768\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnExistssht1args2() {
    final XQuery query = new XQuery(
      "fn:exists((xs:short(\"-5324\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnExistssht1args3() {
    final XQuery query = new XQuery(
      "fn:exists((xs:short(\"32767\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnExistsulng1args1() {
    final XQuery query = new XQuery(
      "fn:exists((xs:unsignedLong(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnExistsulng1args2() {
    final XQuery query = new XQuery(
      "fn:exists((xs:unsignedLong(\"130747108607674654\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnExistsulng1args3() {
    final XQuery query = new XQuery(
      "fn:exists((xs:unsignedLong(\"184467440737095516\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnExistsusht1args1() {
    final XQuery query = new XQuery(
      "fn:exists((xs:unsignedShort(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnExistsusht1args2() {
    final XQuery query = new XQuery(
      "fn:exists((xs:unsignedShort(\"44633\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "exists" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnExistsusht1args3() {
    final XQuery query = new XQuery(
      "fn:exists((xs:unsignedShort(\"65535\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }
}
