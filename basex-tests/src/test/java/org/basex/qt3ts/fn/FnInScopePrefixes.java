package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the in-scope-prefixes() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnInScopePrefixes extends QT3TestSet {

  /**
   *  A test whose essence is: `in-scope-prefixes()`. .
   */
  @org.junit.Test
  public void kInScopePrefixesFunc1() {
    final XQuery query = new XQuery(
      "in-scope-prefixes()",
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
   *  A test whose essence is: `in-scope-prefixes("string", "nodetest", "wrong param")`. .
   */
  @org.junit.Test
  public void kInScopePrefixesFunc2() {
    final XQuery query = new XQuery(
      "in-scope-prefixes(\"string\", \"nodetest\", \"wrong param\")",
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
   *  For a directly constructed element fn:in-scope-prefixes() returns 'xml' and the zero length string for the default element namespace. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc1() {
    final XQuery query = new XQuery(
      "count(in-scope-prefixes(<e/>))",
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
   *  Check that a default namespace declaration is picked up. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc10() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com\"; count(fn:in-scope-prefixes(<e/>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
   *  Check the in-scope namespaces of different elements, constructed with direct constructors. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc11() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/\"; let $i := <e> <a xmlns=\"\"/> <b xmlns=\"http://www.example.com/\"/> <c xmlns=\"http://www.example.com/Second\"/> </e> return (count(in-scope-prefixes($i)), count(in-scope-prefixes(exactly-one($i/*[namespace-uri() eq \"\"]))), count(in-scope-prefixes(exactly-one($i/b))), count(in-scope-prefixes(exactly-one($i/*[namespace-uri() eq \"http://www.example.com/Second\"]))), $i)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("2 1 2 2<e xmlns=\"http://www.example.com/\"><a xmlns=\"\"/><b/><c xmlns=\"http://www.example.com/Second\"/></e>", false)
    );
  }

  /**
   *  Check the in-scope namespaces of different elements, constructed with computed constructors. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc12() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/\"; let $i := element e { element {QName(\"\", \"a\")} {}, element {QName(\"http://www.example.com/\", \"b\")} {}, element {QName(\"http://www.example.com/Second\", \"c\")} {} } return (count(in-scope-prefixes($i)), count(in-scope-prefixes(exactly-one($i/*[namespace-uri() eq \"\"]))), count(in-scope-prefixes(exactly-one($i/b))), count(in-scope-prefixes(exactly-one($i/*[namespace-uri() eq \"http://www.example.com/Second\"]))), $i)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("2 1 2 2<e xmlns=\"http://www.example.com/\"><a xmlns=\"\"/><b/><c xmlns=\"http://www.example.com/Second\"/></e>", false)
    );
  }

  /**
   *  Ensure the in-scope prefixes are correct with computed constructors when combined with a default element namespace declaration. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc13() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/\"; let $i := element e { element b {()} } return (count(in-scope-prefixes($i/b)), count(in-scope-prefixes($i)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 2")
    );
  }

  /**
   *  Check the in-scope namespaces of a single, computed element. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc14() {
    final XQuery query = new XQuery(
      "count(in-scope-prefixes(element e{()}))",
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
   *  Ensure the in-scope prefixes are correct with computed constructors. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc15() {
    final XQuery query = new XQuery(
      "let $i := element e { element b {()} } return (count(in-scope-prefixes($i/b)), count(in-scope-prefixes($i)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 1")
    );
  }

  /**
   *  Check that a default namespace declaration attribute on a direct element constructor 
   *         correctly affect a computed child constructor. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc16() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $i := <e xmlns=\"http://example.com/\"> \n" +
      "                    {element a {()}} \n" +
      "                  </e> \n" +
      "        return (count(in-scope-prefixes($i)), count(in-scope-prefixes(exactly-one($i/*))))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 2")
    );
  }

  /**
   *  Ensure the namespace used in the name is part of the in-scope prefixes. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc17() {
    final XQuery query = new XQuery(
      "declare namespace p = \"http://example.com/\"; count(in-scope-prefixes(<p:e/>)), count(in-scope-prefixes(element p:e {()}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 2")
    );
  }

  /**
   *  Ensure prefix namespace declarations are counted as in-scope bindings. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc18() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://example.com/\"; <e xmlns:p=\"http://example.com/\"> { count(in-scope-prefixes(<e/>)), count(in-scope-prefixes(element e {()})) } </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns=\"http://example.com/\" xmlns:p=\"http://example.com/\">3 3</e>", false)
    );
  }

  /**
   *  Count the in-scope namespaces of a node with name xml:space. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc19() {
    final XQuery query = new XQuery(
      "count(in-scope-prefixes(element xml:space {()}))",
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
   *  For a computed element fn:in-scope-prefixes() returns 'xml'. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc2() {
    final XQuery query = new XQuery(
      "count(in-scope-prefixes(element name {7}))",
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
   *  Count the in-scope namespaces of a node with name fn:space. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc20() {
    final XQuery query = new XQuery(
      "count(in-scope-prefixes(element fn:space {()}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
   *  Count the in-scope namespaces of a node with name fn:space. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc21() {
    final XQuery query = new XQuery(
      "count(in-scope-prefixes(element xs:space {()}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
   *  Count the in-scope namespaces of a node with name fn:space(#2). .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc22() {
    final XQuery query = new XQuery(
      "count(in-scope-prefixes(<fn:space/>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
   *  Count the in-scope namespaces of a node with name fn:space(#2). .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc23() {
    final XQuery query = new XQuery(
      "count(in-scope-prefixes(<xs:space/>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
   *  Ensure a prolog namespace declaration isn't in the in-scope bindings unless it's used. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc24() {
    final XQuery query = new XQuery(
      "declare namespace p = \"http://example.com/\"; count(in-scope-prefixes(<element/>))",
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
   *  Check the in-scope namespaces of two nodes. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc25() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace a = \"http://example.com/1\"; \n" +
      "        declare namespace b = \"http://example.com/2\"; \n" +
      "        declare namespace unused = \"http://example.com/3\"; \n" +
      "        declare namespace unused2 = \"http://example.com/4\"; \n" +
      "        <unused:e/>[2], \n" +
      "        <e a:n1=\"content\" b:n1=\"content\"> <a:n1/> </e>/\n" +
      "            (for $i in in-scope-prefixes(.) order by $i return $i, \n" +
      "             '|', \n" +
      "             for $i in a:n1/in-scope-prefixes(.) order by $i return $i)\n" +
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
      assertStringValue(false, "a b xml | a b xml")
    );
  }

  /**
   *  . .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc26() {
    final XQuery query = new XQuery(
      "let $i := <e> { attribute {QName(\"http://example.com/\", \"prefix:attributeName\")} {()} } </e> return ($i, for $ps in in-scope-prefixes($i) order by $ps return $ps)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns:prefix=\"http://example.com/\" prefix:attributeName=\"\"/>prefix xml", false)
    );
  }

  /**
   *  Check in scope namespaces. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc27() {
    final XQuery query = new XQuery(
      "declare namespace a = \"http://example.com/1\"; declare namespace b = \"http://example.com/2\"; <e a:n1=\"content\" b:n1=\"content\"/>/(for $i in in-scope-prefixes(.) order by $i return $i)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a b xml")
    );
  }

  /**
   *  Check in-scope declaration of a tree fragment overriding and undeclaration the default namespace. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc28() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/\"; let $i := <e> <a xmlns=\"\"/> <b xmlns=\"http://www.example.com/\"/> <c xmlns=\"http://www.example.com/Second\"/> </e> return (count(in-scope-prefixes($i)), count(in-scope-prefixes(exactly-one($i/*[namespace-uri() eq \"\"]))), count(in-scope-prefixes(exactly-one($i/b))), count(in-scope-prefixes(exactly-one($i/*[namespace-uri() eq \"http://www.example.com/Second\"]))), $i)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("2 1 2 2<e xmlns=\"http://www.example.com/\"><a xmlns=\"\"/><b/><c xmlns=\"http://www.example.com/Second\"/></e>", false)
    );
  }

  /**
   *  Use a computed element constructor which undeclares the default namespace, as operand to a path expression. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc29() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/\"; <a2/>/element e { element {QName(\"\", \"a\")} {} }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns=\"http://www.example.com/\"><a xmlns=\"\"/></e>", false)
    );
  }

  /**
   *  in-scope-prefixes() can't take text nodes. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc3() {
    final XQuery query = new XQuery(
      "in-scope-prefixes(text {\"some text\"})",
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
   *  Use a computed element constructor which undeclares the default namespace, as operand to a path expression(#2). .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc30() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/\"; <a2 xmlns:p=\"http://ns.example.com/foo\"/>/element e { element {QName(\"http://example.com/2\", \"p:a\")} {} }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns=\"http://www.example.com/\"><p:a xmlns:p=\"http://example.com/2\"/></e>", false)
    );
  }

  /**
   *  in-scope-prefixes() can't take text nodes. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc4() {
    final XQuery query = new XQuery(
      "in-scope-prefixes(comment {\"content\"})",
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
   *  in-scope-prefixes() can't take processing instructions. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc5() {
    final XQuery query = new XQuery(
      "in-scope-prefixes(<?target data?>)",
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
   *  Ensure the default element namespace is in-scope properly. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc6() {
    final XQuery query = new XQuery(
      "count(in-scope-prefixes(<a xmlns=\"http://www.example.com\" xmlns:p=\"http://ns.example.com/asd\" xmlns:b=\"http://ns.example.com/asd\"/>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("4")
    );
  }

  /**
   *  Ensure the default namespace is properly handled. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc7() {
    final XQuery query = new XQuery(
      "count(in-scope-prefixes(<a xmlns=\"\"/>))",
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
   *  Ensure the in-scope prefixes dealt with correctly for double default namespace declarations of different kinds. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc8() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/A\"; count(in-scope-prefixes(<anElement xmlns=\"http://www.example.com/B\"/>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
   *  Check that a prefix declaration is in scope in a sub-node. .
   */
  @org.junit.Test
  public void k2InScopePrefixesFunc9() {
    final XQuery query = new XQuery(
      "for $i in fn:in-scope-prefixes(<e xmlns:p=\"http://example.com\" xmlns:a=\"http://example.com\"> <b/> </e>/b) order by $i return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a p xml")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function with incorrect arity. .
   */
  @org.junit.Test
  public void fnInScopePrefixes1() {
    final XQuery query = new XQuery(
      "fn:in-scope-prefixes(<a1 xmlns:p1=\"http://www.exampole.com\"></a1>,\"Second Argument\")",
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
   *  Evaluation of fn:in-scope-prefixes function for a directly constructed element used as argument to fn:string-length. .
   */
  @org.junit.Test
  public void fnInScopePrefixes10() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:in-scope-prefixes(<anElement>Some content</anElement>)[1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for a directly constructed element used as argument to fn:upper-case. .
   */
  @org.junit.Test
  public void fnInScopePrefixes11() {
    final XQuery query = new XQuery(
      "fn:upper-case(fn:in-scope-prefixes(<anElement>Some content</anElement>)[1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "XML")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for a directly constructed element used as argument to fn:lower-case. .
   */
  @org.junit.Test
  public void fnInScopePrefixes12() {
    final XQuery query = new XQuery(
      "fn:lower-case(fn:in-scope-prefixes(<anElement>Some content</anElement>)[1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xml")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for a directly constructed element used as argument to fn:string-to-codepoints. .
   */
  @org.junit.Test
  public void fnInScopePrefixes13() {
    final XQuery query = new XQuery(
      "fn:string-to-codepoints(fn:in-scope-prefixes(<anElement>Some content</anElement>)[1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "120 109 108")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for a directly constructed element used as argument to xs:string. .
   */
  @org.junit.Test
  public void fnInScopePrefixes14() {
    final XQuery query = new XQuery(
      "xs:string(fn:in-scope-prefixes(<anElement>Some content</anElement>)[1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xml")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for a directly constructed element used as argument to fn:substring-before. .
   */
  @org.junit.Test
  public void fnInScopePrefixes15() {
    final XQuery query = new XQuery(
      "fn:substring-before(fn:in-scope-prefixes(<anElement>Some content</anElement>)[1],\"m\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "x")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for a directly constructed element used as argument to fn:substring-after. .
   */
  @org.junit.Test
  public void fnInScopePrefixes16() {
    final XQuery query = new XQuery(
      "fn:substring-after(fn:in-scope-prefixes(<anElement>Some content</anElement>)[1],\"m\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "l")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for a directly constructed element used as argument to fn:concat. .
   */
  @org.junit.Test
  public void fnInScopePrefixes17() {
    final XQuery query = new XQuery(
      "fn:concat(fn:in-scope-prefixes(<anElement>Some content</anElement>),\"m\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xmlm")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for a directly constructed element used as argument to fn:string-join. .
   */
  @org.junit.Test
  public void fnInScopePrefixes18() {
    final XQuery query = new XQuery(
      "fn:string-join((fn:in-scope-prefixes(<anElement>Some content</anElement>),\"xml\"),\"\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xmlxml")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for a directly constructed element used as argument to fn:susbtring. .
   */
  @org.junit.Test
  public void fnInScopePrefixes19() {
    final XQuery query = new XQuery(
      "fn:substring(fn:in-scope-prefixes(<anElement>Some content</anElement>)[1],2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ml")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function with incorrect argument type. .
   */
  @org.junit.Test
  public void fnInScopePrefixes2() {
    final XQuery query = new XQuery(
      "fn:in-scope-prefixes(200)",
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
   *  Evaluation of fn:in-scope-prefixes function for a directly constructed element used as argument to fn:contains. .
   */
  @org.junit.Test
  public void fnInScopePrefixes20() {
    final XQuery query = new XQuery(
      "fn:contains(fn:in-scope-prefixes(<anElement>Some content</anElement>)[1],\"l\")",
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
   *  Evaluation of fn:in-scope-prefixes function for an external document. .
   */
  @org.junit.Test
  public void fnInScopePrefixes21() {
    final XQuery query = new XQuery(
      "in-scope-prefixes(/*)",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertPermutation("\"ma\", \"xlink\", \"anyzone\", \"eachbay\", \"yabadoo\", \"xml\"")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for an external document. .
   */
  @org.junit.Test
  public void fnInScopePrefixes22() {
    final XQuery query = new XQuery(
      "in-scope-prefixes((//*)[19])",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertPermutation("\"ma\", \"xlink\", \"anyzone\", \"eachbay\", \"yabadoo\", \"xml\"")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for an external document. .
   */
  @org.junit.Test
  public void fnInScopePrefixes23() {
    final XQuery query = new XQuery(
      "in-scope-prefixes(/)",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
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
   *  Evaluation of fn:in-scope-prefixes function for a document with namespaces declared in the DTD. .
   */
  @org.junit.Test
  public void fnInScopePrefixes25() {
    final XQuery query = new XQuery(
      "in-scope-prefixes(/*)",
      ctx);
    try {
      query.context(node(file("fn/in-scope-prefixes/NamespaceSuppliedInternally.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertPermutation("\"\", \"xml\", \"xlink\"")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for a document that undeclares the default namespace. .
   */
  @org.junit.Test
  public void fnInScopePrefixes26() {
    final XQuery query = new XQuery(
      "in-scope-prefixes(/*/p)",
      ctx);
    try {
      query.context(node(file("fn/path/pathdata.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xml")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for a directly constructed element node. .
   */
  @org.junit.Test
  public void fnInScopePrefixes3() {
    final XQuery query = new XQuery(
      "fn:in-scope-prefixes(<anElement>some content</anElement>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xml")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for a directly constructed element node with a namespace declaration. .
   */
  @org.junit.Test
  public void fnInScopePrefixes4() {
    final XQuery query = new XQuery(
      "fn:in-scope-prefixes(<anElement xmlns:p1 = \"http://www.example.com\">some content</anElement>)",
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
        assertStringValue(false, "xml p1")
      ||
        assertStringValue(false, "p1 xml")
      )
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for a computed constructed element node with no namespace declaration. .
   */
  @org.junit.Test
  public void fnInScopePrefixes5() {
    final XQuery query = new XQuery(
      "fn:in-scope-prefixes(element anElement {\"Some content\"})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xml")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for a computed constructed element node and a default namesapce declaration. .
   */
  @org.junit.Test
  public void fnInScopePrefixes6() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com\"; let $seq := fn:in-scope-prefixes(element anElement {\"Some content\"}) return (count($seq),$seq=(\"xml\",\"\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 true")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for a directly constructed element node and a default namesapce declaration. .
   */
  @org.junit.Test
  public void fnInScopePrefixes7() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com\"; let $seq := fn:in-scope-prefixes(<anElement>Some content</anElement>) return (count($seq),$seq=(\"xml\",\"\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 true")
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for a directly constructed element node with xmlns attribute and a prolog namesapce declaration. .
   */
  @org.junit.Test
  public void fnInScopePrefixes8() {
    final XQuery query = new XQuery(
      "declare namespace p1 = \"http://www.example.com\"; fn:in-scope-prefixes(<anElement xmlns:p1=\"http://www.somenamespace.com\">Some content</anElement>)",
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
        assertStringValue(false, "xml p1")
      ||
        assertStringValue(false, "p1 xml")
      )
    );
  }

  /**
   *  Evaluation of fn:in-scope-prefixes function for a directly constructed element node without xmlns attribute and a prolog namesapce declaration. .
   */
  @org.junit.Test
  public void fnInScopePrefixes9() {
    final XQuery query = new XQuery(
      "declare namespace p1 = \"http://www.example.com\"; fn:in-scope-prefixes(<anElement>Some content</anElement>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xml")
    );
  }
}
