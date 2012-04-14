package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the node-name() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnNodeName extends QT3TestSet {

  /**
   * Written by: Frans Englich modified by Michael Kay  node-name() does use the context item by default in XPath 3.0. .
   */
  @org.junit.Test
  public void kNodeNameFunc1a() {
    final XQuery query = new XQuery(
      "node-name()",
      ctx);

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
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
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "employee")
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }
}
