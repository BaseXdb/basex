package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests the fn:number() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnNumber extends QT3TestSet {

  /**
   *  A test whose essence is: `number(1, 2)`. .
   */
  @org.junit.Test
  public void kNodeNumberFunc1() {
    final XQuery query = new XQuery(
      "number(1, 2)",
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
   *  A test whose essence is: `fn:number(()) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNodeNumberFunc10() {
    final XQuery query = new XQuery(
      "fn:number(()) instance of xs:double",
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
   *  A test whose essence is: `not(fn:number(()))`. .
   */
  @org.junit.Test
  public void kNodeNumberFunc11() {
    final XQuery query = new XQuery(
      "not(fn:number(()))",
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
   *  fn:number() applied to a type which a cast regardless of source value never would succeed for. .
   */
  @org.junit.Test
  public void kNodeNumberFunc12() {
    final XQuery query = new XQuery(
      "string(number(xs:anyURI(\"example.com/\"))) eq \"NaN\"",
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
   *  A test whose essence is: `string(number(xs:anyURI("1"))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kNodeNumberFunc13() {
    final XQuery query = new XQuery(
      "string(number(xs:anyURI(\"1\"))) eq \"NaN\"",
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
   *  A test whose essence is: `string(number("1")) eq "1"`. .
   */
  @org.junit.Test
  public void kNodeNumberFunc14() {
    final XQuery query = new XQuery(
      "string(number(\"1\")) eq \"1\"",
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
   *  A test whose essence is: `string(number(xs:gYear("2005"))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kNodeNumberFunc15() {
    final XQuery query = new XQuery(
      "string(number(xs:gYear(\"2005\"))) eq \"NaN\"",
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
   *  fn:number() inside a predicate. .
   */
  @org.junit.Test
  public void kNodeNumberFunc16() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3)[number()], (1, 2, 3))",
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
   *  fn:number() inside a predicate(#2). .
   */
  @org.junit.Test
  public void kNodeNumberFunc17() {
    final XQuery query = new XQuery(
      "(1)[number()] eq 1",
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
   *  A test whose essence is: `fn:number(1) eq 1`. .
   */
  @org.junit.Test
  public void kNodeNumberFunc2() {
    final XQuery query = new XQuery(
      "fn:number(1) eq 1",
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
   *  A test whose essence is: `not(fn:number("results in NaN"))`. .
   */
  @org.junit.Test
  public void kNodeNumberFunc3() {
    final XQuery query = new XQuery(
      "not(fn:number(\"results in NaN\"))",
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
   *  A test whose essence is: `fn:number("results in NaN") instance of xs:double`. .
   */
  @org.junit.Test
  public void kNodeNumberFunc4() {
    final XQuery query = new XQuery(
      "fn:number(\"results in NaN\") instance of xs:double",
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
   *  A test whose essence is: `fn:number(1) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNodeNumberFunc5() {
    final XQuery query = new XQuery(
      "fn:number(1) instance of xs:double",
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
   *  A test whose essence is: `fn:number(1.1) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNodeNumberFunc6() {
    final XQuery query = new XQuery(
      "fn:number(1.1) instance of xs:double",
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
   *  A test whose essence is: `fn:number(xs:float(3)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNodeNumberFunc7() {
    final XQuery query = new XQuery(
      "fn:number(xs:float(3)) instance of xs:double",
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
   *  A test whose essence is: `fn:number(xs:double(3)) instance of xs:double`. .
   */
  @org.junit.Test
  public void kNodeNumberFunc8() {
    final XQuery query = new XQuery(
      "fn:number(xs:double(3)) instance of xs:double",
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
   *  A test whose essence is: `fn:number("NaN") instance of xs:double`. .
   */
  @org.junit.Test
  public void kNodeNumberFunc9() {
    final XQuery query = new XQuery(
      "fn:number(\"NaN\") instance of xs:double",
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
   *  Evaluation of the fn:number function with empty sequence as an argument. .
   */
  @org.junit.Test
  public void fnNumber1() {
    final XQuery query = new XQuery(
      "fn:number(())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * number() applied to an untypedAtomic.
   */
  @org.junit.Test
  public void fnNumber10() {
    final XQuery query = new XQuery(
      "number(xs:untypedAtomic(\"1000\"))",
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
        assertType("xs:double")
      &&
        assertEq("1e3")
      )
    );
  }

  /**
   *  Evaluation of the fn:number function with a string set as an argument. .
   */
  @org.junit.Test
  public void fnNumber2() {
    final XQuery query = new XQuery(
      "fn:number(\"A String\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  Evaluation of the fn:number function with an undefined context node. .
   */
  @org.junit.Test
  public void fnNumber3() {
    final XQuery query = new XQuery(
      "fn:number()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Evaluation of the fn:number function with an undefined context node and argument set to ".". .
   */
  @org.junit.Test
  public void fnNumber4() {
    final XQuery query = new XQuery(
      "fn:number(.)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   * number() applied to a boolean.
   */
  @org.junit.Test
  public void fnNumber5() {
    final XQuery query = new XQuery(
      "number(true())",
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
        assertType("xs:double")
      &&
        assertEq("1.0e0")
      )
    );
  }

  /**
   * number() applied to a boolean.
   */
  @org.junit.Test
  public void fnNumber6() {
    final XQuery query = new XQuery(
      "number(false())",
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
        assertType("xs:double")
      &&
        assertEq("0.0e0")
      )
    );
  }

  /**
   * number() applied to a duration.
   */
  @org.junit.Test
  public void fnNumber7() {
    final XQuery query = new XQuery(
      "number(implicit-timezone())",
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
        assertType("xs:double")
      &&
        assertStringValue(false, "NaN")
      )
    );
  }

  /**
   * number() applied to a string with whitespace.
   */
  @org.junit.Test
  public void fnNumber8() {
    final XQuery query = new XQuery(
      "number(\"  -22e0  \")",
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
        assertType("xs:double")
      &&
        assertEq("-22e0")
      )
    );
  }

  /**
   * number() applied to a string with leading plus sign.
   */
  @org.junit.Test
  public void fnNumber9() {
    final XQuery query = new XQuery(
      "number(\"+22e0\")",
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
        assertType("xs:double")
      &&
        assertEq("2.2e1")
      )
    );
  }

  /**
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnNumberdbl1args1() {
    final XQuery query = new XQuery(
      "fn:number(xs:double(\"-1.7976931348623157E308\"))",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnNumberdbl1args2() {
    final XQuery query = new XQuery(
      "fn:number(xs:double(\"0\"))",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnNumberdbl1args3() {
    final XQuery query = new XQuery(
      "fn:number(xs:double(\"1.7976931348623157E308\"))",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnNumberdec1args1() {
    final XQuery query = new XQuery(
      "fn:number(xs:decimal(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1.0E18")
    );
  }

  /**
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnNumberdec1args2() {
    final XQuery query = new XQuery(
      "fn:number(xs:decimal(\"617375191608514839\")) eq 617375191608514839",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnNumberdec1args3() {
    final XQuery query = new XQuery(
      "fn:number(xs:decimal(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.0E18")
    );
  }

  /**
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnNumberflt1args1() {
    final XQuery query = new XQuery(
      "fn:number(xs:float(\"-3.4028235E38\")) eq -3.4028234663852885E38",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnNumberflt1args2() {
    final XQuery query = new XQuery(
      "fn:number(xs:float(\"0\"))",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnNumberflt1args3() {
    final XQuery query = new XQuery(
      "fn:number(xs:float(\"3.4028235E38\"))",
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
        assertStringValue(false, "3.4028234663852885E38")
      ||
        assertStringValue(false, "3.402823466385289E38")
      ||
        assertStringValue(false, "3.4028234663852886E38")
      )
    );
  }

  /**
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnNumberint1args1() {
    final XQuery query = new XQuery(
      "fn:number(xs:int(\"-2147483648\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-2.147483648E9")
    );
  }

  /**
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnNumberint1args2() {
    final XQuery query = new XQuery(
      "fn:number(xs:int(\"-1873914410\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1.87391441E9")
    );
  }

  /**
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnNumberint1args3() {
    final XQuery query = new XQuery(
      "fn:number(xs:int(\"2147483647\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2.147483647E9")
    );
  }

  /**
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnNumberintg1args1() {
    final XQuery query = new XQuery(
      "fn:number(xs:integer(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1.0E18")
    );
  }

  /**
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnNumberintg1args2() {
    final XQuery query = new XQuery(
      "fn:number(xs:integer(\"830993497117024304\")) eq 830993497117024304",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnNumberintg1args3() {
    final XQuery query = new XQuery(
      "fn:number(xs:integer(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.0E18")
    );
  }

  /**
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnNumberlng1args1() {
    final XQuery query = new XQuery(
      "fn:number(xs:long(\"-92233720368547758\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-9.223372036854776E16")
    );
  }

  /**
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnNumberlng1args2() {
    final XQuery query = new XQuery(
      "fn:number(xs:long(\"-47175562203048468\")) eq -47175562203048468",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnNumberlng1args3() {
    final XQuery query = new XQuery(
      "fn:number(xs:long(\"92233720368547758\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9.223372036854776E16")
    );
  }

  /**
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnNumbernint1args1() {
    final XQuery query = new XQuery(
      "fn:number(xs:negativeInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1.0E18")
    );
  }

  /**
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnNumbernint1args2() {
    final XQuery query = new XQuery(
      "fn:number(xs:negativeInteger(\"-297014075999096793\")) eq -297014075999096793",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnNumbernint1args3() {
    final XQuery query = new XQuery(
      "fn:number(xs:negativeInteger(\"-1\"))",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnNumbernni1args1() {
    final XQuery query = new XQuery(
      "fn:number(xs:nonNegativeInteger(\"0\"))",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnNumbernni1args2() {
    final XQuery query = new XQuery(
      "fn:number(xs:nonNegativeInteger(\"303884545991464527\")) eq 303884545991464527",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnNumbernni1args3() {
    final XQuery query = new XQuery(
      "fn:number(xs:nonNegativeInteger(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.0E18")
    );
  }

  /**
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnNumbernpi1args1() {
    final XQuery query = new XQuery(
      "fn:number(xs:nonPositiveInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1.0E18")
    );
  }

  /**
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnNumbernpi1args2() {
    final XQuery query = new XQuery(
      "fn:number(xs:nonPositiveInteger(\"-475688437271870490\"))",
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
        assertStringValue(false, "-4.7568843727187049E17")
      ||
        assertStringValue(false, "-4.756884372718705E17")
      ||
        assertStringValue(false, "-4.7568843727187046E17")
      )
    );
  }

  /**
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnNumbernpi1args3() {
    final XQuery query = new XQuery(
      "fn:number(xs:nonPositiveInteger(\"0\"))",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnNumberpint1args1() {
    final XQuery query = new XQuery(
      "fn:number(xs:positiveInteger(\"1\"))",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnNumberpint1args2() {
    final XQuery query = new XQuery(
      "fn:number(xs:positiveInteger(\"52704602390610033\")) eq 52704602390610033",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnNumberpint1args3() {
    final XQuery query = new XQuery(
      "fn:number(xs:positiveInteger(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.0E18")
    );
  }

  /**
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnNumbersht1args1() {
    final XQuery query = new XQuery(
      "fn:number(xs:short(\"-32768\"))",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnNumbersht1args2() {
    final XQuery query = new XQuery(
      "fn:number(xs:short(\"-5324\"))",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnNumbersht1args3() {
    final XQuery query = new XQuery(
      "fn:number(xs:short(\"32767\"))",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnNumberulng1args1() {
    final XQuery query = new XQuery(
      "fn:number(xs:unsignedLong(\"0\"))",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnNumberulng1args2() {
    final XQuery query = new XQuery(
      "fn:number(xs:unsignedLong(\"130747108607674654\"))",
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
        assertStringValue(false, "1.3074710860767466E17")
      ||
        assertStringValue(false, "1.30747108607674648E17")
      ||
        assertStringValue(false, "1.30747108607674649E17")
      ||
        assertStringValue(false, "1.3074710860767465E17")
      ||
        assertStringValue(false, "1.30747108607674651E17")
      ||
        assertStringValue(false, "1.30747108607674652E17")
      ||
        assertStringValue(false, "1.30747108607674653E17")
      ||
        assertStringValue(false, "1.30747108607674654E17")
      ||
        assertStringValue(false, "1.30747108607674655E17")
      ||
        assertStringValue(false, "1.30747108607674656E17")
      ||
        assertStringValue(false, "1.30747108607674657E17")
      ||
        assertStringValue(false, "1.30747108607674658E17")
      ||
        assertStringValue(false, "1.30747108607674659E17")
      ||
        assertStringValue(false, "1.30747108607674661E17")
      ||
        assertStringValue(false, "1.30747108607674662E17")
      ||
        assertStringValue(false, "1.30747108607674663E17")
      ||
        assertStringValue(false, "1.30747108607674664E17")
      )
    );
  }

  /**
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnNumberulng1args3() {
    final XQuery query = new XQuery(
      "fn:number(xs:unsignedLong(\"184467440737095516\")) eq 1.8446744073709551E17",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnNumberusht1args1() {
    final XQuery query = new XQuery(
      "fn:number(xs:unsignedShort(\"0\"))",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnNumberusht1args2() {
    final XQuery query = new XQuery(
      "fn:number(xs:unsignedShort(\"44633\"))",
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
   *  Evaluates The "number" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnNumberusht1args3() {
    final XQuery query = new XQuery(
      "fn:number(xs:unsignedShort(\"65535\"))",
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
