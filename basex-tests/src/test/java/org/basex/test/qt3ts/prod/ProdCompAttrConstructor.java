package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the CompAttrConstructor (computed attribute constructor) production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdCompAttrConstructor extends QT3TestSet {

  /**
   *  empty computed name .
   */
  @org.junit.Test
  public void constrCompattrCompname1() {
    final XQuery query = new XQuery(
      "element elem {attribute {()} {'text'}}",
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
  public void constrCompattrCompname10() {
    final XQuery query = new XQuery(
      "element elem {attribute {'attr'} {'text'}}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"text\"/>", false)
    );
  }

  /**
   *  string as name .
   */
  @org.junit.Test
  public void constrCompattrCompname11() {
    final XQuery query = new XQuery(
      "element elem {attribute {'attr', ()} {'text'}}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"text\"/>", false)
    );
  }

  /**
   *  string as name .
   */
  @org.junit.Test
  public void constrCompattrCompname12() {
    final XQuery query = new XQuery(
      "element elem {attribute {(), 'attr'} {'text'}}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"text\"/>", false)
    );
  }

  /**
   *  string with prefix as name .
   */
  @org.junit.Test
  public void constrCompattrCompname13() {
    final XQuery query = new XQuery(
      "<elem xmlns:foo=\"http://www.example.com/foo\">{element elem {attribute {'foo:attr'} {}}}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xmlns:foo=\"http://www.example.com/foo\"><elem foo:attr=\"\"/></elem>", false)
    );
  }

  /**
   *  string with undeclared prefix as name .
   */
  @org.junit.Test
  public void constrCompattrCompname14() {
    final XQuery query = new XQuery(
      "element elem {attribute {'foo:attr'} {}}",
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
  public void constrCompattrCompname15() {
    final XQuery query = new XQuery(
      "element elem {attribute {xs:untypedAtomic('attr')} {'text'}}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"text\"/>", false)
    );
  }

  /**
   *  untyped atomic with prefix as name .
   */
  @org.junit.Test
  public void constrCompattrCompname16() {
    final XQuery query = new XQuery(
      "<elem xmlns:foo=\"http://www.example.com/foo\">{attribute {xs:untypedAtomic('foo:attr')} {'text'}}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xmlns:foo=\"http://www.example.com/foo\" foo:attr=\"text\"/>", false)
    );
  }

  /**
   *  untyped atomic with undeclared prefix as name .
   */
  @org.junit.Test
  public void constrCompattrCompname17() {
    final XQuery query = new XQuery(
      "element elem {attribute {xs:untypedAtomic('foo:elem')} {'text'}}",
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
  public void constrCompattrCompname18() {
    final XQuery query = new XQuery(
      "element elem {attribute {'el em'} {'text'}}",
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
  public void constrCompattrCompname19() {
    final XQuery query = new XQuery(
      "element elem {attribute {xs:untypedAtomic('el em')} {'text'}}",
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
  public void constrCompattrCompname2() {
    final XQuery query = new XQuery(
      "element elem {attribute {'one', 'two'} {'text'}}",
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
  public void constrCompattrCompname3() {
    final XQuery query = new XQuery(
      "element elem {attribute {xs:untypedAtomic('one'), xs:untypedAtomic('two')} {'text'}}",
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
  public void constrCompattrCompname4() {
    final XQuery query = new XQuery(
      "element elem {attribute {//a} {'text'}}",
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
  public void constrCompattrCompname5() {
    final XQuery query = new XQuery(
      "element elem {attribute {1,2} {'text'}}",
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
  public void constrCompattrCompname6() {
    final XQuery query = new XQuery(
      "element elem {attribute {123} {'text'}}",
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
  public void constrCompattrCompname7() {
    final XQuery query = new XQuery(
      "element elem {attribute {xs:dateTime(\"1999-05-31T13:20:00\")} {'text'}}",
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
  public void constrCompattrCompname9() {
    final XQuery query = new XQuery(
      "element elem {attribute {xs:QName('aQname')} {'text'}}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem aQname=\"text\"/>", false)
    );
  }

  /**
   *  typed value of element .
   */
  @org.junit.Test
  public void constrCompattrData1() {
    final XQuery query = new XQuery(
      "fn:data(attribute attr {'a', element a {}, 'b'})",
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
  public void constrCompattrDoc1() {
    final XQuery query = new XQuery(
      "element elem {attribute attr {., .}}",
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
      assertSerialization("<elem attr=\"texttext texttext\"/>", false)
    );
  }

  /**
   *  enclosed expression in attribute content - atomic values .
   */
  @org.junit.Test
  public void constrCompattrEnclexpr1() {
    final XQuery query = new XQuery(
      "element elem {attribute attr {1,'string',3.14,xs:float('1.2345e-2'),xs:dateTime('2002-04-02T12:00:00-01:00')}}",
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
  public void constrCompattrEnclexpr2() {
    final XQuery query = new XQuery(
      "element elem {attribute attr {<elem>123</elem>, (<elem attr='456'/>)/@attr, (<elem>789</elem>)/text()}}",
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
  public void constrCompattrEnclexpr3() {
    final XQuery query = new XQuery(
      "element elem {attribute attr {1,'',2}}",
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
  public void constrCompattrEnclexpr4() {
    final XQuery query = new XQuery(
      "element elem {attribute attr {1,<a/>,2}}",
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
   *  attribute xml:id - content is ncname .
   */
  @org.junit.Test
  public void constrCompattrId1() {
    final XQuery query = new XQuery(
      "element elem {attribute xml:id {\"ncname\"}}",
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
  public void constrCompattrId2() {
    final XQuery query = new XQuery(
      "element elem {attribute xml:id {\" ab c d \"}}",
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
   *  NCName for computed attribute constructor .
   */
  @org.junit.Test
  public void constrCompattrName1() {
    final XQuery query = new XQuery(
      "element elem {attribute attr {'text'}}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"text\"/>", false)
    );
  }

  /**
   *  QName for computed attribute constructor .
   */
  @org.junit.Test
  public void constrCompattrName2() {
    final XQuery query = new XQuery(
      "declare namespace foo=\"http://www.example.com/foo\"; element elem {attribute foo:attr {'text'}}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xmlns:foo=\"http://www.example.com/foo\" foo:attr=\"text\"/>", false)
    );
  }

  /**
   *  QName with undeclared prefix for computed attribute constructor .
   */
  @org.junit.Test
  public void constrCompattrName3() {
    final XQuery query = new XQuery(
      "element elem {attribute foo:attr {'text'}}",
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
   *  empty parent .
   */
  @org.junit.Test
  public void constrCompattrParent1() {
    final XQuery query = new XQuery(
      "count((attribute attr {})/..)",
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
   *  string value of attribute .
   */
  @org.junit.Test
  public void constrCompattrString1() {
    final XQuery query = new XQuery(
      "fn:string(attribute attr {'a', element a {}, 'b'})",
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
   *  The name can't be specified as a string literal. .
   */
  @org.junit.Test
  public void k2ComputeConAttr1() {
    final XQuery query = new XQuery(
      "attribute \"name\" {\"content\"}",
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
   *  Ensure XQTY0024 is not issued when appearing nested. .
   */
  @org.junit.Test
  public void k2ComputeConAttr10() {
    final XQuery query = new XQuery(
      "<a> <?target content?> {<b>{attribute name{\"content\"}}</b>} </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a><?target content?><b name=\"content\"/></a>", false)
    );
  }

  /**
   *  Ensure XQTY0024 is not issued when a predicate is used to filter the children. .
   */
  @org.junit.Test
  public void k2ComputeConAttr11() {
    final XQuery query = new XQuery(
      "<a>{(<?target content?>, attribute name{\"content\"})[2]} </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a name=\"content\"/>", false)
    );
  }

  /**
   *  Ensure XQTY0024 is issued when content appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr12() {
    final XQuery query = new XQuery(
      "<a> <!-- content --> {attribute name{\"content\"}} </a>",
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
   *  Ensure XQTY0024 is issued when content appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr13() {
    final XQuery query = new XQuery(
      "<foo > <doo/> {attribute name {\"content\"}} </foo>",
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
   *  Ensure XQTY0024 is issued when content, set via a user declared function, appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr14() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() { <a/> }; <b> {local:myFunc()} {attribute name {\"content\"}} </b>",
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
   *  Ensure XQTY0024 is issued when content, set via a user declared function, appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr15() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() { comment {\"content\"} }; <b> {local:myFunc()} {attribute name {\"content\"}} </b>",
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
   *  Ensure XQTY0024 is issued when content, set via a user declared function, appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr16() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() as element()+ { <a/> }; <b> {local:myFunc()} {attribute name {\"content\"}} </b>",
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
   *  Ensure XQTY0024 is issued when content, set via a user declared function, appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr17() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() as item() { <a/> }; <b> {local:myFunc()} {attribute name {\"content\"}} </b>",
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
   *  Ensure XQTY0024 is issued when content, set via a user declared function, appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr18() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() as item() { <a/> }; <b> {local:myFunc()} {attribute name {\"content\"}} </b>",
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
   *  Ensure XQTY0024 is issued when content, set via a user declared, recursive function, appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr19() {
    final XQuery query = new XQuery(
      "declare function local:myFunc($recurse as xs:integer) { <nested> { if ($recurse = 0) then () else local:myFunc($recurse - 1) } </nested> }; <b> {local:myFunc(3)} {attribute name {\"content\"}} </b>",
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
   *  Ensure XQTY0024 is issued when content appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr2() {
    final XQuery query = new XQuery(
      "<elem> <?target content ?> {attribute name {\"content\"}} </elem>",
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
   *  Ensure XQTY0024 is issued when content, set via a user declared, recursive function, appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr20() {
    final XQuery query = new XQuery(
      "declare function local:myFunc($recurse as xs:integer) as item() { <nested> { if ($recurse = 0) then () else local:myFunc($recurse - 1) } </nested> }; <b> {local:myFunc(3)} {attribute name {\"content\"}} </b>",
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
   *  Ensure XQTY0024 is issued when content, set via a user declared, recursive function, appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr21() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() { <elem/>, attribute name {\"content\"} }; <b> {local:myFunc()} </b>",
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
   *  Ensure XQTY0024 is issued when content, set via a user declared variable, appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr22() {
    final XQuery query = new XQuery(
      "declare variable $myVar := (<elem/>, attribute name {\"content\"}); <b> {$myVar} </b>",
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
   *  Ensure XQTY0024 is not issued when a predicate avoid the condition. .
   */
  @org.junit.Test
  public void k2ComputeConAttr23() {
    final XQuery query = new XQuery(
      "declare variable $myVar := (<elem/>, attribute name {\"content\"}); <b> {$myVar[2]} </b>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b name=\"content\"/>", false)
    );
  }

  /**
   *  Ensure XQTY0024 is issued when content, set via a user declared variable, appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr24() {
    final XQuery query = new XQuery(
      "declare variable $myVar := (attribute name {\"content\"}, <elem/>); <b> {$myVar[2]} </b>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b><elem/></b>", false)
    );
  }

  /**
   *  Add many attributes with a recursive user function. .
   */
  @org.junit.Test
  public void k2ComputeConAttr25() {
    final XQuery query = new XQuery(
      "declare function local:myFunc($recurse as xs:integer) { attribute {concat(\"name\", $recurse)} {\"content\"} , if ($recurse = 0) then () else local:myFunc($recurse - 1) }; <b> {local:myFunc(2)} {attribute name {\"content\"}} </b>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b name2=\"content\" name1=\"content\" name0=\"content\" name=\"content\"/>", false)
    );
  }

  /**
   *  Add many attributes with a recursive user function. .
   */
  @org.junit.Test
  public void k2ComputeConAttr26() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() { attribute name {\"content\"}, <elem/> }; <b> {local:myFunc()} </b>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b name=\"content\"><elem/></b>", false)
    );
  }

  /**
   *  Add many attributes with a recursive user function. .
   */
  @org.junit.Test
  public void k2ComputeConAttr27() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() { <elem/>, attribute name {\"content\"} }; <b> {local:myFunc()[2]} </b>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b name=\"content\"/>", false)
    );
  }

  /**
   *  Ensure XQTY0024 is issued when content, set via a user declared variable, appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr28() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := <a/>; <b> {$local:myVar} {attribute name {\"content\"}} </b>",
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
   *  Ensure XQTY0024 is issued when content, set via a user declared variable, appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr29() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar as item() := <a/>; <b> {$local:myVar} {attribute name {\"content\"}} </b>",
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
   *  Ensure XQTY0024 is issued when content appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr3() {
    final XQuery query = new XQuery(
      "<elem> {\"a string\", attribute name {\"content\"}} </elem>",
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
   *  Add an attribute from a variable to an element. .
   */
  @org.junit.Test
  public void k2ComputeConAttr30() {
    final XQuery query = new XQuery(
      "declare variable $local:myVar := attribute n2 {\"content\"}; <b> {$local:myVar} {attribute name {\"content\"}} </b>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b n2=\"content\" name=\"content\"/>", false)
    );
  }

  /**
   *  Add an attribute from a function to an element. .
   */
  @org.junit.Test
  public void k2ComputeConAttr31() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() { attribute n2 {\"content\"} }; <b> {local:myFunc()} {attribute name {\"content\"}} </b>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b n2=\"content\" name=\"content\"/>", false)
    );
  }

  /**
   *  Add two computed attributes, where one gets it content from an element. .
   */
  @org.junit.Test
  public void k2ComputeConAttr32() {
    final XQuery query = new XQuery(
      "<e> { attribute name {<anElement/>}, attribute name2 {\"content\"} } </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e name=\"\" name2=\"content\"/>", false)
    );
  }

  /**
   *  Add two computed attributes, where one gets it content from an atomic value. .
   */
  @org.junit.Test
  public void k2ComputeConAttr33() {
    final XQuery query = new XQuery(
      "<e> { attribute name {\"content\"}, attribute name2 {\"content\"} } </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e name=\"content\" name2=\"content\"/>", false)
    );
  }

  /**
   *  Add two computed attributes, where one gets it content from an atomic value. .
   */
  @org.junit.Test
  public void k2ComputeConAttr34() {
    final XQuery query = new XQuery(
      "<e> { attribute name {xs:hexBinary(\"ff\")}, attribute name2 {\"content\"} } </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e name=\"FF\" name2=\"content\"/>", false)
    );
  }

  /**
   *  Empty CDATA sections generate no text nodes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr35() {
    final XQuery query = new XQuery(
      "<elem><![CDATA[]]>{attribute name {\"content\"}}<alem/> </elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem name=\"content\"><alem/></elem>", false)
    );
  }

  /**
   *  Empty CDATA sections generate no text nodes(#2). .
   */
  @org.junit.Test
  public void k2ComputeConAttr36() {
    final XQuery query = new XQuery(
      "count(<elem><![CDATA[]]></elem>/text())",
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
   *  This query yields XPST0081 because the prefix 'xmlns' is unbound. .
   */
  @org.junit.Test
  public void k2ComputeConAttr37() {
    final XQuery query = new XQuery(
      "attribute xmlns:localName {\"content\"}",
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
   *  This query yields XPST0081 because the prefix 'xmlns' is unbound(#2). .
   */
  @org.junit.Test
  public void k2ComputeConAttr38() {
    final XQuery query = new XQuery(
      "attribute {\"xmlns:localName\"} {\"content\"}",
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
        error("XQDY0044")
      ||
        error("XQDY0074")
      )
    );
  }

  /**
   *  This query yields XPST0081 because the prefix 'aPrefix' is unbound. .
   */
  @org.junit.Test
  public void k2ComputeConAttr39() {
    final XQuery query = new XQuery(
      "attribute aPrefix:localName {\"content\"}",
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
   *  Ensure XQTY0024 is issued when content appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr4() {
    final XQuery query = new XQuery(
      "<elem> {\"a string\", attribute name {\"content\"}} </elem>",
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
   *  An unbound prefix in a lexical QName yields QDY0074. .
   */
  @org.junit.Test
  public void k2ComputeConAttr40() {
    final XQuery query = new XQuery(
      "attribute {\"aPrefix:localName\"} {\"content\"}",
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
  public void k2ComputeConAttr41() {
    final XQuery query = new XQuery(
      "attribute {xs:untypedAtomic(\"aPrefix::localName\")} {\"content\"}",
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
   *  It's not allowed to bind the xmlns namespace to any prefix with computed attribute constructors. .
   */
  @org.junit.Test
  public void k2ComputeConAttr42() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace prefix = \"http://www.w3.org/2000/xmlns/\"; \n" +
      "        <e>{attribute prefix:localName {\"content\"}}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0070")
    );
  }

  /**
   *  It's not allowed to bind the xmlns namespace to any prefix(#2). .
   */
  @org.junit.Test
  public void k2ComputeConAttr43() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://www.w3.org/2000/xmlns/\"; <e>{attribute {\"prefix:localName\"} {\"content\"}}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0070")
    );
  }

  /**
   *  One cannot create namespace declarations with computed attribute constructors. .
   */
  @org.junit.Test
  public void k2ComputeConAttr44() {
    final XQuery query = new XQuery(
      "attribute {\"xmlns\"} {\"content\"}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0044")
    );
  }

  /**
   *  One cannot create namespace declarations with computed attribute constructors(#2). .
   */
  @org.junit.Test
  public void k2ComputeConAttr45() {
    final XQuery query = new XQuery(
      "attribute xmlns {\"content\"}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0044")
    );
  }

  /**
   *  The default element namespace declaration doesn't affect attribute declarations. .
   */
  @org.junit.Test
  public void k2ComputeConAttr46() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/\"; <e>{attribute xmlns {\"content\"}}</e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0044")
    );
  }

  /**
   *  Ensure the typed value of computed, constructed attributes has the correct type. .
   */
  @org.junit.Test
  public void k2ComputeConAttr47() {
    final XQuery query = new XQuery(
      "data(attribute foo {\"content\"}) instance of xs:untypedAtomic",
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
   *  Ensure xml:id is properly normalized, and not done at the serialization stage. .
   */
  @org.junit.Test
  public void k2ComputeConAttr48() {
    final XQuery query = new XQuery(
      "string(attribute xml:id {\" ab c d \"})",
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
        assertStringValue(false, "ab c d")
      ||
        error("XQDY0091")
      )
    );
  }

  /**
   *  Ensure XQDY0025 is issued when triggered by computed constructors. .
   */
  @org.junit.Test
  public void k2ComputeConAttr49() {
    final XQuery query = new XQuery(
      "declare namespace a = \"http://example.com/A\"; declare namespace b = \"http://example.com/A\"; <e> { attribute a:localName {()}, attribute b:localName {()} } </e>",
      ctx);
    try {
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
   *  Ensure XQTY0024 is issued when content appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr5() {
    final XQuery query = new XQuery(
      "<elem> <![CDATA[]]> {attribute name {\"content\"}} </elem>",
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
   *  Duplicate attributes whose names are non-prefixed while non-empty namespace URIs. .
   */
  @org.junit.Test
  public void k2ComputeConAttr50() {
    final XQuery query = new XQuery(
      "<e> { attribute {QName(\"http://example.com/\", \"attr\")} {()}, attribute {QName(\"http://example.com/\", \"attr\")} {()} } </e>",
      ctx);
    try {
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
   *  Construct an attribute with no prefix and a non-empty namespace URI. The specification is currently unclear on this area, http://www.w3.org/Bugs/Public/show_bug.cgi?id=4443 . .
   */
  @org.junit.Test
  public void k2ComputeConAttr51() {
    final XQuery query = new XQuery(
      "<e> { attribute {QName(\"http://example.com/\", \"attr\")} {()} } </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns:ns0=\"http://example.com/\" ns0:attr=\"\"/>", false)
    );
  }

  /**
   *  Duplicated attributes, but constructed from different kinds of constructors. .
   */
  @org.junit.Test
  public void k2ComputeConAttr52() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"http://example.com/\" p:attr=\"\"> { attribute {QName(\"http://example.com/\", \"p:attr\")} {()} } </e>",
      ctx);
    try {
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
   *  Serialize an attribute that has a namespace URI but no prefix. The implementation invents a prefix in this case. .
   */
  @org.junit.Test
  public void k2ComputeConAttr53() {
    final XQuery query = new XQuery(
      "<e> { attribute {QName(\"http://example.com/\", \"attr\")} {()} } </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns:ns0=\"http://example.com/\" ns0:attr=\"\"/>", false)
    );
  }

  /**
   *  Ensure that a valid prefix has been constructed for a QName which the implementation is supposed to create a prefix for. .
   */
  @org.junit.Test
  public void k2ComputeConAttr54() {
    final XQuery query = new XQuery(
      "string-length(xs:NCName(prefix-from-QName(node-name(attribute {QName(\"http://example.com/\", \"attr\")} {()})))) > 0",
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
   *  Ensure that the 'xml' prefix has been constructed for a QName which has the XML namespace. .
   */
  @org.junit.Test
  public void k2ComputeConAttr55() {
    final XQuery query = new XQuery(
      "prefix-from-QName(node-name(attribute {QName(\"http://www.w3.org/XML/1998/namespace\", \"attr\")} {()}))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xml")
    );
  }

  /**
   *  Ensure that the 'xml' prefix is used for the XML namespace when no prefix is supplied. .
   */
  @org.junit.Test
  public void k2ComputeConAttr56() {
    final XQuery query = new XQuery(
      "<e> { attribute {QName(\"http://www.w3.org/XML/1998/namespace\", \"space\")} {\"default\"} } </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xml:space=\"default\"/>", false)
    );
  }

  /**
   *  Ensure the xmlns namespace is flagged as invalid even though no prefix is supplied. .
   */
  @org.junit.Test
  public void k2ComputeConAttr57() {
    final XQuery query = new XQuery(
      "<e> { attribute {QName(\"http://www.w3.org/2000/xmlns/\", \"space\")} {\"default\"} } </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0044")
    );
  }

  /**
   *  Serialize an attribute that has a namespace URI but no prefix, and whose namespace URI is already in scope. .
   */
  @org.junit.Test
  public void k2ComputeConAttr58() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"http://example.com/\" p:attr1=\"value\"> { attribute {QName(\"http://example.com/\", \"attr2\")} {()} } </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns:p=\"http://example.com/\" xmlns:ns0=\"http://example.com/\" p:attr1=\"value\" ns0:attr2=\"\"/>", true)
    );
  }

  /**
   *  '1' is an invalid value for xml:id. .
   */
  @org.junit.Test
  public void k2ComputeConAttr59() {
    final XQuery query = new XQuery(
      "<a> { attribute xml:id {\"1\"} } </a>",
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
        assertSerialization("<a xml:id=\"1\"/>", false)
      ||
        error("XQDY0091")
      )
    );
  }

  /**
   *  Ensure XQTY0024 is issued when content appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr6() {
    final XQuery query = new XQuery(
      "<elem> <![CDATA[content]]> {attribute name {\"content\"}} </elem>",
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
   *  'DEFAULT' is an invalid value for xml:space. .
   */
  @org.junit.Test
  public void k2ComputeConAttr60() {
    final XQuery query = new XQuery(
      "<a> { attribute xml:space {\"DEFAULT\"} } </a>",
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
        assertSerialization("<a xml:space=\"DEFAULT\"/>", false)
      ||
        error("XQDY0092")
      )
    );
  }

  /**
   *  Trigger XQTY0024 in a query with a bit of complexity. .
   */
  @org.junit.Test
  public void k2ComputeConAttr61() {
    final XQuery query = new XQuery(
      "let $x := ( attribute a { \"a\" }, element b { \"b\" }, attribute c { \"c\" } ) return <foo> { $x } </foo>",
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
   *  Ensure XQTY0024 is issued when content appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr7() {
    final XQuery query = new XQuery(
      "<elem> <!-- content --> {attribute name {\"content\"}} </elem>",
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
   *  Ensure XQTY0024 is issued when content appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr8() {
    final XQuery query = new XQuery(
      "<elem> <!-- comment --> { \"a string\", 999, attribute name {\"content\"}, xs:hexBinary(\"FF\") } </elem>",
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
   *  Ensure XQTY0024 is issued when content appears before computed attributes. .
   */
  @org.junit.Test
  public void k2ComputeConAttr9() {
    final XQuery query = new XQuery(
      "<elem> <!-- comment --> { \"a string\", 999, (\"another string\", attribute name {\"content\"}, 383), xs:hexBinary(\"FF\") } </elem>",
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
   *  test XQDY0044 in attribute construction .
   */
  @org.junit.Test
  public void cbclConstrCompattr001() {
    final XQuery query = new XQuery(
      "for $a in attribute { fn:QName(\"http://www.w3.org/2000/xmlns/\", \"namespace:foo\") } { \"bar\" } return name($a)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0044")
    );
  }

  /**
   *  test XQDY0044 in attribute construction .
   */
  @org.junit.Test
  public void cbclConstrCompattr002() {
    final XQuery query = new XQuery(
      "for $a in attribute { fn:QName(\"http://www.example.com/\", \"xmlns:foo\") } { \"bar\" } return name($a)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0044")
    );
  }

  /**
   *  test XQDY0044 in attribute construction .
   */
  @org.junit.Test
  public void cbclConstrCompattr003() {
    final XQuery query = new XQuery(
      "for $a in attribute { \"xmlns\" } { \"bar\" } return name($a)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0044")
    );
  }

  /**
   *  test XQDY0044 in attribute construction .
   */
  @org.junit.Test
  public void cbclConstrCompattr005() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tfor $a in attribute { fn:QName(\"http://www.example.com/\", \"xml:foo\") } { \"bar\" } \n" +
      "      \treturn name($a)\n" +
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
      error("XQDY0044")
    );
  }

  /**
   *  test XQDY0044 in attribute construction .
   */
  @org.junit.Test
  public void cbclConstrCompattr006() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tfor $a in attribute { fn:QName(\"http://www.w3.org/XML/1998/namespace\", \"sgml:foo\") } { \"bar\" } \n" +
      "      \treturn name($a)\n" +
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
      error("XQDY0044")
    );
  }

  /**
   *  test attribute constructionn .
   */
  @org.junit.Test
  public void cbclConstrCompattr007() {
    final XQuery query = new XQuery(
      "\n" +
      "      \t<element xmlns:sgml=\"http://www.example.com/other\"> { \n" +
      "      \t\tfor $a in attribute { fn:QName(\"http://www.example.com/\", \"sgml:foo\") } { } \n" +
      "      \t\treturn concat(name($a), \"=\", namespace-uri($a)) } \n" +
      "      \t</element>\n" +
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
      assertSerialization("<element xmlns:sgml=\"http://www.example.com/other\">sgml:foo=http://www.example.com/</element>", false)
    );
  }

  /**
   *  test attribute constructionn .
   */
  @org.junit.Test
  public void cbclConstrCompattr008() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:factorial($arg as xs:integer) as xs:integer { \n" +
      "      \t\tif ($arg le 1) then 1 else $arg * local:factorial($arg - 1) \n" +
      "      \t}; \n" +
      "      \t<element> { attribute { fn:QName(\"http://www.example.com/\", \"sgml:foo\") } { local:factorial(5) } } </element>\n" +
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
      assertSerialization("<element xmlns:sgml=\"http://www.example.com/\" sgml:foo=\"120\"/>", false)
    );
  }

  /**
   *  test attribute construction with empty content .
   */
  @org.junit.Test
  public void cbclConstrCompattr009() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:even-range($arg as xs:integer) as xs:integer* { (1 to $arg)[. mod 2 = 9] }; \n" +
      "      \t<element> { attribute { 'attr' } { local:even-range(0) } } </element>\n" +
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
      assertSerialization("<element attr=\"\"/>", false)
    );
  }

  /**
   * 3.7.3.2 Computed Attribute Constructor per XQ.E19 XQDY0044 if namespace prefix is 'xmlns' Mary Holstege .
   */
  @org.junit.Test
  public void compAttrBadName1() {
    final XQuery query = new XQuery(
      "(: 3.7.3.2 Computed Attribute Constructor per XQ.E19 XQDY0044 if namespace prefix is 'xmlns' Mary Holstege :) <result>{attribute {\"xmlns:error\"} {}}</result>",
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
        error("XQDY0044")
      ||
        error("XQDY0074")
      )
    );
  }

  /**
   * 3.7.3.2 Computed Attribute Constructor per XQ.E19 XQDY0044 if no namespace prefix and local name is 'xmlns' Mary Holstege .
   */
  @org.junit.Test
  public void compAttrBadName2() {
    final XQuery query = new XQuery(
      "(: 3.7.3.2 Computed Attribute Constructor per XQ.E19 XQDY0044 if no namespace prefix and local name is 'xmlns' Mary Holstege :) <result>{attribute {\"xmlns\"} {}}</result>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0044")
    );
  }

  /**
   * 3.7.3.2 Computed Attribute Constructor per XQ.E19 XQDY0044 if namespace URI is 'http://www.w3.org/2000/xmlns/' Mary Holstege .
   */
  @org.junit.Test
  public void compAttrBadName3() {
    final XQuery query = new XQuery(
      "(: 3.7.3.2 Computed Attribute Constructor per XQ.E19 XQDY0044 if namespace URI is 'http://www.w3.org/2000/xmlns/' Mary Holstege :) <result>{ attribute { fn:QName(\"http://www.w3.org/2000/xmlns/\",\"error\")} {} }</result>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0044")
    );
  }

  /**
   * 3.7.3.2 Computed Attribute Constructor per XQ.E19 XQDY0044 if namespace URI is 'http://www.w3.org/2000/xmlns/' Mary Holstege .
   */
  @org.junit.Test
  public void compAttrBadName4() {
    final XQuery query = new XQuery(
      "(: 3.7.3.2 Computed Attribute Constructor per XQ.E19 XQDY0044 if namespace URI is 'http://www.w3.org/2000/xmlns/' Mary Holstege :) <result>{ attribute { fn:QName(\"http://www.w3.org/2000/xmlns/\",\"foo:error\")} {} }</result>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0044")
    );
  }

  /**
   * 3.7.3.2 Computed Attribute Constructor per XQ.E19 XQDY0044 if namespace prefix is 'xml' and namespace URI is not 'http://www.w3.org/XML/1998/namespace' Mary Holstege .
   */
  @org.junit.Test
  public void compAttrBadName5() {
    final XQuery query = new XQuery(
      "(: 3.7.3.2 Computed Attribute Constructor per XQ.E19 XQDY0044 if namespace prefix is 'xml' and namespace URI is not 'http://www.w3.org/XML/1998/namespace' Mary Holstege :) <result>{ attribute { fn:QName(\"http://example.com/not-XML-uri\",\"xml:error\") } {} }</result>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0044")
    );
  }

  /**
   * 3.7.3.2 Computed Attribute Constructor per XQ.E19 XQDY0044 if namespace prefix is not 'xml' and namespace URI is 'http://www.w3.org/XML/1998/namespace' Mary Holstege .
   */
  @org.junit.Test
  public void compAttrBadName6() {
    final XQuery query = new XQuery(
      "(: 3.7.3.2 Computed Attribute Constructor per XQ.E19 XQDY0044 if namespace prefix is not 'xml' and namespace URI is 'http://www.w3.org/XML/1998/namespace' Mary Holstege :) <result>{ attribute { fn:QName(\"http://www.w3.org/XML/1998/namespace\",\"foo:error\")} {} }</result>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0044")
    );
  }

  /**
   * 3.7.3.2 Computed Attribute Constructor per XQ.E19 XQDY0044 if namespace prefix is 'xmlns' Mary Holstege .
   */
  @org.junit.Test
  public void compAttrBadName7() {
    final XQuery query = new XQuery(
      "(: 3.7.3.2 Computed Attribute Constructor per XQ.E19 XQDY0044 if namespace prefix is 'xmlns' Mary Holstege :) <result>{attribute {fn:QName(\"http://example.com/some-uri\",\"xmlns:error\")} {}}</result>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0044")
    );
  }

  /**
   *  Evaluation of constructor function xs:QName for which the argument is not a literal. .
   */
  @org.junit.Test
  public void constattrerr1() {
    final XQuery query = new XQuery(
      "declare variable $input-context1 external; attribute xmlns {}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0044")
    );
  }
}
