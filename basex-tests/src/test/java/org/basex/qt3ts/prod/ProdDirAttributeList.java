package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the DirAttributeList production (the list of attributes in a direct element constructor).
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdDirAttributeList extends QT3TestSet {

  /**
   *  character references in attribute content .
   */
  @org.junit.Test
  public void constrAttrCharref1() {
    final XQuery query = new XQuery(
      "<elem attr=\"&#x30;&#x31;&#x32;\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"012\"/>", false)
    );
  }

  /**
   *  Direct attribute content characters .
   */
  @org.junit.Test
  public void constrAttrContent1() {
    final XQuery query = new XQuery(
      "<elem attr=\"abxxyz123890!@#$%^*()[]\\|?/>:;\"/>",
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
        assertSerialization("<elem attr=\"abxxyz123890!@#$%^*()[]\\|?/&gt;:;\"/>", false)
      ||
        assertSerialization("<elem attr=\"abxxyz123890!@#$%^*()[]\\|?/>:;\"/>", false)
      )
    );
  }

  /**
   *  Illegal attribute content "{" .
   */
  @org.junit.Test
  public void constrAttrContent2() {
    final XQuery query = new XQuery(
      "<elem attr=\"{\"/>",
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
   *  Illegal attribute content "}" .
   */
  @org.junit.Test
  public void constrAttrContent3() {
    final XQuery query = new XQuery(
      "<elem attr=\"}\"/>",
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
   *  Illegal attribute content "<" .
   */
  @org.junit.Test
  public void constrAttrContent4() {
    final XQuery query = new XQuery(
      "<elem attr=\"<\"/>",
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
   *  Illegal attribute content "&" .
   */
  @org.junit.Test
  public void constrAttrContent5() {
    final XQuery query = new XQuery(
      "<elem attr=\"&\"/>",
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
   *  attributes with identical local names .
   */
  @org.junit.Test
  public void constrAttrDistnames1() {
    final XQuery query = new XQuery(
      "<elem attr=\"val1\" attr=\"val2\" attr2=\"val3\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0040")
    );
  }

  /**
   *  attributes with identical local names .
   */
  @org.junit.Test
  public void constrAttrDistnames2() {
    final XQuery query = new XQuery(
      "<elem attr=\"val1\" attr2=\"val2\" attr=\"val3\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0040")
    );
  }

  /**
   *  attributes with identical local names .
   */
  @org.junit.Test
  public void constrAttrDistnames3() {
    final XQuery query = new XQuery(
      "<elem attr1=\"val1\" attr=\"val2\" attr2=\"val3\" attr=\"val4\" attr3=\"val5\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0040")
    );
  }

  /**
   *  attributes with identical local name and URI .
   */
  @org.junit.Test
  public void constrAttrDistnames4() {
    final XQuery query = new XQuery(
      "<elem xmlns:foo=\"http://www.w3.org/XQueryTest/Construct\" xmlns:bar=\"http://www.w3.org/XQueryTest/Construct\" foo:attr=\"val1\" bar:attr=\"val2\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0040")
    );
  }

  /**
   *  enclosed expression in attribute content - atomic values .
   */
  @org.junit.Test
  public void constrAttrEnclexpr1() {
    final XQuery query = new XQuery(
      "<elem attr=\"{1,'string',3.14,xs:float('1.2345e-2'),xs:dateTime('2002-04-02T12:00:00-01:00')}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"1 string 3.14 0.012345 2002-04-02T12:00:00-01:00\"/>", false)
    );
  }

  /**
   *  enclosed expression in attribute content - nodes .
   */
  @org.junit.Test
  public void constrAttrEnclexpr2() {
    final XQuery query = new XQuery(
      "<elem attr=\"{<elem>123</elem>, (<elem attr='456'/>)/@attr, (<elem>789</elem>)/text()}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"123 456 789\"/>", false)
    );
  }

  /**
   *  enclosed expression in attribute content - empty string .
   */
  @org.junit.Test
  public void constrAttrEnclexpr3() {
    final XQuery query = new XQuery(
      "<elem attr=\"{1,'',2}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"1  2\"/>", false)
    );
  }

  /**
   *  enclosed expression in attribute content - empty node .
   */
  @org.junit.Test
  public void constrAttrEnclexpr4() {
    final XQuery query = new XQuery(
      "<elem attr=\"{1,<a/>,2}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"1  2\"/>", false)
    );
  }

  /**
   *  enclosed expression in attribute content - mix direct content and enclosed expressions .
   */
  @org.junit.Test
  public void constrAttrEnclexpr5() {
    final XQuery query = new XQuery(
      "<elem attr=\"123{456}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"123456\"/>", false)
    );
  }

  /**
   *  enclosed expression in attribute content - mix direct content and enclosed expressions .
   */
  @org.junit.Test
  public void constrAttrEnclexpr6() {
    final XQuery query = new XQuery(
      "<elem attr=\"{123}456\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"123456\"/>", false)
    );
  }

  /**
   *  enclosed expression in attribute content - mix direct content and enclosed expressions .
   */
  @org.junit.Test
  public void constrAttrEnclexpr7() {
    final XQuery query = new XQuery(
      "<elem attr=\"1{2,3}{4,5}6{<a>7</a>}{<a>8</a>}9\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"12 34 56789\"/>", false)
    );
  }

  /**
   * Author: Oliver Hallam  Multiple content units in attribute content. .
   */
  @org.junit.Test
  public void constrAttrEnclexpr8() {
    final XQuery query = new XQuery(
      "<elem attr=\"{(1,2)}{3}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"1 23\"/>", false)
    );
  }

  /**
   *  entity references in attribute content .
   */
  @org.junit.Test
  public void constrAttrEntref1() {
    final XQuery query = new XQuery(
      "<elem attr=\"&amp;&lt;&gt;\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"&amp;&lt;&gt;\"/>", false)
    );
  }

  /**
   *  entity references in attribute content .
   */
  @org.junit.Test
  public void constrAttrEntref2() {
    final XQuery query = new XQuery(
      "fn:string-length(string((<elem attr=\"&amp;&lt;&gt;\"/>)/@attr))",
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
   *  attribute xml:id - content is ncname .
   */
  @org.junit.Test
  public void constrAttrId1() {
    final XQuery query = new XQuery(
      "<elem xml:id=\"ncname\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xml:id=\"ncname\"/>", false)
    );
  }

  /**
   *  attribute xml:id - content is to be further normalized .
   */
  @org.junit.Test
  public void constrAttrId2() {
    final XQuery query = new XQuery(
      "<elem xml:id=\" a{'b c d',' '}\"/>",
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
        assertSerialization("<elem xml:id=\"ab c d\"/>", false)
      ||
        error("XQDY0091")
      )
    );
  }

  /**
   *  namespace declaration does not count as attribute .
   */
  @org.junit.Test
  public void constrAttrNsdecl1() {
    final XQuery query = new XQuery(
      "fn:count((<elem xmlns:foo=\"http://ns.example.com/uri\"/>)/@*)",
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
   *  namespace default declaration does not count as attribute .
   */
  @org.junit.Test
  public void constrAttrNsdecl2() {
    final XQuery query = new XQuery(
      "fn:count((<elem xmlns=\"http://ns.example.com/uri\"/>)/@*)",
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
   *  attribute prefix declared in prolog .
   */
  @org.junit.Test
  public void constrAttrNspre1() {
    final XQuery query = new XQuery(
      "declare namespace foo=\"http://www.w3.org/XQueryTest/Construct\"; <elem foo:attr=\"value\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xmlns:foo=\"http://www.w3.org/XQueryTest/Construct\" foo:attr=\"value\"/>", false)
    );
  }

  /**
   *  attribute prefix declared in parent element .
   */
  @org.junit.Test
  public void constrAttrNspre2() {
    final XQuery query = new XQuery(
      "<elem xmlns:foo=\"http://www.w3.org/XQueryTest/Construct\"><child foo:attr=\"value\"/></elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xmlns:foo=\"http://www.w3.org/XQueryTest/Construct\"><child foo:attr=\"value\"/></elem>", false)
    );
  }

  /**
   *  attribute prefix declared in same element before .
   */
  @org.junit.Test
  public void constrAttrNsprein1() {
    final XQuery query = new XQuery(
      "<elem xmlns:foo=\"http://www.w3.org/XQueryTest/Construct\" foo:attr=\"value\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xmlns:foo=\"http://www.w3.org/XQueryTest/Construct\" foo:attr=\"value\"/>", false)
    );
  }

  /**
   *  attribute prefix declared in same element after .
   */
  @org.junit.Test
  public void constrAttrNsprein2() {
    final XQuery query = new XQuery(
      "<elem foo:attr=\"value\" xmlns:foo=\"http://www.w3.org/XQueryTest/Construct\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xmlns:foo=\"http://www.w3.org/XQueryTest/Construct\" foo:attr=\"value\"/>", false)
    );
  }

  /**
   *  namespace prefix used in content before it is declared .
   */
  @org.junit.Test
  public void constrAttrNsprein3() {
    final XQuery query = new XQuery(
      "<elem att=\"{<p:e/>/namespace-uri()}\" xmlns:p=\"http://ns.example.com/uri\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem att=\"http://ns.example.com/uri\" xmlns:p=\"http://ns.example.com/uri\"/>", false)
    );
  }

  /**
   *  namespace prefix used deeply nested in content before it is declared .
   */
  @org.junit.Test
  public void constrAttrNsprein4() {
    final XQuery query = new XQuery(
      "<elem att=\"{<e2 a2=\"{<e3 a3=\"{<p:e/>/namespace-uri()}\"></e3>/@a3}\"></e2>/@a2}\" \n" +
      "                                     xmlns:p=\"http://ns.example.com/uri\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem att=\"http://ns.example.com/uri\" xmlns:p=\"http://ns.example.com/uri\"/>", false)
    );
  }

  /**
   *  Attribute parent element .
   */
  @org.junit.Test
  public void constrAttrParent1() {
    final XQuery query = new XQuery(
      "for $x in <elem attr=\"value\"/> return $x is $x/@attr/..",
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
   *  Single attribute .
   */
  @org.junit.Test
  public void constrAttrSyntax1() {
    final XQuery query = new XQuery(
      "<elem attr=\"value\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"value\"/>", false)
    );
  }

  /**
   *  Whitespace after last attribute .
   */
  @org.junit.Test
  public void constrAttrSyntax10() {
    final XQuery query = new XQuery(
      "<elem attr=\"value\" />",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"value\"/>", false)
    );
  }

  /**
   *  Multiple attributes .
   */
  @org.junit.Test
  public void constrAttrSyntax2() {
    final XQuery query = new XQuery(
      "<elem attr1=\"val1\" attr2=\"val2\" attr3=\"val3\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr1=\"val1\" attr2=\"val2\" attr3=\"val3\"/>", false)
    );
  }

  /**
   *  Single quotes for attribute .
   */
  @org.junit.Test
  public void constrAttrSyntax3() {
    final XQuery query = new XQuery(
      "<elem attr='value'/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"value\"/>", false)
    );
  }

  /**
   *  Escaped single quote .
   */
  @org.junit.Test
  public void constrAttrSyntax4() {
    final XQuery query = new XQuery(
      "<elem attr=''''/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"'\"/>", false)
    );
  }

  /**
   *  Escaped double quote .
   */
  @org.junit.Test
  public void constrAttrSyntax5() {
    final XQuery query = new XQuery(
      "<elem attr=\"\"\"\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"&quot;\"/>", false)
    );
  }

  /**
   *  Mismatched quotes .
   */
  @org.junit.Test
  public void constrAttrSyntax6() {
    final XQuery query = new XQuery(
      "<elem attr='value\"/>",
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
   *  Mismatched quotes .
   */
  @org.junit.Test
  public void constrAttrSyntax7() {
    final XQuery query = new XQuery(
      "<elem attr=\"value'/>",
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
   *  Whitespace between attributes .
   */
  @org.junit.Test
  public void constrAttrSyntax8() {
    final XQuery query = new XQuery(
      "<elem attr1=\"val1\" attr2=\"val2\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr1=\"val1\" attr2=\"val2\"/>", false)
    );
  }

  /**
   *  Whitespace in attribute definition .
   */
  @org.junit.Test
  public void constrAttrSyntax9() {
    final XQuery query = new XQuery(
      "<elem attr = \"value\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"value\"/>", false)
    );
  }

  /**
   *  Attribute normalization line feed .
   */
  @org.junit.Test
  public void constrAttrWs1() {
    final XQuery query = new XQuery(
      "<elem attr=\" \"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\" \"/>", false)
    );
  }

  /**
   *  Attribute normalization tab .
   */
  @org.junit.Test
  public void constrAttrWs2() {
    final XQuery query = new XQuery(
      "<elem attr=\" \"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\" \"/>", false)
    );
  }

  /**
   *  Attribute normalization char ref &#xd; .
   */
  @org.junit.Test
  public void constrAttrWs3() {
    final XQuery query = new XQuery(
      "<elem attr=\"&#xd;\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"&#xD;\"/>", false)
    );
  }

  /**
   *  Attribute normalization char ref &#xa; .
   */
  @org.junit.Test
  public void constrAttrWs4() {
    final XQuery query = new XQuery(
      "<elem attr=\"&#xa;\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"&#xA;\"/>", false)
    );
  }

  /**
   *  Attribute normalization char ref &#x9; .
   */
  @org.junit.Test
  public void constrAttrWs5() {
    final XQuery query = new XQuery(
      "<elem attr=\"&#x9;\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"&#x9;\"/>", false)
    );
  }

  /**
   *  Namespace declaration attributes in direct element constructors. .
   */
  @org.junit.Test
  public void directConElemAttr1() {
    final XQuery query = new XQuery(
      "<shoe name=\" \"\"\"\" \"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<shoe name=\" &#34;&#34; \"/>", false)
    );
  }

  /**
   *  Namespace declaration attributes in direct element constructors. .
   */
  @org.junit.Test
  public void directConElemAttr2() {
    final XQuery query = new XQuery(
      "<shoe name=\" '''''''' \"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<shoe name=\" '''''''' \"/>", false)
    );
  }

  /**
   *  Comments cannot appear inside direct element constructors. a misplaced comment .
   */
  @org.junit.Test
  public void k2DirectConElemAttr1() {
    final XQuery query = new XQuery(
      "<ncname (:a misplaced comment:)/>",
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
   *  Syntax error in direct attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr10() {
    final XQuery query = new XQuery(
      "<foo attr=\"content<content\"/>",
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
   *  Syntax error in direct attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr11() {
    final XQuery query = new XQuery(
      "<foo attr=\"content}content\"/>",
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
   *  Syntax error in direct attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr12() {
    final XQuery query = new XQuery(
      "<foo attr=\"content{1\"/>",
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
   *  Syntax error in direct attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr13() {
    final XQuery query = new XQuery(
      "<foo attr=\"{{{\"",
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
   *  Syntax error in direct attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr14() {
    final XQuery query = new XQuery(
      "<foo attr=\"{\"",
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
   *  Syntax error in direct attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr15() {
    final XQuery query = new XQuery(
      "<foo attr=\"{",
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
   *  Syntax error in direct attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr16() {
    final XQuery query = new XQuery(
      "<e attr=\"content}\"/>",
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
   *  Syntax error in direct attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr17() {
    final XQuery query = new XQuery(
      "<foo attr=\"",
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
   *  Syntax error in direct attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr18() {
    final XQuery query = new XQuery(
      "<foo attr=\"<foo/>\"/>",
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
   *  Syntax error in direct attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr19() {
    final XQuery query = new XQuery(
      "<foo attr=\"<?target content?>\"/>",
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
   *  Comments cannot appear inside direct element constructors. a misplaced comment .
   */
  @org.junit.Test
  public void k2DirectConElemAttr2() {
    final XQuery query = new XQuery(
      "<(:a misplaced comment:)ncname/>",
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
   *  Syntax error in direct attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr20() {
    final XQuery query = new XQuery(
      "<foo attr=\"<!-- a comment-->\"/>",
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
   *  Syntax error in direct attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr21() {
    final XQuery query = new XQuery(
      "<foo attr=\"<![CDATA[content]]>\"/>",
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
   *  Syntax error in direct attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr22() {
    final XQuery query = new XQuery(
      "<foo attr=",
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
   *  Syntax error in direct attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr23() {
    final XQuery query = new XQuery(
      "<elem attr=\"content'/>",
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
   *  Syntax error in direct attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr24() {
    final XQuery query = new XQuery(
      "<elem attr='content\"/>",
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
   *  Comments cannot appear inside direct element/attribute constructors. comment .
   */
  @org.junit.Test
  public void k2DirectConElemAttr25() {
    final XQuery query = new XQuery(
      "<foo (:comment :)/>",
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
   *  Comments cannot appear inside direct element/attribute constructors. comment .
   */
  @org.junit.Test
  public void k2DirectConElemAttr26() {
    final XQuery query = new XQuery(
      "<foo attr=(:comment:)\"value\" />",
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
   *  Comments cannot appear inside direct element/attribute constructors. comment .
   */
  @org.junit.Test
  public void k2DirectConElemAttr27() {
    final XQuery query = new XQuery(
      "<foo attr(:comment:)=\"value\" />",
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
   *  Comments cannot appear inside direct element/attribute constructors. comment .
   */
  @org.junit.Test
  public void k2DirectConElemAttr28() {
    final XQuery query = new XQuery(
      "<foo attr=\"value\" (:comment:) attr2=\"value\" />",
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
   *  Attribute containing two quotes. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr29() {
    final XQuery query = new XQuery(
      "string(<foo attr=\"\"\"\"/>/@attr)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "\"")
    );
  }

  /**
   *  Space is not allowed between '</' and the element name. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr3() {
    final XQuery query = new XQuery(
      "<ncname></ ncname>",
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
   *  Attribute containing two quotes(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr30() {
    final XQuery query = new XQuery(
      "string(<foo attr='\"\"'/>/@attr)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "\"\"")
    );
  }

  /**
   *  Attribute containing two apostrophes. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr31() {
    final XQuery query = new XQuery(
      "string(<foo attr=\"''\"/>/@attr)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "''")
    );
  }

  /**
   *  Attribute containing two apostrophes(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr32() {
    final XQuery query = new XQuery(
      "string(<foo attr=''''/>/@attr)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "'")
    );
  }

  /**
   *  Test that simple content computation is done properly with complex input sequence. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr33() {
    final XQuery query = new XQuery(
      "<e attr=\"x{<e>a</e>, <e>b</e>, <e>c</e>, 1, 2, 3}y\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e attr=\"xa b c 1 2 3y\"/>", false)
    );
  }

  /**
   *  Extract the string value from a directly constructed attribute whose content consists of a computed comment constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr34() {
    final XQuery query = new XQuery(
      "string(<elem attr=\"{comment {\" content \"}}\"/>/@attr)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, " content ")
    );
  }

  /**
   *  Extract the string value from a directly constructed attribute whose content consists of a computed comment constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr35() {
    final XQuery query = new XQuery(
      "local-name(<elem attr=\"{comment {\" content \"}}\"/>/@attr)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "attr")
    );
  }

  /**
   *  Extract the string value from a directly constructed attribute whose content consists of a computed processing-instruction constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr36() {
    final XQuery query = new XQuery(
      "string(<elem attr=\"{processing-instruction name {\" content \"}}\"/>/@attr)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "content ")
    );
  }

  /**
   *  Extract the local-name from a directly constructed attribute whose content consists of a computed processing-instruction constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr37() {
    final XQuery query = new XQuery(
      "local-name(<elem attr=\"{processing-instruction name {\" content \"}}\"/>/@attr)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "attr")
    );
  }

  /**
   *  Extract the string value from a directly constructed attribute whose content consists of a computed attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr38() {
    final XQuery query = new XQuery(
      "local-name(<elem attr=\"{attribute name {\" content \"}}\"/>/@attr)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "attr")
    );
  }

  /**
   *  Extract the string value from a directly constructed attribute whose content consists of a computed attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr39() {
    final XQuery query = new XQuery(
      "string(<elem attr=\"{attribute name {\" content \"}}\"/>/@attr)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, " content ")
    );
  }

  /**
   *  Comments are not allowed where whitespace is, in direct element constructors. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr4() {
    final XQuery query = new XQuery(
      "<ncname></ncname (:a misplaced comment:)>",
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
        error("XPST0003")
      ||
        error("XQST0118")
      )
    );
  }

  /**
   *  Attributes with many apostrophes and quotes. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr40() {
    final XQuery query = new XQuery(
      "<elem attr1=\"\"\"\" attr2='''' attr3=\"''\" attr4='\"\"' attr5=\"'\" attr6='\"'/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr1=\"&quot;\" attr2=\"'\" attr3=\"''\" attr4=\"&quot;&quot;\" attr5=\"'\" attr6=\"&quot;\"/>", false)
    );
  }

  /**
   *  A direct attribute constructor taking a direct element constructor as input. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr41() {
    final XQuery query = new XQuery(
      "<foo attr=\"{<foo attr=\"foo\"/>}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<foo attr=\"\"/>", false)
    );
  }

  /**
   *  A direct attribute constructor taking a direct comment constructor as input. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr42() {
    final XQuery query = new XQuery(
      "<foo attr=\"{<!-- comment -->}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<foo attr=\" comment \"/>", false)
    );
  }

  /**
   *  A direct attribute constructor taking a direct PI constructor as input. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr43() {
    final XQuery query = new XQuery(
      "<foo attr=\"{<?target dat a ?>}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<foo attr=\"dat a \"/>", false)
    );
  }

  /**
   *  xml:base attributes do not affect the static base-uri. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr44() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.com/level/file.ext\"; <e xml:base=\"../\">{ static-base-uri()}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xml:base=\"../\">http://example.com/level/file.ext</e>", false)
    );
  }

  /**
   *  xml:base attributes do not affect the static base-uri(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr45() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.com/level/file.ext\"; <e xml:base=\"http://example.com/2/2\">{ static-base-uri()}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xml:base=\"http://example.com/2/2\">http://example.com/level/file.ext</e>", false)
    );
  }

  /**
   *  Copy attributes from one element to another. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr46() {
    final XQuery query = new XQuery(
      "<e> { <b attr=\"fo\" a=\"bo\"/>/@* } </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e attr=\"fo\" a=\"bo\"/>", false)
    );
  }

  /**
   *  Ensure the typed value of directly constructed attributes has the correct type. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr47() {
    final XQuery query = new XQuery(
      "data(<e foo=\"content\"/>/@*) instance of xs:untypedAtomic",
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
   *  There must be space between attributes. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr48() {
    final XQuery query = new XQuery(
      "<a foo=\"1\"fb=\"1\"/>",
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
   *  A newline is a valid separator between two attributes. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr49() {
    final XQuery query = new XQuery(
      "<a b=\"1\" c=\"1\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a b=\"1\" c=\"1\"/>", false)
    );
  }

  /**
   *  Whitespace is allowed in the end tag after the QName. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr5() {
    final XQuery query = new XQuery(
      "<ncname>content</ncname > = 'content'",
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
   *  A tab is a valid separator between two attributes. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr50() {
    final XQuery query = new XQuery(
      "<a b=\"1\" c=\"1\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a b=\"1\" c=\"1\"/>", false)
    );
  }

  /**
   *  There must be space between attributes(using apostrophes). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr51() {
    final XQuery query = new XQuery(
      "<a foo='1'fb='1'/>",
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
   *  A tab is a valid separator between two attributes(using apostrophes). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr52() {
    final XQuery query = new XQuery(
      "<a b='1' c='1'/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a b=\"1\" c=\"1\"/>", false)
    );
  }

  /**
   *  '/' is an invalid separator between two attributes. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr53() {
    final XQuery query = new XQuery(
      "<a b=\"1\"/a=\"1\"/>",
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
   *  '/' is an invalid separator between two attributes(using apostrophes). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr54() {
    final XQuery query = new XQuery(
      "<a b='1'/a='1'/>",
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
   *  '/' is an invalid separator between two attributes. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr55() {
    final XQuery query = new XQuery(
      "<a b=\"1\">a=\"1\"/>",
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
   *  '/' is an invalid separator between two attributes(using apostrophes). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr56() {
    final XQuery query = new XQuery(
      "<a b='1'>a='1'/>",
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
   *  End unexpectedly inside a direct element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr57() {
    final XQuery query = new XQuery(
      "<a attr=\"content\"",
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
   *  End unexpectedly inside a direct element constructor(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr58() {
    final XQuery query = new XQuery(
      "<a attr='content'",
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
   *  End unexpectedly inside a direct element constructor(#3). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr59() {
    final XQuery query = new XQuery(
      "<a attr=\"content\"",
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
   *  XML tags must be balanced. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr6() {
    final XQuery query = new XQuery(
      "<ncname></ncnameNOTBALANCED>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0118")
    );
  }

  /**
   *  End unexpectedly inside a direct element constructor(#4). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr60() {
    final XQuery query = new XQuery(
      "<a attr='content'",
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
   *  End unexpectedly inside a direct element constructor(#5). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr61() {
    final XQuery query = new XQuery(
      "<a attr='con",
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
   *  End unexpectedly inside a direct element constructor(#6). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr62() {
    final XQuery query = new XQuery(
      "<a attr=\"con",
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
   *  End unexpectedly inside a direct element constructor(#7). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr63() {
    final XQuery query = new XQuery(
      "<a attr=",
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
   *  End unexpectedly inside a direct element constructor(#8). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr64() {
    final XQuery query = new XQuery(
      "<a attr=",
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
   *  End unexpectedly inside a direct element constructor(#9). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr65() {
    final XQuery query = new XQuery(
      "<a attr",
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
   *  End unexpectedly inside a direct element constructor(#10). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr66() {
    final XQuery query = new XQuery(
      "<a b='1'/",
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
   *  End unexpectedly inside a direct element constructor(#10). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr67() {
    final XQuery query = new XQuery(
      "<a b='1'/",
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
   *  Use two namespace declarations with the same name. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr68() {
    final XQuery query = new XQuery(
      "<e xmlns=\"\" xmlns=\"\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0071")
    );
  }

  /**
   *  Use two namespace declarations with the same name, in addition to doing invalid bindings. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr69() {
    final XQuery query = new XQuery(
      "<e xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0071")
    );
  }

  /**
   *  CDATA sections can only appear inside element constructors. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr7() {
    final XQuery query = new XQuery(
      "<![CDATA[a string]]>",
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
   *  Ensure that attributes constructed with direct constructors doesn't pick up the default namespace. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr70() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://typedecl\"; namespace-uri-from-QName(node-name(exactly-one(<e attr=\"foo\"/>/@attr))) eq \"\"",
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
   *  Ensure that attributes constructed with computed constructors doesn't pick up the default namespace. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr71() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://typedecl\"; namespace-uri-from-QName(node-name(exactly-one(<e>{attribute attr {()} }/</e>/@attr))) eq \"\"",
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
   *  Ensure that attributes constructed with computed constructors doesn't pick up the default namespace. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr72() {
    final XQuery query = new XQuery(
      "namespace-uri-from-QName(node-name(exactly-one(<e xmlns=\"http://example.com/\">{attribute attr {()} }/</e>/@attr))) eq \"\"",
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
   *  Ensure that attributes constructed with computed constructors doesn't pick up the default namespace(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemAttr73() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://example.com/\"; namespace-uri-from-QName(node-name(attribute e {()})) eq \"\"",
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
   *  Check that character references next to embedded expressions are parsed correctly. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr74() {
    final XQuery query = new XQuery(
      "<e attr=\"{1}&#86;{1}&#86;\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e attr=\"1V1V\"/>", false)
    );
  }

  /**
   *  Mix several ways for creating text for various kinds of nodes. This test is useful if an implementation is performing normalization of such constructors. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr75() {
    final XQuery query = new XQuery(
      queryFile(
        file(
            "prod/DirAttributeList/K2-DirectConElemAttr-75.xq"
        )
      ),
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e attribute=\"abc def ghi 1 2 3 abc a text node a text node def ghi FF abc def ghi textNode xs:string textNode xs:string textNode xs:string text a text node, xs:string xs:stringtextnode\" name=\"a text node a text node abc def a text node ghi 1 2 a text node 3 abc def a text node ghi FF abc def ghi xs:string ghi xs:string xs:string xs:string ghi\">a text nodea text nodeabc defa text nodeghi 1 2a text node3 abca text nodedef ghi FF abc def ghi\ntextNode\nxs:string\ntextNode\nxs:string\ntextNode\nxs:stringxs:stringxs:string\ntext a text node,\ntext a text node,\ntext a text node,\ntext a text node,\n\n<!--abc def ghi 1 2 3 abc def ghi FF abc def ghi xs:string xs:string ghi xs:string a text node a text node xs:string ghi xs:string--><?target abc def ghi 1 2 3 abc def ghi a text node a text node FF abc def ghi xs:string xs:string ghi xs:string xs:string ghi a text node xs:string?>a text node a text node a text node a text node abc def ghi 1 2 a text node 3 abc a text node def ghi FF abc def ghi xs:string xs:string ghi xs:string xs:string ghi xs:string a text node</e>", false)
    );
  }

  /**
   *  Pass text nodes through a function and variable, into AVTs. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr76() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:t() { text{\"\"}, text{\"[\"}, text{\"3\"}, text{\"]\"}, text{\"\"} }; \n" +
      "        declare variable $var := (text{\"\"}, text{\"[\"}, text{\"3\"}, text{\"]\"}, text{\"\"}); \n" +
      "        <out fromFunction=\"{local:t()}\" fromVariable=\"{$var}\"/>\n" +
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
      assertSerialization("<out fromFunction=\" [ 3 ] \" fromVariable=\" [ 3 ] \"/>", false)
    );
  }

  /**
   *  Syntax error in direct attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr8() {
    final XQuery query = new XQuery(
      "<foo attr=\"\"\"/>",
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
   *  Syntax error in direct attribute constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemAttr9() {
    final XQuery query = new XQuery(
      "<foo attr='''/>",
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
}
