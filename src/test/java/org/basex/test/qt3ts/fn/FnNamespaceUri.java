package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the namespace-uri() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnNamespaceUri extends QT3TestSet {

  /**
   *  A test whose essence is: `namespace-uri((), "wrong param")`. .
   */
  @org.junit.Test
  public void kNodeNamespaceURIFunc1() {
    final XQuery query = new XQuery(
      "namespace-uri((), \"wrong param\")",
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
   *  A test whose essence is: `if(false()) then namespace-uri() else true()`. .
   */
  @org.junit.Test
  public void kNodeNamespaceURIFunc2() {
    final XQuery query = new XQuery(
      "if(false()) then namespace-uri() else true()",
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
   *  A test whose essence is: `namespace-uri(()) eq xs:anyURI("")`. .
   */
  @org.junit.Test
  public void kNodeNamespaceURIFunc3() {
    final XQuery query = new XQuery(
      "namespace-uri(()) eq xs:anyURI(\"\")",
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
   *  Extract the namespace URI from a processing instruction node. .
   */
  @org.junit.Test
  public void k2NodeNamespaceURIFunc1() {
    final XQuery query = new XQuery(
      "namespace-uri(<?target data?>) eq \"\"",
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
   *  Extract the namespace URI from a comment node. .
   */
  @org.junit.Test
  public void k2NodeNamespaceURIFunc2() {
    final XQuery query = new XQuery(
      "namespace-uri(<!--comment-->) eq \"\"",
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
   *  Extract the namespace URI from a text node. .
   */
  @org.junit.Test
  public void k2NodeNamespaceURIFunc3() {
    final XQuery query = new XQuery(
      "namespace-uri(text{()}) eq \"\"",
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
   *  test fn:boolean on fn:namespace-uri .
   */
  @org.junit.Test
  public void cbclNamespaceUri001() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:namespace-uri(<element />))",
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
   *  Evaluation of the fn:namespace-uri function with an undefined context node and no argument. .
   */
  @org.junit.Test
  public void fnNamespaceUri1() {
    final XQuery query = new XQuery(
      "fn:namespace-uri()",
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
   *  Evaluation of the fn:namespace-uri function argument set to an element node with no namespace. Use the fn:count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnNamespaceUri10() {
    final XQuery query = new XQuery(
      "namespace-uri(/*)",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"\"")
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function argument set to a computed element node with no namespace. Use the fn:count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnNamespaceUri11() {
    final XQuery query = new XQuery(
      "fn:namespace-uri(element elementNode {\"with no namespace\"})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"\"")
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function argument set to an attribute node with no namespace. Use the fn:count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnNamespaceUri12() {
    final XQuery query = new XQuery(
      "fn:namespace-uri(/works/employee[1]/@name)",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"\"")
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function argument set to a computed attribute node with no namespace. .
   */
  @org.junit.Test
  public void fnNamespaceUri13() {
    final XQuery query = new XQuery(
      "fn:namespace-uri(attribute anAttribute {\"Attribute Value No Namespace\"})",
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
        assertEq("\"\"")
      &&
        assertType("xs:anyURI")
      )
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function argument set to an element node with no namespace queried from a file. .
   */
  @org.junit.Test
  public void fnNamespaceUri14() {
    final XQuery query = new XQuery(
      "fn:namespace-uri(/works[1]/employee[1])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"\"")
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function argument set to an attribute node with no namespace queried from a file. .
   */
  @org.junit.Test
  public void fnNamespaceUri15() {
    final XQuery query = new XQuery(
      "fn:namespace-uri(/works[1]/employee[1]/@name)",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"\"")
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function with no argument. Use an element node with no namespace queried from a file. .
   */
  @org.junit.Test
  public void fnNamespaceUri16() {
    final XQuery query = new XQuery(
      "let $var := /works/employee[1] return $var/fn:namespace-uri()",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("\"\"")
      &&
        assertType("xs:anyURI")
      )
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function with argument set to a direct element node with a namespace attribute. Use the string function .
   */
  @org.junit.Test
  public void fnNamespaceUri17() {
    final XQuery query = new XQuery(
      "namespace-uri(<anElement xmlns = \"http://www.example.com/examples\"/>)",
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
        assertEq("\"http://www.example.com/examples\"")
      &&
        assertType("xs:anyURI")
      )
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function with argument set to a computed element node (with prefix) that uses a declared namespace attribute. Use the string function .
   */
  @org.junit.Test
  public void fnNamespaceUri18() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://www.example.com/examples\"; \n" +
      "            fn:string(fn:namespace-uri(element ex:anElement {\"An Element Content\"}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   * Evaluation of the fn:namespace-uri function with argument set to a computed element node (with no prefix) 
   *         that should not use a declared namespace attribute. Use the count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnNamespaceUri19() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://www.example.com/examples\"; \n" +
      "            fn:namespace-uri(element anElement {\"An Element Content\"})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function with more than one argument. .
   */
  @org.junit.Test
  public void fnNamespaceUri2() {
    final XQuery query = new XQuery(
      "fn:namespace-uri(/*,\"A Second Argument\")",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
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
   *  Evaluation of the fn:namespace-uri function with argument set to a direct element node (with no prefix) 
   *         that should not use a declared namespace attribute. Use the count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnNamespaceUri20() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://www.example.com/examples\"; \n" +
      "              fn:namespace-uri(<anElement>An Element Content</anElement>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function with argument set to a direct element node with prefix that should use a declared namespace attribute. Use the string function. .
   */
  @org.junit.Test
  public void fnNamespaceUri21() {
    final XQuery query = new XQuery(
      "declare namespace ex = \"http://www.example.com/examples\"; \n" +
      "            fn:namespace-uri(<ex:anElement>An Element Content</ex:anElement>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function with argument set to a direct element node with no prefix that should use a declared defaultnamespace attribute. Use the string function. .
   */
  @org.junit.Test
  public void fnNamespaceUri22() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/examples\"; \n" +
      "            fn:string(fn:namespace-uri(<anElement>An Element Content</anElement>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function with argument set to a direct element node with prefix that should not used the declared defaultnamespace attribute. Use the string function. .
   */
  @org.junit.Test
  public void fnNamespaceUri23() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/examples\"; \n" +
      "        declare namespace ex = \"http://www.example.com/exampleswithPrefix\"; \n" +
      "        fn:string(fn:namespace-uri(<ex:anElement>An Element Content</ex:anElement>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/exampleswithPrefix")
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function with argument set to a computed element node with prefix that should not used the declared defaultnamespace attribute. Use the string function. .
   */
  @org.junit.Test
  public void fnNamespaceUri24() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/examples\"; \n" +
      "        declare namespace ex = \"http://www.example.com/exampleswithPrefix\"; \n" +
      "        fn:string(fn:namespace-uri(element ex:anElement {\"An Element Content\"}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/exampleswithPrefix")
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function with argument set to a computed element node with prefix no that should used the declared default namespace attribute. Use the string function. .
   */
  @org.junit.Test
  public void fnNamespaceUri25() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/examples\"; \n" +
      "        declare namespace ex = \"http://www.example.com/exampleswithPrefix\"; \n" +
      "        fn:string(fn:namespace-uri(element anElement {\"An Element Content\"}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function with an undefined context node and argument set to ".". .
   */
  @org.junit.Test
  public void fnNamespaceUri26() {
    final XQuery query = new XQuery(
      "fn:namespace-uri(.)",
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
   *  Evaluation of the fn:namespace-uri function with the context item not being a node. .
   */
  @org.junit.Test
  public void fnNamespaceUri3() {
    final XQuery query = new XQuery(
      "(1 to 100)[fn:namespace-uri()]",
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
   *  Evaluation of the fn:namespace-uri function argument set to empty sequence. .
   */
  @org.junit.Test
  public void fnNamespaceUri4() {
    final XQuery query = new XQuery(
      "fn:namespace-uri(())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"\"")
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function argument set to a comment node. Use the fn:count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnNamespaceUri5() {
    final XQuery query = new XQuery(
      "namespace-uri((//comment())[1])",
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
      assertEq("\"\"")
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function argument set to a computed comment node. .
   */
  @org.junit.Test
  public void fnNamespaceUri6() {
    final XQuery query = new XQuery(
      "namespace-uri(<!--comment-->)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"\"")
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function argument set to a processing instruction node. .
   */
  @org.junit.Test
  public void fnNamespaceUri7() {
    final XQuery query = new XQuery(
      "namespace-uri((//processing-instruction())[1])",
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
      assertEq("\"\"")
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function argument set to a constructed processing instruction node. .
   */
  @org.junit.Test
  public void fnNamespaceUri8() {
    final XQuery query = new XQuery(
      "namespace-uri(<?pi data?>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"\"")
    );
  }

  /**
   *  Evaluation of the fn:namespace-uri function argument set to a text node. Use the fn:count function to avoid empty file. .
   */
  @org.junit.Test
  public void fnNamespaceUri9() {
    final XQuery query = new XQuery(
      "namespace-uri((//text())[1])",
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
      assertEq("\"\"")
    );
  }
}
