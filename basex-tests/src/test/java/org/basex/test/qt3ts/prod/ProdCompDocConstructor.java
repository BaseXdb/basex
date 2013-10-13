package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the CompDocConstructor (Document node constructor) production.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdCompDocConstructor extends QT3TestSet {

  /**
   *  merge adjacent atomic values to text node .
   */
  @org.junit.Test
  public void constrDocnodeAdjtext1() {
    final XQuery query = new XQuery(
      "count((document {1, 'string', 1,2e3})/text())",
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
   *  merge adjacent atomic values and text nodes to text node .
   */
  @org.junit.Test
  public void constrDocnodeAdjtext2() {
    final XQuery query = new XQuery(
      "count((document {1, //text(), 'string'})/text())",
      ctx);
    try {
      query.context(node(file("prod/CompAttrConstructor/DupNode.xml")));
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
   *  merge adjacent atomic values between other nodes to text node .
   */
  @org.junit.Test
  public void constrDocnodeAdjtext3() {
    final XQuery query = new XQuery(
      "count((document {1, 2, <a/>, 3, 4, <b/>, 5, 6})/text())",
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
   *  attribute in document constructor .
   */
  @org.junit.Test
  public void constrDocnodeAttr1() {
    final XQuery query = new XQuery(
      "document {//@mark}",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  attribute in document constructor .
   */
  @org.junit.Test
  public void constrDocnodeAttr2() {
    final XQuery query = new XQuery(
      "document {<a/>, //@mark}",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  attribute in document constructor .
   */
  @org.junit.Test
  public void constrDocnodeAttr3() {
    final XQuery query = new XQuery(
      "document {<a/>, //@mark, <b/>}",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  typed value of element .
   */
  @org.junit.Test
  public void constrDocnodeData1() {
    final XQuery query = new XQuery(
      "fn:data(document {'a', element a {}, 'b'})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ab")
    );
  }

  /**
   *  strip document nodes .
   */
  @org.junit.Test
  public void constrDocnodeDoc1() {
    final XQuery query = new XQuery(
      "document {., .}",
      ctx);
    try {
      query.context(node(file("prod/CompAttrConstructor/DupNode.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<root><child1><a>text</a><!--comment--><?pi content?></child1><child2><a>text</a><!--comment--><?pi content?></child2></root><root><child1><a>text</a><!--comment--><?pi content?></child1><child2><a>text</a><!--comment--><?pi content?></child2></root>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  empty computed element content .
   */
  @org.junit.Test
  public void constrDocnodeEnclexpr1() {
    final XQuery query = new XQuery(
      "document {()}",
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
   *  atomic values in computed element content .
   */
  @org.junit.Test
  public void constrDocnodeEnclexpr2() {
    final XQuery query = new XQuery(
      "document {1,'a',3.5,4e2}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 a 3.5 400")
    );
  }

  /**
   *  atomic values and nodes in computed element content .
   */
  @org.junit.Test
  public void constrDocnodeEnclexpr3() {
    final XQuery query = new XQuery(
      "document {1,//a,2,3,//comment(),4,5,//processing-instruction(),6,7,//text(),8}",
      ctx);
    try {
      query.context(node(file("prod/CompAttrConstructor/DupNode.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("1<a>text</a><a>text</a>2 3<!--comment--><!--comment-->4 5<?pi content?><?pi content?>6 7texttext8", false)
    );
  }

  /**
   *  empty string in element content .
   */
  @org.junit.Test
  public void constrDocnodeEnclexpr4() {
    final XQuery query = new XQuery(
      "document {1, '', 2}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1  2")
    );
  }

  /**
   *  nested computed element constructors .
   */
  @org.junit.Test
  public void constrDocnodeNested1() {
    final XQuery query = new XQuery(
      "document {1, document {2, document {document {()}, document {3}}, 4}}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1234")
    );
  }

  /**
   *  copy node tree into computed element constructor .
   */
  @org.junit.Test
  public void constrDocnodeNested2() {
    final XQuery query = new XQuery(
      "document {/root}",
      ctx);
    try {
      query.context(node(file("prod/CompAttrConstructor/DupNode.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<root><child1><a>text</a><!--comment--><?pi content?></child1><child2><a>text</a><!--comment--><?pi content?></child2></root>", false)
    );
  }

  /**
   *  Check that text nodes are merged in nested document constructors. .
   */
  @org.junit.Test
  public void constrDocnodeNested3() {
    final XQuery query = new XQuery(
      "count(document {1, document{2}, document { document {()}, document {3}}, 4}/text())",
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
   *  Check that empty text nodes are stripped in nested document constructors. .
   */
  @org.junit.Test
  public void constrDocnodeNested4() {
    final XQuery query = new XQuery(
      "count(document {\"\", document{\"\"}, document { document {()}, document {\"\"}}, \"\"}/text())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Copied element node has new node identity .
   */
  @org.junit.Test
  public void constrDocnodeNodeid1() {
    final XQuery query = new XQuery(
      "for $x in <a/>, $y in document {$x} return exactly-one($y/a) is $x",
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
   *  Copied comment node has new node identity .
   */
  @org.junit.Test
  public void constrDocnodeNodeid3() {
    final XQuery query = new XQuery(
      "for $x in <!--comment-->, $y in document {$x} return exactly-one($y/comment()) is $x",
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
   *  Copied pi node has new node identity .
   */
  @org.junit.Test
  public void constrDocnodeNodeid4() {
    final XQuery query = new XQuery(
      "for $x in <?pi content?>, $y in document {$x} return exactly-one($y/processing-instruction()) is $x",
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
   *  Copied text node has new node identity .
   */
  @org.junit.Test
  public void constrDocnodeNodeid5() {
    final XQuery query = new XQuery(
      "for $x in <a>text</a>, $y in document {$x/text()} return exactly-one($y/text()) is exactly-one($x/text())",
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
   *  empty parent .
   */
  @org.junit.Test
  public void constrDocnodeParent1() {
    final XQuery query = new XQuery(
      "count((document {()})/..)",
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
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  string value of element .
   */
  @org.junit.Test
  public void constrDocnodeString1() {
    final XQuery query = new XQuery(
      "fn:string(document {'a', element a {}, 'b'})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ab")
    );
  }

  /**
   *  No node constructor by name document-node exists. .
   */
  @org.junit.Test
  public void k2ConDocNode1() {
    final XQuery query = new XQuery(
      "document-node{\"content\"}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Document nodes may have several elements as children. .
   */
  @org.junit.Test
  public void k2ConDocNode10() {
    final XQuery query = new XQuery(
      "document{<a/>, <b/>, <c/>}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/><b/><c/>", false)
    );
  }

  /**
   *  Extract the typed value from a document node. .
   */
  @org.junit.Test
  public void k2ConDocNode11() {
    final XQuery query = new XQuery(
      "<a>{data(document{<a/>, <b/>, <c/>})}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/>", false)
    );
  }

  /**
   *  Extract the string value from a document node. .
   */
  @org.junit.Test
  public void k2ConDocNode12() {
    final XQuery query = new XQuery(
      "<a>{string(document{<a/>, <b/>, <c/>})}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/>", false)
    );
  }

  /**
   *  Extract the string value from a document node. .
   */
  @org.junit.Test
  public void k2ConDocNode13() {
    final XQuery query = new XQuery(
      "<a>{string(document{<a/>, <b/>, <c/>})}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/>", false)
    );
  }

  /**
   *  Extract the string value from a document node. .
   */
  @org.junit.Test
  public void k2ConDocNode14() {
    final XQuery query = new XQuery(
      "string(document{\"abc\"})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc")
    );
  }

  /**
   *  Test the atomized type. .
   */
  @org.junit.Test
  public void k2ConDocNode15() {
    final XQuery query = new XQuery(
      "data(document {\"content\"}) instance of xs:untypedAtomic",
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
   *  Test node identity of two document nodes. .
   */
  @org.junit.Test
  public void k2ConDocNode16() {
    final XQuery query = new XQuery(
      "document {\"content\"} is document{\"content\"}",
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
   *  Extract the string value from a complex document node. .
   */
  @org.junit.Test
  public void k2ConDocNode17() {
    final XQuery query = new XQuery(
      "string(document{\"string\", <e>more<a>even more</a><b attr=\"thisIsIgnored\"/><![CDATA[ButNotThis]]><?target butThisIs?> content</e>})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "stringmoreeven moreButNotThis content")
    );
  }

  /**
   *  Ensure processing instructions and comments are ignored when extracting the string value from a document node. .
   */
  @org.junit.Test
  public void k2ConDocNode18() {
    final XQuery query = new XQuery(
      "string(document{ text {\"data\"}, processing-instruction name {\"data\"}, processing-instruction name {\"data\"}, text {\"data\"}, processing-instruction name {\"data\"}, processing-instruction name1 {\"data\"}, comment {\"content\"}, comment {\"content\"}, text {\"data\"}, processing-instruction name2 {\"data\"}, comment {\"content\"}, text {\"data\"} })",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "datadatadatadata")
    );
  }

  /**
   *  A recursive construction of document nodes. .
   */
  @org.junit.Test
  public void k2ConDocNode19() {
    final XQuery query = new XQuery(
      "count(document{document{document{document{()}}}}/child::node())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  A semi-colon can't follow document{}. .
   */
  @org.junit.Test
  public void k2ConDocNode2() {
    final XQuery query = new XQuery(
      "document{\"content\"};",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  A recursive construction of document nodes, combined with the comma operator. .
   */
  @org.junit.Test
  public void k2ConDocNode20() {
    final XQuery query = new XQuery(
      "1, document{document{document{document{()}}}}/child::node(), 1",
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
   *  A recursive construction of document nodes, with one child element. .
   */
  @org.junit.Test
  public void k2ConDocNode21() {
    final XQuery query = new XQuery(
      "document{document{document{document{<e/>}}}}/child::node()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e/>", false)
    );
  }

  /**
   *  A recursive construction of document nodes, combined with elements. .
   */
  @org.junit.Test
  public void k2ConDocNode22() {
    final XQuery query = new XQuery(
      "document{document{document{document{<e/>, document{()}, <e>{document{()}}</e>}}}}//child::node()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e/><e/>", false)
    );
  }

  /**
   *  Extract the string value from a document node with four computed text nodes. .
   */
  @org.junit.Test
  public void k2ConDocNode23() {
    final XQuery query = new XQuery(
      "string(document{ text {\"data\"}, text {\"data\"}, text {\"data\"}, text {\"data\"} })",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "datadatadatadata")
    );
  }

  /**
   *  Ensure text nodes gets merged. .
   */
  @org.junit.Test
  public void k2ConDocNode24() {
    final XQuery query = new XQuery(
      "count(document{ text {\"data\"}, text {\"data\"}, <e/>, text {\"data\"}, text {\"data\"} }/child::node())",
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
   *  Ensure text nodes gets merged(#2). .
   */
  @org.junit.Test
  public void k2ConDocNode25() {
    final XQuery query = new XQuery(
      "count(document{ text {\"data\"}, text {\"data\"}, text {\"data\"}, text {\"data\"} }/child::node())",
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
   *  Attributes cannot be children of document nodes. .
   */
  @org.junit.Test
  public void k2ConDocNode26() {
    final XQuery query = new XQuery(
      "<doo> { document { attribute name {\"content\"} } } </doo>",
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
   *  Attributes cannot be children of document nodes(#2). .
   */
  @org.junit.Test
  public void k2ConDocNode27() {
    final XQuery query = new XQuery(
      "<doo> { document { <e/>, attribute name {\"content\"} } } </doo>",
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
   *  Attributes cannot be children of document nodes(#3). .
   */
  @org.junit.Test
  public void k2ConDocNode28() {
    final XQuery query = new XQuery(
      "<doo> { document { <e> <b/> <b/> <b/> <c> <d/> </c> </e>, attribute name {\"content\"} } } </doo>",
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
   *  An invalid attribute constructor as child to the document constructor. .
   */
  @org.junit.Test
  public void k2ConDocNode29() {
    final XQuery query = new XQuery(
      "<doo> { document { <e> { <?target data?>, attribute name {\"content\"} } </e> } } </doo>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQTY0024")
    );
  }

  /**
   *  A document constructor can't receive attribute nodes. .
   */
  @org.junit.Test
  public void k2ConDocNode3() {
    final XQuery query = new XQuery(
      "document{\"some text\", <e/>, attribute name {\"content\"}}",
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
   *  Ensure two text nodes appearing after a document constructor are merged properly. .
   */
  @org.junit.Test
  public void k2ConDocNode30() {
    final XQuery query = new XQuery(
      "count(<a>{document {text{'a'}}}b</a>/node())",
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
   *  Ensure a text node is properly constructed with nested document constructors, when extracting the string-value. .
   */
  @org.junit.Test
  public void k2ConDocNode31() {
    final XQuery query = new XQuery(
      "string(document {1, document {2, document {document {()}, 3, document {4}}, 5}, 6})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "123456")
    );
  }

  /**
   *  Ensure a text node is properly constructed with nested document constructors, when serializing. .
   */
  @org.junit.Test
  public void k2ConDocNode32() {
    final XQuery query = new XQuery(
      "document {1, document {2, document {document {()}, 3, document {4}}, 5}, 6}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "123456")
    );
  }

  /**
   *  A document constructor can't receive attribute nodes(#2). .
   */
  @org.junit.Test
  public void k2ConDocNode4() {
    final XQuery query = new XQuery(
      "document{<e/>, attribute name {\"content\"}, \"some text\"}",
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
   *  A document constructor can't receive attribute nodes(#3). .
   */
  @org.junit.Test
  public void k2ConDocNode5() {
    final XQuery query = new XQuery(
      "document{attribute name {\"content\"}, <e/>, \"some text\"}",
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
   *  A document constructor can't receive attribute nodes(#4). .
   */
  @org.junit.Test
  public void k2ConDocNode6() {
    final XQuery query = new XQuery(
      "string(document{\"some text\", <e/>, attribute name {\"content\"}})",
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
   *  A document constructor can't receive attribute nodes(#5). .
   */
  @org.junit.Test
  public void k2ConDocNode7() {
    final XQuery query = new XQuery(
      "string(document{<e/>, attribute name {\"content\"}, \"some text\"})",
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
   *  A document constructor can't receive attribute nodes(#7). .
   */
  @org.junit.Test
  public void k2ConDocNode8() {
    final XQuery query = new XQuery(
      "string(document{attribute name {\"content\"}, <e/>, \"some text\"})",
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
   *  Document nodes may have several elements as children(#8). .
   */
  @org.junit.Test
  public void k2ConDocNode9() {
    final XQuery query = new XQuery(
      "document{<a/>, <b/>, <c/>}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/><b/><c/>", false)
    );
  }
}
