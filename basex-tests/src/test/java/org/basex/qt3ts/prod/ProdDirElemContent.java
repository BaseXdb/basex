package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the DirElemContent production.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdDirElemContent extends QT3TestSet {

  /**
   *  single text node in element content .
   */
  @org.junit.Test
  public void constrContAdjtext1() {
    final XQuery query = new XQuery(
      "count((<elem>a{1,2,3}b</elem>)/text())",
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
   *  two text nodes in element content .
   */
  @org.junit.Test
  public void constrContAdjtext2() {
    final XQuery query = new XQuery(
      "count((<elem>a{1,<a/>,3}b</elem>)/text())",
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
   *  strip empty text node .
   */
  @org.junit.Test
  public void constrContAdjtext3() {
    final XQuery query = new XQuery(
      "count((<elem>{''}</elem>)/text())",
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
   *  no text node for empty sequence constructed .
   */
  @org.junit.Test
  public void constrContAdjtext4() {
    final XQuery query = new XQuery(
      "count((<elem>{()}</elem>)/text())",
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
   *  attribute node following atomic value .
   */
  @org.junit.Test
  public void constrContAttr1() {
    final XQuery query = new XQuery(
      "<elem>{1, //west/@mark}</elem>",
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
  public void constrContAttr2() {
    final XQuery query = new XQuery(
      "<elem><a/>{//west/@mark}</elem>",
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
   *  attribute node following empty sequence .
   */
  @org.junit.Test
  public void constrContAttr3() {
    final XQuery query = new XQuery(
      "<elem>{()}{//west/@mark}</elem>",
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
      assertSerialization("<elem mark=\"w0\"/>", false)
    );
  }

  /**
   *  attribute node following direct content .
   */
  @org.junit.Test
  public void constrContAttr4() {
    final XQuery query = new XQuery(
      "<elem>{//west/@mark}x{//west/@west-attr-1}</elem>",
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
  public void constrContAttr5() {
    final XQuery query = new XQuery(
      "<elem>{//west/@mark, //west/@west-attr-1}</elem>",
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
   *  attributes direct and in content .
   */
  @org.junit.Test
  public void constrContAttr6() {
    final XQuery query = new XQuery(
      "<elem mark=\"w0\">{//west/@west-attr-1, //west/@west-attr-2}</elem>",
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
      assertSerialization("<elem mark=\"w0\" west-attr-1=\"w1\" west-attr-2=\"w2\"/>", false)
    );
  }

  /**
   *  attribute in content with same name .
   */
  @org.junit.Test
  public void constrContAttr7() {
    final XQuery query = new XQuery(
      "<elem>{//west/@mark, //center/@mark}</elem>",
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
   *  attributes direct and in content with same name .
   */
  @org.junit.Test
  public void constrContAttr8() {
    final XQuery query = new XQuery(
      "<elem mark=\"w0\">{//west/@west-attr-1, //west/@mark}</elem>",
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
   *  Attributes in seperate content units .
   */
  @org.junit.Test
  public void constrContAttr9() {
    final XQuery query = new XQuery(
      "<elem>{//west/@west-attr-1}{//west/@west-attr-2}</elem>",
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
      assertSerialization("<elem west-attr-1=\"w1\" west-attr-2=\"w2\"/>", false)
    );
  }

  /**
   *  base-uri through xml:base attribute .
   */
  @org.junit.Test
  public void constrContBaseuri1() {
    final XQuery query = new XQuery(
      "fn:base-uri(<elem xml:base=\"http://www.example.com\"/>)",
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
  public void constrContBaseuri2() {
    final XQuery query = new XQuery(
      "fn:base-uri(exactly-one((<elem xml:base=\"http://www.example.com\"><a/></elem>)/a))",
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
  public void constrContBaseuri3() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; fn:base-uri(<elem/>)",
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
   *  CDATA section in element constructor .
   */
  @org.junit.Test
  public void constrContCdata1() {
    final XQuery query = new XQuery(
      "<elem><![CDATA[cdata&<>'\"&lt;&#x20;]]></elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>cdata&amp;&lt;&gt;'\"&amp;lt;&amp;#x20;</elem>", false)
    );
  }

  /**
   *  character reference .
   */
  @org.junit.Test
  public void constrContCharref1() {
    final XQuery query = new XQuery(
      "<elem>&#x30;</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>0</elem>", false)
    );
  }

  /**
   *  invalid character reference .
   */
  @org.junit.Test
  public void constrContCharref2() {
    final XQuery query = new XQuery(
      "<elem>&#x0;</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0090")
    );
  }

  /**
   *  typed value of element .
   */
  @org.junit.Test
  public void constrContData1() {
    final XQuery query = new XQuery(
      "fn:data(<elem>a<a/>b</elem>)",
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
  public void constrContDoc1() {
    final XQuery query = new XQuery(
      "<elem>{(/), (/)}</elem>",
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
   *  Check the merging of text nodes introduced by the contents of a document node This test case was motivated by the resolution of Bug Report 3637 .
   */
  @org.junit.Test
  public void constrContDocument1() {
    final XQuery query = new XQuery(
      "count(<wrapper> {'abc', document {'def', <anode/>, 'ghi'}, 'jkl'} </wrapper>/node())",
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
   *  Check the merging of text nodes introduced by the contents of a document node This test case was motivated by the resolution of Bug Report 3637 .
   */
  @org.junit.Test
  public void constrContDocument2() {
    final XQuery query = new XQuery(
      "count(<wrapper> abc {document {'def', <anode/>, 'ghi'}} jkl </wrapper>/node())",
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
   * #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF] .
   */
  @org.junit.Test
  public void constrContDocument3() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $codepoints as xs:integer+ := (9, 10, 13, 32 to 55295, 57344 to 65532, 65536 to 1114111 ); \n" +
      "        declare variable $count as xs:integer := count($codepoints); \n" +
      "        declare variable $lineWidth as xs:integer := 70;\n" +
      "        <allCodepoints><r>{codepoints-to-string($codepoints)}</r></allCodepoints> \n" +
      "        (:<allCodepoints>{ \n" +
      "            for $i in (1 to $count idiv $lineWidth) \n" +
      "            let $startOffset := (($i - 1) * $lineWidth) + 1 \n" +
      "            return (<r s=\"{$codepoints[$startOffset]}\" e=\"{$codepoints[$startOffset] + $lineWidth}\"> { \n" +
      "                codepoints-to-string(subsequence($codepoints, $startOffset, $lineWidth)) } </r>, \"&#xA;\") \n" +
      "                } </allCodepoints>:)\n" +
      "        ",
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
        assertQuery("sum($result//r/text()/string-length()) = count((9, 10, 13, 32 to 55295, 57344 to 65532, 65536 to 1114111 ))")
      &&
        assertQuery("starts-with(($result//r/text())[1], '\t')")
      &&
        assertQuery("ends-with(($result//r/text())[last()], '􏿿')")
      )
    );
  }

  /**
   *  Check the merging of text nodes introduced by the contents of a document node. This test case was motivated by the resolution of Bug Report #3637 .
   */
  @org.junit.Test
  public void constrContDocument4() {
    final XQuery query = new XQuery(
      "count( document {'abc', 'def', document {'ghi', <anode/>, 'jkl'}, 'mno' } /node() )",
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
   *  Check the merging of text nodes introduced by the contents of a document node. This test case was motivated by the resolution of Bug Report #3637 .
   */
  @org.junit.Test
  public void constrContDocument5() {
    final XQuery query = new XQuery(
      "count( document {'abc', 'def', document {'ghi', 'jkl'}, 'mno' } /node() )",
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
   *  1 text node for enclosed expression with atomic values .
   */
  @org.junit.Test
  public void constrContEnclexpr1() {
    final XQuery query = new XQuery(
      "count((<elem>{1,'a',3.5,4e2}</elem>)/text())",
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
   *  1 text node for enclosed expression with atomic values .
   */
  @org.junit.Test
  public void constrContEnclexpr2() {
    final XQuery query = new XQuery(
      "count((<elem>{1,'a',<a/>,3.5,4e2}</elem>)/text())",
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
   *  space between atomic values in enclosed expression .
   */
  @org.junit.Test
  public void constrContEnclexpr3() {
    final XQuery query = new XQuery(
      "<elem>{1,'a',3.5,4e2}</elem>",
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
   *  enclosed expression with mix of atomic values and nodes .
   */
  @org.junit.Test
  public void constrContEnclexpr4() {
    final XQuery query = new XQuery(
      "<elem>{1,//a,2,3,//comment(),4,5,//processing-instruction(),6,7,//text(),8}</elem>",
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
   *  space between atomic values and empty string .
   */
  @org.junit.Test
  public void constrContEnclexpr5() {
    final XQuery query = new XQuery(
      "<elem>{1, '', 2}</elem>",
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
      assertSerialization("<elem>1  2</elem>", false)
    );
  }

  /**
   *  Pre-defined entity reference in element content .
   */
  @org.junit.Test
  public void constrContEntref1() {
    final XQuery query = new XQuery(
      "string-to-codepoints(<elem>&lt;</elem>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("60")
    );
  }

  /**
   *  Pre-defined entity reference in element content .
   */
  @org.junit.Test
  public void constrContEntref2() {
    final XQuery query = new XQuery(
      "string-to-codepoints(<elem>&gt;</elem>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("62")
    );
  }

  /**
   *  Pre-defined entity reference in element content .
   */
  @org.junit.Test
  public void constrContEntref3() {
    final XQuery query = new XQuery(
      "string-to-codepoints(<elem>&amp;</elem>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("38")
    );
  }

  /**
   *  Pre-defined entity reference in element content .
   */
  @org.junit.Test
  public void constrContEntref4() {
    final XQuery query = new XQuery(
      "string-to-codepoints(<elem>&quot;</elem>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("34")
    );
  }

  /**
   *  Pre-defined entity reference in element content .
   */
  @org.junit.Test
  public void constrContEntref5() {
    final XQuery query = new XQuery(
      "string-to-codepoints(<elem>&apos;</elem>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("39")
    );
  }

  /**
   *  end-of-line handling .
   */
  @org.junit.Test
  public void constrContEol1() {
    final XQuery query = new XQuery(
      "<codepoints>{string-to-codepoints(<elem>1\n" +
      "2</elem>)}</codepoints>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<codepoints>49 10 50</codepoints>", false)
    );
  }

  /**
   *  end-of-line handling .
   */
  @org.junit.Test
  public void constrContEol2() {
    final XQuery query = new XQuery(
      "<codepoints>{string-to-codepoints(<elem>1&#xa;2</elem>) }</codepoints>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<codepoints>49 10 50</codepoints>", false)
    );
  }

  /**
   *  end-of-line handling with character reference .
   */
  @org.junit.Test
  public void constrContEol3() {
    final XQuery query = new XQuery(
      "<codepoints>{string-to-codepoints(<elem>&#xD;&#xA;</elem>)}</codepoints>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<codepoints>13 10</codepoints>", false)
    );
  }

  /**
   *  end-of-line handling with character reference .
   */
  @org.junit.Test
  public void constrContEol4() {
    final XQuery query = new XQuery(
      "<codepoints>{string-to-codepoints(<elem>&#xD;</elem>)}</codepoints>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<codepoints>13</codepoints>", false)
    );
  }

  /**
   *  invalid character '{' in element content .
   */
  @org.junit.Test
  public void constrContInvalid1() {
    final XQuery query = new XQuery(
      "<elem>{</elem>",
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
   *  invalid character '}' in element content .
   */
  @org.junit.Test
  public void constrContInvalid2() {
    final XQuery query = new XQuery(
      "<elem>}</elem>",
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
   *  invalid character '&lt;' in element content .
   */
  @org.junit.Test
  public void constrContInvalid3() {
    final XQuery query = new XQuery(
      "<elem><</elem>",
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
   *  invalid character '&amp;' in element content .
   */
  @org.junit.Test
  public void constrContInvalid4() {
    final XQuery query = new XQuery(
      "<elem>&</elem>",
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
   *  nested element nodes .
   */
  @org.junit.Test
  public void constrContNested1() {
    final XQuery query = new XQuery(
      "<elem><a><b/></a><a/><c/></elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem><a><b/></a><a/><c/></elem>", false)
    );
  }

  /**
   *  nested pi nodes .
   */
  @org.junit.Test
  public void constrContNested2() {
    final XQuery query = new XQuery(
      "<elem><?pi?><?pi content?></elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem><?pi ?><?pi content?></elem>", false)
    );
  }

  /**
   *  nested comment nodes .
   */
  @org.junit.Test
  public void constrContNested3() {
    final XQuery query = new XQuery(
      "<elem><!----><!--content--></elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem><!----><!--content--></elem>", false)
    );
  }

  /**
   *  nested mix of nodes .
   */
  @org.junit.Test
  public void constrContNested4() {
    final XQuery query = new XQuery(
      "<elem>A<a>B<?pi?>C<b/>D<!---->E</a>F<!--content-->G<a/>H<?pi content?>I<c/>J</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>A<a>B<?pi ?>C<b/>D<!---->E</a>F<!--content-->G<a/>H<?pi content?>I<c/>J</elem>", false)
    );
  }

  /**
   *  Copied node structure in element content .
   */
  @org.junit.Test
  public void constrContNested5() {
    final XQuery query = new XQuery(
      "<elem>{/root}</elem>",
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
      assertSerialization("<elem><root><child1><a>text</a><!--comment--><?pi content?></child1><child2><a>text</a><!--comment--><?pi content?></child2></root></elem>", false)
    );
  }

  /**
   *  Copied element node has new node identity .
   */
  @org.junit.Test
  public void constrContNodeid1() {
    final XQuery query = new XQuery(
      "for $x in <a/>, $y in <elem>{$x}</elem> return exactly-one($y/a) is $x",
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
  public void constrContNodeid2() {
    final XQuery query = new XQuery(
      "for $x in <a b=\"b\"/>, $y in <elem>{$x/@b}</elem> return $y/@b is $x/@b",
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
  public void constrContNodeid3() {
    final XQuery query = new XQuery(
      "for $x in <!--comment-->, $y in <elem>{$x}</elem> return exactly-one($y/comment()) is $x",
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
  public void constrContNodeid4() {
    final XQuery query = new XQuery(
      "for $x in <?pi content?>, $y in <elem>{$x}</elem> return exactly-one($y/processing-instruction()) is $x",
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
  public void constrContNodeid5() {
    final XQuery query = new XQuery(
      "for $x in <a>text</a>, $y in <elem>{$x/text()}</elem> return exactly-one($y/text()) is exactly-one($x/text())",
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
   *  Copy-namespace mode preserve, inherit .
   */
  @org.junit.Test
  public void constrContNsmode1() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve, inherit; <y xmlns:inherit=\"http://www.example.com/inherit\">{(/)}</y>/x/z",
      ctx);
    try {
      query.context(node(file("prod/DirElemContent/nsmode.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<z xmlns:preserve=\"http://www.example.com/preserve\" xmlns:inherit=\"http://www.example.com/inherit\"/>", false)
    );
  }

  /**
   *  Copy-namespace mode no-preserve, inherit .
   */
  @org.junit.Test
  public void constrContNsmode2() {
    final XQuery query = new XQuery(
      "declare copy-namespaces no-preserve, inherit; <y xmlns:inherit=\"http://www.example.com/inherit\">{(/)}</y>/x/z",
      ctx);
    try {
      query.context(node(file("prod/DirElemContent/nsmode.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<z xmlns:inherit=\"http://www.example.com/inherit\"/>", false)
    );
  }

  /**
   *  Copy-namespace mode preserve, no-inherit .
   */
  @org.junit.Test
  public void constrContNsmode3() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve, no-inherit; <y xmlns:inherit=\"http://www.example.com/inherit\">{(/)}</y>/x/z",
      ctx);
    try {
      query.context(node(file("prod/DirElemContent/nsmode.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<z xmlns:preserve=\"http://www.example.com/preserve\"/>", false)
    );
  }

  /**
   *  Copy-namespace mode no-preserve, no-inherit .
   */
  @org.junit.Test
  public void constrContNsmode4() {
    final XQuery query = new XQuery(
      "declare copy-namespaces no-preserve, no-inherit; <y xmlns:inherit=\"http://www.example.com/inherit\">{(/)}</y>/x/z",
      ctx);
    try {
      query.context(node(file("prod/DirElemContent/nsmode.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<z/>", false)
    );
  }

  /**
   *  empty parent .
   */
  @org.junit.Test
  public void constrContParent1() {
    final XQuery query = new XQuery(
      "count((<elem/>)/..)",
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
  public void constrContString1() {
    final XQuery query = new XQuery(
      "fn:string(<elem>a<a/>b</elem>)",
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
   *  count text nodes for direct element content .
   */
  @org.junit.Test
  public void constrContText1() {
    final XQuery query = new XQuery(
      "count((<elem>text</elem>)/text())",
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
   *  count text nodes for direct element content and CDATA section .
   */
  @org.junit.Test
  public void constrContText2() {
    final XQuery query = new XQuery(
      "count((<elem>text<![CDATA[cdata]]></elem>)/text())",
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
   *  Preserve base uri for copied element nodes .
   */
  @org.junit.Test
  public void constrContUripres1() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $x in <a xml:base=\"http://www.example.com/base1\"><b/></a>, \n" +
      "            $y in <a xml:base=\"http://www.example.com/base2\">{$x/b}</a> \n" +
      "        return fn:base-uri(exactly-one($y/b))\n" +
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
      assertStringValue(false, "http://www.example.com/base2")
    );
  }

  /**
   *  An astray '}'. .
   */
  @org.junit.Test
  public void k2DirectConElemContent1() {
    final XQuery query = new XQuery(
      "3}",
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
   *  Test that the typed value of comment nodes is correct. .
   */
  @org.junit.Test
  public void k2DirectConElemContent10() {
    final XQuery query = new XQuery(
      "not(data(<!-- a comment -->) instance of xs:untypedAtomic)",
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
   *  Test that the typed value of comment nodes is correct. .
   */
  @org.junit.Test
  public void k2DirectConElemContent11() {
    final XQuery query = new XQuery(
      "<!-- a comment --> instance of comment()",
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
   *  Test that the typed value of comment nodes is correct. .
   */
  @org.junit.Test
  public void k2DirectConElemContent12() {
    final XQuery query = new XQuery(
      "not(<!-- a comment --> instance of xs:untypedAtomic)",
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
   *  Test that the typed value of comment nodes is correct. .
   */
  @org.junit.Test
  public void k2DirectConElemContent13() {
    final XQuery query = new XQuery(
      "not(<!-- a comment --> instance of xs:string)",
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
   *  Test that the typed value of PI nodes is correct. .
   */
  @org.junit.Test
  public void k2DirectConElemContent14() {
    final XQuery query = new XQuery(
      "data(<?target content?>) instance of xs:string",
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
   *  Test that the typed value of PI nodes is correct. .
   */
  @org.junit.Test
  public void k2DirectConElemContent15() {
    final XQuery query = new XQuery(
      "not(data(<?target content?>) instance of xs:untypedAtomic)",
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
   *  Test that the typed value of PI nodes is correct. .
   */
  @org.junit.Test
  public void k2DirectConElemContent16() {
    final XQuery query = new XQuery(
      "<?target content?> instance of processing-instruction()",
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
   *  Test that the typed value of PI nodes is correct. .
   */
  @org.junit.Test
  public void k2DirectConElemContent17() {
    final XQuery query = new XQuery(
      "not(<?target content?> instance of xs:untypedAtomic)",
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
   *  Syntactically invalid CDATA section. .
   */
  @org.junit.Test
  public void k2DirectConElemContent18() {
    final XQuery query = new XQuery(
      "<![CDATA[content]]>",
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
   *  Syntactically invalid CDATA section. .
   */
  @org.junit.Test
  public void k2DirectConElemContent19() {
    final XQuery query = new XQuery(
      "<elem><![THISISWRONG[content]]></elem>",
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
   *  An astray '}'. .
   */
  @org.junit.Test
  public void k2DirectConElemContent2() {
    final XQuery query = new XQuery(
      "\"a string\" }",
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
   *  Syntactically invalid CDATA section. .
   */
  @org.junit.Test
  public void k2DirectConElemContent20() {
    final XQuery query = new XQuery(
      "<elem><![CDA",
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
   *  Syntactically invalid CDATA section. .
   */
  @org.junit.Test
  public void k2DirectConElemContent21() {
    final XQuery query = new XQuery(
      "<elem><![CDATA[CONTENT]]>",
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
   *  Syntactically invalid CDATA section. .
   */
  @org.junit.Test
  public void k2DirectConElemContent22() {
    final XQuery query = new XQuery(
      "<elem><![CDATA[CONTENT]]",
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
   *  Syntactically invalid CDATA section. .
   */
  @org.junit.Test
  public void k2DirectConElemContent23() {
    final XQuery query = new XQuery(
      "<elem><![CDATA[CONTENT]",
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
   *  Syntactically invalid CDATA section. .
   */
  @org.junit.Test
  public void k2DirectConElemContent24() {
    final XQuery query = new XQuery(
      "<elem><![cdata[CONTENT]]></elem>",
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
   *  XQuery comments inside elements are not recognized..
   */
  @org.junit.Test
  public void k2DirectConElemContent25() {
    final XQuery query = new XQuery(
      "string(<eg> (: an (:example:) </eg>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, " (: an (:example:) ")
    );
  }

  /**
   *  Enclosed expressions in element content must have expressions. .
   */
  @org.junit.Test
  public void k2DirectConElemContent26() {
    final XQuery query = new XQuery(
      "<elem>content{}content</elem>",
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
   *  Many CDATA sections. .
   */
  @org.junit.Test
  public void k2DirectConElemContent27() {
    final XQuery query = new XQuery(
      "string(<elem><![CDATA[str]]>str<![CDATA[str]]><![CDATA[str]]><![CDATA[str]]>strstr{ \"str\", \"str\", \"strstr\", \"str\"}strstr<![CDATA[str]]>s<?target str?>tr</elem>) eq \"strstrstrstrstrstrstrstr str strstr strstrstrstrstr\"",
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
   *  Ensure CDATA doesn't receive special attention. .
   */
  @org.junit.Test
  public void k2DirectConElemContent28() {
    final XQuery query = new XQuery(
      "string(<elem><![CDATA[con<<< ]] >\"\"'*\"*\">>tent]]&#00;&#x12;&amp;&quot;&notrecognized;&apos]]></elem>) eq \"con&lt;&lt;&lt; ]] &gt;\"\"\"\"'*\"\"*\"\"&gt;&gt;tent]]&amp;#00;&amp;#x12;&amp;amp;&amp;quot;&amp;notrecognized;&amp;apos\"",
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
   *  Test that the type annotation of text nodes is correct. .
   */
  @org.junit.Test
  public void k2DirectConElemContent29() {
    final XQuery query = new XQuery(
      "data(text{\"content\"}) instance of xs:untypedAtomic",
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
   *  An astray '}'. .
   */
  @org.junit.Test
  public void k2DirectConElemContent3() {
    final XQuery query = new XQuery(
      "}",
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
   *  Two atomic values with a text node inbetween. .
   */
  @org.junit.Test
  public void k2DirectConElemContent30() {
    final XQuery query = new XQuery(
      "<e>{1}A{1}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e>1A1</e>", false)
    );
  }

  /**
   *  Two atomic values with a text node inbetween(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemContent31() {
    final XQuery query = new XQuery(
      "string(<e>{1}A{1}</e>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1A1")
    );
  }

  /**
   *  Tests the type of the typed value of a directly constructed element node. .
   */
  @org.junit.Test
  public void k2DirectConElemContent32() {
    final XQuery query = new XQuery(
      "data(<e>dsa</e>) instance of xs:untypedAtomic",
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
   *  Tests the typed value of a directly constructed element node. .
   */
  @org.junit.Test
  public void k2DirectConElemContent33() {
    final XQuery query = new XQuery(
      "data(<e>dsa</e>)",
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
  public void k2DirectConElemContent34() {
    final XQuery query = new XQuery(
      "<e/> instance of element(*, xs:anyType)",
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
   *       	Check that a directly constructed element gets the right type(#2).
   *       	Note: see bug 11585, especially comment 9. 
   *       .
   */
  @org.junit.Test
  public void k2DirectConElemContent35() {
    final XQuery query = new XQuery(
      "<e/> instance of element(*, xs:untyped)",
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
   *  Check that a directly constructed element gets the right type(#3). .
   */
  @org.junit.Test
  public void k2DirectConElemContent36() {
    final XQuery query = new XQuery(
      "declare construction strip; <e/> instance of element(*, xs:untyped)",
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
  public void k2DirectConElemContent37() {
    final XQuery query = new XQuery(
      "<e/> instance of element(a, xs:anyType)",
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
  public void k2DirectConElemContent38() {
    final XQuery query = new XQuery(
      "declare construction strip; <e/> instance of element(b, xs:untyped)",
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
   *  Use many enclosed expressions. .
   */
  @org.junit.Test
  public void k2DirectConElemContent39() {
    final XQuery query = new XQuery(
      "<elem>{1}{2}{3}{4}{5}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>12345</elem>", false)
    );
  }

  /**
   *  An astray '}'. .
   */
  @org.junit.Test
  public void k2DirectConElemContent4() {
    final XQuery query = new XQuery(
      "}",
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
   *  Use many enclosed expressions(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemContent40() {
    final XQuery query = new XQuery(
      "<elem>{1}{2}{3}{4}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>1234</elem>", false)
    );
  }

  /**
   *  Use many enclosed expressions(#3). .
   */
  @org.junit.Test
  public void k2DirectConElemContent41() {
    final XQuery query = new XQuery(
      "<elem>{1}{2}{3}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>123</elem>", false)
    );
  }

  /**
   *  Use many enclosed expressions(#4). .
   */
  @org.junit.Test
  public void k2DirectConElemContent42() {
    final XQuery query = new XQuery(
      "<elem>{1}{2}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>12</elem>", false)
    );
  }

  /**
   *  Tricky whitespace case. .
   */
  @org.junit.Test
  public void k2DirectConElemContent43() {
    final XQuery query = new XQuery(
      "<a> <![CDATA[ ]]> {\"abc\"}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>   abc</a>", false)
    );
  }

  /**
   *  Inproperly balanced attribute quotes. .
   */
  @org.junit.Test
  public void k2DirectConElemContent44() {
    final XQuery query = new XQuery(
      "<e attr='content\"/>",
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
   *  Inproperly balanced attribute quotes(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemContent45() {
    final XQuery query = new XQuery(
      "<e attr=\"content'/>",
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
   *  Have an empty text node constructor between two atomic values. .
   */
  @org.junit.Test
  public void k2DirectConElemContent46() {
    final XQuery query = new XQuery(
      "<e>{1}{text{()}}{2}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e>12</e>", false)
    );
  }

  /**
   *  Have an text node constructor that constructs an empty string, between two atomic values. .
   */
  @org.junit.Test
  public void k2DirectConElemContent47() {
    final XQuery query = new XQuery(
      "<e>{1}{text{\"\"}}{2}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e>12</e>", false)
    );
  }

  /**
   *  Serialize an undeclaration. .
   */
  @org.junit.Test
  public void k2DirectConElemContent48() {
    final XQuery query = new XQuery(
      "\n" +
      "      \t<e xmlns=\"http://www.example.com/\"> <a xmlns=\"\"/> </e>, \n" +
      "      \t<e xmlns=\"http://www.example.com/\"> <a xmlns=\"\"/> </e>/count(in-scope-prefixes(a)), \n" +
      "      \t<e xmlns=\"http://www.example.com/\"> <a xmlns=\"\"> <b xmlns=\"\"/> </a> </e>\n" +
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
      assertSerialization("<e xmlns=\"http://www.example.com/\"><a xmlns=\"\"/></e>1<e xmlns=\"http://www.example.com/\"><a xmlns=\"\"><b/></a></e>", false)
    );
  }

  /**
   *  Test that the typed value of element nodes is correct. .
   */
  @org.junit.Test
  public void k2DirectConElemContent5() {
    final XQuery query = new XQuery(
      "data(<name>some text</name>) instance of xs:untypedAtomic",
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
   *  Test that the typed value of element nodes is correct. .
   */
  @org.junit.Test
  public void k2DirectConElemContent6() {
    final XQuery query = new XQuery(
      "not(data(<name>some text</name>) instance of xs:string)",
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
   *  Test that the typed value of element nodes is correct. .
   */
  @org.junit.Test
  public void k2DirectConElemContent7() {
    final XQuery query = new XQuery(
      "<name>some, if(1) then else</name> instance of element()",
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
   *  Test that the typed of element nodes is correct. .
   */
  @org.junit.Test
  public void k2DirectConElemContent8() {
    final XQuery query = new XQuery(
      "not(<name>some text</name> instance of xs:untypedAtomic)",
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
   *  Test that the typed value of comment nodes is correct. .
   */
  @org.junit.Test
  public void k2DirectConElemContent9() {
    final XQuery query = new XQuery(
      "data(<!-- a comment -->) instance of xs:string",
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
   *  Ensure that namespace fixup occurs when namespace prefix names clash .
   */
  @org.junit.Test
  public void cbclNsFixup1() {
    final XQuery query = new XQuery(
      " \n" +
      "      \tlet $x := <ns:foo xmlns:ns=\"http://www.w3.org/foo\" ns:attr=\"foo\" /> \n" +
      "      \treturn let $y := <ns:foo xmlns:ns=\"http://www.w3.org/bar\" ns:attr=\"bar\" /> \n" +
      "      \treturn let $z := <root> { $x/@*, $y/@* } </root> \n" +
      "      \treturn count(distinct-values(in-scope-prefixes($z)))\n" +
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
      assertEq("3")
    );
  }
}
