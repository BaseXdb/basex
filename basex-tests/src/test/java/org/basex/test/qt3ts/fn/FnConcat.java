package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnConcat extends QT3TestSet {

  /**
   *  A test whose essence is: `concat()`. .
   */
  @org.junit.Test
  public void kConcatFunc1() {
    final XQuery query = new XQuery(
      "concat()",
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
   *  A test whose essence is: `concat("a string")`. .
   */
  @org.junit.Test
  public void kConcatFunc2() {
    final XQuery query = new XQuery(
      "concat(\"a string\")",
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
   *  A test whose essence is: `concat("ab", "c") eq "abc"`. .
   */
  @org.junit.Test
  public void kConcatFunc3() {
    final XQuery query = new XQuery(
      "concat(\"ab\", \"c\") eq \"abc\"",
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
   *  A test whose essence is: `concat("ab", "c") instance of xs:string`. .
   */
  @org.junit.Test
  public void kConcatFunc4() {
    final XQuery query = new XQuery(
      "concat(\"ab\", \"c\") instance of xs:string",
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
   *  A test whose essence is: `concat((), ()) instance of xs:string`. .
   */
  @org.junit.Test
  public void kConcatFunc5() {
    final XQuery query = new XQuery(
      "concat((), ()) instance of xs:string",
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
   *  A test whose essence is: `concat((), ()) eq ""`. .
   */
  @org.junit.Test
  public void kConcatFunc6() {
    final XQuery query = new XQuery(
      "concat((), ()) eq \"\"",
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
   *  A test whose essence is: `concat('a', 'b', 'c', (), 'd', 'e', 'f', 'g', 'h', ' ', 'i', 'j', 'k l') eq "abcdefgh ijk l"`. .
   */
  @org.junit.Test
  public void kConcatFunc7() {
    final XQuery query = new XQuery(
      "concat('a', 'b', 'c', (), 'd', 'e', 'f', 'g', 'h', ' ', 'i', 'j', 'k l') eq \"abcdefgh ijk l\"",
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
   *  A test whose essence is: `concat(1, 2, 3) eq "123"`. .
   */
  @org.junit.Test
  public void kConcatFunc8() {
    final XQuery query = new XQuery(
      "concat(1, 2, 3) eq \"123\"",
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
   *  A test whose essence is: `concat(1, "2", 3) eq "123"`. .
   */
  @org.junit.Test
  public void kConcatFunc9() {
    final XQuery query = new XQuery(
      "concat(1, \"2\", 3) eq \"123\"",
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
   *  Invalid amount of arguments to fn:concat(). .
   */
  @org.junit.Test
  public void k2ConcatFunc1() {
    final XQuery query = new XQuery(
      "concat((\"a\", \"b\"), \"c\")",
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
   *  Invalid amount of arguments to fn:concat(). .
   */
  @org.junit.Test
  public void k2ConcatFunc2() {
    final XQuery query = new XQuery(
      "concat(\"1\", \"2\", \"3\", (\"a\", \"b\"), \"c\")",
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
   *  Invalid arguments to fn:concat(). .
   */
  @org.junit.Test
  public void k2ConcatFunc3() {
    final XQuery query = new XQuery(
      "concat(\"1\", \"2\", \"3\", \"c\", (\"a\", \"b\"))",
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
   *  test fn:boolean(fn:concat()) where concat returns empty string .
   */
  @org.junit.Test
  public void cbclConcat001() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:concat('', ''))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluation of concat function as per example 1 (for this function) from the F&O specs. .
   */
  @org.junit.Test
  public void fnConcat1() {
    final XQuery query = new XQuery(
      "fn:concat('un', 'grateful')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ungrateful")
    );
  }

  /**
   * Evaluation of concat function with argument set to "*****" .
   */
  @org.junit.Test
  public void fnConcat10() {
    final XQuery query = new XQuery(
      "fn:concat(\"**\",\"***\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "*****")
    );
  }

  /**
   * Evaluation of concat function with argument that uses another concat function .
   */
  @org.junit.Test
  public void fnConcat11() {
    final XQuery query = new XQuery(
      "fn:concat(fn:concat(\"zzz\",\"zz\"),\"123\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "zzzzz123")
    );
  }

  /**
   * Evaluation of concat function as an argument to the "fn:boolean" function .
   */
  @org.junit.Test
  public void fnConcat12() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:concat(\"ab\",\"cde\"))",
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
   * Evaluation of concat function as an argument to the "fn:string" function .
   */
  @org.junit.Test
  public void fnConcat13() {
    final XQuery query = new XQuery(
      "fn:string(fn:concat(\"abc\",\"de\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abcde")
    );
  }

  /**
   * Evaluation of concat function as an argument to the "fn:not" function .
   */
  @org.junit.Test
  public void fnConcat14() {
    final XQuery query = new XQuery(
      "fn:not(fn:concat(\"ab\",\"cde\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluation of concat function with argument set to "%$",#@!" .
   */
  @org.junit.Test
  public void fnConcat15() {
    final XQuery query = new XQuery(
      "fn:concat(\"%$\",\"#@!\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "%$#@!")
    );
  }

  /**
   * Evaluation of concat function with argument set to "concat","concat" .
   */
  @org.junit.Test
  public void fnConcat16() {
    final XQuery query = new XQuery(
      "fn:concat(\"concat\",\"concat\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "concatconcat")
    );
  }

  /**
   * Evaluation of concat function as part of a boolean expression .
   */
  @org.junit.Test
  public void fnConcat17() {
    final XQuery query = new XQuery(
      "fn:concat(\"abc\",\"abc\") and fn:concat(\"abc\",\"abc\")",
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
   * Can't have a function item as an argument to concat .
   */
  @org.junit.Test
  public void fnConcat18() {
    final XQuery query = new XQuery(
      "fn:concat(\"abc\",\"abc\", fn:concat#3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOTY0013")
    );
  }

  /**
   * Evaluation of concat function as per example 2 (for this function) from the F&O specs. .
   */
  @org.junit.Test
  public void fnConcat2() {
    final XQuery query = new XQuery(
      "fn:concat('Thy ', (), 'old ', \"groans\", \"\", ' ring', ' yet', ' in', ' my', ' ancient',' ears.')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Thy old groans ring yet in my ancient ears.")
    );
  }

  /**
   * Evaluation of concat function as per example 3 (for this function) from the F&O specs. .
   */
  @org.junit.Test
  public void fnConcat3() {
    final XQuery query = new XQuery(
      "fn:concat('Ciao!',())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Ciao!")
    );
  }

  /**
   * Evaluation of concat function as per example 4 (for this function) from the F&O specs. .
   */
  @org.junit.Test
  public void fnConcat4() {
    final XQuery query = new XQuery(
      "fn:concat('Ingratitude, ', 'thou ', 'marble-hearted', ' fiend!')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Ingratitude, thou marble-hearted fiend!")
    );
  }

  /**
   * Evaluation of concat function that uses only upper case letters as part of argument .
   */
  @org.junit.Test
  public void fnConcat5() {
    final XQuery query = new XQuery(
      "fn:concat(\"AB\",\"CD\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ABCD")
    );
  }

  /**
   * Evaluation of concat function that uses only lower case letters as part of argument .
   */
  @org.junit.Test
  public void fnConcat6() {
    final XQuery query = new XQuery(
      "fn:concat(\"abc\",\"de\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abcde")
    );
  }

  /**
   * Evaluation of concat function that uses both upper and lower case letters as part of argument .
   */
  @org.junit.Test
  public void fnConcat7() {
    final XQuery query = new XQuery(
      "fn:concat(\"ABCDE\",\"abcde\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ABCDEabcde")
    );
  }

  /**
   * Evaluation of concat function that uses the empty string as part of argument Uses "fn:count" to avoid the empty file .
   */
  @org.junit.Test
  public void fnConcat8() {
    final XQuery query = new XQuery(
      "fn:count(fn:concat(\"\",\"\"))",
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
   * Evaluation of concat function that uses the "upper-case" function as part of argument .
   */
  @org.junit.Test
  public void fnConcat9() {
    final XQuery query = new XQuery(
      "fn:concat(fn:upper-case(\"Abc\"),fn:upper-case(\"DH\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ABCDH")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnConcatdbl2args1() {
    final XQuery query = new XQuery(
      "fn:concat(xs:double(\"-1.7976931348623157E308\"),xs:double(\"-1.7976931348623157E308\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1.7976931348623157E308-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:double(mid range) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnConcatdbl2args2() {
    final XQuery query = new XQuery(
      "fn:concat(xs:double(\"0\"),xs:double(\"-1.7976931348623157E308\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:double(upper bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnConcatdbl2args3() {
    final XQuery query = new XQuery(
      "fn:concat(xs:double(\"1.7976931348623157E308\"),xs:double(\"-1.7976931348623157E308\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.7976931348623157E308-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnConcatdbl2args4() {
    final XQuery query = new XQuery(
      "fn:concat(xs:double(\"-1.7976931348623157E308\"),xs:double(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1.7976931348623157E3080")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnConcatdbl2args5() {
    final XQuery query = new XQuery(
      "fn:concat(xs:double(\"-1.7976931348623157E308\"),xs:double(\"1.7976931348623157E308\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1.7976931348623157E3081.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnConcatdec2args1() {
    final XQuery query = new XQuery(
      "fn:concat(xs:decimal(\"-999999999999999999\"),xs:decimal(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999-999999999999999999")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:decimal(mid range) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnConcatdec2args2() {
    final XQuery query = new XQuery(
      "fn:concat(xs:decimal(\"617375191608514839\"),xs:decimal(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "617375191608514839-999999999999999999")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:decimal(upper bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnConcatdec2args3() {
    final XQuery query = new XQuery(
      "fn:concat(xs:decimal(\"999999999999999999\"),xs:decimal(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "999999999999999999-999999999999999999")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnConcatdec2args4() {
    final XQuery query = new XQuery(
      "fn:concat(xs:decimal(\"-999999999999999999\"),xs:decimal(\"617375191608514839\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999617375191608514839")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnConcatdec2args5() {
    final XQuery query = new XQuery(
      "fn:concat(xs:decimal(\"-999999999999999999\"),xs:decimal(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999999999999999999999")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnConcatflt2args1() {
    final XQuery query = new XQuery(
      "fn:concat(xs:float(\"-3.4028235E38\"),xs:float(\"-3.4028235E38\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-3.4028235E38-3.4028235E38")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:float(mid range) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnConcatflt2args2() {
    final XQuery query = new XQuery(
      "fn:concat(xs:float(\"0\"),xs:float(\"-3.4028235E38\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0-3.4028235E38")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:float(upper bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnConcatflt2args3() {
    final XQuery query = new XQuery(
      "fn:concat(xs:float(\"3.4028235E38\"),xs:float(\"-3.4028235E38\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3.4028235E38-3.4028235E38")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnConcatflt2args4() {
    final XQuery query = new XQuery(
      "fn:concat(xs:float(\"-3.4028235E38\"),xs:float(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-3.4028235E380")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnConcatflt2args5() {
    final XQuery query = new XQuery(
      "fn:concat(xs:float(\"-3.4028235E38\"),xs:float(\"3.4028235E38\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-3.4028235E383.4028235E38")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnConcatint2args1() {
    final XQuery query = new XQuery(
      "fn:concat(xs:int(\"-2147483648\"),xs:int(\"-2147483648\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-2147483648-2147483648")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:int(mid range) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnConcatint2args2() {
    final XQuery query = new XQuery(
      "fn:concat(xs:int(\"-1873914410\"),xs:int(\"-2147483648\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1873914410-2147483648")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:int(upper bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnConcatint2args3() {
    final XQuery query = new XQuery(
      "fn:concat(xs:int(\"2147483647\"),xs:int(\"-2147483648\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2147483647-2147483648")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnConcatint2args4() {
    final XQuery query = new XQuery(
      "fn:concat(xs:int(\"-2147483648\"),xs:int(\"-1873914410\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-2147483648-1873914410")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnConcatint2args5() {
    final XQuery query = new XQuery(
      "fn:concat(xs:int(\"-2147483648\"),xs:int(\"2147483647\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-21474836482147483647")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnConcatintg2args1() {
    final XQuery query = new XQuery(
      "fn:concat(xs:integer(\"-999999999999999999\"),xs:integer(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999-999999999999999999")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:integer(mid range) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnConcatintg2args2() {
    final XQuery query = new XQuery(
      "fn:concat(xs:integer(\"830993497117024304\"),xs:integer(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "830993497117024304-999999999999999999")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:integer(upper bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnConcatintg2args3() {
    final XQuery query = new XQuery(
      "fn:concat(xs:integer(\"999999999999999999\"),xs:integer(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "999999999999999999-999999999999999999")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnConcatintg2args4() {
    final XQuery query = new XQuery(
      "fn:concat(xs:integer(\"-999999999999999999\"),xs:integer(\"830993497117024304\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999830993497117024304")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnConcatintg2args5() {
    final XQuery query = new XQuery(
      "fn:concat(xs:integer(\"-999999999999999999\"),xs:integer(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999999999999999999999")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnConcatlng2args1() {
    final XQuery query = new XQuery(
      "fn:concat(xs:long(\"-92233720368547758\"),xs:long(\"-92233720368547758\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-92233720368547758-92233720368547758")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:long(mid range) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnConcatlng2args2() {
    final XQuery query = new XQuery(
      "fn:concat(xs:long(\"-47175562203048468\"),xs:long(\"-92233720368547758\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-47175562203048468-92233720368547758")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:long(upper bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnConcatlng2args3() {
    final XQuery query = new XQuery(
      "fn:concat(xs:long(\"92233720368547758\"),xs:long(\"-92233720368547758\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "92233720368547758-92233720368547758")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnConcatlng2args4() {
    final XQuery query = new XQuery(
      "fn:concat(xs:long(\"-92233720368547758\"),xs:long(\"-47175562203048468\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-92233720368547758-47175562203048468")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnConcatlng2args5() {
    final XQuery query = new XQuery(
      "fn:concat(xs:long(\"-92233720368547758\"),xs:long(\"92233720368547758\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-9223372036854775892233720368547758")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnConcatnint2args1() {
    final XQuery query = new XQuery(
      "fn:concat(xs:negativeInteger(\"-999999999999999999\"),xs:negativeInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999-999999999999999999")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:negativeInteger(mid range) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnConcatnint2args2() {
    final XQuery query = new XQuery(
      "fn:concat(xs:negativeInteger(\"-297014075999096793\"),xs:negativeInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-297014075999096793-999999999999999999")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:negativeInteger(upper bound) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnConcatnint2args3() {
    final XQuery query = new XQuery(
      "fn:concat(xs:negativeInteger(\"-1\"),xs:negativeInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1-999999999999999999")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnConcatnint2args4() {
    final XQuery query = new XQuery(
      "fn:concat(xs:negativeInteger(\"-999999999999999999\"),xs:negativeInteger(\"-297014075999096793\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999-297014075999096793")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnConcatnint2args5() {
    final XQuery query = new XQuery(
      "fn:concat(xs:negativeInteger(\"-999999999999999999\"),xs:negativeInteger(\"-1\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999-1")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnConcatnni2args1() {
    final XQuery query = new XQuery(
      "fn:concat(xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "00")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(mid range) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnConcatnni2args2() {
    final XQuery query = new XQuery(
      "fn:concat(xs:nonNegativeInteger(\"303884545991464527\"),xs:nonNegativeInteger(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3038845459914645270")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(upper bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnConcatnni2args3() {
    final XQuery query = new XQuery(
      "fn:concat(xs:nonNegativeInteger(\"999999999999999999\"),xs:nonNegativeInteger(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9999999999999999990")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnConcatnni2args4() {
    final XQuery query = new XQuery(
      "fn:concat(xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"303884545991464527\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0303884545991464527")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnConcatnni2args5() {
    final XQuery query = new XQuery(
      "fn:concat(xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0999999999999999999")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnConcatnpi2args1() {
    final XQuery query = new XQuery(
      "fn:concat(xs:nonPositiveInteger(\"-999999999999999999\"),xs:nonPositiveInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999-999999999999999999")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(mid range) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnConcatnpi2args2() {
    final XQuery query = new XQuery(
      "fn:concat(xs:nonPositiveInteger(\"-475688437271870490\"),xs:nonPositiveInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-475688437271870490-999999999999999999")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(upper bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnConcatnpi2args3() {
    final XQuery query = new XQuery(
      "fn:concat(xs:nonPositiveInteger(\"0\"),xs:nonPositiveInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0-999999999999999999")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnConcatnpi2args4() {
    final XQuery query = new XQuery(
      "fn:concat(xs:nonPositiveInteger(\"-999999999999999999\"),xs:nonPositiveInteger(\"-475688437271870490\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999-475688437271870490")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnConcatnpi2args5() {
    final XQuery query = new XQuery(
      "fn:concat(xs:nonPositiveInteger(\"-999999999999999999\"),xs:nonPositiveInteger(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-9999999999999999990")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnConcatpint2args1() {
    final XQuery query = new XQuery(
      "fn:concat(xs:positiveInteger(\"1\"),xs:positiveInteger(\"1\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "11")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:positiveInteger(mid range) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnConcatpint2args2() {
    final XQuery query = new XQuery(
      "fn:concat(xs:positiveInteger(\"52704602390610033\"),xs:positiveInteger(\"1\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "527046023906100331")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:positiveInteger(upper bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnConcatpint2args3() {
    final XQuery query = new XQuery(
      "fn:concat(xs:positiveInteger(\"999999999999999999\"),xs:positiveInteger(\"1\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9999999999999999991")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnConcatpint2args4() {
    final XQuery query = new XQuery(
      "fn:concat(xs:positiveInteger(\"1\"),xs:positiveInteger(\"52704602390610033\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "152704602390610033")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnConcatpint2args5() {
    final XQuery query = new XQuery(
      "fn:concat(xs:positiveInteger(\"1\"),xs:positiveInteger(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1999999999999999999")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnConcatsht2args1() {
    final XQuery query = new XQuery(
      "fn:concat(xs:short(\"-32768\"),xs:short(\"-32768\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-32768-32768")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:short(mid range) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnConcatsht2args2() {
    final XQuery query = new XQuery(
      "fn:concat(xs:short(\"-5324\"),xs:short(\"-32768\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-5324-32768")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:short(upper bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnConcatsht2args3() {
    final XQuery query = new XQuery(
      "fn:concat(xs:short(\"32767\"),xs:short(\"-32768\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "32767-32768")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnConcatsht2args4() {
    final XQuery query = new XQuery(
      "fn:concat(xs:short(\"-32768\"),xs:short(\"-5324\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-32768-5324")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnConcatsht2args5() {
    final XQuery query = new XQuery(
      "fn:concat(xs:short(\"-32768\"),xs:short(\"32767\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-3276832767")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnConcatulng2args1() {
    final XQuery query = new XQuery(
      "fn:concat(xs:unsignedLong(\"0\"),xs:unsignedLong(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "00")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:unsignedLong(mid range) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnConcatulng2args2() {
    final XQuery query = new XQuery(
      "fn:concat(xs:unsignedLong(\"130747108607674654\"),xs:unsignedLong(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1307471086076746540")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:unsignedLong(upper bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnConcatulng2args3() {
    final XQuery query = new XQuery(
      "fn:concat(xs:unsignedLong(\"184467440737095516\"),xs:unsignedLong(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1844674407370955160")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnConcatulng2args4() {
    final XQuery query = new XQuery(
      "fn:concat(xs:unsignedLong(\"0\"),xs:unsignedLong(\"130747108607674654\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0130747108607674654")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnConcatulng2args5() {
    final XQuery query = new XQuery(
      "fn:concat(xs:unsignedLong(\"0\"),xs:unsignedLong(\"184467440737095516\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0184467440737095516")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnConcatusht2args1() {
    final XQuery query = new XQuery(
      "fn:concat(xs:unsignedShort(\"0\"),xs:unsignedShort(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "00")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:unsignedShort(mid range) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnConcatusht2args2() {
    final XQuery query = new XQuery(
      "fn:concat(xs:unsignedShort(\"44633\"),xs:unsignedShort(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "446330")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:unsignedShort(upper bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnConcatusht2args3() {
    final XQuery query = new XQuery(
      "fn:concat(xs:unsignedShort(\"65535\"),xs:unsignedShort(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "655350")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnConcatusht2args4() {
    final XQuery query = new XQuery(
      "fn:concat(xs:unsignedShort(\"0\"),xs:unsignedShort(\"44633\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "044633")
    );
  }

  /**
   *  Evaluates The "concat" function with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnConcatusht2args5() {
    final XQuery query = new XQuery(
      "fn:concat(xs:unsignedShort(\"0\"),xs:unsignedShort(\"65535\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "065535")
    );
  }
}
