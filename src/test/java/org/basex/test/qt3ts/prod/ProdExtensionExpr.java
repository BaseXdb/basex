package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the ExtensionExpr production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdExtensionExpr extends QT3TestSet {

  /**
   *  A pragma expression that never ends is syntactically invalid. .
   */
  @org.junit.Test
  public void kExtensionExpression1() {
    final XQuery query = new XQuery(
      "(#local:pr content # {1}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  A pragma expression that never starts is syntactically invalid. .
   */
  @org.junit.Test
  public void kExtensionExpression2() {
    final XQuery query = new XQuery(
      "local:pr content #) {1}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  A simple pragma expression. .
   */
  @org.junit.Test
  public void kExtensionExpression3() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://example.com/NotRecognized\"; (#prefix:pr content #) {1 eq 1}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A pragma expression cannot be in the empty namespace. .
   */
  @org.junit.Test
  public void kExtensionExpression4() {
    final XQuery query = new XQuery(
      "(#name content #) {1}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0081")
    );
  }

  /**
   *  A pragma expression cannot be in the empty namespace even though a prefix is used. .
   */
  @org.junit.Test
  public void kExtensionExpression5() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"\"; (# prefix:notRecognized #){1}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0081")
    );
  }

  /**
   *  A pragma expression containing complex content. .
   */
  @org.junit.Test
  public void kExtensionExpression6() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://example.com/NotRecognized\"; 1 eq (#prefix:notRecognized ##cont## # # ( \"# ) # )# )#ent #) {1}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A fallback expression must be present when no supported pragmas are specified. .
   */
  @org.junit.Test
  public void kExtensionExpression7() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://example.com/NotRecognized\"; (#prefix:PragmaNotSupported content #) {}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0079")
    );
  }

  /**
   *  A pragma expression containing many comments. .
   */
  @org.junit.Test
  public void kExtensionExpression8() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://example.com/NotRecognized\";\n" +
      "(::)1(::)eq(::)(#prefix:name ##cont## # # ( \"# ) #\n" +
      "\t\t)# )#ent #)(::){(::)1(::)}(::)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  An extension expression cannot be in an undeclared namespace. .
   */
  @org.junit.Test
  public void k2ExtensionExpression1() {
    final XQuery query = new XQuery(
      "declare namespace xs = \"\"; (#xs:name content #) {1}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0081")
    );
  }

  /**
   *  Whitespace between pragma-start and name cannot contain comments. .
   */
  @org.junit.Test
  public void k2ExtensionExpression10() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://example.com/\"; (#ex:myExtensionExpression:)#) {true()}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  A single whitespace must separate pragma name and content. .
   */
  @org.junit.Test
  public void k2ExtensionExpression11() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://example.com/\"; (#ex:myExtensionExpression :)#) {true()}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  No whitespace is required between pragma content and name if the content is empty. .
   */
  @org.junit.Test
  public void k2ExtensionExpression12() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://example.com/\"; (#ex:myExtensionExpression#) {true()}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A pragma expression that hasn't even specified a name, but has trailing whitespace. .
   */
  @org.junit.Test
  public void k2ExtensionExpression13() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://example.com/\"; (#",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  A pragma expression that hasn't even specified a name. .
   */
  @org.junit.Test
  public void k2ExtensionExpression14() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://example.com/\"; (#",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  A pragma expression with name and trailing whitespace, but without content and end. .
   */
  @org.junit.Test
  public void k2ExtensionExpression15() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://example.com/\"; (# ex:name",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  A pragma expression with name but without content and end. .
   */
  @org.junit.Test
  public void k2ExtensionExpression16() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://example.com/\"; (# ex:name",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Use four nested pragma expressions. .
   */
  @org.junit.Test
  public void k2ExtensionExpression17() {
    final XQuery query = new XQuery(
      "(#xs:a#)(#xs:a#)(#local:a#){-5}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-5")
    );
  }

  /**
   *  Whitespace isn't required if there is no pragma content. .
   */
  @org.junit.Test
  public void k2ExtensionExpression2() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://example.com/\"; (#ex:myExtensionExpression#) {true()}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Whitespace is allowed but not required if there is no pragma content. .
   */
  @org.junit.Test
  public void k2ExtensionExpression3() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://example.com/\"; (#ex:myExtensionExpression #) {true()}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Content looking like comments are not recognized as so in pragma content. asdad .
   */
  @org.junit.Test
  public void k2ExtensionExpression4() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://example.com/\"; (#ex:myExtensionExpression content#) {true()}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Content looking like comments are not recognized as so in pragma content. .
   */
  @org.junit.Test
  public void k2ExtensionExpression5() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://example.com/\"; (#ex:myExtensionExpression (:(:(:(:(: content #) {true()}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A single whitespace must separate pragma name and content. .
   */
  @org.junit.Test
  public void k2ExtensionExpression6() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://example.com/\"; (#ex:myExtensionExpression(content)#) {true()}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  A single whitespace must separate pragma name and content. content .
   */
  @org.junit.Test
  public void k2ExtensionExpression7() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://example.com/\";\n" +
      "(#ex:myExtensionExpression(:content:)#) {true()}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  A single whitespace must separate pragma name and content. .
   */
  @org.junit.Test
  public void k2ExtensionExpression8() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://example.com/\"; (#ex:myExtensionExpression:)#) {true()}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Whitespace between pragma-start and name cannot contain comments. a comment .
   */
  @org.junit.Test
  public void k2ExtensionExpression9() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://example.com/\"; (#ex:myExtensionExpression:)#) {true()}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  A simple call to an extension expression, that should not be recognized (and thus ignored) .
   */
  @org.junit.Test
  public void extexpr1() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) { /works/employee[12]/overtime }",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<overtime>\n     <day>Monday</day>\n     <day>Tuesday</day>\n   </overtime>", false)
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression is a call to fn:false() .
   */
  @org.junit.Test
  public void extexpr10() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {(fn:false())}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression is an "or" true expression ("or" operator) .
   */
  @org.junit.Test
  public void extexpr11() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {(fn:false() or fn:true())}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression is an "or" false expression ("or" operator) .
   */
  @org.junit.Test
  public void extexpr12() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {(fn:false() or fn:false())}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression is an "or" true expression ("and" operator) .
   */
  @org.junit.Test
  public void extexpr13() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {(fn:true() and fn:true())}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression is an "or" false expression ("and" operator) .
   */
  @org.junit.Test
  public void extexpr14() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {(fn:true() and fn:false())}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression is an "if" true expression .
   */
  @org.junit.Test
  public void extexpr15() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {if(fn:true()) then \"passed\" else \"failed\"}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "passed")
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression is an "if" false expression (returns "else" part of expression) .
   */
  @org.junit.Test
  public void extexpr16() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {if(fn:false()) then \"failed\" else \"passed\"}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "passed")
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression is a "typeswitch" expression .
   */
  @org.junit.Test
  public void extexpr17() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {typeswitch (\"A String\") case $i as xs:decimal return \"test failed\" case $i as xs:integer return \"test failed\" case $i as xs:string return \"test passed\" default return \"test failed\" }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "test passed")
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression uses the fn:not function. .
   */
  @org.junit.Test
  public void extexpr18() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {(fn:not(fn:true()))}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression uses the fn:string-length function. .
   */
  @org.junit.Test
  public void extexpr19() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {fn:string-length(\"abc\")}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("3")
    );
  }

  /**
   *  An extension expression with no expression .
   */
  @org.junit.Test
  public void extexpr2() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0079")
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression uses the fn:count function. .
   */
  @org.junit.Test
  public void extexpr20() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {fn:count((1,2,3))}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("3")
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression is an addition operation. .
   */
  @org.junit.Test
  public void extexpr21() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {3+2}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("5")
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression is a subtraction operation. .
   */
  @org.junit.Test
  public void extexpr22() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {10 - 5}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("5")
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression is a multiplication operation. .
   */
  @org.junit.Test
  public void extexpr23() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {10 * 2}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("20")
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression is a division operation (div operator). .
   */
  @org.junit.Test
  public void extexpr24() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {10 div 2}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("5")
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression is a division operation (idiv operator). .
   */
  @org.junit.Test
  public void extexpr25() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {10 idiv 2}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("5")
    );
  }

  /**
   *  Extension expression for missing space after pragma name. .
   */
  @org.junit.Test
  public void extexpr26() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index#){fn:count((1,2,3))}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("3")
    );
  }

  /**
   *  An pragma content containing the "#" symbol, which is ignored in an extension expression .
   */
  @org.junit.Test
  public void extexpr3() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index \"ABC#\" #) {/works/employee[12]/overtime}",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<overtime>\n     <day>Monday</day>\n     <day>Tuesday</day>\n   </overtime>", false)
    );
  }

  /**
   *  An extension expression, for which its QName can not be resolved to a namespace URI. .
   */
  @org.junit.Test
  public void extexpr4() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns2:you-do-not-know-me-as-index #) {/works/employee[12]/overtime}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0081")
    );
  }

  /**
   *  An extension expression that contains more than one pragma, both of wihc are ignored .
   */
  @org.junit.Test
  public void extexpr5() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) (# ns1:you-should-not-know-me-either #) {/works/employee[12]/overtime}",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<overtime>\n     <day>Monday</day>\n     <day>Tuesday</day>\n   </overtime>", false)
    );
  }

  /**
   *  A FLOWR expression that uses the same pragma twice and both times should be ignored. .
   */
  @org.junit.Test
  public void extexpr6() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; for $x in (# ns1:you-do-not-know-me-as-index #) {/works/employee[12]/overtime} return (# ns1:you-do-not-know-me-as-index #) {/works/employee[12]/overtime}",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<overtime>\n     <day>Monday</day>\n     <day>Tuesday</day>\n   </overtime>", false)
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression is a quantified expression ("every" operator). .
   */
  @org.junit.Test
  public void extexpr7() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {(every $x in (1,2,3) satisfies $x < 4)}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression is a quantified expression ("every" operator). .
   */
  @org.junit.Test
  public void extexpr8() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {(some $x in (1,2,3) satisfies $x = 2)}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  An extension expression for which the pragma is ignored and default expression is a call to fn:true() .
   */
  @org.junit.Test
  public void extexpr9() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://example.org/someweirdnamespace\"; (# ns1:you-do-not-know-me-as-index #) {(fn:true())}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }
}
