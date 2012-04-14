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
public class FnSubstringAfter extends QT3TestSet {

  /**
   *  A test whose essence is: `substring-after()`. .
   */
  @org.junit.Test
  public void kSubstringAfterFunc1() {
    final XQuery query = new XQuery(
      "substring-after()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `substring-after("a string", ()) eq "a string"`. .
   */
  @org.junit.Test
  public void kSubstringAfterFunc10() {
    final XQuery query = new XQuery(
      "substring-after(\"a string\", ()) eq \"a string\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `substring-after("a string", "not in other") eq ""`. .
   */
  @org.junit.Test
  public void kSubstringAfterFunc11() {
    final XQuery query = new XQuery(
      "substring-after(\"a string\", \"not in other\") eq \"\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `substring-after(())`. .
   */
  @org.junit.Test
  public void kSubstringAfterFunc2() {
    final XQuery query = new XQuery(
      "substring-after(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `substring-after((), (), "http://www.w3.org/2005/xpath-functions/collation/codepoint", "wrong param")`. .
   */
  @org.junit.Test
  public void kSubstringAfterFunc3() {
    final XQuery query = new XQuery(
      "substring-after((), (), \"http://www.w3.org/2005/xpath-functions/collation/codepoint\", \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `substring-after("a string", "a string", "http://www.example.com/COLLATION/NOT/SUPPORTED")`. .
   */
  @org.junit.Test
  public void kSubstringAfterFunc4() {
    final XQuery query = new XQuery(
      "substring-after(\"a string\", \"a string\", \"http://www.example.com/COLLATION/NOT/SUPPORTED\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOCH0002")
    );
  }

  /**
   *  A test whose essence is: `substring-after("foo", "fo", "http://www.w3.org/2005/xpath-functions/collation/codepoint") eq "o"`. .
   */
  @org.junit.Test
  public void kSubstringAfterFunc5() {
    final XQuery query = new XQuery(
      "substring-after(\"foo\", \"fo\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\") eq \"o\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `substring-after("tattoo", "tat") eq "too"`. .
   */
  @org.junit.Test
  public void kSubstringAfterFunc6() {
    final XQuery query = new XQuery(
      "substring-after(\"tattoo\", \"tat\") eq \"too\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `substring-after("tattoo", "tattoo") eq ""`. .
   */
  @org.junit.Test
  public void kSubstringAfterFunc7() {
    final XQuery query = new XQuery(
      "substring-after(\"tattoo\", \"tattoo\") eq \"\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `substring-after("abcdefgedij", "def") eq "gedij"`. .
   */
  @org.junit.Test
  public void kSubstringAfterFunc8() {
    final XQuery query = new XQuery(
      "substring-after(\"abcdefgedij\", \"def\") eq \"gedij\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `substring-after((), ()) eq ""`. .
   */
  @org.junit.Test
  public void kSubstringAfterFunc9() {
    final XQuery query = new XQuery(
      "substring-after((), ()) eq \"\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = "" $arg2 = "" .
   */
  @org.junit.Test
  public void fnSubstringAfter1() {
    final XQuery query = new XQuery(
      "fn:substring-after(\"\",\"\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = " " $arg2 = " AAAAABBBBB" .
   */
  @org.junit.Test
  public void fnSubstringAfter10() {
    final XQuery query = new XQuery(
      "fn:substring-after(\" \",\"AAAAABBBBB\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluates The "substring-after" function using it as a argument of a fn:not - returns true .
   */
  @org.junit.Test
  public void fnSubstringAfter11() {
    final XQuery query = new XQuery(
      "fn:not(fn:substring-after(\"A\",\"A\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "substring-after" function using it as a argument of a fn:not - returns true .
   */
  @org.junit.Test
  public void fnSubstringAfter12() {
    final XQuery query = new XQuery(
      "fn:not(fn:substring-after(\"A\",\"B\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = xs:string("A") $arg2 = "A" .
   */
  @org.junit.Test
  public void fnSubstringAfter13() {
    final XQuery query = new XQuery(
      "fn:substring-after(xs:string(\"A\"),\"A\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = "A" $arg2 = xs:string("A") .
   */
  @org.junit.Test
  public void fnSubstringAfter14() {
    final XQuery query = new XQuery(
      "fn:substring-after(\"A\",xs:string(\"A\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = "A" $arg2 = "a" .
   */
  @org.junit.Test
  public void fnSubstringAfter15() {
    final XQuery query = new XQuery(
      "fn:substring-after(\"A\",\"a\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = "a" $arg2 = "A" .
   */
  @org.junit.Test
  public void fnSubstringAfter16() {
    final XQuery query = new XQuery(
      "fn:substring-after(\"a\",\"A\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = "substring-after" $arg2 = "substring-after" .
   */
  @org.junit.Test
  public void fnSubstringAfter17() {
    final XQuery query = new XQuery(
      "fn:substring-after(\"substring-after\",\"substring-after\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = "substring-aftersubstring-after" $arg2 = "substring-after" .
   */
  @org.junit.Test
  public void fnSubstringAfter18() {
    final XQuery query = new XQuery(
      "fn:substring-after(\"substring-aftersubstring-after\",\"substring-after\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "substring-after")
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = "****" $arg2 = "***" .
   */
  @org.junit.Test
  public void fnSubstringAfter19() {
    final XQuery query = new XQuery(
      "fn:substring-after(\"****\",\"***\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "*")
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = "" $arg2 = "A Character String" .
   */
  @org.junit.Test
  public void fnSubstringAfter2() {
    final XQuery query = new XQuery(
      "fn:substring-after(\"\",\"A Character String\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = "12345" $arg2 = "1234" .
   */
  @org.junit.Test
  public void fnSubstringAfter20() {
    final XQuery query = new XQuery(
      "fn:substring-after(\"12345\",\"1234\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "5")
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = "substring-after $arg2 = "refta-gnirtsbus ("substring-after" backwards) .
   */
  @org.junit.Test
  public void fnSubstringAfter21() {
    final XQuery query = new XQuery(
      "fn:substring-after(\"substring-after\",\"refta-gnirtsbus\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   * Use absolute collation URI.
   */
  @org.junit.Test
  public void fnSubstringAfter22() {
    final XQuery query = new XQuery(
      "substring-after(\"banana\", \"a\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("\"nana\"")
    );
  }

  /**
   * Use relative collation URI.
   */
  @org.junit.Test
  public void fnSubstringAfter23() {
    final XQuery query = new XQuery(
      "substring-after(\"banana\", \"a\", \"collation/codepoint\")",
      ctx);
    query.baseURI("http://www.w3.org/2005/xpath-functions/");

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("\"nana\"")
    );
  }

  /**
   * Use non-BMP characters.
   */
  @org.junit.Test
  public void fnSubstringAfter25() {
    final XQuery query = new XQuery(
      "substring-after(\"𐀁𐀂𐀃\", \"𐀂\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("\"𐀃\"")
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = "" $arg2 = "A Character String" .
   */
  @org.junit.Test
  public void fnSubstringAfter3() {
    final XQuery query = new XQuery(
      "fn:substring-after(\"A Character String\",\"\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("\"A Character String\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = () $arg2 = "" .
   */
  @org.junit.Test
  public void fnSubstringAfter4() {
    final XQuery query = new XQuery(
      "fn:substring-after((),\"\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = "" $arg2 = () .
   */
  @org.junit.Test
  public void fnSubstringAfter5() {
    final XQuery query = new XQuery(
      "fn:substring-after(\"\",())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = "A Character String" $arg2 = () .
   */
  @org.junit.Test
  public void fnSubstringAfter6() {
    final XQuery query = new XQuery(
      "fn:substring-after(\"A Character String\",())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("\"A Character String\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = () $arg2 = "A Character String" .
   */
  @org.junit.Test
  public void fnSubstringAfter7() {
    final XQuery query = new XQuery(
      "fn:substring-after((),\"A Character String\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = "AAAAABBBBBCCCCC" $arg2 = "BBBBB" .
   */
  @org.junit.Test
  public void fnSubstringAfter8() {
    final XQuery query = new XQuery(
      "fn:substring-after(\"AAAAABBBBBCCCCC\",\"BBBBB\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "CCCCC")
    );
  }

  /**
   *  Evaluates The "substring-after" function with the arguments set as follows: $arg1 = "AAAAABBBBB" $arg2 = " " .
   */
  @org.junit.Test
  public void fnSubstringAfter9() {
    final XQuery query = new XQuery(
      "fn:substring-after(\"AAAAABBBBB\",\" \")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("\"\"")
      &&
        assertCount(1)
      )
    );
  }
}
