package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the fn:substring() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnSubstring extends QT3TestSet {

  /**
   *  A test whose essence is: `sub-string("a string")`. .
   */
  @org.junit.Test
  public void kSubstringFunc1() {
    final XQuery query = new XQuery(
      "sub-string(\"a string\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `substring("12345", 0 div 0E0, 3) eq ""`. .
   */
  @org.junit.Test
  public void kSubstringFunc10() {
    final XQuery query = new XQuery(
      "substring(\"12345\", 0 div 0E0, 3) eq \"\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `substring("12345", 1, 0 div 0E0) eq ""`. .
   */
  @org.junit.Test
  public void kSubstringFunc11() {
    final XQuery query = new XQuery(
      "substring(\"12345\", 1, 0 div 0E0) eq \"\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `substring("12345", -3, 5) eq "1"`. .
   */
  @org.junit.Test
  public void kSubstringFunc12() {
    final XQuery query = new XQuery(
      "substring(\"12345\", -3, 5) eq \"1\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `substring("12345", -42, 1 div 0E0) eq "12345"`. .
   */
  @org.junit.Test
  public void kSubstringFunc13() {
    final XQuery query = new XQuery(
      "substring(\"12345\", -42, 1 div 0E0) eq \"12345\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `substring("12345", -1 div 0E0, 1 div 0E0) eq ""`. .
   */
  @org.junit.Test
  public void kSubstringFunc14() {
    final XQuery query = new XQuery(
      "substring(\"12345\", -1 div 0E0, 1 div 0E0) eq \"\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `substring("12345", 5, -3) eq ""`. .
   */
  @org.junit.Test
  public void kSubstringFunc15() {
    final XQuery query = new XQuery(
      "substring(\"12345\", 5, -3) eq \"\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sub-string("a string", 1, 2, "wrong param")`. .
   */
  @org.junit.Test
  public void kSubstringFunc2() {
    final XQuery query = new XQuery(
      "sub-string(\"a string\", 1, 2, \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `substring((), 1, 2) eq ""`. .
   */
  @org.junit.Test
  public void kSubstringFunc3() {
    final XQuery query = new XQuery(
      "substring((), 1, 2) eq \"\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `substring((), 1) eq ""`. .
   */
  @org.junit.Test
  public void kSubstringFunc4() {
    final XQuery query = new XQuery(
      "substring((), 1) eq \"\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `substring("12345", 1.5, 2.6) eq "234"`. .
   */
  @org.junit.Test
  public void kSubstringFunc5() {
    final XQuery query = new XQuery(
      "substring(\"12345\", 1.5, 2.6) eq \"234\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `substring((), 1, 3) eq ""`. .
   */
  @org.junit.Test
  public void kSubstringFunc6() {
    final XQuery query = new XQuery(
      "substring((), 1, 3) eq \"\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `substring("motor car", 6) eq " car"`. .
   */
  @org.junit.Test
  public void kSubstringFunc7() {
    final XQuery query = new XQuery(
      "substring(\"motor car\", 6) eq \" car\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `substring("12345", 0, 3) eq "12"`. .
   */
  @org.junit.Test
  public void kSubstringFunc8() {
    final XQuery query = new XQuery(
      "substring(\"12345\", 0, 3) eq \"12\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `substring("metadata", 4, 3) eq "ada"`. .
   */
  @org.junit.Test
  public void kSubstringFunc9() {
    final XQuery query = new XQuery(
      "substring(\"metadata\", 4, 3) eq \"ada\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Evaluation of substring function as per example 1 (for this function) from the F&O specs. .
   */
  @org.junit.Test
  public void fnSubstring1() {
    final XQuery query = new XQuery(
      "fn:substring(\"motor car\", 6)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, " car")
      &&
        assertType("xs:string")
      )
    );
  }

  /**
   * Evaluation of substring function as per example 10 (for this function) from the F&O specs. .
   */
  @org.junit.Test
  public void fnSubstring10() {
    final XQuery query = new XQuery(
      "fn:substring(\"12345\", -42, 1 div 0E0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "12345")
    );
  }

  /**
   * Evaluation of substring function as per example 11 (for this function) from the F&O specs. Use "fn:count" to avoid empty file. .
   */
  @org.junit.Test
  public void fnSubstring11() {
    final XQuery query = new XQuery(
      "fn:count(fn:substring(\"12345\", -1 div 0E0, 1 div 0E0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Evaluation of substring function, where the source string is the empty string Use "fn:count" to avoid empty file. .
   */
  @org.junit.Test
  public void fnSubstring12() {
    final XQuery query = new XQuery(
      "fn:count(fn:substring(\"\",0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Evaluation of substring function, as an argument to an "fn:boolean" function" .
   */
  @org.junit.Test
  public void fnSubstring13() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:substring(\"ABC\",1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Evaluation of substring function, as an argument to an "fn:not" function" .
   */
  @org.junit.Test
  public void fnSubstring14() {
    final XQuery query = new XQuery(
      "fn:not(fn:substring(\"ABC\",1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Evaluation of substring function, as an argument to another "fn:substring" function" .
   */
  @org.junit.Test
  public void fnSubstring15() {
    final XQuery query = new XQuery(
      "fn:substring(fn:substring(\"ABCDE\",1),1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ABCDE")
    );
  }

  /**
   * Evaluation of substring function, where the source string is the string "substring". .
   */
  @org.junit.Test
  public void fnSubstring16() {
    final XQuery query = new XQuery(
      "fn:substring(\"substring\",1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "substring")
    );
  }

  /**
   * Evaluation of substring function as an argument to a concat function. .
   */
  @org.junit.Test
  public void fnSubstring17() {
    final XQuery query = new XQuery(
      "fn:concat(fn:substring(\"ABC\",1),\"DEF\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ABCDEF")
    );
  }

  /**
   * Evaluation of substring function as an argument to a contains function. .
   */
  @org.junit.Test
  public void fnSubstring18() {
    final XQuery query = new XQuery(
      "fn:contains(fn:substring(\"ABCDEF\",1),\"DEF\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Evaluation of substring function using the special chracter "!@#$%^&*()". .
   */
  @org.junit.Test
  public void fnSubstring19() {
    final XQuery query = new XQuery(
      "fn:substring(\"!@#$%^*()\",1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "!@#$%^*()")
    );
  }

  /**
   * Evaluation of substring function as per example 2 (for this function) from the F&O specs. .
   */
  @org.junit.Test
  public void fnSubstring2() {
    final XQuery query = new XQuery(
      "fn:substring(\"metadata\", 4, 3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ada")
    );
  }

  /**
   * Evaluation of substring function, where the start location uses a "double" constructor. .
   */
  @org.junit.Test
  public void fnSubstring20() {
    final XQuery query = new XQuery(
      "fn:substring(\"ABCD\",xs:double(1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ABCD")
    );
  }

  /**
   * Evaluation of substring function, where the starting locatin is an addition expression. .
   */
  @org.junit.Test
  public void fnSubstring21() {
    final XQuery query = new XQuery(
      "fn:substring(\"ABCDE\",1+1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "BCDE")
    );
  }

  /**
   * Evaluation of substring function, where start<0 and (start+len)<0 .
   */
  @org.junit.Test
  public void fnSubstring22() {
    final XQuery query = new XQuery(
      "concat('#', fn:substring(./concepts/@id, string-length(./concepts/@id) - 18, 1), '#')",
      ctx);
    query.context(node(file("fn/substring/concepts.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "##")
    );
  }

  /**
   * substring() of a string containing non-BMP characters.
   */
  @org.junit.Test
  public void fnSubstring23() {
    final XQuery query = new XQuery(
      "substring(\"abcd𐀁efgh\", 6)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("\"efgh\"")
    );
  }

  /**
   * substring() of a string containing non-BMP characters.
   */
  @org.junit.Test
  public void fnSubstring24() {
    final XQuery query = new XQuery(
      "substring(\"abcd𐀁efgh\", 5, 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("\"𐀁e\"")
    );
  }

  /**
   * Evaluation of substring function as per example 3 (for this function) from the F&O specs. .
   */
  @org.junit.Test
  public void fnSubstring3() {
    final XQuery query = new XQuery(
      "fn:substring(\"12345\", 1.5, 2.6)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "234")
    );
  }

  /**
   * Evaluation of substring function as per example 4 (for this function) from the F&O specs. .
   */
  @org.junit.Test
  public void fnSubstring4() {
    final XQuery query = new XQuery(
      "fn:substring(\"12345\", 0, 3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "12")
    );
  }

  /**
   * Evaluation of substring function as per example 5 (for this function) from the F&O specs. Use "fn:count" to avoid empty file. .
   */
  @org.junit.Test
  public void fnSubstring5() {
    final XQuery query = new XQuery(
      "fn:count(fn:substring(\"12345\", 5, -3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Evaluation of substring function as per example 6 (for this function) from the F&O specs. .
   */
  @org.junit.Test
  public void fnSubstring6() {
    final XQuery query = new XQuery(
      "fn:substring(\"12345\", -3, 5)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Evaluation of substring function as per example 7 (for this function) from the F&O specs. Use "fn:count" to avoid empty file. .
   */
  @org.junit.Test
  public void fnSubstring7() {
    final XQuery query = new XQuery(
      "fn:count(fn:substring(\"12345\", 0 div 0E0, 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Evaluation of substring function as per example 8 (for this function) from the F&O specs. Use "fn:count" to avoid empty file. .
   */
  @org.junit.Test
  public void fnSubstring8() {
    final XQuery query = new XQuery(
      "fn:count(fn:substring(\"12345\", 1, 0 div 0E0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Evaluation of substring function as per example 9 (for this function) from the F&O specs. Use "fn:count" to avoid empty file. .
   */
  @org.junit.Test
  public void fnSubstring9() {
    final XQuery query = new XQuery(
      "fn:count(fn:substring((), 1, 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }
}
