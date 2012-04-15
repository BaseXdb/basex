package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the lower-case() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnLowerCase extends QT3TestSet {

  /**
   *  A test whose essence is: `lower-case()`. .
   */
  @org.junit.Test
  public void kLowerCaseFunc1() {
    final XQuery query = new XQuery(
      "lower-case()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `lower-case("string", "wrong param")`. .
   */
  @org.junit.Test
  public void kLowerCaseFunc2() {
    final XQuery query = new XQuery(
      "lower-case(\"string\", \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `lower-case(()) eq ""`. .
   */
  @org.junit.Test
  public void kLowerCaseFunc3() {
    final XQuery query = new XQuery(
      "lower-case(()) eq \"\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `upper-case("abCd0") eq "ABCD0"`. .
   */
  @org.junit.Test
  public void kLowerCaseFunc4() {
    final XQuery query = new XQuery(
      "upper-case(\"abCd0\") eq \"ABCD0\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of lower-case function as per example 1 (for this function) from the F&O specs. .
   */
  @org.junit.Test
  public void fnLowerCase1() {
    final XQuery query = new XQuery(
      "fn:lower-case(\"ABc!D\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "abc!d")
    );
  }

  /**
   *  Evaluation of lower-case function with argument set to "*****" .
   */
  @org.junit.Test
  public void fnLowerCase10() {
    final XQuery query = new XQuery(
      "fn:lower-case(\"*****\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "*****")
    );
  }

  /**
   *  Evaluation of lower-case function with argument set to another lower-case function .
   */
  @org.junit.Test
  public void fnLowerCase11() {
    final XQuery query = new XQuery(
      "fn:lower-case(lower-case(\"zzzzz\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "zzzzz")
    );
  }

  /**
   *  Evaluation of lower-case function as an argument to the "fn:boolean" function .
   */
  @org.junit.Test
  public void fnLowerCase12() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:lower-case(\"abcde\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of lower-case function as an argument to the "fn:concat" function .
   */
  @org.junit.Test
  public void fnLowerCase13() {
    final XQuery query = new XQuery(
      "fn:concat(fn:lower-case(\"abcde\"), fn:lower-case(\"fghi\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "abcdefghi")
    );
  }

  /**
   *  Evaluation of lower-case function as an argument to the "fn:not" function .
   */
  @org.junit.Test
  public void fnLowerCase14() {
    final XQuery query = new XQuery(
      "fn:not(fn:lower-case(\"abcde\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of lower-case function with argument set to "%$#@!" .
   */
  @org.junit.Test
  public void fnLowerCase15() {
    final XQuery query = new XQuery(
      "fn:lower-case(\"%$#@!\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "%$#@!")
    );
  }

  /**
   *  Evaluation of lower-case function with argument set to "lower-case" .
   */
  @org.junit.Test
  public void fnLowerCase16() {
    final XQuery query = new XQuery(
      "fn:lower-case(\"lower-case\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "lower-case")
    );
  }

  /**
   *  Evaluation of lower-case function as part of a boolean expression .
   */
  @org.junit.Test
  public void fnLowerCase17() {
    final XQuery query = new XQuery(
      "fn:lower-case(\"abc\") and fn:lower-case(\"abc\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of lower-case function using the empty sequence Uses the count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnLowerCase2() {
    final XQuery query = new XQuery(
      "fn:count(fn:lower-case(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluation of lower-case function that uses only numbers as part of argument Use of count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnLowerCase3() {
    final XQuery query = new XQuery(
      "fn:lower-case(\"12345\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "12345")
    );
  }

  /**
   *  Evaluation of lower-case function that uses both numbers and letters as part of argument .
   */
  @org.junit.Test
  public void fnLowerCase4() {
    final XQuery query = new XQuery(
      "fn:lower-case(\"12345abcd\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "12345abcd")
    );
  }

  /**
   *  Evaluation of lower-case function that uses only upper case letters as part of argument .
   */
  @org.junit.Test
  public void fnLowerCase5() {
    final XQuery query = new XQuery(
      "fn:lower-case(\"ABCD\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "abcd")
    );
  }

  /**
   *  Evaluation of lower-case function that uses only lower case letters as part of argument .
   */
  @org.junit.Test
  public void fnLowerCase6() {
    final XQuery query = new XQuery(
      "fn:lower-case(\"abcde\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "abcde")
    );
  }

  /**
   *  Evaluation of lower-case function that uses both upper and lower case letters as part of argument .
   */
  @org.junit.Test
  public void fnLowerCase7() {
    final XQuery query = new XQuery(
      "fn:lower-case(\"ABCDEabcde\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "abcdeabcde")
    );
  }

  /**
   *  Evaluation of lower-case function that uses the empty string as part of argument Uses "fn:count" to avoid the empty file .
   */
  @org.junit.Test
  public void fnLowerCase8() {
    final XQuery query = new XQuery(
      "fn:count(fn:lower-case(\"\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluation of lower-case function that uses the "upper-case" as part of argument .
   */
  @org.junit.Test
  public void fnLowerCase9() {
    final XQuery query = new XQuery(
      "fn:lower-case(upper-case(\"AbcDH\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "abcdh")
    );
  }

  /**
   *  Evaluates The "lower-case" function with the arguments set as follows: $arg = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnLowerCase1args1() {
    final XQuery query = new XQuery(
      "fn:lower-case(xs:string(\"This is a characte\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "this is a characte")
    );
  }

  /**
   *  Evaluates The "lower-case" function with the arguments set as follows: $arg = xs:string(mid range) .
   */
  @org.junit.Test
  public void fnLowerCase1args2() {
    final XQuery query = new XQuery(
      "fn:lower-case(xs:string(\"This is a characte\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "this is a characte")
    );
  }

  /**
   *  Evaluates The "lower-case" function with the arguments set as follows: $arg = xs:string(upper bound) .
   */
  @org.junit.Test
  public void fnLowerCase1args3() {
    final XQuery query = new XQuery(
      "fn:lower-case(xs:string(\"This is a characte\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "this is a characte")
    );
  }
}
