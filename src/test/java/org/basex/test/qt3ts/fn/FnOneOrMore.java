package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the one-or-more() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnOneOrMore extends QT3TestSet {

  /**
   *  A test whose essence is: `one-or-more(1, 2)`. .
   */
  @org.junit.Test
  public void kSeqOneOrMoreFunc1() {
    final XQuery query = new XQuery(
      "one-or-more(1, 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `one-or-more()`. .
   */
  @org.junit.Test
  public void kSeqOneOrMoreFunc2() {
    final XQuery query = new XQuery(
      "one-or-more()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `one-or-more("one")`. .
   */
  @org.junit.Test
  public void kSeqOneOrMoreFunc3() {
    final XQuery query = new XQuery(
      "one-or-more(\"one\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "one")
    );
  }

  /**
   *  A test whose essence is: `exists(one-or-more(("one", 2)))`. .
   */
  @org.junit.Test
  public void kSeqOneOrMoreFunc4() {
    final XQuery query = new XQuery(
      "exists(one-or-more((\"one\", 2)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(one-or-more( "one" )) eq 1`. .
   */
  @org.junit.Test
  public void kSeqOneOrMoreFunc5() {
    final XQuery query = new XQuery(
      "count(one-or-more( \"one\" )) eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(one-or-more( (1, 2, 3, "four") )) eq 4`. .
   */
  @org.junit.Test
  public void kSeqOneOrMoreFunc6() {
    final XQuery query = new XQuery(
      "count(one-or-more( (1, 2, 3, \"four\") )) eq 4",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `one-or-more(error())`. .
   */
  @org.junit.Test
  public void kSeqOneOrMoreFunc7() {
    final XQuery query = new XQuery(
      "one-or-more(error())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOER0000")
    );
  }

  /**
   *  A test whose essence is: `one-or-more( () )`. .
   */
  @org.junit.Test
  public void kSeqOneOrMoreFunc8() {
    final XQuery query = new XQuery(
      "one-or-more( () )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0004")
    );
  }

  /**
   *  Have fn:one-or-more() as an operand to an expression that requires zero or more, that itself has cardinality zero or more. .
   */
  @org.junit.Test
  public void k2SeqOneOrMoreFunc1() {
    final XQuery query = new XQuery(
      "codepoints-to-string(one-or-more(string-to-codepoints(\"foo\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "foo")
    );
  }

  /**
   * Evaluation of the fn:one-or-more function with argument set to empty sequence..
   */
  @org.junit.Test
  public void fnOneOrMore1() {
    final XQuery query = new XQuery(
      "fn:one-or-more(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0004")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnOneOrMoredbl1args1() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:double(\"-1.7976931348623157E308\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnOneOrMoredbl1args2() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:double(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnOneOrMoredbl1args3() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:double(\"1.7976931348623157E308\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnOneOrMoredec1args1() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:decimal(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-999999999999999999")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnOneOrMoredec1args2() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:decimal(\"617375191608514839\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "617375191608514839")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnOneOrMoredec1args3() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:decimal(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "999999999999999999")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnOneOrMoreflt1args1() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:float(\"-3.4028235E38\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-3.4028235E38")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnOneOrMoreflt1args2() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:float(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnOneOrMoreflt1args3() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:float(\"3.4028235E38\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "3.4028235E38")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnOneOrMoreint1args1() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:int(\"-2147483648\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-2147483648")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnOneOrMoreint1args2() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:int(\"-1873914410\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-1873914410")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnOneOrMoreint1args3() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:int(\"2147483647\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2147483647")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnOneOrMoreintg1args1() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:integer(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-999999999999999999")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnOneOrMoreintg1args2() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:integer(\"830993497117024304\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "830993497117024304")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnOneOrMoreintg1args3() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:integer(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "999999999999999999")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnOneOrMorelng1args1() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:long(\"-92233720368547758\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-92233720368547758")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnOneOrMorelng1args2() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:long(\"-47175562203048468\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-47175562203048468")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnOneOrMorelng1args3() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:long(\"92233720368547758\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "92233720368547758")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnOneOrMorenint1args1() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:negativeInteger(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-999999999999999999")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnOneOrMorenint1args2() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:negativeInteger(\"-297014075999096793\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-297014075999096793")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnOneOrMorenint1args3() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:negativeInteger(\"-1\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-1")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnOneOrMorenni1args1() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:nonNegativeInteger(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnOneOrMorenni1args2() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:nonNegativeInteger(\"303884545991464527\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "303884545991464527")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnOneOrMorenni1args3() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:nonNegativeInteger(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "999999999999999999")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnOneOrMorenpi1args1() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:nonPositiveInteger(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-999999999999999999")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnOneOrMorenpi1args2() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:nonPositiveInteger(\"-475688437271870490\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-475688437271870490")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnOneOrMorenpi1args3() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:nonPositiveInteger(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnOneOrMorepint1args1() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:positiveInteger(\"1\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnOneOrMorepint1args2() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:positiveInteger(\"52704602390610033\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "52704602390610033")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnOneOrMorepint1args3() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:positiveInteger(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "999999999999999999")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnOneOrMoresht1args1() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:short(\"-32768\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-32768")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnOneOrMoresht1args2() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:short(\"-5324\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-5324")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnOneOrMoresht1args3() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:short(\"32767\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "32767")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnOneOrMoreulng1args1() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:unsignedLong(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnOneOrMoreulng1args2() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:unsignedLong(\"130747108607674654\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "130747108607674654")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnOneOrMoreulng1args3() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:unsignedLong(\"184467440737095516\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "184467440737095516")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnOneOrMoreusht1args1() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:unsignedShort(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnOneOrMoreusht1args2() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:unsignedShort(\"44633\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "44633")
    );
  }

  /**
   *  Evaluates The "one-or-more" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnOneOrMoreusht1args3() {
    final XQuery query = new XQuery(
      "fn:one-or-more(xs:unsignedShort(\"65535\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "65535")
    );
  }
}
