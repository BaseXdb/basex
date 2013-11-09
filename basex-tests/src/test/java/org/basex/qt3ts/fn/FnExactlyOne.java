package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the exactly-one() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnExactlyOne extends QT3TestSet {

  /**
   *  A test whose essence is: `exactly-one(1, 2)`. .
   */
  @org.junit.Test
  public void kSeqExactlyOneFunc1() {
    final XQuery query = new XQuery(
      "exactly-one(1, 2)",
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
   *  No function by name zero-or-more exists. .
   */
  @org.junit.Test
  public void kSeqExactlyOneFunc10() {
    final XQuery query = new XQuery(
      "zero-or-more(1)",
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
   *  A test whose essence is: `exactly-one()`. .
   */
  @org.junit.Test
  public void kSeqExactlyOneFunc2() {
    final XQuery query = new XQuery(
      "exactly-one()",
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
   *  A test whose essence is: `exactly-one( (1, 2, 3) )`. .
   */
  @org.junit.Test
  public void kSeqExactlyOneFunc3() {
    final XQuery query = new XQuery(
      "exactly-one( (1, 2, 3) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0005")
    );
  }

  /**
   *  A test whose essence is: `exactly-one("one") eq "one"`. .
   */
  @org.junit.Test
  public void kSeqExactlyOneFunc4() {
    final XQuery query = new XQuery(
      "exactly-one(\"one\") eq \"one\"",
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
   *  A test whose essence is: `count(exactly-one( "one" )) eq 1`. .
   */
  @org.junit.Test
  public void kSeqExactlyOneFunc5() {
    final XQuery query = new XQuery(
      "count(exactly-one( \"one\" )) eq 1",
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
   *  A test whose essence is: `exactly-one(error())`. .
   */
  @org.junit.Test
  public void kSeqExactlyOneFunc6() {
    final XQuery query = new XQuery(
      "exactly-one(error())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   *  A test whose essence is: `exactly-one((true(), error()))`. .
   */
  @org.junit.Test
  public void kSeqExactlyOneFunc7() {
    final XQuery query = new XQuery(
      "exactly-one((true(), error()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   *  A test whose essence is: `exactly-one(( error(), true()))`. .
   */
  @org.junit.Test
  public void kSeqExactlyOneFunc8() {
    final XQuery query = new XQuery(
      "exactly-one(( error(), true()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   *  A test whose essence is: `exactly-one( () )`. .
   */
  @org.junit.Test
  public void kSeqExactlyOneFunc9() {
    final XQuery query = new XQuery(
      "exactly-one( () )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0005")
    );
  }

  /**
   *  Test fn:boolean(fn:exactly-one). .
   */
  @org.junit.Test
  public void cbclExactlyOne001() {
    final XQuery query = new XQuery(
      "boolean( exactly-one( remove( (<a/>, 1), 1 ) ) )",
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
   *  test fn:exactly-one on a count-preserving function .
   */
  @org.junit.Test
  public void cbclExactlyOne002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:generate($arg as xs:integer) { if ($arg = 0) then (1, 2, 3) else $arg }; \n" +
      "      \tfn:exactly-one(fn:unordered( local:generate(1) ))\n" +
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
      assertStringValue(false, "1")
    );
  }

  /**
   *  test fn:exactly-one on a sequence of one-or-more items .
   */
  @org.junit.Test
  public void cbclExactlyOne003() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:generate($arg as xs:integer?) { if ($arg = 0) then () else if ($arg = 1) then $arg else ($arg, $arg) }; \n" +
      "      \t1 + fn:exactly-one(fn:one-or-more( local:generate( 1 ) ))\n" +
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
      assertStringValue(false, "2")
    );
  }

  /**
   *  test fn:exactly-one on a sequence of zero-or-one items .
   */
  @org.junit.Test
  public void cbclExactlyOne004() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:generate($arg as xs:integer?) { if ($arg = 0) then () else if ($arg = 1) then $arg else ($arg, $arg) }; \n" +
      "      \t1 + fn:exactly-one(fn:zero-or-one( local:generate( 1 ) ))\n" +
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
      assertStringValue(false, "2")
    );
  }

  /**
   *  Evaluation of the fn:exactly-one function with argument to sequence with two arguments. .
   */
  @org.junit.Test
  public void fnExactlyOne1() {
    final XQuery query = new XQuery(
      "fn:exactly-one((1,2))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0005")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnExactlyOnedbl1args1() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:double(\"-1.7976931348623157E308\"))",
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
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnExactlyOnedbl1args2() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:double(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnExactlyOnedbl1args3() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:double(\"1.7976931348623157E308\"))",
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
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnExactlyOnedec1args1() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:decimal(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnExactlyOnedec1args2() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:decimal(\"617375191608514839\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "617375191608514839")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnExactlyOnedec1args3() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:decimal(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "999999999999999999")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnExactlyOneflt1args1() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:float(\"-3.4028235E38\"))",
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
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnExactlyOneflt1args2() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:float(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnExactlyOneflt1args3() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:float(\"3.4028235E38\"))",
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
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnExactlyOneint1args1() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:int(\"-2147483648\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-2147483648")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnExactlyOneint1args2() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:int(\"-1873914410\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1873914410")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnExactlyOneint1args3() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:int(\"2147483647\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2147483647")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnExactlyOneintg1args1() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:integer(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnExactlyOneintg1args2() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:integer(\"830993497117024304\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "830993497117024304")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnExactlyOneintg1args3() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:integer(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "999999999999999999")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnExactlyOnelng1args1() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:long(\"-92233720368547758\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-92233720368547758")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnExactlyOnelng1args2() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:long(\"-47175562203048468\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-47175562203048468")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnExactlyOnelng1args3() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:long(\"92233720368547758\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "92233720368547758")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnExactlyOnenint1args1() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:negativeInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnExactlyOnenint1args2() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:negativeInteger(\"-297014075999096793\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-297014075999096793")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnExactlyOnenint1args3() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:negativeInteger(\"-1\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnExactlyOnenni1args1() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:nonNegativeInteger(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnExactlyOnenni1args2() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:nonNegativeInteger(\"303884545991464527\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "303884545991464527")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnExactlyOnenni1args3() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:nonNegativeInteger(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "999999999999999999")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnExactlyOnenpi1args1() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:nonPositiveInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnExactlyOnenpi1args2() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:nonPositiveInteger(\"-475688437271870490\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-475688437271870490")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnExactlyOnenpi1args3() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:nonPositiveInteger(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnExactlyOnepint1args1() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:positiveInteger(\"1\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnExactlyOnepint1args2() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:positiveInteger(\"52704602390610033\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "52704602390610033")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnExactlyOnepint1args3() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:positiveInteger(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "999999999999999999")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnExactlyOnesht1args1() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:short(\"-32768\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-32768")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnExactlyOnesht1args2() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:short(\"-5324\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-5324")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnExactlyOnesht1args3() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:short(\"32767\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "32767")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnExactlyOneulng1args1() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:unsignedLong(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnExactlyOneulng1args2() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:unsignedLong(\"130747108607674654\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "130747108607674654")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnExactlyOneulng1args3() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:unsignedLong(\"184467440737095516\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "184467440737095516")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnExactlyOneusht1args1() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:unsignedShort(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnExactlyOneusht1args2() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:unsignedShort(\"44633\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "44633")
    );
  }

  /**
   *  Evaluates The "exactly-one" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnExactlyOneusht1args3() {
    final XQuery query = new XQuery(
      "fn:exactly-one(xs:unsignedShort(\"65535\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "65535")
    );
  }
}
