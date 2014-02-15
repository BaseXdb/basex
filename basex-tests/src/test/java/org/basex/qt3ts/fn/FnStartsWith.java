package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the starts-with() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnStartsWith extends QT3TestSet {

  /**
   *  A test whose essence is: `starts-with()`. .
   */
  @org.junit.Test
  public void kStartsWithFunc1() {
    final XQuery query = new XQuery(
      "starts-with()",
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
   *  A test whose essence is: `starts-with("a string", ())`. .
   */
  @org.junit.Test
  public void kStartsWithFunc10() {
    final XQuery query = new XQuery(
      "starts-with(\"a string\", ())",
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
   *  A test whose essence is: `not(starts-with("", "a string"))`. .
   */
  @org.junit.Test
  public void kStartsWithFunc11() {
    final XQuery query = new XQuery(
      "not(starts-with(\"\", \"a string\"))",
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
   *  A test whose essence is: `starts-with(())`. .
   */
  @org.junit.Test
  public void kStartsWithFunc2() {
    final XQuery query = new XQuery(
      "starts-with(())",
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
   *  A test whose essence is: `starts-with((), (), "http://www.w3.org/2005/xpath-functions/collation/codepoint", "wrong param")`. .
   */
  @org.junit.Test
  public void kStartsWithFunc3() {
    final XQuery query = new XQuery(
      "starts-with((), (), \"http://www.w3.org/2005/xpath-functions/collation/codepoint\", \"wrong param\")",
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
   *  A test whose essence is: `starts-with("a string", "a string", "http://www.example.com/COLLATION/NOT/SUPPORTED")`. .
   */
  @org.junit.Test
  public void kStartsWithFunc4() {
    final XQuery query = new XQuery(
      "starts-with(\"a string\", \"a string\", \"http://www.example.com/COLLATION/NOT/SUPPORTED\")",
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
   *  A test whose essence is: `starts-with("foo", "foo", "http://www.w3.org/2005/xpath-functions/collation/codepoint")`. .
   */
  @org.junit.Test
  public void kStartsWithFunc5() {
    final XQuery query = new XQuery(
      "starts-with(\"foo\", \"foo\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\")",
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
   *  A test whose essence is: `starts-with("foo", "foo")`. .
   */
  @org.junit.Test
  public void kStartsWithFunc6() {
    final XQuery query = new XQuery(
      "starts-with(\"foo\", \"foo\")",
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
   *  A test whose essence is: `starts-with("tattoo", "tat")`. .
   */
  @org.junit.Test
  public void kStartsWithFunc7() {
    final XQuery query = new XQuery(
      "starts-with(\"tattoo\", \"tat\")",
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
   *  A test whose essence is: `not(starts-with("tattoo", "att"))`. .
   */
  @org.junit.Test
  public void kStartsWithFunc8() {
    final XQuery query = new XQuery(
      "not(starts-with(\"tattoo\", \"att\"))",
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
   *  A test whose essence is: `starts-with((), ())`. .
   */
  @org.junit.Test
  public void kStartsWithFunc9() {
    final XQuery query = new XQuery(
      "starts-with((), ())",
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
  public void k2StartsWithFunc1() {
    final XQuery query = new XQuery(
      "let $vA := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string, \n" +
      "        $vB := (\"b string\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "        return starts-with(lower-case($vA), lower-case($vB))",
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
  public void k2StartsWithFunc2() {
    final XQuery query = new XQuery(
      "let $vA := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string,\n" +
      "         $vB := (\"b string\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "         return starts-with(upper-case($vA), upper-case($vB))",
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
  public void k2StartsWithFunc3() {
    final XQuery query = new XQuery(
      "let $vA := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string, \n" +
      "        $vB := (\"no match\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "        return starts-with(lower-case($vA), lower-case($vB))",
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
  public void k2StartsWithFunc4() {
    final XQuery query = new XQuery(
      "let $vA := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string,\n" +
      "         $vB := (\"no match\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "         return starts-with(upper-case($vA), upper-case($vB))",
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
  public void k2StartsWithFunc5() {
    final XQuery query = new XQuery(
      "let $vA := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string,\n" +
      "         $vB := (\"b string\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "         return starts-with(upper-case($vA), lower-case($vB))",
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
  public void k2StartsWithFunc6() {
    final XQuery query = new XQuery(
      "let $vA := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string,\n" +
      "         $vB := (\"b string\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "         return starts-with(lower-case($vA), upper-case($vB))",
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
   *  test fn:starts-with with collation and empty string .
   */
  @org.junit.Test
  public void cbclStartsWith001() {
    final XQuery query = new XQuery(
      "\n" +
      "        fn:boolean(fn:starts-with('input', '', 'http://www.w3.org/2005/xpath-functions/collation/codepoint'))\n" +
      "      ",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = "" $arg2 = "" .
   */
  @org.junit.Test
  public void fnStartsWith1() {
    final XQuery query = new XQuery(
      "fn:starts-with(\"\",\"\")",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = " " $arg2 = " AAAAABBBBB" .
   */
  @org.junit.Test
  public void fnStartsWith10() {
    final XQuery query = new XQuery(
      "fn:starts-with(\" \",\"AAAAABBBBB\")",
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
   *  Evaluates The "starts-with" function using it as a argument of a fn:not - returns false .
   */
  @org.junit.Test
  public void fnStartsWith11() {
    final XQuery query = new XQuery(
      "fn:not(fn:starts-with(\"A\",\"A\"))",
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
   *  Evaluates The "starts-with" function using it as a argument of a fn:not - returns true .
   */
  @org.junit.Test
  public void fnStartsWith12() {
    final XQuery query = new XQuery(
      "fn:not(fn:starts-with(\"A\",\"B\"))",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = xs:string("A") $arg2 = "A" .
   */
  @org.junit.Test
  public void fnStartsWith13() {
    final XQuery query = new XQuery(
      "fn:starts-with(xs:string(\"A\"),\"A\")",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = "A" $arg2 = xs:string("A") .
   */
  @org.junit.Test
  public void fnStartsWith14() {
    final XQuery query = new XQuery(
      "fn:starts-with(\"A\",xs:string(\"A\"))",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = "A" $arg2 = "a" .
   */
  @org.junit.Test
  public void fnStartsWith15() {
    final XQuery query = new XQuery(
      "fn:starts-with(\"A\",\"a\")",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = "a" $arg2 = "A" .
   */
  @org.junit.Test
  public void fnStartsWith16() {
    final XQuery query = new XQuery(
      "fn:starts-with(\"a\",\"A\")",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = "" $arg2 = "A Character String" .
   */
  @org.junit.Test
  public void fnStartsWith2() {
    final XQuery query = new XQuery(
      "fn:starts-with(\"\",\"A Character String\")",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = "" $arg2 = "A Character String" .
   */
  @org.junit.Test
  public void fnStartsWith3() {
    final XQuery query = new XQuery(
      "fn:starts-with(\"A Character String\",\"\")",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = () $arg2 = "" .
   */
  @org.junit.Test
  public void fnStartsWith4() {
    final XQuery query = new XQuery(
      "fn:starts-with((),\"\")",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = "" $arg2 = () .
   */
  @org.junit.Test
  public void fnStartsWith5() {
    final XQuery query = new XQuery(
      "fn:starts-with(\"\",())",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = "A Character String" $arg2 = () .
   */
  @org.junit.Test
  public void fnStartsWith6() {
    final XQuery query = new XQuery(
      "fn:starts-with(\"A Character String\",())",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = () $arg2 = "A Character String" .
   */
  @org.junit.Test
  public void fnStartsWith7() {
    final XQuery query = new XQuery(
      "fn:starts-with((),\"A Character String\")",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = "AAAAABBBBBCCCCC" $arg2 = "BBBBB" .
   */
  @org.junit.Test
  public void fnStartsWith8() {
    final XQuery query = new XQuery(
      "fn:starts-with(\"AAAAABBBBBCCCCC\",\"BBBBB\")",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = "AAAAABBBBB" $arg2 = " " .
   */
  @org.junit.Test
  public void fnStartsWith9() {
    final XQuery query = new XQuery(
      "fn:starts-with(\"AAAAABBBBB\",\" \")",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = xs:string(lower bound) $arg2 = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnStartsWith2args1() {
    final XQuery query = new XQuery(
      "fn:starts-with(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = xs:string(mid range) $arg2 = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnStartsWith2args2() {
    final XQuery query = new XQuery(
      "fn:starts-with(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = xs:string(upper bound) $arg2 = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnStartsWith2args3() {
    final XQuery query = new XQuery(
      "fn:starts-with(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = xs:string(lower bound) $arg2 = xs:string(mid range) .
   */
  @org.junit.Test
  public void fnStartsWith2args4() {
    final XQuery query = new XQuery(
      "fn:starts-with(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
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
   *  Evaluates The "starts-with" function with the arguments set as follows: $arg1 = xs:string(lower bound) $arg2 = xs:string(upper bound) .
   */
  @org.junit.Test
  public void fnStartsWith2args5() {
    final XQuery query = new XQuery(
      "fn:starts-with(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
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
