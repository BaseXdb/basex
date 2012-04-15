package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnStringLength extends QT3TestSet {

  /**
   * A test whose essence is: `string-length("a string", "wrong param")`..
   */
  @org.junit.Test
  public void kStringLengthFunc1() {
    final XQuery query = new XQuery(
      "string-length(\"a string\", \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * A test whose essence is: `string-length("ebv") eq 3`..
   */
  @org.junit.Test
  public void kStringLengthFunc2() {
    final XQuery query = new XQuery(
      "string-length(\"ebv\") eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * A test whose essence is: `string-length("ebv") instance of xs:integer`..
   */
  @org.junit.Test
  public void kStringLengthFunc3() {
    final XQuery query = new XQuery(
      "string-length(\"ebv\") instance of xs:integer",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * A test whose essence is: `string-length(()) eq 0`..
   */
  @org.junit.Test
  public void kStringLengthFunc4() {
    final XQuery query = new XQuery(
      "string-length(()) eq 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * A test whose essence is: `string-length("") eq 0`..
   */
  @org.junit.Test
  public void kStringLengthFunc5() {
    final XQuery query = new XQuery(
      "string-length(\"\") eq 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * A test whose essence is: `string-length(()) instance of xs:integer`..
   */
  @org.junit.Test
  public void kStringLengthFunc6() {
    final XQuery query = new XQuery(
      "string-length(()) instance of xs:integer",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * A test whose essence is: `if(false()) then string-length() else true()`..
   */
  @org.junit.Test
  public void kStringLengthFunc7() {
    final XQuery query = new XQuery(
      "if(false()) then string-length() else true()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   * A test whose essence is: `string-length("Harp not on that string, madam; that is past.") eq 45`..
   */
  @org.junit.Test
  public void kStringLengthFunc8() {
    final XQuery query = new XQuery(
      "string-length(\"Harp not on that string, madam; that is past.\") eq 45",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of string-length function as per example 1 (for this function) from the F&O specs. .
   */
  @org.junit.Test
  public void fnStringLength1() {
    final XQuery query = new XQuery(
      "fn:string-length(\"Harp not on that string, madam; that is past.\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("45")
    );
  }

  /**
   *  Evaluation of string-length function with argument set to "*****" .
   */
  @org.junit.Test
  public void fnStringLength10() {
    final XQuery query = new XQuery(
      "fn:string-length(\"*****\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("5")
    );
  }

  /**
   *  Evaluation of string-length function as part of an addition operation .
   */
  @org.junit.Test
  public void fnStringLength11() {
    final XQuery query = new XQuery(
      "fn:string-length(\"zzzzz\") + fn:string-length(\"zzzzz\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("10")
    );
  }

  /**
   *  Evaluation of string-length function as an argument to the "fn:boolean" function .
   */
  @org.junit.Test
  public void fnStringLength12() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string-length(\"abcde\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of string-length function as an argument to the "fn:concat" function .
   */
  @org.junit.Test
  public void fnStringLength13() {
    final XQuery query = new XQuery(
      "fn:concat(fn:string-length(\"abcde\"), fn:string-length(\"fghi\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("\"54\"")
    );
  }

  /**
   *  Evaluation of string-length function as an argument to the "fn:not" function .
   */
  @org.junit.Test
  public void fnStringLength14() {
    final XQuery query = new XQuery(
      "fn:not(fn:string-length(\"abcde\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of string-length function with argument set to "%$#@!" .
   */
  @org.junit.Test
  public void fnStringLength15() {
    final XQuery query = new XQuery(
      "fn:string-length(\"%$#@!\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("5")
    );
  }

  /**
   *  Evaluation of string-length function with argument set to "string-length" .
   */
  @org.junit.Test
  public void fnStringLength16() {
    final XQuery query = new XQuery(
      "fn:string-length(\"string-length\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("13")
    );
  }

  /**
   *  Evaluation of string-length function as part of a boolean expression .
   */
  @org.junit.Test
  public void fnStringLength17() {
    final XQuery query = new XQuery(
      "fn:string-length(\"abc\") and fn:string-length(\"abc\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of string-length function with no argument and no context item defined. .
   */
  @org.junit.Test
  public void fnStringLength18() {
    final XQuery query = new XQuery(
      "fn:string-length()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Evaluation of string-length function with an argument that is a sequence of more than one item .
   */
  @org.junit.Test
  public void fnStringLength19() {
    final XQuery query = new XQuery(
      "fn:string-length(.//employee/@name )",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of string-length function using the empty sequence .
   */
  @org.junit.Test
  public void fnStringLength2() {
    final XQuery query = new XQuery(
      "fn:string-length(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   * Evaluation of string-length function with non-BMP characters..
   */
  @org.junit.Test
  public void fnStringLength20() {
    final XQuery query = new XQuery(
      "string-length(\"𐀂\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   * Evaluation of string-length applied to a function item..
   */
  @org.junit.Test
  public void fnStringLength21() {
    final XQuery query = new XQuery(
      "string-length(string-length#1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of string-length function that uses only numbers as part of argument .
   */
  @org.junit.Test
  public void fnStringLength3() {
    final XQuery query = new XQuery(
      "fn:string-length(\"12345\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("5")
    );
  }

  /**
   *  Evaluation of string-length function that uses both numbers and letters as part of argument .
   */
  @org.junit.Test
  public void fnStringLength4() {
    final XQuery query = new XQuery(
      "fn:string-length(\"12345abcd\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("9")
    );
  }

  /**
   *  Evaluation of string-length function that uses only upper case letters as part of argument .
   */
  @org.junit.Test
  public void fnStringLength5() {
    final XQuery query = new XQuery(
      "fn:string-length(\"ABCD\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("4")
    );
  }

  /**
   *  Evaluation of string-length function that uses only lower case letters as part of argument .
   */
  @org.junit.Test
  public void fnStringLength6() {
    final XQuery query = new XQuery(
      "fn:string-length(\"abcde\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("5")
    );
  }

  /**
   *  Evaluation of string-length function that uses both upper and lower case letters as part of argument .
   */
  @org.junit.Test
  public void fnStringLength7() {
    final XQuery query = new XQuery(
      "fn:string-length(\"ABCDEabcde\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("10")
    );
  }

  /**
   *  Evaluation of string-length function that uses the empty string as part of argument .
   */
  @org.junit.Test
  public void fnStringLength8() {
    final XQuery query = new XQuery(
      "fn:string-length(\"\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluation of string-length function that uses the "string" as part of argument .
   */
  @org.junit.Test
  public void fnStringLength9() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:string(\"AbcDH\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("5")
    );
  }

  /**
   *  Evaluates The "string-length" function with the arguments set as follows: $arg = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnStringLength1args1() {
    final XQuery query = new XQuery(
      "fn:string-length(xs:string(\"This is a characte\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("18")
    );
  }

  /**
   *  Evaluates The "string-length" function with the arguments set as follows: $arg = xs:string(mid range) .
   */
  @org.junit.Test
  public void fnStringLength1args2() {
    final XQuery query = new XQuery(
      "fn:string-length(xs:string(\"This is a characte\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("18")
    );
  }

  /**
   *  Evaluates The "string-length" function with the arguments set as follows: $arg = xs:string(upper bound) .
   */
  @org.junit.Test
  public void fnStringLength1args3() {
    final XQuery query = new XQuery(
      "fn:string-length(xs:string(\"This is a characte\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("18")
    );
  }
}
