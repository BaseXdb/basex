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
public class FnTranslate extends QT3TestSet {

  /**
   *  A test whose essence is: `translate()`. .
   */
  @org.junit.Test
  public void kTranslateFunc1() {
    final XQuery query = new XQuery(
      "translate()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `translate("--aaa--","abc-","ABC") eq "AAA"`. .
   */
  @org.junit.Test
  public void kTranslateFunc10() {
    final XQuery query = new XQuery(
      "translate(\"--aaa--\",\"abc-\",\"ABC\") eq \"AAA\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `translate("string", "map string")`. .
   */
  @org.junit.Test
  public void kTranslateFunc2() {
    final XQuery query = new XQuery(
      "translate(\"string\", \"map string\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `translate("arg", "map string", "transString", "wrong param")`. .
   */
  @org.junit.Test
  public void kTranslateFunc3() {
    final XQuery query = new XQuery(
      "translate(\"arg\", \"map string\", \"transString\", \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `translate("--aaa--","-","") eq "aaa"`. .
   */
  @org.junit.Test
  public void kTranslateFunc4() {
    final XQuery query = new XQuery(
      "translate(\"--aaa--\",\"-\",\"\") eq \"aaa\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `translate("--aaa--","bbb++","") eq "--aaa--"`. .
   */
  @org.junit.Test
  public void kTranslateFunc5() {
    final XQuery query = new XQuery(
      "translate(\"--aaa--\",\"bbb++\",\"\") eq \"--aaa--\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `translate("argstr", "", "matrs") eq "argstr"`. .
   */
  @org.junit.Test
  public void kTranslateFunc6() {
    final XQuery query = new XQuery(
      "translate(\"argstr\", \"\", \"matrs\") eq \"argstr\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `translate((), "map", "trans") eq ""`. .
   */
  @org.junit.Test
  public void kTranslateFunc7() {
    final XQuery query = new XQuery(
      "translate((), \"map\", \"trans\") eq \"\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `translate("abcdabc", "abc", "AB") eq "ABdAB"`. .
   */
  @org.junit.Test
  public void kTranslateFunc8() {
    final XQuery query = new XQuery(
      "translate(\"abcdabc\", \"abc\", \"AB\") eq \"ABdAB\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `translate("bar","abc","ABC") eq "BAr"`. .
   */
  @org.junit.Test
  public void kTranslateFunc9() {
    final XQuery query = new XQuery(
      "translate(\"bar\",\"abc\",\"ABC\") eq \"BAr\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The second argument to fn:translate() cannot be the empty sequence. .
   */
  @org.junit.Test
  public void k2TranslateFunc1() {
    final XQuery query = new XQuery(
      "fn:translate(\"arg\", (), \"transString\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  The third argument to fn:translate() cannot be the empty sequence. .
   */
  @org.junit.Test
  public void k2TranslateFunc2() {
    final XQuery query = new XQuery(
      "fn:translate(\"arg\", \"mapString\", ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Simple test of translate function as per example one for this function from the F andO specs. .
   */
  @org.junit.Test
  public void fnTranslate1() {
    final XQuery query = new XQuery(
      "fn:translate(\"bar\",\"abc\",\"ABC\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "BAr")
    );
  }

  /**
   *  Evaluation of translate function, where all three arguments are an invocation to "fn:string". .
   */
  @org.junit.Test
  public void fnTranslate10() {
    final XQuery query = new XQuery(
      "fn:translate(fn:string(\"ABC\"), fn:string(\"ABC\"), fn:string(\"ABC\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ABC")
    );
  }

  /**
   *  Evaluation of translate function as an argument to the "fn:string" function. .
   */
  @org.junit.Test
  public void fnTranslate11() {
    final XQuery query = new XQuery(
      "fn:string(fn:translate(\"ABC\", \"ABC\", \"ABC\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ABC")
    );
  }

  /**
   *  Evaluation of translate function as an argument to the "fn:string-length" function. .
   */
  @org.junit.Test
  public void fnTranslate12() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:translate(\"ABC\",\"ABC\",\"ABC\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   *  Evaluation of translate function as an argument to the "xs:decimal" constructor function. .
   */
  @org.junit.Test
  public void fnTranslate13() {
    final XQuery query = new XQuery(
      "xs:decimal(fn:translate(\"123\",\"123\",\"123\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "123")
    );
  }

  /**
   *  Evaluation of translate function as an argument to the "xs:integer" constructor function. .
   */
  @org.junit.Test
  public void fnTranslate14() {
    final XQuery query = new XQuery(
      "xs:integer(fn:translate(\"123\",\"123\",\"123\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "123")
    );
  }

  /**
   *  Evaluation of translate function as an argument to the "xs:float" constructor function. .
   */
  @org.junit.Test
  public void fnTranslate15() {
    final XQuery query = new XQuery(
      "xs:float(fn:translate(\"123\",\"123\",\"123\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "123")
    );
  }

  /**
   *  Evaluation of translate function as an argument to the "xs:double" constructor function. .
   */
  @org.junit.Test
  public void fnTranslate16() {
    final XQuery query = new XQuery(
      "xs:double(fn:translate(\"123\",\"123\",\"123\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "123")
    );
  }

  /**
   * Evaluation of translate function using non-BMP characters..
   */
  @org.junit.Test
  public void fnTranslate17() {
    final XQuery query = new XQuery(
      "translate(\"abcd\", \"𐀁a\", \"xy\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("\"ybcd\"")
    );
  }

  /**
   * Evaluation of translate function using non-BMP characters..
   */
  @org.junit.Test
  public void fnTranslate18() {
    final XQuery query = new XQuery(
      "translate(\"abcd\", \"xa\", \"𐀁y\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("\"ybcd\"")
    );
  }

  /**
   * Evaluation of translate function using non-BMP characters..
   */
  @org.junit.Test
  public void fnTranslate19() {
    final XQuery query = new XQuery(
      "translate(\"abcd𐀄e\", \"a𐀄e\", \"XYZ\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("\"XbcdYZ\"")
    );
  }

  /**
   *  Simple test of translate function as per example two for this function from the F andO specs. .
   */
  @org.junit.Test
  public void fnTranslate2() {
    final XQuery query = new XQuery(
      "fn:translate(\"--aaa--\",\"abc-\",\"ABC\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "AAA")
    );
  }

  /**
   * Evaluation of translate function using non-BMP characters..
   */
  @org.junit.Test
  public void fnTranslate20() {
    final XQuery query = new XQuery(
      "translate(\"abcd𐀄e\", \"a𐀄e\", \"𐀆YZ\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("\"𐀆bcdYZ\"")
    );
  }

  /**
   *  Simple test of translate function as per example three for this function from the F andO specs. .
   */
  @org.junit.Test
  public void fnTranslate3() {
    final XQuery query = new XQuery(
      "fn:translate(\"abcdabc\", \"abc\", \"AB\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ABdAB")
    );
  }

  /**
   *  Evaluation of translate function. Translate lower case letters to upper case letters. .
   */
  @org.junit.Test
  public void fnTranslate4() {
    final XQuery query = new XQuery(
      "fn:translate(\"acdefghijklmnopqrstuvwxyz\", \"abcdefghijklmnopqrstuvwxyz\", \"ABCDEFGHIJKLMNOPQRSTUVWXYZ\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ACDEFGHIJKLMNOPQRSTUVWXYZ")
    );
  }

  /**
   *  Evaluation of translate function. Translate upper case letters to lower case letters. .
   */
  @org.junit.Test
  public void fnTranslate5() {
    final XQuery query = new XQuery(
      "fn:translate(\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\",\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\",\"abcdefghijklmnopqrstuvwxyz\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "abcdefghijklmnopqrstuvwxyz")
    );
  }

  /**
   *  Evaluation of translate function, where all three arguments are the zero length string. Use fn;count to avoid empty file. .
   */
  @org.junit.Test
  public void fnTranslate6() {
    final XQuery query = new XQuery(
      "fn:count(fn:translate(\"\",\"\",\"\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluation of translate function, where the first and third arguments are the same (letters). .
   */
  @org.junit.Test
  public void fnTranslate7() {
    final XQuery query = new XQuery(
      "fn:translate(\"ABC\", \"ABC\", \"ABC\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ABC")
    );
  }

  /**
   *  Evaluation of translate function, where all arguments are the same (numbers). .
   */
  @org.junit.Test
  public void fnTranslate8() {
    final XQuery query = new XQuery(
      "fn:translate(\"123\", \"123\", \"123\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "123")
    );
  }

  /**
   *  Evaluation of translate function, there is a mixture of numbers and letters. .
   */
  @org.junit.Test
  public void fnTranslate9() {
    final XQuery query = new XQuery(
      "fn:translate(\"123ABC\", \"123ABC\", \"123ABC\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "123ABC")
    );
  }

  /**
   * Test simple translate expression .
   */
  @org.junit.Test
  public void fnTranslate3args1() {
    final XQuery query = new XQuery(
      "translate('---abcABCxyz---','-abcABCxyz','1ABCabcXYZ')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "111ABCabcXYZ111")
    );
  }

  /**
   * Test translate on space, tab, and newline .
   */
  @org.junit.Test
  public void fnTranslate3args2() {
    final XQuery query = new XQuery(
      "translate('newline\n" +
      "tab\tspace ','\n" +
      "\t ','123')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "newline1tab2space3")
    );
  }

  /**
   * Test translate with zero-length string argument .
   */
  @org.junit.Test
  public void fnTranslate3args3() {
    final XQuery query = new XQuery(
      "translate('','-','x')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "")
    );
  }

  /**
   * Test translate with an empty sequence argument .
   */
  @org.junit.Test
  public void fnTranslate3args4() {
    final XQuery query = new XQuery(
      "translate((),'-','x')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "")
    );
  }

  /**
   * Test translate with invalid type in 1st argument .
   */
  @org.junit.Test
  public void fnTranslate3args5() {
    final XQuery query = new XQuery(
      "translate(1,'-','x')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Test translate with invalid type in 2nd argument .
   */
  @org.junit.Test
  public void fnTranslate3args6() {
    final XQuery query = new XQuery(
      "translate('abc',1,'x')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Test translate with invalid type in 3rd argument .
   */
  @org.junit.Test
  public void fnTranslate3args7() {
    final XQuery query = new XQuery(
      "translate('abc','x',1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Test translate with incorrect arity .
   */
  @org.junit.Test
  public void fnTranslate3args8() {
    final XQuery query = new XQuery(
      "translate('abc')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }
}
