package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the upper-case() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnUpperCase extends QT3TestSet {

  /**
   *  A test whose essence is: `upper-case()`. .
   */
  @org.junit.Test
  public void kUpperCaseFunc1() {
    final XQuery query = new XQuery(
      "upper-case()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `upper-case("string", "wrong param")`. .
   */
  @org.junit.Test
  public void kUpperCaseFunc2() {
    final XQuery query = new XQuery(
      "upper-case(\"string\", \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `upper-case(()) eq ""`. .
   */
  @org.junit.Test
  public void kUpperCaseFunc3() {
    final XQuery query = new XQuery(
      "upper-case(()) eq \"\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `lower-case("ABc!D") eq "abc!d"`. .
   */
  @org.junit.Test
  public void kUpperCaseFunc4() {
    final XQuery query = new XQuery(
      "lower-case(\"ABc!D\") eq \"abc!d\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of upper-case function as per example 1 (for this function) from the F&O specs. .
   */
  @org.junit.Test
  public void fnUpperCase1() {
    final XQuery query = new XQuery(
      "fn:upper-case(\"ABc!D\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ABC!D")
    );
  }

  /**
   *  Evaluation of upper-case function with argument set to "*****" .
   */
  @org.junit.Test
  public void fnUpperCase10() {
    final XQuery query = new XQuery(
      "fn:upper-case(\"*****\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "*****")
    );
  }

  /**
   *  Evaluation of upper-case function with argument set to another upper case function .
   */
  @org.junit.Test
  public void fnUpperCase11() {
    final XQuery query = new XQuery(
      "fn:upper-case(upper-case(\"zzzzz\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ZZZZZ")
    );
  }

  /**
   *  Evaluation of upper-case function as an argument to the "fn:boolean" function .
   */
  @org.junit.Test
  public void fnUpperCase12() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:upper-case(\"abcde\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of upper-case function as an argument to the "fn:concat" function .
   */
  @org.junit.Test
  public void fnUpperCase13() {
    final XQuery query = new XQuery(
      "fn:concat(fn:upper-case(\"abcde\"), fn:upper-case(\"fghi\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ABCDEFGHI")
    );
  }

  /**
   *  Evaluation of upper-case function as an argument to the "fn:not" function .
   */
  @org.junit.Test
  public void fnUpperCase14() {
    final XQuery query = new XQuery(
      "fn:not(fn:upper-case(\"abcde\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of upper-case function with argument set to "%$#@!" .
   */
  @org.junit.Test
  public void fnUpperCase15() {
    final XQuery query = new XQuery(
      "fn:upper-case(\"%$#@!\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "%$#@!")
    );
  }

  /**
   *  Evaluation of upper-case function with argument set to "upper-case" .
   */
  @org.junit.Test
  public void fnUpperCase16() {
    final XQuery query = new XQuery(
      "fn:upper-case(\"upper-case\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "UPPER-CASE")
    );
  }

  /**
   *  Evaluation of upper-case function as part of a boolean expression .
   */
  @org.junit.Test
  public void fnUpperCase17() {
    final XQuery query = new XQuery(
      "fn:upper-case(\"abc\") and fn:upper-case(\"abc\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of upper-case function using the empty sequence Uses the count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnUpperCase2() {
    final XQuery query = new XQuery(
      "fn:count(fn:upper-case(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluation of upper-case function that uses only numbers as part of argument Use of count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnUpperCase3() {
    final XQuery query = new XQuery(
      "fn:upper-case(\"12345\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "12345")
    );
  }

  /**
   *  Evaluation of upper-case function that uses both numbers and letters as part of argument .
   */
  @org.junit.Test
  public void fnUpperCase4() {
    final XQuery query = new XQuery(
      "fn:upper-case(\"12345abcd\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "12345ABCD")
    );
  }

  /**
   *  Evaluation of upper-case function that uses only upper case letters as part of argument .
   */
  @org.junit.Test
  public void fnUpperCase5() {
    final XQuery query = new XQuery(
      "fn:upper-case(\"ABCD\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ABCD")
    );
  }

  /**
   *  Evaluation of upper-case function that uses only lower case letters as part of argument .
   */
  @org.junit.Test
  public void fnUpperCase6() {
    final XQuery query = new XQuery(
      "fn:upper-case(\"abcde\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ABCDE")
    );
  }

  /**
   *  Evaluation of upper-case function that uses both upper and lower case letters as part of argument .
   */
  @org.junit.Test
  public void fnUpperCase7() {
    final XQuery query = new XQuery(
      "fn:upper-case(\"ABCDEabcde\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ABCDEABCDE")
    );
  }

  /**
   *  Evaluation of upper-case function that uses the empty string as part of argument Uses "fn:count" to avoid the empty file .
   */
  @org.junit.Test
  public void fnUpperCase8() {
    final XQuery query = new XQuery(
      "fn:count(fn:upper-case(\"\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluation of upper-case function that uses the "lower-case" as part of argument .
   */
  @org.junit.Test
  public void fnUpperCase9() {
    final XQuery query = new XQuery(
      "fn:upper-case(lower-case(\"AbcDH\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ABCDH")
    );
  }

  /**
   *  Evaluates The "upper-case" function with the arguments set as follows: $arg = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnUpperCase1args1() {
    final XQuery query = new XQuery(
      "fn:upper-case(xs:string(\"This is a characte\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "THIS IS A CHARACTE")
    );
  }

  /**
   *  Evaluates The "upper-case" function with the arguments set as follows: $arg = xs:string(mid range) .
   */
  @org.junit.Test
  public void fnUpperCase1args2() {
    final XQuery query = new XQuery(
      "fn:upper-case(xs:string(\"This is a characte\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "THIS IS A CHARACTE")
    );
  }

  /**
   *  Evaluates The "upper-case" function with the arguments set as follows: $arg = xs:string(upper bound) .
   */
  @org.junit.Test
  public void fnUpperCase1args3() {
    final XQuery query = new XQuery(
      "fn:upper-case(xs:string(\"This is a characte\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "THIS IS A CHARACTE")
    );
  }
}
