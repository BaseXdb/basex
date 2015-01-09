package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the CompElemConstructor (Computed Element Constructor) production.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdCompElemConstructor extends QT3TestSet {

  /**
   *  merge adjacent atomic values to text node .
   */
  @org.junit.Test
  public void constrCompelemAdjtext1() {
    final XQuery query = new XQuery(
      "count((element elem {1, 'string', 1,2e3})/text())",
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
  public void constrCompelemAdjtext2() {
    final XQuery query = new XQuery(
      "count((element elem {1, //text(), 'string'})/text())",
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
  public void constrCompelemAdjtext3() {
    final XQuery query = new XQuery(
      "count((element elem {1, 2, <a/>, 3, 4, <b/>, 5, 6})/text())",
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
   *  attribute node following atomic value .
   */
  @org.junit.Test
  public void constrCompelemAttr1() {
    final XQuery query = new XQuery(
      "element elem {1, //west/@mark}",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
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
   *  attribute node following node .
   */
  @org.junit.Test
  public void constrCompelemAttr2() {
    final XQuery query = new XQuery(
      "element elem {element a {}, //west/@mark}",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
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
   *  attributes in content .
   */
  @org.junit.Test
  public void constrCompelemAttr3() {
    final XQuery query = new XQuery(
      "element elem {//west/@mark, //west/@west-attr-1}",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem mark=\"w0\" west-attr-1=\"w1\"/>", false)
    );
  }

  /**
   *  attribute in content with same name .
   */
  @org.junit.Test
  public void constrCompelemAttr4() {
    final XQuery query = new XQuery(
      "element elem {//west/@mark, //center/@mark}",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0025")
    );
  }

  /**
   *  base-uri through xml:base attribute .
   */
  @org.junit.Test
  public void constrCompelemBaseuri1() {
    final XQuery query = new XQuery(
      "fn:base-uri(element elem {attribute xml:base {\"http://www.example.com\"}})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com")
    );
  }

  /**
   *  base-uri through parent .
   */
  @org.junit.Test
  public void constrCompelemBaseuri2() {
    final XQuery query = new XQuery(
      "fn:base-uri(exactly-one((<elem xml:base=\"http://www.example.com\">{element a {}}</elem>)/a))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com")
    );
  }

  /**
   *  base-uri through declaration .
   */
  @org.junit.Test
  public void constrCompelemBaseuri3() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; fn:base-uri(element elem {})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com")
    );
  }

  /**
   *  empty computed name .
   */
  @org.junit.Test
  public void constrCompelemCompname1() {
    final XQuery query = new XQuery(
      "element {()} {'text'}",
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
   *  string as name .
   */
  @org.junit.Test
  public void constrCompelemCompname10() {
    final XQuery query = new XQuery(
      "element {'elem'} {'text'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>text</elem>", false)
    );
  }

  /**
   *  string as name .
   */
  @org.junit.Test
  public void constrCompelemCompname11() {
    final XQuery query = new XQuery(
      "element {'elem', ()} {'text'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>text</elem>", false)
    );
  }

  /**
   *  string as name .
   */
  @org.junit.Test
  public void constrCompelemCompname12() {
    final XQuery query = new XQuery(
      "element {(), 'elem'} {'text'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>text</elem>", false)
    );
  }

  /**
   *  string with prefix as name .
   */
  @org.junit.Test
  public void constrCompelemCompname13() {
    final XQuery query = new XQuery(
      "<elem xmlns:foo=\"http://www.example.com/foo\">{element {'foo:elem'} {'text'}}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xmlns:foo=\"http://www.example.com/foo\"><foo:elem>text</foo:elem></elem>", false)
    );
  }

  /**
   *  string with undeclared prefix as name .
   */
  @org.junit.Test
  public void constrCompelemCompname14() {
    final XQuery query = new XQuery(
      "element {'foo:elem'} {}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0074")
    );
  }

  /**
   *  untyped atomic as name .
   */
  @org.junit.Test
  public void constrCompelemCompname15() {
    final XQuery query = new XQuery(
      "element {xs:untypedAtomic('elem')} {'text'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>text</elem>", false)
    );
  }

  /**
   *  untyped atomic with prefix as name .
   */
  @org.junit.Test
  public void constrCompelemCompname16() {
    final XQuery query = new XQuery(
      "<elem xmlns:foo=\"http://www.example.com/foo\">{element {xs:untypedAtomic('foo:elem')} {'text'}}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xmlns:foo=\"http://www.example.com/foo\"><foo:elem>text</foo:elem></elem>", false)
    );
  }

  /**
   *  untyped atomic with undeclared prefix as name .
   */
  @org.junit.Test
  public void constrCompelemCompname17() {
    final XQuery query = new XQuery(
      "element {xs:untypedAtomic('foo:elem')} {'text'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0074")
    );
  }

  /**
   *  non-ncname string as name .
   */
  @org.junit.Test
  public void constrCompelemCompname18() {
    final XQuery query = new XQuery(
      "element {'el em'} {'text'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0074")
    );
  }

  /**
   *  non-ncname untyped atomic as name .
   */
  @org.junit.Test
  public void constrCompelemCompname19() {
    final XQuery query = new XQuery(
      "element {xs:untypedAtomic('el em')} {'text'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0074")
    );
  }

  /**
   *  two strings as name .
   */
  @org.junit.Test
  public void constrCompelemCompname2() {
    final XQuery query = new XQuery(
      "element {'one', 'two'} {'text'}",
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
   *  two untypedAtomic values as name .
   */
  @org.junit.Test
  public void constrCompelemCompname3() {
    final XQuery query = new XQuery(
      "element {xs:untypedAtomic('one'), xs:untypedAtomic('two')} {'text'}",
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
   *  content of two nodes as name .
   */
  @org.junit.Test
  public void constrCompelemCompname4() {
    final XQuery query = new XQuery(
      "element {//a} {'text'}",
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
      error("XPTY0004")
    );
  }

  /**
   *  two numeric values as name .
   */
  @org.junit.Test
  public void constrCompelemCompname5() {
    final XQuery query = new XQuery(
      "element {1,2} {'text'}",
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
   *  numeric value as name .
   */
  @org.junit.Test
  public void constrCompelemCompname6() {
    final XQuery query = new XQuery(
      "element {123} {'text'}",
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
   *  dataTime value as name .
   */
  @org.junit.Test
  public void constrCompelemCompname7() {
    final XQuery query = new XQuery(
      "element {xs:dateTime(\"1999-05-31T13:20:00\")} {'text'}",
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
   *  QName as name .
   */
  @org.junit.Test
  public void constrCompelemCompname9() {
    final XQuery query = new XQuery(
      "element {xs:QName('aQname')} {'text'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<aQname>text</aQname>", false)
    );
  }

  /**
   *  strip decimal type .
   */
  @org.junit.Test
  public void constrCompelemConstrmod3() {
    final XQuery query = new XQuery(
      "declare construction strip; (element elem {xs:decimal((//decimal[1]))}) cast as xs:integer",
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
      error("FORG0001")
    );
  }

  /**
   *  preserve decimal type .
   */
  @org.junit.Test
  public void constrCompelemConstrmod4() {
    final XQuery query = new XQuery(
      "declare construction preserve; (element elem {xs:decimal((//decimal[1]))}) cast as xs:integer",
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
      (
        assertEq("12678967")
      ||
        error("FORG0001")
      )
    );
  }

  /**
   *  strip decimal type in attribute .
   */
  @org.junit.Test
  public void constrCompelemConstrmod7() {
    final XQuery query = new XQuery(
      "declare construction strip; (element elem {//*:decimal/@*:attr})/@*:attr cast as xs:integer",
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
      error("FORG0001")
    );
  }

  /**
   *  preserve decimal type in attribute .
   */
  @org.junit.Test
  public void constrCompelemConstrmod8() {
    final XQuery query = new XQuery(
      "declare construction preserve; (element elem {xs:decimal(//*:decimal[1]/@*:attr)}) cast as xs:integer",
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
      (
        assertEq("12678967")
      ||
        error("FORG0001")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  typed value of element .
   */
  @org.junit.Test
  public void constrCompelemData1() {
    final XQuery query = new XQuery(
      "fn:data(element elem {'a', element a {}, 'b'})",
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
  public void constrCompelemDoc1() {
    final XQuery query = new XQuery(
      "element elem {., .}",
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
      assertSerialization("<elem><root><child1><a>text</a><!--comment--><?pi content?></child1><child2><a>text</a><!--comment--><?pi content?></child2></root><root><child1><a>text</a><!--comment--><?pi content?></child1><child2><a>text</a><!--comment--><?pi content?></child2></root></elem>", false)
    );
  }

  /**
   *  empty computed element content .
   */
  @org.junit.Test
  public void constrCompelemEnclexpr1() {
    final XQuery query = new XQuery(
      "element elem {}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem/>", false)
    );
  }

  /**
   *  atomic values in computed element content .
   */
  @org.junit.Test
  public void constrCompelemEnclexpr2() {
    final XQuery query = new XQuery(
      "element elem {1,'a',3.5,4e2}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>1 a 3.5 400</elem>", false)
    );
  }

  /**
   *  atomic values and nodes in computed element content .
   */
  @org.junit.Test
  public void constrCompelemEnclexpr3() {
    final XQuery query = new XQuery(
      "element elem {1,//a,2,3,//comment(),4,5,//processing-instruction(),6,7,//text(),8}",
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
      assertSerialization("<elem>1<a>text</a><a>text</a>2 3<!--comment--><!--comment-->4 5<?pi content?><?pi content?>6 7texttext8</elem>", false)
    );
  }

  /**
   *  empty string in element content .
   */
  @org.junit.Test
  public void constrCompelemEnclexpr4() {
    final XQuery query = new XQuery(
      "element elem {1, '', 2}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>1  2</elem>", false)
    );
  }

  /**
   *  NCName for computed element constructor .
   */
  @org.junit.Test
  public void constrCompelemName1() {
    final XQuery query = new XQuery(
      "element elem {'text'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>text</elem>", false)
    );
  }

  /**
   *  QName for computed element constructor .
   */
  @org.junit.Test
  public void constrCompelemName2() {
    final XQuery query = new XQuery(
      "declare namespace foo=\"http://www.example.com/foo\"; element foo:elem {'text'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<foo:elem xmlns:foo=\"http://www.example.com/foo\">text</foo:elem>", false)
    );
  }

  /**
   *  QName with undeclared prefix for computed element constructor .
   */
  @org.junit.Test
  public void constrCompelemName3() {
    final XQuery query = new XQuery(
      "element foo:elem {'text'}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0081")
    );
  }

  /**
   *  nested computed element constructors .
   */
  @org.junit.Test
  public void constrCompelemNested1() {
    final XQuery query = new XQuery(
      "element elem {1, element a {2, element b {element c {}, element d {3}}, 4}}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>1<a>2<b><c/><d>3</d></b>4</a></elem>", false)
    );
  }

  /**
   *  copy node tree into computed element constructor .
   */
  @org.junit.Test
  public void constrCompelemNested2() {
    final XQuery query = new XQuery(
      "element elem {}",
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
      assertSerialization("<elem/>", false)
    );
  }

  /**
   *  Copied element node has new node identity .
   */
  @org.junit.Test
  public void constrCompelemNodeid1() {
    final XQuery query = new XQuery(
      "for $x in <a/>, $y in element elem {$x} return exactly-one($y/a) is $x",
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
   *  Copied attribute node has new node identity .
   */
  @org.junit.Test
  public void constrCompelemNodeid2() {
    final XQuery query = new XQuery(
      "for $x in <a b=\"b\"/>, $y in element elem {$x/@b} return $y/@b is $x/@b",
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
  public void constrCompelemNodeid3() {
    final XQuery query = new XQuery(
      "for $x in <!--comment-->, $y in element elem {$x} return exactly-one($y/comment()) is $x",
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
  public void constrCompelemNodeid4() {
    final XQuery query = new XQuery(
      "for $x in <?pi content?>, $y in element elem {$x} return exactly-one($y/processing-instruction()) is $x",
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
  public void constrCompelemNodeid5() {
    final XQuery query = new XQuery(
      "for $x in <a>text</a>, $y in element elem {$x/text()} return exactly-one($y/text()) is exactly-one($x/text())",
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
  public void constrCompelemParent1() {
    final XQuery query = new XQuery(
      "count((element elem {})/..)",
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
   *  string value of element .
   */
  @org.junit.Test
  public void constrCompelemString1() {
    final XQuery query = new XQuery(
      "fn:string(element elem {'a', element a {}, 'b'})",
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
   *  The name can't be specified as a string literal. .
   */
  @org.junit.Test
  public void k2ComputeConElem1() {
    final XQuery query = new XQuery(
      "element \"name\" {\"content\"}",
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
   *  Check that a directly constructed element gets the right type(#3). .
   */
  @org.junit.Test
  public void k2ComputeConElem10() {
    final XQuery query = new XQuery(
      "declare construction strip; element e {\"content\"} instance of element(*, xs:untyped)",
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
   *  Check that a directly constructed element gets the right type, and that the name test fails. .
   */
  @org.junit.Test
  public void k2ComputeConElem11() {
    final XQuery query = new XQuery(
      "element e {\"content\"} instance of element(a, xs:anyType)",
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
   *  Check that a directly constructed element gets the right type, and that the name test fails(#2). .
   */
  @org.junit.Test
  public void k2ComputeConElem12() {
    final XQuery query = new XQuery(
      "declare construction strip; element e {\"content\"} instance of element(b, xs:untyped)",
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
   *  Two simple nested constructors. .
   */
  @org.junit.Test
  public void k2ComputeConElem13() {
    final XQuery query = new XQuery(
      "element e {element b{()}}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e><b/></e>", false)
    );
  }

  /**
   *  Use a technique that sometimes is used for adding namespace nodes. .
   */
  @org.junit.Test
  public void k2ComputeConElem14() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:addNamespace($argElement as element(), $argPrefix as xs:string, $namespaceURI as xs:string) as element() { \n" +
      "            element { QName($namespaceURI, concat($argPrefix, \":x\")) }{$argElement}/* \n" +
      "        }; \n" +
      "        local:addNamespace(<a><b/></a>, \"prefix\", \"http://example.com/\")\n" +
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
      assertSerialization("<a xmlns:prefix=\"http://example.com/\"><b/></a>", false)
    );
  }

  /**
   *  Use the default element, where the name is computed dynamically. .
   */
  @org.junit.Test
  public void k2ComputeConElem15() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default element namespace \"http://example.com/NS\"; \n" +
      "        element {exactly-one((//*)[3])} {}",
      ctx);
    try {
      query.context(node(file("op/union/acme_corp.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<Boston xmlns=\"http://example.com/NS\"/>", false)
    );
  }

  /**
   *  Simple content that only is empty string in an element constructor. .
   */
  @org.junit.Test
  public void k2ComputeConElem2() {
    final XQuery query = new XQuery(
      "<elem>{\"\", \"\", <e/>, <b></b>}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem> <e/><b/></elem>", false)
    );
  }

  /**
   *  Simple content that only is empty string in an element constructor(#2). .
   */
  @org.junit.Test
  public void k2ComputeConElem3() {
    final XQuery query = new XQuery(
      "<elem>{<e/>, <b></b>, \"\", \"\"}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem><e/><b/> </elem>", false)
    );
  }

  /**
   *  An unbound prefix in a lexical QName yields QDY0074. .
   */
  @org.junit.Test
  public void k2ComputeConElem4() {
    final XQuery query = new XQuery(
      "element {\"aPrefix:localName\"} {\"content\"}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0074")
    );
  }

  /**
   *  An unbound prefix in a lexical QName yields QDY0074. .
   */
  @org.junit.Test
  public void k2ComputeConElem5() {
    final XQuery query = new XQuery(
      "element {xs:untypedAtomic(\"aPrefix::localName\")} {\"content\"}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0074")
    );
  }

  /**
   *  Test the type of the typed value of a computationally constructed element node. .
   */
  @org.junit.Test
  public void k2ComputeConElem6() {
    final XQuery query = new XQuery(
      "data(element foo {\"dsa\"}) instance of xs:untypedAtomic",
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
   *  Test the data of the typed value of a computationally constructed element node. .
   */
  @org.junit.Test
  public void k2ComputeConElem7() {
    final XQuery query = new XQuery(
      "data(element foo {\"dsa\"})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "dsa")
    );
  }

  /**
   *  Check that a directly constructed element gets the right type. .
   */
  @org.junit.Test
  public void k2ComputeConElem8() {
    final XQuery query = new XQuery(
      "element e {\"content\"} instance of element(*, xs:anyType)",
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
   *  Check that a directly constructed element gets the right type(#2). .
   */
  @org.junit.Test
  public void k2ComputeConElem9() {
    final XQuery query = new XQuery(
      "element e {\"content\"} instance of element(*, xs:untyped)",
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
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   * 3.7.3.1 Computed Element Constructor per XQ.E19 XQDY0096 if namespace prefix is 'xmlns' Mary Holstege .
   */
  @org.junit.Test
  public void compElemBadName1() {
    final XQuery query = new XQuery(
      "element {\"xmlns:error\"} {}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0096")
    );
  }

  /**
   * 3.7.3.1 Computed Element Constructor per XQ.E19 XQDY0096 if namespace URI is 'http://www.w3.org/2000/xmlns/' Mary Holstege .
   */
  @org.junit.Test
  public void compElemBadName2() {
    final XQuery query = new XQuery(
      "(: 3.7.3.1 Computed Element Constructor per XQ.E19 XQDY0096 if namespace URI is 'http://www.w3.org/2000/xmlns/' Mary Holstege :) element { fn:QName(\"http://www.w3.org/2000/xmlns/\",\"error\")} {}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0096")
    );
  }

  /**
   * 3.7.3.1 Computed Element Constructor per XQ.E19 XQDY0096 if namespace URI is 'http://www.w3.org/2000/xmlns/' Mary Holstege .
   */
  @org.junit.Test
  public void compElemBadName3() {
    final XQuery query = new XQuery(
      "(: 3.7.3.1 Computed Element Constructor per XQ.E19 XQDY0096 if namespace URI is 'http://www.w3.org/2000/xmlns/' Mary Holstege :) element { fn:QName(\"http://www.w3.org/2000/xmlns/\",\"foo:error\")} {}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0096")
    );
  }

  /**
   * 3.7.3.1 Computed Element Constructor per XQ.E19 XQDY0096 if namespace prefix is 'xml' and namespace URI is not 'http://www.w3.org/XML/1998/namespace' Mary Holstege .
   */
  @org.junit.Test
  public void compElemBadName4() {
    final XQuery query = new XQuery(
      "(: 3.7.3.1 Computed Element Constructor per XQ.E19 XQDY0096 if namespace prefix is 'xml' and namespace URI is not 'http://www.w3.org/XML/1998/namespace' Mary Holstege :) element { fn:QName(\"http://example.com/not-XML-uri\",\"xml:error\") } {}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0096")
    );
  }

  /**
   * 3.7.3.1 Computed Element Constructor per XQ.E19 XQDY0096 if namespace prefix is not 'xml' and its namespace URI is 'http://www.w3.org/XML/1998/namespace' Mary Holstege .
   */
  @org.junit.Test
  public void compElemBadName5() {
    final XQuery query = new XQuery(
      "(: 3.7.3.1 Computed Element Constructor per XQ.E19 XQDY0096 if namespace prefix is not 'xml' and its namespace URI is 'http://www.w3.org/XML/1998/namespace' Mary Holstege :) element { fn:QName(\"http://www.w3.org/XML/1998/namespace\",\"foo:error\") } {}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0096")
    );
  }

  /**
   * 3.7.3.1 Computed Element Constructor per XQ.E19 XQDY0096 if namespace prefix is 'xmlns' Mary Holstege .
   */
  @org.junit.Test
  public void compElemBadName6() {
    final XQuery query = new XQuery(
      "(: 3.7.3.1 Computed Element Constructor per XQ.E19 XQDY0096 if namespace prefix is 'xmlns' Mary Holstege :) element { fn:QName(\"http://example.com/some-uri\",\"xmlns:error\") } {}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0096")
    );
  }
}
