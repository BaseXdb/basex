package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the ends-with() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnEndsWith extends QT3TestSet {

  /**
   *  A test whose essence is: `ends-with()`. .
   */
  @org.junit.Test
  public void kEndsWithFunc1() {
    final XQuery query = new XQuery(
      "ends-with()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `not(ends-with ((), "a string"))`. .
   */
  @org.junit.Test
  public void kEndsWithFunc10() {
    final XQuery query = new XQuery(
      "not(ends-with ((), \"a string\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `ends-with(())`. .
   */
  @org.junit.Test
  public void kEndsWithFunc2() {
    final XQuery query = new XQuery(
      "ends-with(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `ends-with((), (), "http://www.w3.org/2005/xpath-functions/collation/codepoint", "wrong param")`. .
   */
  @org.junit.Test
  public void kEndsWithFunc3() {
    final XQuery query = new XQuery(
      "ends-with((), (), \"http://www.w3.org/2005/xpath-functions/collation/codepoint\", \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `ends-with("a string", "a string", "http://www.example.com/COLLATION/NOT/SUPPORTED")`. .
   */
  @org.junit.Test
  public void kEndsWithFunc4() {
    final XQuery query = new XQuery(
      "ends-with(\"a string\", \"a string\", \"http://www.example.com/COLLATION/NOT/SUPPORTED\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOCH0002")
    );
  }

  /**
   *  A test whose essence is: `ends-with("foo", "foo", "http://www.w3.org/2005/xpath-functions/collation/codepoint")`. .
   */
  @org.junit.Test
  public void kEndsWithFunc5() {
    final XQuery query = new XQuery(
      "ends-with(\"foo\", \"foo\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `ends-with("tattoo", "tattoo")`. .
   */
  @org.junit.Test
  public void kEndsWithFunc6() {
    final XQuery query = new XQuery(
      "ends-with(\"tattoo\", \"tattoo\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(ends-with("tattoo", "atto"))`. .
   */
  @org.junit.Test
  public void kEndsWithFunc7() {
    final XQuery query = new XQuery(
      "not(ends-with(\"tattoo\", \"atto\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(ends-with("tattoo", "atto"))`. .
   */
  @org.junit.Test
  public void kEndsWithFunc8() {
    final XQuery query = new XQuery(
      "not(ends-with(\"tattoo\", \"atto\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `ends-with((), ())`. .
   */
  @org.junit.Test
  public void kEndsWithFunc9() {
    final XQuery query = new XQuery(
      "ends-with((), ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Compare two values returned from fn:lower-case(). .
   */
  @org.junit.Test
  public void k2EndsWithFunc1() {
    final XQuery query = new XQuery(
      "let $vA := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string, \n" +
      "        $vB := (\"b string\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "        return ends-with(lower-case($vA), lower-case($vB))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Compare two values returned from fn:upper-case(). .
   */
  @org.junit.Test
  public void k2EndsWithFunc2() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $vA  := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string,\n" +
      "         $vB  := (\"b string\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "         return ends-with(upper-case($vA), upper-case($vB))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Compare two non-matching values returned from fn:lower-case(). .
   */
  @org.junit.Test
  public void k2EndsWithFunc3() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $vA  := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string,\n" +
      "         $vB  := (\"no match\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "         return ends-with(lower-case($vA), lower-case($vB))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Compare two non-matching values returned from fn:upper-case(). .
   */
  @org.junit.Test
  public void k2EndsWithFunc4() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $vA  := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string,\n" +
      "         $vB  := (\"no match\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "         return ends-with(upper-case($vA), upper-case($vB))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Compare two non-matching values returned from fn:lower-case() and fn:upper-case(). .
   */
  @org.junit.Test
  public void k2EndsWithFunc5() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $vA := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string, \n" +
      "        $vB := (\"b string\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "        return ends-with(upper-case($vA), lower-case($vB))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Compare two non-matching values returned from fn:lower-case() and fn:upper-case(). .
   */
  @org.junit.Test
  public void k2EndsWithFunc6() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $vA := (\"B STRING\", current-time(), string(\"content\"))[1] treat as xs:string, \n" +
      "        $vB := (\"b string\", current-time(), string(\"content\"))[1] treat as xs:string\n" +
      "        return ends-with(lower-case($vA), upper-case($vB))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = "" $arg2 = "" .
   */
  @org.junit.Test
  public void fnEndsWith1() {
    final XQuery query = new XQuery(
      "fn:ends-with(\"\",\"\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = " " $arg2 = " AAAAABBBBB" .
   */
  @org.junit.Test
  public void fnEndsWith10() {
    final XQuery query = new XQuery(
      "fn:ends-with(\" \",\"AAAAABBBBB\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "ends-with" function using it as a argument of a fn:not - returns false .
   */
  @org.junit.Test
  public void fnEndsWith11() {
    final XQuery query = new XQuery(
      "fn:not(fn:ends-with(\"A\",\"A\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "ends-with" function using it as a argument of a fn:not - returns true .
   */
  @org.junit.Test
  public void fnEndsWith12() {
    final XQuery query = new XQuery(
      "fn:not(fn:ends-with(\"A\",\"B\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = xs:string("A") $arg2 = "A" .
   */
  @org.junit.Test
  public void fnEndsWith13() {
    final XQuery query = new XQuery(
      "fn:ends-with(xs:string(\"A\"),\"A\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = "A" $arg2 = xs:string("A") .
   */
  @org.junit.Test
  public void fnEndsWith14() {
    final XQuery query = new XQuery(
      "fn:ends-with(\"A\",xs:string(\"A\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = "A" $arg2 = "a" .
   */
  @org.junit.Test
  public void fnEndsWith15() {
    final XQuery query = new XQuery(
      "fn:ends-with(\"A\",\"a\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = "a" $arg2 = "A" .
   */
  @org.junit.Test
  public void fnEndsWith16() {
    final XQuery query = new XQuery(
      "fn:ends-with(\"a\",\"A\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = "" $arg2 = "A Character String" .
   */
  @org.junit.Test
  public void fnEndsWith2() {
    final XQuery query = new XQuery(
      "fn:ends-with(\"\",\"A Character String\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = "" $arg2 = "A Character String" .
   */
  @org.junit.Test
  public void fnEndsWith3() {
    final XQuery query = new XQuery(
      "fn:ends-with(\"A Character String\",\"\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = () $arg2 = "" .
   */
  @org.junit.Test
  public void fnEndsWith4() {
    final XQuery query = new XQuery(
      "fn:ends-with((),\"\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = "" $arg2 = () .
   */
  @org.junit.Test
  public void fnEndsWith5() {
    final XQuery query = new XQuery(
      "fn:ends-with(\"\",())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = "A Character String" $arg2 = () .
   */
  @org.junit.Test
  public void fnEndsWith6() {
    final XQuery query = new XQuery(
      "fn:ends-with(\"A Character String\",())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = () $arg2 = "A Character String" .
   */
  @org.junit.Test
  public void fnEndsWith7() {
    final XQuery query = new XQuery(
      "fn:ends-with((),\"A Character String\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = "AAAAABBBBBCCCCC" $arg2 = "BBBBB" .
   */
  @org.junit.Test
  public void fnEndsWith8() {
    final XQuery query = new XQuery(
      "fn:ends-with(\"AAAAABBBBBCCCCC\",\"BBBBB\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = "AAAAABBBBB" $arg2 = " " .
   */
  @org.junit.Test
  public void fnEndsWith9() {
    final XQuery query = new XQuery(
      "fn:ends-with(\"AAAAABBBBB\",\" \")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = xs:string(lower bound) $arg2 = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnEndsWith2args1() {
    final XQuery query = new XQuery(
      "fn:ends-with(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = xs:string(mid range) $arg2 = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnEndsWith2args2() {
    final XQuery query = new XQuery(
      "fn:ends-with(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = xs:string(upper bound) $arg2 = xs:string(lower bound) .
   */
  @org.junit.Test
  public void fnEndsWith2args3() {
    final XQuery query = new XQuery(
      "fn:ends-with(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = xs:string(lower bound) $arg2 = xs:string(mid range) .
   */
  @org.junit.Test
  public void fnEndsWith2args4() {
    final XQuery query = new XQuery(
      "fn:ends-with(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "ends-with" function with the arguments set as follows: $arg1 = xs:string(lower bound) $arg2 = xs:string(upper bound) .
   */
  @org.junit.Test
  public void fnEndsWith2args5() {
    final XQuery query = new XQuery(
      "fn:ends-with(xs:string(\"This is a characte\"),xs:string(\"This is a characte\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }
}
