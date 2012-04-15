package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the DirElemConstructor (Direct Element Constructor) production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdDirElemConstructor extends QT3TestSet {

  /**
   *  Element constructor with open curly brace .
   */
  @org.junit.Test
  public void constrElemCurlybr1() {
    final XQuery query = new XQuery(
      "<elem>{{</elem>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<elem>{</elem>", false)
    );
  }

  /**
   *  Element constructor with closing curly brace .
   */
  @org.junit.Test
  public void constrElemCurlybr2() {
    final XQuery query = new XQuery(
      "<elem>}}</elem>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<elem>}</elem>", false)
    );
  }

  /**
   *  Character reference for open curly brace .
   */
  @org.junit.Test
  public void constrElemCurlybr3() {
    final XQuery query = new XQuery(
      "<elem>&#x7b;</elem>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<elem>{</elem>", false)
    );
  }

  /**
   *  Character reference for closing curly brace .
   */
  @org.junit.Test
  public void constrElemCurlybr4() {
    final XQuery query = new XQuery(
      "<elem>&#x7d;</elem>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<elem>}</elem>", false)
    );
  }

  /**
   *  Single open curly brace .
   */
  @org.junit.Test
  public void constrElemCurlybr5() {
    final XQuery query = new XQuery(
      "<elem>{</elem>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Single closing curly brace .
   */
  @org.junit.Test
  public void constrElemCurlybr6() {
    final XQuery query = new XQuery(
      "<elem>}</elem>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Empty element constructor .
   */
  @org.junit.Test
  public void constrElemEmpty1() {
    final XQuery query = new XQuery(
      "<elem/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<elem/>", false)
    );
  }

  /**
   *  Empty element constructor with closing tag .
   */
  @org.junit.Test
  public void constrElemEmpty2() {
    final XQuery query = new XQuery(
      "<elem></elem>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<elem/>", false)
    );
  }

  /**
   *  Number of child nodes for empty element .
   */
  @org.junit.Test
  public void constrElemEmpty3() {
    final XQuery query = new XQuery(
      "fn:count((<elem/>)/node())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Number of child nodes for empty element .
   */
  @org.junit.Test
  public void constrElemEmpty4() {
    final XQuery query = new XQuery(
      "fn:count((<elem></elem>)/node())",
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
   *  Whitespace in element constructor .
   */
  @org.junit.Test
  public void constrElemEmpty5() {
    final XQuery query = new XQuery(
      "<elem />",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<elem/>", false)
    );
  }

  /**
   *  Begin tag matches end tag .
   */
  @org.junit.Test
  public void constrElemMatchtag1() {
    final XQuery query = new XQuery(
      "<elem></elemother>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0118")
    );
  }

  /**
   *  Begin tag matches end tag with namespace prefix .
   */
  @org.junit.Test
  public void constrElemMatchtag2() {
    final XQuery query = new XQuery(
      "<foo:elem xmlns:foo=\"http://www.w3.org/XQueryTest/Construct\" xmlns:bar=\"http://www.w3.org/XQueryTest/Construct\"></bar:elem>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0118")
    );
  }

  /**
   *  Ensure processing-instructions aren't included when extracting the string-value from elements. .
   */
  @org.junit.Test
  public void k2DirectConElem1() {
    final XQuery query = new XQuery(
      "string(<pi>{<?pi x?>}</pi>) eq \"\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Syntax error in direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem10() {
    final XQuery query = new XQuery(
      "<prefix:foo",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XPST0003")
      ||
        error("XPST0081")
      )
    );
  }

  /**
   *  Syntax error in direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem11() {
    final XQuery query = new XQuery(
      "<prefix: foo/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Syntax error in direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem12() {
    final XQuery query = new XQuery(
      "<foo attr=\"{'a string'}><<<\"/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Syntax error in direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem13() {
    final XQuery query = new XQuery(
      "</>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Syntactically invalid direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem14() {
    final XQuery query = new XQuery(
      "<e> content}</e>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Syntactically invalid direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem15() {
    final XQuery query = new XQuery(
      "<f><c></f></c>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Syntactically invalid direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem16() {
    final XQuery query = new XQuery(
      "<a><b><c/><d/><a/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Syntactically invalid direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem17() {
    final XQuery query = new XQuery(
      "<a><b><c/><b/><d/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Syntactically invalid direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem18() {
    final XQuery query = new XQuery(
      "<elem>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Syntactically invalid direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem19() {
    final XQuery query = new XQuery(
      "<elem>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Ensure comments aren't included when extracting the string-value from elements. .
   */
  @org.junit.Test
  public void k2DirectConElem2() {
    final XQuery query = new XQuery(
      "string(<a attr=\"content\"><!-- NOTINC -->1<b>2<c><!-- NOTINC -->34</c><!-- NOTINC --><d/>56</b>7</a>) eq \"1234567\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Syntactically invalid direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem20() {
    final XQuery query = new XQuery(
      "<elem><",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Syntactically invalid direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem21() {
    final XQuery query = new XQuery(
      "<elem><[",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Syntactically invalid direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem22() {
    final XQuery query = new XQuery(
      "<elem><![",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Syntactically invalid direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem23() {
    final XQuery query = new XQuery(
      "<elem><!-",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Syntactically invalid direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem24() {
    final XQuery query = new XQuery(
      "<elem>&</elem>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Enclosed expressions aren't allowed inside element constructors. .
   */
  @org.junit.Test
  public void k2DirectConElem25() {
    final XQuery query = new XQuery(
      "<elem {\"attribute-name\"} = \"attribute value\"/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Enclosed expressions aren't allowed inside element constructors.(#2). .
   */
  @org.junit.Test
  public void k2DirectConElem26() {
    final XQuery query = new XQuery(
      "<elem attributename = {\"attribute value\"} />",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Enclosed expressions aren't allowed inside element constructors.(#3). .
   */
  @org.junit.Test
  public void k2DirectConElem27() {
    final XQuery query = new XQuery(
      "<elem attributename = {\"attribute value\"}></elem>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  A simple direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem28() {
    final XQuery query = new XQuery(
      "<a></a>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a/>", false)
    );
  }

  /**
   *  Serialize a sequence of direct element constructors. .
   */
  @org.junit.Test
  public void k2DirectConElem29() {
    final XQuery query = new XQuery(
      "<e>a</e>, <e>b</e>, <e>c</e>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<e>a</e><e>b</e><e>c</e>", false)
    );
  }

  /**
   *  Syntax error in direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem3() {
    final XQuery query = new XQuery(
      "<",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Serialize a couple of element constructors. .
   */
  @org.junit.Test
  public void k2DirectConElem30() {
    final XQuery query = new XQuery(
      "<elem>some text<node/>some text</elem>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<elem>some text<node/>some text</elem>", false)
    );
  }

  /**
   *  A couple of simple elements. .
   */
  @org.junit.Test
  public void k2DirectConElem31() {
    final XQuery query = new XQuery(
      "<foo > <doo/> </foo>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<foo><doo/></foo>", false)
    );
  }

  /**
   *  A couple of simple elements(#2). .
   */
  @org.junit.Test
  public void k2DirectConElem32() {
    final XQuery query = new XQuery(
      "<foo><doo/></foo>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<foo><doo/></foo>", false)
    );
  }

  /**
   *  A couple of simple elements(#3). .
   */
  @org.junit.Test
  public void k2DirectConElem33() {
    final XQuery query = new XQuery(
      "<foo><doo/> </foo>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<foo><doo/></foo>", false)
    );
  }

  /**
   *  An element with a computed attribute and element. .
   */
  @org.junit.Test
  public void k2DirectConElem34() {
    final XQuery query = new XQuery(
      "<foo > {attribute name {\"content\"}} <doo/> </foo>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<foo name=\"content\"><doo/></foo>", false)
    );
  }

  /**
   *  Bind the 'xml' namespace URI to an invalid prefix. .
   */
  @org.junit.Test
  public void k2DirectConElem35() {
    final XQuery query = new XQuery(
      "<e xmlns:aPrefixOtherThanXml=\"http://www.w3.org/XML/1998/namespace\"/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0070")
    );
  }

  /**
   *  Bind the 'xml' namespace URI to an invalid prefix(#2). .
   */
  @org.junit.Test
  public void k2DirectConElem36() {
    final XQuery query = new XQuery(
      "<e xmlns=\"http://www.w3.org/XML/1998/namespace\"/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0070")
    );
  }

  /**
   *  Bind the 'xml' namespace URI to an invalid prefix(#3). .
   */
  @org.junit.Test
  public void k2DirectConElem37() {
    final XQuery query = new XQuery(
      "<e xmlns:XML=\"http://www.w3.org/XML/1998/namespace\"/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0070")
    );
  }

  /**
   *  Bind the 'xml' namespace URI to a valid prefix. The output doesn't expect the declaration because the c14n specification ignores declarations of the xml prefix if it binds to the XML namespace(see section 2.3). In either case, serializing this declaration is redundant. See the public report #4217 in W3C's Bugzilla database. .
   */
  @org.junit.Test
  public void k2DirectConElem38() {
    final XQuery query = new XQuery(
      "<e xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<e/>", false)
    );
  }

  /**
   *  Bind the 'xmlns' namespace URI to an invalid prefix(#3). .
   */
  @org.junit.Test
  public void k2DirectConElem39() {
    final XQuery query = new XQuery(
      "<e xmlns:xmlns=\"http://www.w3.org/XML/1998/namespace\"/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0070")
    );
  }

  /**
   *  Syntax error in direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem4() {
    final XQuery query = new XQuery(
      "< foo/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Bind the 'xmlns' namespace URI to an invalid prefix(#3). .
   */
  @org.junit.Test
  public void k2DirectConElem40() {
    final XQuery query = new XQuery(
      "<e xmlns:xmlns=\"http://www.w3.org/2000/xmlns/\"/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0070")
    );
  }

  /**
   *  Bind the 'xmlns' namespace URI to an invalid prefix(#3). .
   */
  @org.junit.Test
  public void k2DirectConElem41() {
    final XQuery query = new XQuery(
      "<e xmlns:xmlns=\"http://www.example.com/\"/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0070")
    );
  }

  /**
   *  Use a content sequence that is a strange combination of a computed document constructor and a path. .
   */
  @org.junit.Test
  public void k2DirectConElem42() {
    final XQuery query = new XQuery(
      "<e> { document{()}/(/) } </e>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<e/>", false)
    );
  }

  /**
   *  Use a content sequence that is a computed document constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem43() {
    final XQuery query = new XQuery(
      "<e> { document{()} } </e>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<e/>", false)
    );
  }

  /**
   *  Extract the string value of a single element node. .
   */
  @org.junit.Test
  public void k2DirectConElem44() {
    final XQuery query = new XQuery(
      "string(<e>text</e>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "text")
    );
  }

  /**
   *  Extract the typed value of a single element node. .
   */
  @org.junit.Test
  public void k2DirectConElem45() {
    final XQuery query = new XQuery(
      "data(<e>text</e>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "text")
    );
  }

  /**
   *  Use an element constructor that use a namespace declare in a prolog declaration. .
   */
  @org.junit.Test
  public void k2DirectConElem46() {
    final XQuery query = new XQuery(
      "declare namespace p = \"http://www.example.com/\"; <p:e/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<p:e xmlns:p=\"http://www.example.com/\"/>", false)
    );
  }

  /**
   *  Use content that needs to be escaped, inside namespace declaration attributes. .
   */
  @org.junit.Test
  public void k2DirectConElem47() {
    final XQuery query = new XQuery(
      "let $in := <r> <e xmlns=\"http://example.com/&lt;&gt;&quot;&apos;\"\"\"/> <e xmlns='http://example.com/&lt;&gt;&quot;&apos;'''/> <p:e xmlns:p=\"http://example.com/&lt;&gt;&quot;&apos;\"\"\"/> <p:e xmlns:p='http://example.com/&lt;&gt;&quot;&apos;'''/> </r> return <r>{for $n in $in/*/namespace-uri(.) return <e ns=\"{$n}\"/>}</r>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<r><e ns=\"http://example.com/&lt;&gt;&quot;'&quot;\"/><e ns=\"http://example.com/&lt;&gt;&quot;''\"/><e ns=\"http://example.com/&lt;&gt;&quot;'&quot;\"/><e ns=\"http://example.com/&lt;&gt;&quot;''\"/></r>", false)
      ||
        error("XQST0046")
      ||
        error("XQST0022")
      )
    );
  }

  /**
   *  In XQuery, as opposed to XML, the sequence ]]< is allowed in element content. .
   */
  @org.junit.Test
  public void k2DirectConElem48() {
    final XQuery query = new XQuery(
      "<e>]]></e>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "]]>")
    );
  }

  /**
   *  In XQuery, as opposed to XML, the sequence ]]< is allowed in attribute content. .
   */
  @org.junit.Test
  public void k2DirectConElem49() {
    final XQuery query = new XQuery(
      "<e attr=\"]]>\"/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<e attr=\"]]>\"/>", false)
    );
  }

  /**
   *  Syntax error in direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem5() {
    final XQuery query = new XQuery(
      "<foo/ >",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Character < cannot appear in attributes. .
   */
  @org.junit.Test
  public void k2DirectConElem50() {
    final XQuery query = new XQuery(
      "<e attr=\"<\"/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Ensure that whitespace normalization of xml:id is performed. .
   */
  @org.junit.Test
  public void k2DirectConElem51() {
    final XQuery query = new XQuery(
      "<e xml:id=\" fo\"/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<e xml:id=\"fo\"/>", false)
    );
  }

  /**
   *  Output sharp S. .
   */
  @org.junit.Test
  public void k2DirectConElem52() {
    final XQuery query = new XQuery(
      "<a>&#223;</a>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>&#223;</a>", false)
    );
  }

  /**
   *  There is a 'namespace' constructor in XQuery 3.0. .
   */
  @org.junit.Test
  public void k2DirectConElem53a() {
    final XQuery query = new XQuery(
      "namespace {\"p\"} {\"abc\"}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertType("namespace-node()")
      &&
        assertStringValue(false, "abc")
      )
    );
  }

  /**
   *  There is no 'namespace-node' constructor in XQuery. .
   */
  @org.junit.Test
  public void k2DirectConElem54() {
    final XQuery query = new XQuery(
      "namespace-node {\"p\"} {\"abc\"}",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Syntax error in direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem6() {
    final XQuery query = new XQuery(
      "< foo></foo>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Syntax error in direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem7() {
    final XQuery query = new XQuery(
      "<foo>< /foo>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Syntax error in direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem8() {
    final XQuery query = new XQuery(
      "<foo /",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Syntax error in direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElem9() {
    final XQuery query = new XQuery(
      "<foo",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }
}
