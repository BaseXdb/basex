package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the compare() function.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnCompare extends QT3TestSet {

  /**
   * A test whose essence is: `codepoint-equal()`..
   */
  @org.junit.Test
  public void kCompareFunc1() {
    final XQuery query = new XQuery(
      "codepoint-equal()",
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
   * A test whose essence is: `compare((), (), "http://www.w3.org/2005/xpath-functions/collation/codepoint", "wrong param")`..
   */
  @org.junit.Test
  public void kCompareFunc10() {
    final XQuery query = new XQuery(
      "compare((), (), \"http://www.w3.org/2005/xpath-functions/collation/codepoint\", \"wrong param\")",
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
   * A test whose essence is: `empty(compare((), "a string"))`..
   */
  @org.junit.Test
  public void kCompareFunc11() {
    final XQuery query = new XQuery(
      "empty(compare((), \"a string\"))",
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
   * A test whose essence is: `empty(compare("a string", ()))`..
   */
  @org.junit.Test
  public void kCompareFunc12() {
    final XQuery query = new XQuery(
      "empty(compare(\"a string\", ()))",
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
   * A test whose essence is: `empty(compare("a string", (), "http://www.w3.org/2005/xpath-functions/collation/codepoint"))`..
   */
  @org.junit.Test
  public void kCompareFunc13() {
    final XQuery query = new XQuery(
      "empty(compare(\"a string\", (), \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"))",
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
   * A test whose essence is: `compare("str", "str") instance of xs:integer`..
   */
  @org.junit.Test
  public void kCompareFunc14() {
    final XQuery query = new XQuery(
      "compare(\"str\", \"str\") instance of xs:integer",
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
   * A test whose essence is: `empty(compare("a string", "a string", "http://www.example.com/COLLATION/NOT/SUPPORTED"))`..
   */
  @org.junit.Test
  public void kCompareFunc15() {
    final XQuery query = new XQuery(
      "empty(compare(\"a string\", \"a string\", \"http://www.example.com/COLLATION/NOT/SUPPORTED\"))",
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
        assertBoolean(false)
      ||
        error("FOCH0002")
      )
    );
  }

  /**
   * A test whose essence is: `codepoint-equal(())`..
   */
  @org.junit.Test
  public void kCompareFunc2() {
    final XQuery query = new XQuery(
      "codepoint-equal(())",
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
   * A test whose essence is: `codepoint-equal((), (), ())`..
   */
  @org.junit.Test
  public void kCompareFunc3() {
    final XQuery query = new XQuery(
      "codepoint-equal((), (), ())",
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
   * A test whose essence is: `empty(codepoint-equal((), "a string"))`..
   */
  @org.junit.Test
  public void kCompareFunc4() {
    final XQuery query = new XQuery(
      "empty(codepoint-equal((), \"a string\"))",
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
   * A test whose essence is: `empty(codepoint-equal("a string", ()))`..
   */
  @org.junit.Test
  public void kCompareFunc5() {
    final XQuery query = new XQuery(
      "empty(codepoint-equal(\"a string\", ()))",
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
   * A test whose essence is: `codepoint-equal("a string", "a string")`..
   */
  @org.junit.Test
  public void kCompareFunc6() {
    final XQuery query = new XQuery(
      "codepoint-equal(\"a string\", \"a string\")",
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
   * A test whose essence is: `not(codepoint-equal("cow", "a string"))`..
   */
  @org.junit.Test
  public void kCompareFunc7() {
    final XQuery query = new XQuery(
      "not(codepoint-equal(\"cow\", \"a string\"))",
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
   * A test whose essence is: `compare()`..
   */
  @org.junit.Test
  public void kCompareFunc8() {
    final XQuery query = new XQuery(
      "compare()",
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
   * A test whose essence is: `compare(())`..
   */
  @org.junit.Test
  public void kCompareFunc9() {
    final XQuery query = new XQuery(
      "compare(())",
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
   * Use a complex collation argument..
   */
  @org.junit.Test
  public void k2CompareFunc1() {
    final XQuery query = new XQuery(
      "compare(\"a\", \"a\", (\"http://www.w3.org/2005/xpath-functions/collation/codepoint\", ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * Use a complex collation argument..
   */
  @org.junit.Test
  public void k2CompareFunc2() {
    final XQuery query = new XQuery(
      "compare(\"a\", \"a\", ((), \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Use a complex collation argument. .
   */
  @org.junit.Test
  public void k2CompareFunc3() {
    final XQuery query = new XQuery(
      "compare(\"a\", \"a\", ((), \"http://www.w3.org/2005/xpath-functions/collation/codepoint\", ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Compare two values returned from fn:lower-case(). .
   */
  @org.junit.Test
  public void k2CompareFunc4() {
    final XQuery query = new XQuery(
      "let $vA := (\"B STRING\", current-time())[1] treat as xs:string, \n" +
      "        $vB  := (\"b string\", current-time())[1] treat as xs:string\n" +
      "        return compare(lower-case($vA), lower-case($vB))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Compare two values returned from fn:upper-case(). .
   */
  @org.junit.Test
  public void k2CompareFunc5() {
    final XQuery query = new XQuery(
      "let $vA := (\"B STRING\", current-time())[1] treat as xs:string, \n" +
      "                $vB  := (\"b string\", current-time())[1] treat as xs:string \n" +
      "        return compare(upper-case($vA), upper-case($vB))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Compare two non-matching values returned from fn:lower-case(). .
   */
  @org.junit.Test
  public void k2CompareFunc6() {
    final XQuery query = new XQuery(
      "let $vA  := (\"B STRING\", current-time())[1] treat as xs:string, \n" +
      "                $vB  := (\"no match\", current-time())[1] treat as xs:string \n" +
      "        return compare(lower-case($vA), lower-case($vB))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Compare two non-matching values returned from fn:upper-case(). .
   */
  @org.junit.Test
  public void k2CompareFunc7() {
    final XQuery query = new XQuery(
      "let $vA  := (\"B STRING\", current-time())[1] treat as xs:string, \n" +
      "                $vB  := (\"no match\", current-time())[1] treat as xs:string \n" +
      "        return compare(upper-case($vA), upper-case($vB))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Compare two non-matching values returned from fn:lower-case() and fn:upper-case(). .
   */
  @org.junit.Test
  public void k2CompareFunc8() {
    final XQuery query = new XQuery(
      "let $vA  := (\"B STRING\", current-time())[1] treat as xs:string, \n" +
      "                $vB  := (\"no match\", current-time())[1] treat as xs:string \n" +
      "        return compare(upper-case($vA), lower-case($vB))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Compare two non-matching values returned from fn:lower-case() and fn:upper-case(). .
   */
  @org.junit.Test
  public void k2CompareFunc9() {
    final XQuery query = new XQuery(
      "let $vA  := (\"B STRING\", current-time())[1] treat as xs:string, \n" +
      "                $vB  := (\"no match\", current-time())[1] treat as xs:string \n" +
      "        return compare(lower-case($vA), upper-case($vB))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   * Simple use of compare to compare strings.
   */
  @org.junit.Test
  public void compare001() {
    final XQuery query = new XQuery(
      "compare(\"a\", \"b\")",
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
        assertEq("-1")
      &&
        assertType("xs:integer")
      )
    );
  }

  /**
   * Simple use of compare to compare strings.
   */
  @org.junit.Test
  public void compare002() {
    final XQuery query = new XQuery(
      "compare(\"b\", \"a\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   * Simple use of compare to compare strings.
   */
  @org.junit.Test
  public void compare003() {
    final XQuery query = new XQuery(
      "compare(\"b\", \"b\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * compare() with empty sequence as argument.
   */
  @org.junit.Test
  public void compare004() {
    final XQuery query = new XQuery(
      "compare(\"b\", ())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * compare() with empty sequence as argument.
   */
  @org.junit.Test
  public void compare005() {
    final XQuery query = new XQuery(
      "compare((), \"b\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * compare() with empty sequence as argument.
   */
  @org.junit.Test
  public void compare006() {
    final XQuery query = new XQuery(
      "compare((), ())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * compare() with non-BMP characters.
   */
  @org.junit.Test
  public void compare007() {
    final XQuery query = new XQuery(
      "compare(\"𐀁\", \"𐀂\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   * check that Unicode codepoint collation is used, not comparison of UTF16 surrogates.
   */
  @org.junit.Test
  public void compare008() {
    final XQuery query = new XQuery(
      "compare(\"𐀁\", \"\ufff0\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   * check that Unicode codepoint collation is used, not comparison of UTF16 surrogates.
   */
  @org.junit.Test
  public void compare009() {
    final XQuery query = new XQuery(
      "compare(\"𐀁\", \"\ufff0\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   * compare() only works on strings.
   */
  @org.junit.Test
  public void compare011() {
    final XQuery query = new XQuery(
      "compare(123, 456)",
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
   * compare() only works on strings, but xs:anyURI gets promoted.
   */
  @org.junit.Test
  public void compare012() {
    final XQuery query = new XQuery(
      "compare(xs:anyURI('http://www.example.com/'), 'http://www.example.com/')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * compare() only works on strings, but xs:untypedAtomic gets promoted.
   */
  @org.junit.Test
  public void compare013() {
    final XQuery query = new XQuery(
      "compare(xs:untypedAtomic('http://www.example.com/'), 'http://www.example.com/')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = "" $arg2 = "".
   */
  @org.junit.Test
  public void fnCompare1() {
    final XQuery query = new XQuery(
      "fn:compare(\"\",\"\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = " " $arg2 = " AAAAABBBBB".
   */
  @org.junit.Test
  public void fnCompare10() {
    final XQuery query = new XQuery(
      "fn:compare(\" \",\"AAAAABBBBB\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   * Evaluates The "compare" function using it as a argument of a fn:not - returns true.
   */
  @org.junit.Test
  public void fnCompare11() {
    final XQuery query = new XQuery(
      "fn:not(fn:compare(\"A\",\"A\"))",
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
   * Evaluates The "compare" function using it as a argument of a fn:not - returns false.
   */
  @org.junit.Test
  public void fnCompare12() {
    final XQuery query = new XQuery(
      "fn:not(fn:compare(\"A\",\"B\"))",
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
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = xs:string("A") $arg2 = "A".
   */
  @org.junit.Test
  public void fnCompare13() {
    final XQuery query = new XQuery(
      "fn:compare(xs:string(\"A\"),\"A\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = "A" $arg2 = xs:string("A").
   */
  @org.junit.Test
  public void fnCompare14() {
    final XQuery query = new XQuery(
      "fn:compare(\"A\",xs:string(\"A\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = "A" $arg2 = "a".
   */
  @org.junit.Test
  public void fnCompare15() {
    final XQuery query = new XQuery(
      "fn:compare(\"A\",\"a\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = "a" $arg2 = "A".
   */
  @org.junit.Test
  public void fnCompare16() {
    final XQuery query = new XQuery(
      "fn:compare(\"a\",\"A\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = "compare" $arg2 = "compare".
   */
  @org.junit.Test
  public void fnCompare17() {
    final XQuery query = new XQuery(
      "fn:compare(\"compare\",\"compare\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = "comparecompare" $arg2 = "compare".
   */
  @org.junit.Test
  public void fnCompare18() {
    final XQuery query = new XQuery(
      "fn:compare(\"comparecompare\",\"compare\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = "****" $arg2 = "***".
   */
  @org.junit.Test
  public void fnCompare19() {
    final XQuery query = new XQuery(
      "fn:compare(\"****\",\"***\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = "" $arg2 = "A Character String".
   */
  @org.junit.Test
  public void fnCompare2() {
    final XQuery query = new XQuery(
      "fn:compare(\"\",\"A Character String\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = "12345" $arg2 = "1234".
   */
  @org.junit.Test
  public void fnCompare20() {
    final XQuery query = new XQuery(
      "fn:compare(\"12345\",\"1234\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = "compare" $arg2 = "erapmoc" ("compare" backwards).
   */
  @org.junit.Test
  public void fnCompare21() {
    final XQuery query = new XQuery(
      "fn:compare(\"compare\",\"erapmoc\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   * Description Evaluates The "compare" function with a nonexistent collation..
   */
  @org.junit.Test
  public void fnCompare22() {
    final XQuery query = new XQuery(
      "fn:compare(\"a\",\"a\",\"CollationA\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0002")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = "A Character String" $arg2 = "".
   */
  @org.junit.Test
  public void fnCompare3() {
    final XQuery query = new XQuery(
      "fn:compare(\"A Character String\",\"\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = () $arg2 = "".
   */
  @org.junit.Test
  public void fnCompare4() {
    final XQuery query = new XQuery(
      "fn:count(fn:compare((),\"\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = "" $arg2 = ().
   */
  @org.junit.Test
  public void fnCompare5() {
    final XQuery query = new XQuery(
      "fn:count(fn:compare(\"\",()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = "A Character String" $arg2 = ().
   */
  @org.junit.Test
  public void fnCompare6() {
    final XQuery query = new XQuery(
      "fn:count(fn:compare(\"A Character String\",()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = () $arg2 = "A Character String".
   */
  @org.junit.Test
  public void fnCompare7() {
    final XQuery query = new XQuery(
      "fn:count(fn:compare((),\"A Character String\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = "AAAAABBBBBCCCCC" $arg2 = "BBBBB".
   */
  @org.junit.Test
  public void fnCompare8() {
    final XQuery query = new XQuery(
      "fn:compare(\"AAAAABBBBBCCCCC\",\"BBBBB\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $arg1 = "AAAAABBBBB" $arg2 = " ".
   */
  @org.junit.Test
  public void fnCompare9() {
    final XQuery query = new XQuery(
      "fn:compare(\"AAAAABBBBB\",\" \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $comparand1 = xs:string(lower bound) $comparand2 = xs:string(lower bound).
   */
  @org.junit.Test
  public void fnCompare2args1() {
    final XQuery query = new XQuery(
      "fn:compare(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $comparand1 = xs:string(mid range) $comparand2 = xs:string(lower bound).
   */
  @org.junit.Test
  public void fnCompare2args2() {
    final XQuery query = new XQuery(
      "fn:compare(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $comparand1 = xs:string(upper bound) $comparand2 = xs:string(lower bound).
   */
  @org.junit.Test
  public void fnCompare2args3() {
    final XQuery query = new XQuery(
      "fn:compare(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $comparand1 = xs:string(lower bound) $comparand2 = xs:string(mid range).
   */
  @org.junit.Test
  public void fnCompare2args4() {
    final XQuery query = new XQuery(
      "fn:compare(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * Evaluates The "compare" function with the arguments set as follows: $comparand1 = xs:string(lower bound) $comparand2 = xs:string(upper bound).
   */
  @org.junit.Test
  public void fnCompare2args5() {
    final XQuery query = new XQuery(
      "fn:compare(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }
}
