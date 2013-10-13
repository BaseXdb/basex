package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnStringToCodepoints extends QT3TestSet {

  /**
   * 
   *  Purpose: A test whose essence is: `string-to-codepoints()`. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc1() {
    final XQuery query = new XQuery(
      "string-to-codepoints()",
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
   * 
   *  Purpose: A test whose essence is: `deep-equal(string-to-codepoints("eee"), (101, 101, 101))`. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc10() {
    final XQuery query = new XQuery(
      "deep-equal(string-to-codepoints(\"eee\"), (101, 101, 101))",
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
   * 
   *  Purpose: A test whose essence is: `string-join(for $code in string-to-codepoints("example.com/") return string($code), "") eq "10112097109112108101469911110947"`. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc11() {
    final XQuery query = new XQuery(
      "string-join(for $code in string-to-codepoints(\"example.com/\") return string($code), \"\") eq \"10112097109112108101469911110947\"",
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
   * 
   *  Purpose: Combine fn:deep-equal and string-to-codepoints(). 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc12() {
    final XQuery query = new XQuery(
      "deep-equal(string-to-codepoints(\"Thérèse\"), (84, 104, 233, 114, 232, 115, 101))",
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
   * 
   *  Purpose: A test whose essence is: `codepoints-to-string((87, 36, 56, 87, 102, 96)) eq "W$8Wf`"`. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc13() {
    final XQuery query = new XQuery(
      "codepoints-to-string((87, 36, 56, 87, 102, 96)) eq \"W$8Wf`\"",
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
   * 
   *  Purpose: Combine string-to-codepoints() and a predicate. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc14() {
    final XQuery query = new XQuery(
      "string-to-codepoints(\"Thérèse\")[last()]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("101")
    );
  }

  /**
   * 
   *  Purpose: Combine string-to-codepoints() and a predicate. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc15() {
    final XQuery query = new XQuery(
      "string-to-codepoints(\"Thérèse\")[0 + last()]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("101")
    );
  }

  /**
   * 
   *  Purpose: Combine string-to-codepoints() and a predicate. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc16() {
    final XQuery query = new XQuery(
      "string-to-codepoints(\"Thérèse\")[last() - 1]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("115")
    );
  }

  /**
   * 
   *  Purpose: Combine string-to-codepoints() and a predicate. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc17() {
    final XQuery query = new XQuery(
      "string-to-codepoints(\"Thérèse\")[last() - 0]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("101")
    );
  }

  /**
   * 
   *  Purpose: Combine string-to-codepoints() and a predicate. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc18() {
    final XQuery query = new XQuery(
      "string-to-codepoints(\"Thérèse\")[0 + last()]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("101")
    );
  }

  /**
   * 
   *  Purpose: Combine string-to-codepoints() and a predicate. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc19() {
    final XQuery query = new XQuery(
      "string-to-codepoints(\"Thérèse\")[last() - 2]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("232")
    );
  }

  /**
   * 
   *  Purpose: A test whose essence is: `string-to-codepoints("str", "INVALID")`. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc2() {
    final XQuery query = new XQuery(
      "string-to-codepoints(\"str\", \"INVALID\")",
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
   * 
   *  Purpose: Combine string-to-codepoints() and a predicate. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc20() {
    final XQuery query = new XQuery(
      "empty(string-to-codepoints(\"Thérèse\")[last() - 7])",
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
   * 
   *  Purpose: Combine string-to-codepoints() and a predicate. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc21() {
    final XQuery query = new XQuery(
      "string-to-codepoints(\"Thérèse\")[last() - 6]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("84")
    );
  }

  /**
   * 
   *  Purpose: A test whose essence is: `empty(string-to-codepoints(()))`. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc3() {
    final XQuery query = new XQuery(
      "empty(string-to-codepoints(()))",
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
   * 
   *  Purpose: A test whose essence is: `empty(string-to-codepoints(""))`. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc4() {
    final XQuery query = new XQuery(
      "empty(string-to-codepoints(\"\"))",
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
   * 
   *  Purpose: A test whose essence is: `count(string-to-codepoints("123")) eq 3`. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc5() {
    final XQuery query = new XQuery(
      "count(string-to-codepoints(\"123\")) eq 3",
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
   * 
   *  Purpose: A test whose essence is: `count(string-to-codepoints("")) eq 0`. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc6() {
    final XQuery query = new XQuery(
      "count(string-to-codepoints(\"\")) eq 0",
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
   * 
   *  Purpose: A test whose essence is: `empty(string-to-codepoints(""))`. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc7() {
    final XQuery query = new XQuery(
      "empty(string-to-codepoints(\"\"))",
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
   * 
   *  Purpose: A test whose essence is: `string-to-codepoints("e")`. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc8() {
    final XQuery query = new XQuery(
      "string-to-codepoints(\"e\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("101")
    );
  }

  /**
   * 
   *  Purpose: A test whose essence is: `deep-equal(string-to-codepoints("ee"), (101, 101))`. 
   * .
   */
  @org.junit.Test
  public void kStringToCodepointFunc9() {
    final XQuery query = new XQuery(
      "deep-equal(string-to-codepoints(\"ee\"), (101, 101))",
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
   * 
   *  Description: Evaluation of an "fn:string-to-codepoints" with argument set to a single character ('1'). 
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints1() {
    final XQuery query = new XQuery(
      "fn:string-to-codepoints('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("49")
    );
  }

  /**
   * 
   *  Description: Evaluation of an "fn:string-to-codepoints" that is used as argument to fn:avg function . 
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints10() {
    final XQuery query = new XQuery(
      "fn:avg(fn:string-to-codepoints(\"A String\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("91")
    );
  }

  /**
   * 
   *  Description: Evaluation of an "fn:string-to-codepoints" that is used as argument to fn:empty function . 
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints11() {
    final XQuery query = new XQuery(
      "fn:empty(fn:string-to-codepoints(\"A String\"))",
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
   * 
   *  Description: Evaluation of an "fn:string-to-codepoints" that is used as argument to fn:empty function. 
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints12() {
    final XQuery query = new XQuery(
      "fn:empty(fn:string-to-codepoints(()))",
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
   * 
   *  Description: Evaluation of an "fn:string-to-codepoints" that is used as argument to fn:exits function . 
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints13() {
    final XQuery query = new XQuery(
      "fn:exists(fn:string-to-codepoints(()))",
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
   * 
   *  Description: Evaluation of an "fn:string-to-codepoints" that is used as argument to fn:empty function. 
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints14() {
    final XQuery query = new XQuery(
      "fn:exists(fn:string-to-codepoints(\"A String\"))",
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
   * 
   * Evaluation of  "fn:string-to-codepoints" with non-BMP characters
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints15() {
    final XQuery query = new XQuery(
      "fn:deep-equal(fn:string-to-codepoints(\"𐀁𐀂\"), (65537, 65538))",
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
   * 
   *  Description: Evaluation of an "fn:string-to-codepoints" with argument set to a single character ('a'). 
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints2() {
    final XQuery query = new XQuery(
      "fn:string-to-codepoints('a')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("97")
    );
  }

  /**
   * 
   *  Description: Evaluation of an "fn:string-to-codepoints" with argument set to a combination of number/character ('1a'). 
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints3() {
    final XQuery query = new XQuery(
      "fn:string-to-codepoints('1a')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("49, 97")
    );
  }

  /**
   * 
   *  Description: Evaluation of an "fn:string-to-codepoints" with argument set to the characters "#*^$". 
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints4() {
    final XQuery query = new XQuery(
      "fn:string-to-codepoints('#*^$')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("35, 42, 94, 36")
    );
  }

  /**
   * 
   *  Description: Evaluation of an "fn:string-to-codepoints" with argument set to "string-to-codepoints". 
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints5() {
    final XQuery query = new XQuery(
      "fn:string-to-codepoints('string-to-codepoints')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("115, 116, 114, 105, 110, 103, 45, 116, 111, 45, 99, 111, 100, 101, 112, 111, 105, 110, 116, 115")
    );
  }

  /**
   * 
   *  Description: Evaluation of an "fn:string-to-codepoints" with argument that uses "xs:string()" function . 
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints6() {
    final XQuery query = new XQuery(
      "fn:string-to-codepoints(xs:string(\"A String\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("65, 32, 83, 116, 114, 105, 110, 103")
    );
  }

  /**
   * 
   *  Description: Evaluation of an "fn:string-to-codepoints" with argument that uses "fn:upper-case" function . 
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints7() {
    final XQuery query = new XQuery(
      "fn:string-to-codepoints(fn:upper-case(\"A String\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("(65, 32, 83, 84, 82, 73, 78, 71)")
    );
  }

  /**
   * 
   *  Description: Evaluation of an "fn:string-to-codepoints" with argument that uses "fn:lower-case" function . 
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints8() {
    final XQuery query = new XQuery(
      "fn:string-to-codepoints(fn:lower-case(\"A String\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("(97, 32, 115, 116, 114, 105, 110, 103)")
    );
  }

  /**
   * 
   *  Description: Evaluation of an "fn:string-to-codepoints" that is used as argument to fn:count function . 
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints9() {
    final XQuery query = new XQuery(
      "fn:count(fn:string-to-codepoints(\"A String\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("8")
    );
  }

  /**
   * 
   * Purpose: Evaluates The "string-to-codepoints" function 
   *  with the arguments set as follows:                    
   * $arg = xs:string(lower bound)                          
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints1args1() {
    final XQuery query = new XQuery(
      "fn:string-to-codepoints(xs:string(\"This is a characte\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("84, 104, 105, 115, 32, 105, 115, 32, 97, 32, 99, 104, 97, 114, 97, 99, 116, 101")
    );
  }

  /**
   * 
   * Purpose: Evaluates The "string-to-codepoints" function 
   *  with the arguments set as follows:                    
   * $arg = xs:string(mid range)                            
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints1args2() {
    final XQuery query = new XQuery(
      "fn:string-to-codepoints(xs:string(\"This is a characte\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("84, 104, 105, 115, 32, 105, 115, 32, 97, 32, 99, 104, 97, 114, 97, 99, 116, 101")
    );
  }

  /**
   * 
   * Purpose: Evaluates The "string-to-codepoints" function 
   *  with the arguments set as follows:                    
   * $arg = xs:string(upper bound)                          
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints1args3() {
    final XQuery query = new XQuery(
      "fn:string-to-codepoints(xs:string(\"This is a characte\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("84, 104, 105, 115, 32, 105, 115, 32, 97, 32, 99, 104, 97, 114, 97, 99, 116, 101")
    );
  }

  /**
   * 
   * Purpose:Test string-to-codepoints with variety of characters
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints1args4() {
    final XQuery query = new XQuery(
      "string-to-codepoints('bßڒき豈')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("98, 223, 1682, 12365, 63744")
    );
  }

  /**
   * 
   * Purpose:Test string-to-codepoints with an empty string argument
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints1args5() {
    final XQuery query = new XQuery(
      "fn:string-to-codepoints(\"\")",
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
   * 
   * Purpose:Test string-to-codepoints with empty sequence argument
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints1args6() {
    final XQuery query = new XQuery(
      "string-to-codepoints(())",
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
   * 
   * Purpose:Test string-to-codepoints with invalid type in argument
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints1args7() {
    final XQuery query = new XQuery(
      "string-to-codepoints(12)",
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
   * 
   * Purpose:Test string-to-codepoints with incorrect function arity
   * .
   */
  @org.junit.Test
  public void fnStringToCodepoints1args8() {
    final XQuery query = new XQuery(
      "string-to-codepoints('abc','def')",
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
}
