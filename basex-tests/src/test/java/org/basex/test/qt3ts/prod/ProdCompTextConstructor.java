package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the CompTextConstructor (text node constructor) production.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdCompTextConstructor extends QT3TestSet {

  /**
   *  merge adjacent text nodes .
   */
  @org.junit.Test
  public void constrTextAdjtext1() {
    final XQuery query = new XQuery(
      "count(<elem>{text {'te'}, text {'xt'}}</elem>/text())",
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
   *  merge adjacent text nodes .
   */
  @org.junit.Test
  public void constrTextAdjtext2() {
    final XQuery query = new XQuery(
      "count(document {text {'te'}, text {'xt'}}/text())",
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
   *  count text nodes .
   */
  @org.junit.Test
  public void constrTextCount1() {
    final XQuery query = new XQuery(
      "count(text {''})",
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
   *  count text nodes .
   */
  @org.junit.Test
  public void constrTextCount2() {
    final XQuery query = new XQuery(
      "count(text {()})",
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
   *  count text nodes .
   */
  @org.junit.Test
  public void constrTextCount3() {
    final XQuery query = new XQuery(
      "count(element elem {text {''}}/text())",
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
   *  count text nodes .
   */
  @org.junit.Test
  public void constrTextCount4() {
    final XQuery query = new XQuery(
      "count(document {text {''}}/text())",
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
   *  count text nodes .
   */
  @org.junit.Test
  public void constrTextCount5() {
    final XQuery query = new XQuery(
      "count(<a>{text {''}}<b/>{text {''}}<b/>{text {''}}</a>/text())",
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
   *  count text nodes .
   */
  @org.junit.Test
  public void constrTextCount6() {
    final XQuery query = new XQuery(
      "count(document {text {''},<b/>,text {''},<b/>,text {''}}/text())",
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
   *  typed value of text node .
   */
  @org.junit.Test
  public void constrTextData1() {
    final XQuery query = new XQuery(
      "fn:data(text {'a', element a {}, 'b'})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a  b")
    );
  }

  /**
   *  strip document nodes .
   */
  @org.junit.Test
  public void constrTextDoc1() {
    final XQuery query = new XQuery(
      "text {., .}",
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
      assertStringValue(false, "texttext texttext")
    );
  }

  /**
   *  enclosed expression in text node - atomic values .
   */
  @org.junit.Test
  public void constrTextEnclexpr1() {
    final XQuery query = new XQuery(
      "text {1,'string',3.14,xs:float('1.2345e-2'),xs:dateTime('2002-04-02T12:00:00-01:00')}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 string 3.14 0.012345 2002-04-02T12:00:00-01:00")
    );
  }

  /**
   *  enclosed expression in text node - nodes .
   */
  @org.junit.Test
  public void constrTextEnclexpr2() {
    final XQuery query = new XQuery(
      "text {<elem>123</elem>, (<elem attr='456'/>)/@attr, (<elem>789</elem>)/text()}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "123 456 789")
    );
  }

  /**
   *  enclosed expression in text node - empty string .
   */
  @org.junit.Test
  public void constrTextEnclexpr3() {
    final XQuery query = new XQuery(
      "text {1,'',2}",
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
   *  enclosed expression in text node - empty node .
   */
  @org.junit.Test
  public void constrTextEnclexpr4() {
    final XQuery query = new XQuery(
      "text {1,<a/>,2}",
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
   *  enclosed expression in text node - nodes .
   */
  @org.junit.Test
  public void constrTextEnclexpr5() {
    final XQuery query = new XQuery(
      "text {/root}",
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
      assertStringValue(false, "texttext")
    );
  }

  /**
   *  nested text node constructor .
   */
  @org.junit.Test
  public void constrTextNested1() {
    final XQuery query = new XQuery(
      "text {text {'one', text {'two'}}, 'three', text {'four'}}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "one two three four")
    );
  }

  /**
   *  nested text nodes in element constructor .
   */
  @org.junit.Test
  public void constrTextNested2() {
    final XQuery query = new XQuery(
      "<elem>{text {'one'}}<a>{text {'two'}}</a>{text {'three'}}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem>one<a>two</a>three</elem>", false)
    );
  }

  /**
   *  nested text nodes in element constructor .
   */
  @org.junit.Test
  public void constrTextNested3() {
    final XQuery query = new XQuery(
      "document {text {'one'}, <a/>, text {'two'}, <b/>, text {'three'}}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("one<a/>two<b/>three", false)
    );
  }

  /**
   *  empty parent .
   */
  @org.junit.Test
  public void constrTextParent1() {
    final XQuery query = new XQuery(
      "count((text {'text'})/..)",
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
   *  string value of text node .
   */
  @org.junit.Test
  public void constrTextString1() {
    final XQuery query = new XQuery(
      "fn:string(text {'a', element a {}, 'b'})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a  b")
    );
  }

  /**
   *  Text constructors cannot specify a name. .
   */
  @org.junit.Test
  public void k2ConText1() {
    final XQuery query = new XQuery(
      "text {\"name\"} {\"content\"}",
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
   *  The end of content in a CDATA section may end with many ']'(#4). .
   */
  @org.junit.Test
  public void k2ConText10() {
    final XQuery query = new XQuery(
      "<e><![CDATA[]]]]]]></e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e>]]]]</e>", false)
    );
  }

  /**
   *  A CDATA section ending incorrectly. .
   */
  @org.junit.Test
  public void k2ConText11() {
    final XQuery query = new XQuery(
      "<e><![CDATA]]",
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
   *  A CDATA section ending incorrectly(#2). .
   */
  @org.junit.Test
  public void k2ConText12() {
    final XQuery query = new XQuery(
      "<e><![CDATA]",
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
   *  An element ending incorrectly. .
   */
  @org.junit.Test
  public void k2ConText13() {
    final XQuery query = new XQuery(
      "<e><![CDATA]]>",
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
   *  Ensure that an empty text node between two atomic values are serialized correctly. .
   */
  @org.junit.Test
  public void k2ConText14() {
    final XQuery query = new XQuery(
      "<elem>{1}{text{\"\"}}{2}</elem>",
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
   *  Ensure that an empty text node between two atomic values are serialized correctly(#2). .
   */
  @org.junit.Test
  public void k2ConText15() {
    final XQuery query = new XQuery(
      "string(<elem>{1}{text{\"\"}}{2}</elem>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "12")
    );
  }

  /**
   *  Text constructors cannot specify a name(#2). .
   */
  @org.junit.Test
  public void k2ConText2() {
    final XQuery query = new XQuery(
      "text name {\"content\"}",
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
   *  An empty text node is nevertheless a text node, not the empty sequence. .
   */
  @org.junit.Test
  public void k2ConText3() {
    final XQuery query = new XQuery(
      "string(text {\"\"}) eq \"\"",
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
   *  Construct from an empty sequence. .
   */
  @org.junit.Test
  public void k2ConText4() {
    final XQuery query = new XQuery(
      "<a>{text{()}}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a></a>", false)
    );
  }

  /**
   *  The enclosed expression isn't optional. .
   */
  @org.junit.Test
  public void k2ConText5() {
    final XQuery query = new XQuery(
      "text{}",
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
   *  Test the atomized type. .
   */
  @org.junit.Test
  public void k2ConText6() {
    final XQuery query = new XQuery(
      "data(text {\"content\"}) instance of xs:untypedAtomic",
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
   *  The end of content in a CDATA section may end with ']'. .
   */
  @org.junit.Test
  public void k2ConText7() {
    final XQuery query = new XQuery(
      "<e><![CDATA[content]]]></e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e>content]</e>", false)
    );
  }

  /**
   *  The end of content in a CDATA section may end with many ']'(#2). .
   */
  @org.junit.Test
  public void k2ConText8() {
    final XQuery query = new XQuery(
      "<e><![CDATA[content]]]]]]></e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e>content]]]]</e>", false)
    );
  }

  /**
   *  The end of content in a CDATA section may end with ']'(#3). .
   */
  @org.junit.Test
  public void k2ConText9() {
    final XQuery query = new XQuery(
      "<e><![CDATA[]]]></e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e>]</e>", false)
    );
  }
}
