package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnLang extends QT3TestSet {

  /**
   *  A test whose essence is: `lang()`. .
   */
  @org.junit.Test
  public void kNodeLangFunc1() {
    final XQuery query = new XQuery(
      "lang()",
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
   *  A test whose essence is: `if(false()) then lang("en", .) else true()`. .
   */
  @org.junit.Test
  public void kNodeLangFunc2() {
    final XQuery query = new XQuery(
      "if(false()) then lang(\"en\", .) else true()",
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
        assertBoolean(true)
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   *  A test whose essence is: `if(false()) then lang("en") else true()`. .
   */
  @org.junit.Test
  public void kNodeLangFunc3() {
    final XQuery query = new XQuery(
      "if(false()) then lang(\"en\") else true()",
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
        assertBoolean(true)
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   *  A test whose essence is: `lang("en", 2)`. .
   */
  @org.junit.Test
  public void kNodeLangFunc4() {
    final XQuery query = new XQuery(
      "lang(\"en\", 2)",
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
   *  Invoke fn:lang from the top and bottom of a tree. .
   */
  @org.junit.Test
  public void k2NodeLangFunc1() {
    final XQuery query = new XQuery(
      "let $i := <e xml:lang=\"en\"> <b xml:lang=\"de\"/> </e> return (lang(\"de\", $i/b), lang(\"de\", $i))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true false")
    );
  }

  /**
   *  Start navigating from the actual attribute node. .
   */
  @org.junit.Test
  public void k2NodeLangFunc2() {
    final XQuery query = new XQuery(
      "let $i := <e xml:lang=\"en\"> <b xml:lang=\"de\"/> </e> return lang(\"de\", $i/b/@xml:lang)",
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
   *  Navigate a single, empty attribute. .
   */
  @org.junit.Test
  public void k2NodeLangFunc3() {
    final XQuery query = new XQuery(
      "lang(\"de\", attribute xml:lang {()})",
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
   *  Sub-languages must be separated with dashes. .
   */
  @org.junit.Test
  public void k2NodeLangFunc4() {
    final XQuery query = new XQuery(
      "lang(\"en\", <e xml:lang=\"ene\"/>)",
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
   *  Use a single attribute node as input. .
   */
  @org.junit.Test
  public void k2NodeLangFunc5() {
    final XQuery query = new XQuery(
      "fn:lang(\"fr\", attribute xml:lang {\"fr\"})",
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
   *  Use a single attribute node as input. .
   */
  @org.junit.Test
  public void k2NodeLangFunc6() {
    final XQuery query = new XQuery(
      "fn:lang(\"fr\", attribute xml:lang {()})",
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
   *  Try to force optional item call on fn:lang .
   */
  @org.junit.Test
  public void cbclFnLang001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tif(current-date() > xs:date(\"2000-01-01\")) then lang(\"en\",<a/>) else ()\n" +
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
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of the fn:lang function with no second argument and no context node. .
   */
  @org.junit.Test
  public void fnLang1() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace eg = \"http://example.org\"; \n" +
      "        declare function eg:noContextFunction() { fn:lang(\"en\") }; \n" +
      "        eg:noContextFunction()\n" +
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
      error("XPDY0002")
    );
  }

  /**
   *  Evaluation of the fn:lang function with testlang set to "EN". Context node is '<para xml:lang="en-us"/>' .
   */
  @org.junit.Test
  public void fnLang10() {
    final XQuery query = new XQuery(
      "for $x in /langs/para[3] return $x/fn:lang(\"EN\")",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with testlang set to "En". Context node is '<para xml:lang="en-us"/>' .
   */
  @org.junit.Test
  public void fnLang11() {
    final XQuery query = new XQuery(
      "for $x in /langs/para[3] return $x/fn:lang(\"En\")",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with testlang set to "eN". Context node is '<para xml:lang="en-us"/>' .
   */
  @org.junit.Test
  public void fnLang12() {
    final XQuery query = new XQuery(
      "for $x in /langs/para[3] return $x/fn:lang(\"eN\")",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with testlang set to "en". Context node is '<para xml:lang="en-us"/>' .
   */
  @org.junit.Test
  public void fnLang13() {
    final XQuery query = new XQuery(
      "for $x in /langs/para[3] return $x/fn:lang(\"en\")",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with testlang set to "en-us". Context node is '<para xml:lang="en-us"/>' .
   */
  @org.junit.Test
  public void fnLang14() {
    final XQuery query = new XQuery(
      "for $x in /langs/para[3] return $x/fn:lang(\"en-us\")",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with testlang set to "en-us" and context node. is not a node. .
   */
  @org.junit.Test
  public void fnLang15() {
    final XQuery query = new XQuery(
      "1[fn:lang(\"en-us\")]",
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
   *  Evaluation of the fn:lang function with testlang set to "us-en" and context node has xml:lang attribute set to "en-us". .
   */
  @org.junit.Test
  public void fnLang16() {
    final XQuery query = new XQuery(
      "for $x in /langs/para[3] return $x/fn:lang(\"us-us\")",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with testlang set to "fr" and context node has xml:lang attribute set to "EN". .
   */
  @org.junit.Test
  public void fnLang17() {
    final XQuery query = new XQuery(
      "for $x in /langs/para[2] return $x/fn:lang(\"fr\")",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with testlang set to "en" and specified node (second argument) has xml:lang attribute set to "en". .
   */
  @org.junit.Test
  public void fnLang18() {
    final XQuery query = new XQuery(
      "fn:lang(\"en\",fn:exactly-one(/langs/para[1]))",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with testlang set to "fr" and specified node (second argument) has xml:lang attribute set to "en". .
   */
  @org.junit.Test
  public void fnLang19() {
    final XQuery query = new XQuery(
      "fn:lang(\"fr\",fn:exactly-one(/langs/para[1]))",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with testlang set to empty sequence Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnLang2() {
    final XQuery query = new XQuery(
      "fn:count(fn:lang((), ./langs[1]/para[1]))",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with testlang set to "en" and specified node (second argument) has xml:lang attribute set to "en-us". .
   */
  @org.junit.Test
  public void fnLang20() {
    final XQuery query = new XQuery(
      "fn:lang(\"en\",fn:exactly-one(/langs/para[3]))",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with testlang set to "en" and specified node (second argument) has xml:lang attribute set to "EN". .
   */
  @org.junit.Test
  public void fnLang21() {
    final XQuery query = new XQuery(
      "fn:lang(\"en\",fn:exactly-one(/langs/para[2]))",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with second argument set to "." and no context node. (second argument) has xml:lang attribute set to "EN". .
   */
  @org.junit.Test
  public void fnLang22() {
    final XQuery query = new XQuery(
      "fn:lang(\"en\",.)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Evaluation of the fn:lang function with testlang set to "en" as per example 1 for this function Context node is 'para xml:lang="en"' .
   */
  @org.junit.Test
  public void fnLang3() {
    final XQuery query = new XQuery(
      " for $x in /langs/para[1] return $x/fn:lang(\"en\")",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with testlang set to "en" as per example 2 for this function Context node is '<div xml:lang="en"><para>And now, and forever!</para></div>' .
   */
  @org.junit.Test
  public void fnLang4() {
    final XQuery query = new XQuery(
      "for $x in /langs/div[1]/para return $x/fn:lang(\"en\")",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with testlang set to "EN". Context node is '<para xml:lang="EN"/>' .
   */
  @org.junit.Test
  public void fnLang5() {
    final XQuery query = new XQuery(
      "for $x in /langs/para[2] return $x/fn:lang(\"EN\")",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with testlang set to "En". Context node is '<para xml:lang="EN"/>' .
   */
  @org.junit.Test
  public void fnLang6() {
    final XQuery query = new XQuery(
      " for $x in /langs/para[2] return $x/fn:lang(\"En\")",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with testlang set to "eN". Context node is '<para xml:lang="EN"/>' .
   */
  @org.junit.Test
  public void fnLang7() {
    final XQuery query = new XQuery(
      "for $x in /langs/para[2] return $x/fn:lang(\"eN\")",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with testlang set to "en". Context node is '<para xml:lang="EN"/>' .
   */
  @org.junit.Test
  public void fnLang8() {
    final XQuery query = new XQuery(
      "for $x in /langs/para[2] return $x/fn:lang(\"en\")",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluation of the fn:lang function with testlang set to "en". Context node is '<para xml:lang="en-us"/>' .
   */
  @org.junit.Test
  public void fnLang9() {
    final XQuery query = new XQuery(
      "for $x in /langs/para[3] return $x/fn:lang(\"en-us\")",
      ctx);
    try {
      query.context(node(file("fn/lang/lang.xml")));
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
   *  Evaluates The "lang" function with the arguments set as follows: $testlang = "en", node with no "xml:lang" attribute .
   */
  @org.junit.Test
  public void fnLang1args1() {
    final XQuery query = new XQuery(
      "fn:lang(xs:string(\"en\"),/root[1]/time[1])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
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
   *  Evaluates The "lang" function with the arguments set as follows: $testlang = "EN", node with no "xml:lang" attribute .
   */
  @org.junit.Test
  public void fnLang1args2() {
    final XQuery query = new XQuery(
      "fn:lang(xs:string(\"EN\"),./root[1]/time[1])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
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
   *  Evaluates The "lang" function with the arguments set as follows: $testlang = "eN", node with no "xml:lang" attribute .
   */
  @org.junit.Test
  public void fnLang1args3() {
    final XQuery query = new XQuery(
      "fn:lang(xs:string(\"eN\"),./root[1]/time[1])",
      ctx);
    try {
      query.context(node(file("docs/atomicns.xml")));
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
}
