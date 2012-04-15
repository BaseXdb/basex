package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the NodeTest production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdNodeTest extends QT3TestSet {

  /**
   *  Check the child count of an empty direct element constructor. .
   */
  @org.junit.Test
  public void k2NodeTest1() {
    final XQuery query = new XQuery(
      "count(<a></a>/node())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  There is no type by name document(). .
   */
  @org.junit.Test
  public void k2NodeTest10() {
    final XQuery query = new XQuery(
      "document(*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  There is no type by name document(). .
   */
  @org.junit.Test
  public void k2NodeTest11() {
    final XQuery query = new XQuery(
      "document()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  There is no type by name document(). .
   */
  @org.junit.Test
  public void k2NodeTest12() {
    final XQuery query = new XQuery(
      "1 instance of document(*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  There is no type by name document(). .
   */
  @org.junit.Test
  public void k2NodeTest13() {
    final XQuery query = new XQuery(
      "1 instance of document()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  'name' is not allowed inside document-node(). .
   */
  @org.junit.Test
  public void k2NodeTest14() {
    final XQuery query = new XQuery(
      "document-node(name)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  'name' is not allowed inside document-node(). .
   */
  @org.junit.Test
  public void k2NodeTest15() {
    final XQuery query = new XQuery(
      "document-node(local:name)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  processing-instruction() isn't allowed inside document-node(). .
   */
  @org.junit.Test
  public void k2NodeTest16() {
    final XQuery query = new XQuery(
      "document-node(processing-instruction())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  unknown() isn't allowed inside document-node(). .
   */
  @org.junit.Test
  public void k2NodeTest17() {
    final XQuery query = new XQuery(
      "document-node(unknown())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  schema-attribute() isn't allowed inside document-node(). .
   */
  @org.junit.Test
  public void k2NodeTest18() {
    final XQuery query = new XQuery(
      "document-node(schema-attribute(ncname))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Use document-node() with an unkown schema-element() type. .
   */
  @org.junit.Test
  public void k2NodeTest19() {
    final XQuery query = new XQuery(
      "document-node(schema-element(thisTypeIsNotRecognizedExample.Com))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   *  Check the child count of an empty direct element constructor. .
   */
  @org.junit.Test
  public void k2NodeTest2() {
    final XQuery query = new XQuery(
      "count(<a/>/node())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Use document-node() with an unkown schema-element() type. .
   */
  @org.junit.Test
  public void k2NodeTest20() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace ex = \"http://www.example.com/\"; \n" +
      "         document-node(schema-element(ex:thisTypeIsNotRecognizedExample.Com))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   *  Ensure 'element(local:ncname)' is parsed correctly when inside document-node(). .
   */
  @org.junit.Test
  public void k2NodeTest21() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare namespace ex = \"http://www.example.com/\"; \n" +
      "         declare function local:userFunction() { document-node(element(local:ncname)) }; 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("1")
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   *  Use an unbound prefix inside document-node()/element(). .
   */
  @org.junit.Test
  public void k2NodeTest22() {
    final XQuery query = new XQuery(
      "document-node(element(notBound:ncname))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0081")
    );
  }

  /**
   *  Use an unbound prefix inside element(). .
   */
  @org.junit.Test
  public void k2NodeTest23() {
    final XQuery query = new XQuery(
      "element(notBound:ncname)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0081")
    );
  }

  /**
   *  Use an unbound prefix inside element(). .
   */
  @org.junit.Test
  public void k2NodeTest24() {
    final XQuery query = new XQuery(
      "attribute(notBound:ncname)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0081")
    );
  }

  /**
   *  Use an unbound prefix inside document-node()/schema-element(). .
   */
  @org.junit.Test
  public void k2NodeTest25() {
    final XQuery query = new XQuery(
      "document-node(schema-element(notBound:ncname))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0081")
    );
  }

  /**
   *  Use an unbound prefix inside schema-element(). .
   */
  @org.junit.Test
  public void k2NodeTest26() {
    final XQuery query = new XQuery(
      "schema-element(notBound:ncname)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0081")
    );
  }

  /**
   *  Use an unbound prefix inside schema-attribute(). .
   */
  @org.junit.Test
  public void k2NodeTest27() {
    final XQuery query = new XQuery(
      "schema-attribute(notBound:ncname)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0081")
    );
  }

  /**
   *  Invoke '/' on a tree whose root is not a document node. .
   */
  @org.junit.Test
  public void k2NodeTest28() {
    final XQuery query = new XQuery(
      "<e/>/(/)//f",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0050")
    );
  }

  /**
   *  Apostrophes are valid separators in processing-instruction(). .
   */
  @org.junit.Test
  public void k2NodeTest29() {
    final XQuery query = new XQuery(
      "processing-instruction('ncname')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0002")
    );
  }

  /**
   *  '*' is not allowed inside processing-instruction(). .
   */
  @org.junit.Test
  public void k2NodeTest3() {
    final XQuery query = new XQuery(
      "processing-instruction(*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Ensure element() isn't parsed as a function. .
   */
  @org.junit.Test
  public void k2NodeTest30() {
    final XQuery query = new XQuery(
      "element()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Trigger XPTY0018 inside a function body. .
   */
  @org.junit.Test
  public void k2NodeTest31() {
    final XQuery query = new XQuery(
      "declare function local:aFunction() { <e/>/(1, <e/>) }; 1, local:aFunction()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0018")
    );
  }

  /**
   *  We evaluate to only atomic values, although the static type does not reflect that. .
   */
  @org.junit.Test
  public void k2NodeTest32() {
    final XQuery query = new XQuery(
      "<e> <a/> <b/> </e>/(if(position() = 10) then (<e/>, .) else 4)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("4")
    );
  }

  /**
   *  Trigger XPTY0018 inside a body for a global variable. .
   */
  @org.junit.Test
  public void k2NodeTest33() {
    final XQuery query = new XQuery(
      "declare variable $myVariable := <e/>/(1, <e/>); $myVariable",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0018")
    );
  }

  /**
   *  Trigger XPTY0018 inside a body for a global variable(#2). .
   */
  @org.junit.Test
  public void k2NodeTest34() {
    final XQuery query = new XQuery(
      "declare variable $myVariable := <e/>/(<e/>, 2); $myVariable",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0018")
    );
  }

  /**
   *  Trigger XPTY0018 inside a function body(#2). .
   */
  @org.junit.Test
  public void k2NodeTest35() {
    final XQuery query = new XQuery(
      "declare function local:aFunction() { <e/>/(<e/>, 2) }; 1, local:aFunction()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0018")
    );
  }

  /**
   *  Trigger XPTY0018 inside a function body(#3). .
   */
  @org.junit.Test
  public void k2NodeTest36() {
    final XQuery query = new XQuery(
      "declare function local:aFunction() { (1, 2, 3, (4, <e/>/(<e/>, 2))) }; 1, local:aFunction()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0018")
    );
  }

  /**
   *  Trigger XPTY0018 inside a function body(#4). .
   */
  @org.junit.Test
  public void k2NodeTest37() {
    final XQuery query = new XQuery(
      "declare function local:aFunction() { (<e/>/., <e/>/((<e/>, 2), 1, 2)) }; 1, local:aFunction()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0018")
    );
  }

  /**
   *  Trigger XPTY0018 inside a function body(#5). .
   */
  @org.junit.Test
  public void k2NodeTest38() {
    final XQuery query = new XQuery(
      "declare function local:aFunction() { (<e/>/(., 4, 5, <e/>/((<e/>, 2)))) }; 1, local:aFunction()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0018")
    );
  }

  /**
   *  Trigger XPTY0018 inside a let clause. .
   */
  @org.junit.Test
  public void k2NodeTest39() {
    final XQuery query = new XQuery(
      "let $i := <e/>/(., 4, 5, <e/>/((<e/>, 2))) return ($i, $i)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0018")
    );
  }

  /**
   *  '*' is not allowed inside text(). .
   */
  @org.junit.Test
  public void k2NodeTest4() {
    final XQuery query = new XQuery(
      "text(*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Trigger XPTY0018 inside a for clause. .
   */
  @org.junit.Test
  public void k2NodeTest40() {
    final XQuery query = new XQuery(
      "for $i in <e/>/(., 4, 5, <e/>/((<e/>, 2))) return ($i, $i)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0018")
    );
  }

  /**
   *  Use fn:last() with nested predicates. .
   */
  @org.junit.Test
  public void k2NodeTest41() {
    final XQuery query = new XQuery(
      "<a><b name=\"C\"/><b name= \"D\"/></a>//b[@name=\"D\"][last() = 1]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<b name=\"D\"/>", false)
    );
  }

  /**
   *  Use fn:last() with nested predicates(#2). .
   */
  @org.junit.Test
  public void k2NodeTest42() {
    final XQuery query = new XQuery(
      "(4, 5)[position() = 2][last() = 1]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("5")
    );
  }

  /**
   *  Use fn:last() with nested predicates(#3). .
   */
  @org.junit.Test
  public void k2NodeTest43() {
    final XQuery query = new XQuery(
      "(4, 5)[position() = 2][last() = 1][last() = 1][last()]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("5")
    );
  }

  /**
   *  '*' is not allowed inside comment(). .
   */
  @org.junit.Test
  public void k2NodeTest5() {
    final XQuery query = new XQuery(
      "comment(*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  '*' is not allowed inside node(). .
   */
  @org.junit.Test
  public void k2NodeTest6() {
    final XQuery query = new XQuery(
      "node(*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  '*' is not allowed inside document-node(). .
   */
  @org.junit.Test
  public void k2NodeTest7() {
    final XQuery query = new XQuery(
      "document-node(*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  '*' is not allowed inside schema-attribute(). .
   */
  @org.junit.Test
  public void k2NodeTest8() {
    final XQuery query = new XQuery(
      "schema-attribute(*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  '*' is not allowed inside schema-element(). .
   */
  @org.junit.Test
  public void k2NodeTest9() {
    final XQuery query = new XQuery(
      "schema-element(*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Simple test for comment() node type .
   */
  @org.junit.Test
  public void nodeTest001() {
    final XQuery query = new XQuery(
      "/comment()",
      ctx);
    query.context(node(file("op/union/bib2.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<!-- this file is a copy of bib.xml; just adds a few comments and PI nodes for testing --><!-- Comment 1 --><!-- Comment 2 -->", false)
    );
  }

  /**
   *  Simple test for processing-instruction() node test .
   */
  @org.junit.Test
  public void nodeTest002() {
    final XQuery query = new XQuery(
      "/processing-instruction()",
      ctx);
    query.context(node(file("op/union/bib2.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<?PI1 Processing Instruction 1?><?PI2 Processing Instruction 2?>", false)
    );
  }

  /**
   *  Simple test for node type text() .
   */
  @org.junit.Test
  public void nodeTest006() {
    final XQuery query = new XQuery(
      "<result> {/bib/book/editor/affiliation/text()} </result>",
      ctx);
    query.context(node(file("docs/bib.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>CITI</result>", false)
    );
  }

  /**
   * FileName: NodeTest007  processing-instruction('name') NodeTest can apply under root .
   */
  @org.junit.Test
  public void nodeTest0071() {
    final XQuery query = new XQuery(
      "<out>{fn:count(//processing-instruction('a-pi'))}</out>",
      ctx);
    query.context(node(file("prod/AxisStep/TreeEmpty.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<out>0</out>", false)
    );
  }

  /**
   * FileName: NodeTest007  processing-instruction('name') NodeTest can apply under root .
   */
  @org.junit.Test
  public void nodeTest0072() {
    final XQuery query = new XQuery(
      "<out>{fn:count(//processing-instruction('a-pi'))}</out>",
      ctx);
    query.context(node(file("prod/AxisStep/TopMany.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<out>4</out>", false)
    );
  }

  /**
   * FileName: NodeTest008  text() as a NodeTest .
   */
  @org.junit.Test
  public void nodeTest0081() {
    final XQuery query = new XQuery(
      "<out>{fn:count(//center/text())}</out>",
      ctx);
    query.context(node(file("prod/AxisStep/Tree1Child.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<out>0</out>", false)
    );
  }

  /**
   * FileName: NodeTest008  text() as a NodeTest .
   */
  @org.junit.Test
  public void nodeTest0082() {
    final XQuery query = new XQuery(
      "<out>{fn:count(//center/text())}</out>",
      ctx);
    query.context(node(file("prod/AxisStep/TreeCompass.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<out>6</out>", false)
    );
  }

  /**
   * FileName: NodeTest009  comment() as a NodeTest .
   */
  @org.junit.Test
  public void nodeTest0091() {
    final XQuery query = new XQuery(
      "<out>{fn:count(//center/comment())}</out>",
      ctx);
    query.context(node(file("prod/AxisStep/Tree1Child.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<out>0</out>", false)
    );
  }

  /**
   * FileName: NodeTest009  comment() as a NodeTest .
   */
  @org.junit.Test
  public void nodeTest0092() {
    final XQuery query = new XQuery(
      "<out>{fn:count(//center/comment())}</out>",
      ctx);
    query.context(node(file("prod/AxisStep/TreeCompass.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<out>1</out>", false)
    );
  }

  /**
   * FileName: NodeTest010  comment() NodeTest can apply under root .
   */
  @org.junit.Test
  public void nodeTest0101() {
    final XQuery query = new XQuery(
      "<out>{fn:count(//comment())}</out>",
      ctx);
    query.context(node(file("prod/AxisStep/TreeEmpty.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<out>0</out>", false)
    );
  }

  /**
   * FileName: NodeTest010  comment() NodeTest can apply under root .
   */
  @org.junit.Test
  public void nodeTest0102() {
    final XQuery query = new XQuery(
      "<out>{fn:count(//comment())}</out>",
      ctx);
    query.context(node(file("prod/AxisStep/TopMany.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<out>7</out>", false)
    );
  }

  /**
   * FileName: NodeTest011  processing-instruction() as a NodeTest .
   */
  @org.junit.Test
  public void nodeTest0111() {
    final XQuery query = new XQuery(
      "<out>{fn:count(//center/processing-instruction())}</out>",
      ctx);
    query.context(node(file("prod/AxisStep/Tree1Child.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<out>0</out>", false)
    );
  }

  /**
   * FileName: NodeTest011  processing-instruction() as a NodeTest .
   */
  @org.junit.Test
  public void nodeTest0112() {
    final XQuery query = new XQuery(
      "<out>{fn:count(//center/processing-instruction())}</out>",
      ctx);
    query.context(node(file("prod/AxisStep/TreeCompass.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<out>1</out>", false)
    );
  }

  /**
   * FileName: NodeTest012  processing-instruction() NodeTest can apply under root .
   */
  @org.junit.Test
  public void nodeTest0121() {
    final XQuery query = new XQuery(
      "<out>{fn:count(//processing-instruction())}</out>",
      ctx);
    query.context(node(file("prod/AxisStep/TreeEmpty.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<out>0</out>", false)
    );
  }

  /**
   * FileName: NodeTest012  processing-instruction() NodeTest can apply under root .
   */
  @org.junit.Test
  public void nodeTest0122() {
    final XQuery query = new XQuery(
      "<out>{fn:count(//processing-instruction())}</out>",
      ctx);
    query.context(node(file("prod/AxisStep/TopMany.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<out>6</out>", false)
    );
  }

  /**
   * FileName: NodeTest013  processing-instruction('name') matches only the given name .
   */
  @org.junit.Test
  public void nodeTest0131() {
    final XQuery query = new XQuery(
      "<out>{fn:count(//center/processing-instruction('a-pi'))}</out>",
      ctx);
    query.context(node(file("prod/AxisStep/TreeCompass.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<out>1</out>", false)
    );
  }

  /**
   *  Simple test for text type involving a boolean expression (and fn:true) .
   */
  @org.junit.Test
  public void nodeTesthc1() {
    final XQuery query = new XQuery(
      "<result> {//text() and fn:true()} </result>",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>true</result>", false)
    );
  }

  /**
   *  Simple test for text type involving a boolean expression (or fn:true) .
   */
  @org.junit.Test
  public void nodeTesthc2() {
    final XQuery query = new XQuery(
      "<result> {//text() or fn:true()} </result>",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>true</result>", false)
    );
  }

  /**
   *  Simple test for text type involving a boolean expression (and fn:false) .
   */
  @org.junit.Test
  public void nodeTesthc3() {
    final XQuery query = new XQuery(
      "<result> {//text() and fn:false()} </result>",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>false</result>", false)
    );
  }

  /**
   *  Simple test for text type involving a boolean expression (or fn:false) .
   */
  @org.junit.Test
  public void nodeTesthc4() {
    final XQuery query = new XQuery(
      "<result> {//text() or fn:false()} </result>",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>true</result>", false)
    );
  }

  /**
   *  Simple test for Element node types involving a boolean expression (or fn:false) .
   */
  @org.junit.Test
  public void nodeTesthc5() {
    final XQuery query = new XQuery(
      "<result> {//overtime/node() or fn:false()} </result>",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>true</result>", false)
    );
  }

  /**
   *  Simple test for Element node types involving a boolean expression (or fn:true) .
   */
  @org.junit.Test
  public void nodeTesthc6() {
    final XQuery query = new XQuery(
      "<result> {//overtime/node() or fn:true()} </result>",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>true</result>", false)
    );
  }

  /**
   *  Simple test for Element node types involving a boolean expression (and fn:false) .
   */
  @org.junit.Test
  public void nodeTesthc7() {
    final XQuery query = new XQuery(
      "<result> {//overtime/node() and fn:false()} </result>",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>false</result>", false)
    );
  }

  /**
   *  Simple test for Element node types involving a boolean expression (and fn:true) .
   */
  @org.junit.Test
  public void nodeTesthc8() {
    final XQuery query = new XQuery(
      "<result> {//overtime/node() and fn:true()} </result>",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>true</result>", false)
    );
  }
}
