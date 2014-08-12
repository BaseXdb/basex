package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the contains() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnContains extends QT3TestSet {

  /**
   *  A test whose essence is: `contains()`. .
   */
  @org.junit.Test
  public void kContainsFunc1() {
    final XQuery query = new XQuery(
      "contains()",
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
   *  A test whose essence is: `contains("foo", "foo")`. .
   */
  @org.junit.Test
  public void kContainsFunc10() {
    final XQuery query = new XQuery(
      "contains(\"foo\", \"foo\")",
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
   *  A test whose essence is: `not(contains("", "a string"))`. .
   */
  @org.junit.Test
  public void kContainsFunc11() {
    final XQuery query = new XQuery(
      "not(contains(\"\", \"a string\"))",
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
   *  A test whose essence is: `contains(())`. .
   */
  @org.junit.Test
  public void kContainsFunc2() {
    final XQuery query = new XQuery(
      "contains(())",
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
   *  A test whose essence is: `contains((), (), "http://www.w3.org/2005/xpath-functions/collation/codepoint", "wrong param")`. .
   */
  @org.junit.Test
  public void kContainsFunc3() {
    final XQuery query = new XQuery(
      "contains((), (), \"http://www.w3.org/2005/xpath-functions/collation/codepoint\", \"wrong param\")",
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
   *  A test whose essence is: `contains("a string", "a string", "http://www.example.com/COLLATION/NOT/SUPPORTED")`. .
   */
  @org.junit.Test
  public void kContainsFunc4() {
    final XQuery query = new XQuery(
      "contains(\"a string\", \"a string\", \"http://www.example.com/COLLATION/NOT/SUPPORTED\")",
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
   *  A test whose essence is: `contains("foo", "foo", "http://www.w3.org/2005/xpath-functions/collation/codepoint")`. .
   */
  @org.junit.Test
  public void kContainsFunc5() {
    final XQuery query = new XQuery(
      "contains(\"foo\", \"foo\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\")",
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
   *  A test whose essence is: `contains("tattoo", "t")`. .
   */
  @org.junit.Test
  public void kContainsFunc6() {
    final XQuery query = new XQuery(
      "contains(\"tattoo\", \"t\")",
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
   *  A test whose essence is: `not(contains("tattoo", "ttt"))`. .
   */
  @org.junit.Test
  public void kContainsFunc7() {
    final XQuery query = new XQuery(
      "not(contains(\"tattoo\", \"ttt\"))",
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
   *  A test whose essence is: `contains("", ())`. .
   */
  @org.junit.Test
  public void kContainsFunc8() {
    final XQuery query = new XQuery(
      "contains(\"\", ())",
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
   *  A test whose essence is: `contains("a string", ())`. .
   */
  @org.junit.Test
  public void kContainsFunc9() {
    final XQuery query = new XQuery(
      "contains(\"a string\", ())",
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
   *  Compare two values returned from fn:lower-case(). .
   */
  @org.junit.Test
  public void k2ContainsFunc1() {
    final XQuery query = new XQuery(
      "let $vA := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string,\n" +
      "                $vB  := (\"b string\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "         return contains(lower-case($vA), lower-case($vB))",
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
   *  Compare two values returned from fn:upper-case(). .
   */
  @org.junit.Test
  public void k2ContainsFunc2() {
    final XQuery query = new XQuery(
      "let $vA := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string,\n" +
      "                $vB := (\"b string\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "         return contains(upper-case($vA), upper-case($vB))",
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
   *  Compare two non-matching values returned from fn:lower-case(). .
   */
  @org.junit.Test
  public void k2ContainsFunc3() {
    final XQuery query = new XQuery(
      "let $vA  := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string,\n" +
      "                $vB := (\"no match\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "         return contains(lower-case($vA), lower-case($vB))",
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
   *  Compare two non-matching values returned from fn:upper-case(). .
   */
  @org.junit.Test
  public void k2ContainsFunc4() {
    final XQuery query = new XQuery(
      "let $vA := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string,\n" +
      "                $vB  := (\"no match\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "        return contains(upper-case($vA), upper-case($vB))",
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
   *  Compare two non-matching values returned from fn:lower-case() and fn:upper-case(). .
   */
  @org.junit.Test
  public void k2ContainsFunc5() {
    final XQuery query = new XQuery(
      "let $vA := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string,\n" +
      "                $vB := (\"b string\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "         return contains(upper-case($vA), lower-case($vB))",
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
   *  Compare two non-matching values returned from fn:lower-case() and fn:upper-case(). .
   */
  @org.junit.Test
  public void k2ContainsFunc6() {
    final XQuery query = new XQuery(
      "let $vA := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string,\n" +
      "                $vB := (\"b string\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "         return contains(lower-case($vA), upper-case($vB))",
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
   *  test fn:contains with collation and $arg2 as empty string .
   */
  @org.junit.Test
  public void cbclContains001() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:contains('input', '', 'http://www.w3.org/2005/xpath-functions/collation/codepoint'))",
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
   *  test fn:contains with collation and $arg1 as empty string Author: Tim Mills .
   */
  @org.junit.Test
  public void cbclContains002() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:contains('', 'empty', 'http://www.w3.org/2005/xpath-functions/collation/codepoint'))",
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
   *  test evaluation of fn:contains to exactly one item Author: Tim Mills .
   */
  @org.junit.Test
  public void cbclContains003() {
    final XQuery query = new XQuery(
      "fn:index-of( ( fn:true(), fn:false()), fn:contains('input', 'in', 'http://www.w3.org/2005/xpath-functions/collation/codepoint'))",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = "" $arg2 = "" .
   */
  @org.junit.Test
  public void fnContains1() {
    final XQuery query = new XQuery(
      "fn:contains(\"\",\"\")",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = " " $arg2 = " AAAAABBBBB" .
   */
  @org.junit.Test
  public void fnContains10() {
    final XQuery query = new XQuery(
      "fn:contains(\" \",\"AAAAABBBBB\")",
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
   *  Evaluates The "contains" function using it as a argument of a fn:not - returns false .
   */
  @org.junit.Test
  public void fnContains11() {
    final XQuery query = new XQuery(
      "fn:not(fn:contains(\"A\",\"A\"))",
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
   *  Evaluates The "contains" function using it as a argument of a fn:not - returns true .
   */
  @org.junit.Test
  public void fnContains12() {
    final XQuery query = new XQuery(
      "fn:not(fn:contains(\"A\",\"B\"))",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = xs:string("A") $arg2 = "A" .
   */
  @org.junit.Test
  public void fnContains13() {
    final XQuery query = new XQuery(
      "fn:contains(xs:string(\"A\"),\"A\")",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = "A" $arg2 = xs:string("A") .
   */
  @org.junit.Test
  public void fnContains14() {
    final XQuery query = new XQuery(
      "fn:contains(\"A\",xs:string(\"A\"))",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = "A" $arg2 = "a" .
   */
  @org.junit.Test
  public void fnContains15() {
    final XQuery query = new XQuery(
      "fn:contains(\"A\",\"a\")",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = "a" $arg2 = "A" .
   */
  @org.junit.Test
  public void fnContains16() {
    final XQuery query = new XQuery(
      "fn:contains(\"a\",\"A\")",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = "" $arg2 = "A Character String" .
   */
  @org.junit.Test
  public void fnContains2() {
    final XQuery query = new XQuery(
      "fn:contains(\"\",\"A Character String\")",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = "" $arg2 = "A Character String" .
   */
  @org.junit.Test
  public void fnContains3() {
    final XQuery query = new XQuery(
      "fn:contains(\"A Character String\",\"\")",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = () $arg2 = "" .
   */
  @org.junit.Test
  public void fnContains4() {
    final XQuery query = new XQuery(
      "fn:contains((),\"\")",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = "" $arg2 = () .
   */
  @org.junit.Test
  public void fnContains5() {
    final XQuery query = new XQuery(
      "fn:contains(\"\",())",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = "A Character String" $arg2 = () .
   */
  @org.junit.Test
  public void fnContains6() {
    final XQuery query = new XQuery(
      "fn:contains(\"A Character String\",())",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = () $arg2 = "A Character String" .
   */
  @org.junit.Test
  public void fnContains7() {
    final XQuery query = new XQuery(
      "fn:contains((),\"A Character String\")",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = "AAAAABBBBBCCCCC" $arg2 = "BBBBB" .
   */
  @org.junit.Test
  public void fnContains8() {
    final XQuery query = new XQuery(
      "fn:contains(\"AAAAABBBBBCCCCC\",\"BBBBB\")",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = "AAAAABBBBB" $arg2 = " " .
   */
  @org.junit.Test
  public void fnContains9() {
    final XQuery query = new XQuery(
      "fn:contains(\"AAAAABBBBB\",\" \")",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = xs:string(lower bound) $arg2 = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnContains2args1() {
    final XQuery query = new XQuery(
      "fn:contains(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = xs:string(mid range) $arg2 = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnContains2args2() {
    final XQuery query = new XQuery(
      "fn:contains(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = xs:string(upper bound) $arg2 = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnContains2args3() {
    final XQuery query = new XQuery(
      "fn:contains(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = xs:string(lower bound) $arg2 = xs:string(mid range) .
   */
  @org.junit.Test
  public void fnContains2args4() {
    final XQuery query = new XQuery(
      "fn:contains(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
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
   *  Evaluates The "contains" function with the arguments set as follows: $arg1 = xs:string(lower bound) $arg2 = xs:string(upper bound) .
   */
  @org.junit.Test
  public void fnContains2args5() {
    final XQuery query = new XQuery(
      "fn:contains(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
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
}
