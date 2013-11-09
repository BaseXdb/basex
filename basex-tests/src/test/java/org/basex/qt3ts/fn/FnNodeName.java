package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the node-name() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnNodeName extends QT3TestSet {

  /**
   *  node-name() must be passed one argument, it doesn't use the context item by default in XPath 2.0. .
   */
  @org.junit.Test
  public void kNodeNameFunc1() {
    xquery10();
    final XQuery query = new XQuery(
      "node-name()",
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
   * Written by: Frans Englich modified by Michael Kay  node-name() does use the context item by default in XPath 3.0. .
   */
  @org.junit.Test
  public void kNodeNameFunc1a() {
    final XQuery query = new XQuery(
      "node-name()",
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
   *  A test whose essence is: `node-name((), "wrong param")`. .
   */
  @org.junit.Test
  public void kNodeNameFunc2() {
    final XQuery query = new XQuery(
      "node-name((), \"wrong param\")",
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
   *  A test whose essence is: `empty(node-name(()))`. .
   */
  @org.junit.Test
  public void kNodeNameFunc3() {
    final XQuery query = new XQuery(
      "empty(node-name(()))",
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
   *  Invoke fn:node-name() with too many arguments. .
   */
  @org.junit.Test
  public void k2NodeNameFunc1() {
    final XQuery query = new XQuery(
      "node-name(/*, ())",
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
   *  Invoke fn:node-name() with an invalid argument. .
   */
  @org.junit.Test
  public void k2NodeNameFunc2() {
    final XQuery query = new XQuery(
      "node-name(\"string\")",
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
   *  Check the return value of fn:node-name(). .
   */
  @org.junit.Test
  public void k2NodeNameFunc3() {
    final XQuery query = new XQuery(
      "node-name(/*) instance of xs:QName",
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
      assertBoolean(true)
    );
  }

  /**
   *  Check invalid inputs to fn:node-name(). .
   */
  @org.junit.Test
  public void k3NodeNameFunc1() {
    final XQuery query = new XQuery(
      "node-name(3.3)",
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
      error("XPTY0004")
    );
  }

  /**
   * Check invalid multi-node input sequence to fn:node-name()..
   */
  @org.junit.Test
  public void k3NodeNameFunc2() {
    final XQuery query = new XQuery(
      "node-name( (<a/>, <b/>))",
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
      error("XPTY0004")
    );
  }

  /**
   * Check invalid mix node,item input sequence to fn:node-name()..
   */
  @org.junit.Test
  public void k3NodeNameFunc3() {
    final XQuery query = new XQuery(
      "node-name( (<a/>, \"mystring\"))",
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
      error("XPTY0004")
    );
  }

  /**
   * Check invalid input sequence to fn:node-name() from fn:node-name() call..
   */
  @org.junit.Test
  public void k3NodeNameFunc4() {
    final XQuery query = new XQuery(
      "node-name( node-name(<a/>))",
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
      error("XPTY0004")
    );
  }

  /**
   *  Test fn:node-name on a comment(). .
   */
  @org.junit.Test
  public void cbclNodeName001() {
    final XQuery query = new XQuery(
      "empty(node-name( comment { \"comments have no name \" } ) )",
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
   *  Evaluation of node function with argument set empty sequence. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnNodeName1() {
    final XQuery query = new XQuery(
      "fn:count(fn:node-name(()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to a computed constructed element node with no namespace declaration and one child. Use fn:local-name-from-qName to retrieve local part. .
   */
  @org.junit.Test
  public void fnNodeName10() {
    final XQuery query = new XQuery(
      "fn:local-name-from-QName(fn:node-name(element elementName { element achild {\"some text\"}}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "elementName")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to a computed constructed attribute node with value set to empty string. Use fn:local-name-from-qName to retrieve local name. .
   */
  @org.junit.Test
  public void fnNodeName11() {
    final XQuery query = new XQuery(
      "fn:local-name-from-QName(fn:node-name(attribute attributeName {\"\"}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "attributeName")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to a computed constructed attribute node with value set to a non empty value. Use fn:local-name-from-qName to retrieve local name. .
   */
  @org.junit.Test
  public void fnNodeName12() {
    final XQuery query = new XQuery(
      "fn:local-name-from-QName(fn:node-name(attribute attributeName {\"an attribute value\"}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "attributeName")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to a computed constructed attribute node with value set to a non empty value. Use fn:namespace-uri-from-qName to retrieve local name and fn:count. to avoid empty file. .
   */
  @org.junit.Test
  public void fnNodeName13() {
    final XQuery query = new XQuery(
      "fn:count(fn:namespace-uri-from-QName(fn:node-name(attribute attributeName {\"an attribute value\"})))",
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
   *  Evaluation of node-name function with argument set to a computed constructed document node with value set to the empty string. Use fn:local-name-from-qName to retrieve local name and fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNodeName14() {
    final XQuery query = new XQuery(
      "fn:count(fn:local-name-from-QName(fn:node-name(document {\"\"})))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to a computed constructed document node with value set to the empty string. Use fn:namspace-uri-from-qName to retrieve local name and fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNodeName15() {
    final XQuery query = new XQuery(
      "fn:count(fn:namespace-uri-from-QName(fn:node-name(document {\"\"})))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to a computed constructed document node with value set to the a non empty value. Use fn:local-name-from-qName to retrieve local name and fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNodeName16() {
    final XQuery query = new XQuery(
      "fn:count(fn:local-name-from-QName(fn:node-name(document {\"<element1> text </element1>\"})))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to a computed constructed document node with value set to the a non empty value. Use fn:namespace-uri-from-qName to retrieve local name and fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNodeName17() {
    final XQuery query = new XQuery(
      "fn:count(fn:namespace-uri-from-QName(fn:node-name(document {\"<element1> text </element1>\"})))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to a computed constructed text node with value set to the the empty string. Use fn:local-name-from-qName to retrieve local name and fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNodeName18() {
    final XQuery query = new XQuery(
      "fn:count(fn:local-name-from-QName(fn:node-name(text {\"\"})))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to a computed constructed text node with value set to the the empty string. Use fn:namespace-uri-from-qName to retrieve namespace and fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNodeName19() {
    final XQuery query = new XQuery(
      "fn:count(fn:namespace-uri-from-QName(fn:node-name(text {\"\"})))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to comment node. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnNodeName2() {
    final XQuery query = new XQuery(
      "fn:count(fn:node-name(/works[1]/employee[2]/child::text()[last()]))",
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
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to a computed constructed text node with value set to a non empty value. Use fn:local-name-from-QName to retrieve local name and fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNodeName20() {
    final XQuery query = new XQuery(
      "fn:count(fn:local-name-from-QName(fn:node-name(text {\"a text value\"})))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to a computed constructed text node with value set to a non empty value. Use fn:namespace-uri-from-QName to retrieve namespace and fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNodeName21() {
    final XQuery query = new XQuery(
      "fn:count(fn:namespace-uri-from-QName(fn:node-name(text {\"a text value\"})))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to a computed constructed processing instruction node. Use fn:fn-local-name-from-QName to retrieve local name. .
   */
  @org.junit.Test
  public void fnNodeName22() {
    final XQuery query = new XQuery(
      "fn:local-name-from-QName(fn:node-name(processing-instruction piName {\"Processing Instruction content\"}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "piName")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to a computed constructed processing instruction node. Use fn:fn-namespace-uri-from-QName to retrieve namespace and fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNodeName23() {
    final XQuery query = new XQuery(
      "fn:count(fn:namespace-uri-from-QName(fn:node-name(processing-instruction piName {\"Processing Instruction content\"})))",
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
   *  Evaluation of node-name function with wrong number of arguments. .
   */
  @org.junit.Test
  public void fnNodeName24() {
    final XQuery query = new XQuery(
      "fn:node-name(processing-instruction piName {\"Processing Instruction content\"},\"A Second Argument\")",
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
   *  Evaluation of node-name function with no arguments on element node. .
   */
  @org.junit.Test
  public void fnNodeName25() {
    final XQuery query = new XQuery(
      "<node xmlns=\"http://example.com/ns\"/>/node-name()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("QName(\"http://example.com/ns\", \"node\")")
    );
  }

  /**
   *  Evaluation of node-name function with no arguments on attribute node. .
   */
  @org.junit.Test
  public void fnNodeName26() {
    final XQuery query = new XQuery(
      "<node xml:space=\"preserve\" xmlns=\"http://example.com/ns\"/>/@xml:space/node-name()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("QName(\"http://www.w3.org/XML/1998/namespace\", \"space\")")
    );
  }

  /**
   *  Evaluation of node-name function with no arguments on processing instruction node. .
   */
  @org.junit.Test
  public void fnNodeName27() {
    final XQuery query = new XQuery(
      "<?test data?>/node-name()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("QName(\"\", \"test\")")
    );
  }

  /**
   *  Evaluation of node-name function with no arguments on namespace node. .
   */
  @org.junit.Test
  public void fnNodeName28() {
    final XQuery query = new XQuery(
      "namespace{\"foo\"}{\"http://example.com/foo\"}/node-name()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("QName(\"\", \"foo\")")
    );
  }

  /**
   *  Evaluation of node-name function with no arguments on text node. .
   */
  @org.junit.Test
  public void fnNodeName29() {
    final XQuery query = new XQuery(
      "<a>abc</a>/text()/node-name()",
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
   *  Evaluation of node-name function with argument set to an element node. Uses local-name-from-QName to get local part .
   */
  @org.junit.Test
  public void fnNodeName3() {
    final XQuery query = new XQuery(
      "fn:local-name-from-QName(fn:node-name(/works[1]/employee[2]))",
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
      assertStringValue(false, "employee")
    );
  }

  /**
   *  Evaluation of node-name function with no arguments on atomic value. .
   */
  @org.junit.Test
  public void fnNodeName30() {
    final XQuery query = new XQuery(
      "79[node-name()]",
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
   *  Evaluation of node-name function with no arguments on function item. .
   */
  @org.junit.Test
  public void fnNodeName31() {
    final XQuery query = new XQuery(
      "node-name#0!node-name()",
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
   *  Evaluation of node-name function with no arguments with absent context. .
   */
  @org.junit.Test
  public void fnNodeName32() {
    final XQuery query = new XQuery(
      "node-name()",
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
   *  Evaluation of node-name function with argument set to an element node. use local-name-from-QName to get the local part. .
   */
  @org.junit.Test
  public void fnNodeName4() {
    final XQuery query = new XQuery(
      "fn:local-name-from-QName(fn:node-name(<shoe size = \"5\"/>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "shoe")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to comment node. Use of fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNodeName5() {
    final XQuery query = new XQuery(
      "fn:count(fn:node-name(<!-- This a comment node -->))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to a Processing Instruction node. Use local-name-from-QName to get local part .
   */
  @org.junit.Test
  public void fnNodeName6() {
    final XQuery query = new XQuery(
      "fn:local-name-from-QName(fn:node-name(<?format role=\"output\" ?>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "format")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to a directly constructed element node with namespace declaration. Use namespace-uri-from-qName to retrive values from QName. .
   */
  @org.junit.Test
  public void fnNodeName7() {
    final XQuery query = new XQuery(
      "fn:namespace-uri-from-QName(fn:node-name(<anelement xmlns = \"http://example.com/examples\"></anelement>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.com/examples")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to a computed constructed element node with no namespace declaration and no children. Use local-name-from-qName to retrieve local part. .
   */
  @org.junit.Test
  public void fnNodeName8() {
    final XQuery query = new XQuery(
      "fn:local-name-from-QName(fn:node-name(element elementName {}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "elementName")
    );
  }

  /**
   *  Evaluation of node-name function with argument set to a computed constructed element node with no namespace declaration and no children. Use fn:namespace-uri-from-qName to retrieve namespace. Should return empty string. Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnNodeName9() {
    final XQuery query = new XQuery(
      "fn:count(fn:namespace-uri-from-QName(fn:node-name(element elementName {})))",
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
}
