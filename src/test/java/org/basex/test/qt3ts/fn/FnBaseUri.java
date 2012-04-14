package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the base-uri() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnBaseUri extends QT3TestSet {

  /**
   *  A test whose essence is: `base-uri((), "wrong param")`. .
   */
  @org.junit.Test
  public void kBaseURIFunc1() {
    final XQuery query = new XQuery(
      "base-uri((), \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `empty(base-uri(()))`. .
   */
  @org.junit.Test
  public void kBaseURIFunc2() {
    final XQuery query = new XQuery(
      "empty(base-uri(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoke on a comment node. .
   */
  @org.junit.Test
  public void k2BaseURIFunc1() {
    final XQuery query = new XQuery(
      "empty(document-uri(<!-- comment -->))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure computed processing-instructions pick up the correct xml:base value. .
   */
  @org.junit.Test
  public void k2BaseURIFunc10() {
    final XQuery query = new XQuery(
      "let $i := <e xml:base=\"http://www.example.com/\">{processing-instruction target {\"data\"}}</e> return base-uri($i/processing-instruction()[1])",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/")
    );
  }

  /**
   *  Ensure comments pick up the correct xml:base value. .
   */
  @org.junit.Test
  public void k2BaseURIFunc11() {
    final XQuery query = new XQuery(
      "let $i := <e xml:base=\"http://www.example.com/\"><!-- content --></e> return base-uri($i/comment()[1])",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/")
    );
  }

  /**
   *  Ensure computed comments pick up the correct xml:base value. .
   */
  @org.junit.Test
  public void k2BaseURIFunc12() {
    final XQuery query = new XQuery(
      "let $i := <e xml:base=\"http://www.example.com/\">{comment {\"content\"}}</e> return base-uri($i/comment()[1])",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/")
    );
  }

  /**
   *  Ensure computed comments don't pick up the base-uri from the static context. .
   */
  @org.junit.Test
  public void k2BaseURIFunc13() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; empty(base-uri(comment {\"content\"}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure computed comments don't pick up the base-uri from the static context. .
   */
  @org.junit.Test
  public void k2BaseURIFunc14() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; empty(base-uri(<!-- comment -->))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure computed processing-instructions don't pick up the base-uri from the static context. .
   */
  @org.junit.Test
  public void k2BaseURIFunc15() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; empty(base-uri(processing-instruction target {\"data\"}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure processing-instructions don't pick up the base-uri from the static context. .
   */
  @org.junit.Test
  public void k2BaseURIFunc16() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; empty(base-uri(<?target data?>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure computed attributes don't pick up the base-uri from the static context. .
   */
  @org.junit.Test
  public void k2BaseURIFunc17() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; empty(base-uri(attribute name {\"data\"}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure computed attributes pick up the base-uri from parent. .
   */
  @org.junit.Test
  public void k2BaseURIFunc18() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; let $i := <e attr=\"foo\"></e> return base-uri($i/@attr)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com")
    );
  }

  /**
   *  Ensure computed attributes pick up the resolved base-uri of the parent. .
   */
  @org.junit.Test
  public void k2BaseURIFunc19() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com/\"; let $i := <e xml:base = \"foo/../xml\" attr=\"foo\"> </e> return base-uri($i/@attr)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/xml")
    );
  }

  /**
   *  Invoke on an attribute node. .
   */
  @org.junit.Test
  public void k2BaseURIFunc2() {
    final XQuery query = new XQuery(
      "empty(document-uri(attribute name {\"content\"}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure computed attributes pick up the resolved base-uri of the parent(#2). .
   */
  @org.junit.Test
  public void k2BaseURIFunc20() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com/\"; let $i := <e xml:base = \"foo/../xml\" attr=\"foo\"> </e> return base-uri($i/@xml:base)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/xml")
    );
  }

  /**
   *  Ensure that the return value of document-uri() is of correct type. .
   */
  @org.junit.Test
  public void k2BaseURIFunc21() {
    final XQuery query = new XQuery(
      "for $i in (1, base-uri(.), 3) return \n" +
      "        typeswitch($i) \n" +
      "        case xs:anyURI return \"xs:anyURI\" \n" +
      "        case xs:integer return \"xs:integer\" \n" +
      "        default return \"FAILURE\"",
      ctx);
    query.context(node(file("prod/AxisStep/TopMany.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "xs:integer xs:anyURI xs:integer")
    );
  }

  /**
   *  Ensure that the base URI is empty for direct PI constructors. .
   */
  @org.junit.Test
  public void k2BaseURIFunc22() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.com/baseURI\"; empty(base-uri(<?target data?>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure that the base URI is empty for computed PI constructors. .
   */
  @org.junit.Test
  public void k2BaseURIFunc23() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.com/baseURI\"; empty(base-uri(processing-instruction target {\"data\"}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure that the base URI is empty for direct PI constructors, with no base-uri declaration. .
   */
  @org.junit.Test
  public void k2BaseURIFunc24() {
    final XQuery query = new XQuery(
      "empty(base-uri(<?target data?>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure that the base URI is empty for computed PI constructors, with no base-uri declaration. .
   */
  @org.junit.Test
  public void k2BaseURIFunc25() {
    final XQuery query = new XQuery(
      "empty(base-uri(processing-instruction target {\"data\"}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure that the base URI is empty for computed attribute constructors, with no base-uri declaration. .
   */
  @org.junit.Test
  public void k2BaseURIFunc26() {
    final XQuery query = new XQuery(
      "empty(base-uri(attribute name {\"value\"}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure that the base URI is set for computed documents. .
   */
  @org.junit.Test
  public void k2BaseURIFunc27() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.com/BASEURI\"; base-uri(document {()})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://example.com/BASEURI")
    );
  }

  /**
   *  Check the document URI and base URI simultaneously for a computed document node. .
   */
  @org.junit.Test
  public void k2BaseURIFunc28() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.com/\"; let $i := document {()} return (\"Base URI:\", base-uri($i), \"Document URI:\", document-uri($i))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Base URI: http://example.com/ Document URI:")
    );
  }

  /**
   *  Use an URI in an xml:base element that is a valid URI, but an invalid HTTP URL. Since implementations aren't required to validate specific schemes but allowed to, this may either raise an error or return the URI. .
   */
  @org.junit.Test
  public void k2BaseURIFunc29() {
    final XQuery query = new XQuery(
      "let $i := fn:base-uri(<anElement xml:base=\"http:\\\\example.com\\\\examples\">Element content</anElement>) return $i eq \"http:\\\\example.com\\\\examples\" or empty($i)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoke on a processing instruction node. .
   */
  @org.junit.Test
  public void k2BaseURIFunc3() {
    final XQuery query = new XQuery(
      "empty(document-uri(<?target data?>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Use an empty xml:base attribute. .
   */
  @org.junit.Test
  public void k2BaseURIFunc30() {
    final XQuery query = new XQuery(
      "fn:base-uri(<anElement xml:base=\"http://example.com/examples\"><b xml:base=\"\"/>Element content</anElement>/b)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://example.com/examples")
    );
  }

  /**
   *  Compute the base URI from a processing instruction. .
   */
  @org.junit.Test
  public void k2BaseURIFunc31() {
    final XQuery query = new XQuery(
      "fn:base-uri(exactly-one(<anElement xml:base=\"http://example.com/examples\"><?target data?></anElement>/processing-instruction()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://example.com/examples")
    );
  }

  /**
   *  Compute the base URI from a comment. .
   */
  @org.junit.Test
  public void k2BaseURIFunc32() {
    final XQuery query = new XQuery(
      "fn:base-uri(exactly-one(<anElement xml:base=\"http://example.com/examples\"><!-- a comment --></anElement>/comment()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://example.com/examples")
    );
  }

  /**
   *  Ensure recursive resolution works. .
   */
  @org.junit.Test
  public void k2BaseURIFunc33() {
    final XQuery query = new XQuery(
      "<e xml:base=\"http://example.com/ABC/\"> <a xml:base=\"../\"> <b xml:base=\"DEF/file.test\"/> </a> </e>/a/b/base-uri()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://example.com/DEF/file.test")
    );
  }

  /**
   *  Invoke on a processing instruction node. .
   */
  @org.junit.Test
  public void k2BaseURIFunc4() {
    final XQuery query = new XQuery(
      "empty(document-uri(processing-instruction name {123}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoke on a text node. .
   */
  @org.junit.Test
  public void k2BaseURIFunc5() {
    final XQuery query = new XQuery(
      "empty(document-uri(text {123}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoke on a single element node. .
   */
  @org.junit.Test
  public void k2BaseURIFunc6() {
    final XQuery query = new XQuery(
      "empty(document-uri(<elem/>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoke on a single attribute node. .
   */
  @org.junit.Test
  public void k2BaseURIFunc7() {
    final XQuery query = new XQuery(
      "empty(document-uri(<elem attr=\"f\"/>/@attr))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoke on a single document node. .
   */
  @org.junit.Test
  public void k2BaseURIFunc8() {
    final XQuery query = new XQuery(
      "empty(document-uri(document {1}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure processing-instructions pick up the correct xml:base value. .
   */
  @org.junit.Test
  public void k2BaseURIFunc9() {
    final XQuery query = new XQuery(
      "let $i := <e xml:base=\"http://www.example.com/\"><?target data?></e> \n" +
      "        return base-uri($i/processing-instruction()[1])",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/")
    );
  }

  /**
   *  Evaluation of base-uri function with no arguments and no context item .
   */
  @org.junit.Test
  public void fnBaseUri1() {
    final XQuery query = new XQuery(
      "declare namespace eg = \"http://example.org\"; declare function eg:noContextFunction() { fn:base-uri() }; declare variable $input-context1 external; eg:noContextFunction()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a directly constructed Element node with base-xml argument (no escaping). Use fn:string .
   */
  @org.junit.Test
  public void fnBaseUri10() {
    final XQuery query = new XQuery(
      "fn:string(fn:base-uri(<anElement xml:base=\"http://www.example.com\">Element content</anElement>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com")
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a computed constructed Document node with no base-xml argument. Use fn:count .
   */
  @org.junit.Test
  public void fnBaseUri11() {
    final XQuery query = new XQuery(
      "fn:count(fn:base-uri(document {<aDocument>some content</aDocument>}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0")
      ||
        assertEq("1")
      )
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a computed constructed Document node argument. Uses declared base uri property Use fn:string .
   */
  @org.junit.Test
  public void fnBaseUri12() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.org\"; \n" +
      "            fn:string(fn:base-uri(document {<aDocument>some content</aDocument>}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://example.org")
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a directly constructed element node argument. Should not declared base uri property Use fn:string .
   */
  @org.junit.Test
  public void fnBaseUri13() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.org\"; fn:string(fn:base-uri(<anElement>some content</anElement>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://example.org")
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a computed constructed attribute node argument. .
   */
  @org.junit.Test
  public void fnBaseUri14() {
    final XQuery query = new XQuery(
      "fn:base-uri(attribute anAttribute{\"attribute value\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a directly constructed PI node argument. .
   */
  @org.junit.Test
  public void fnBaseUri15() {
    final XQuery query = new XQuery(
      "fn:base-uri(<?format role=\"output\" ?>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a computed constructed PI node argument. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnBaseUri16() {
    final XQuery query = new XQuery(
      "fn:base-uri(processing-instruction {\"PItarget\"} {\"PIcontent\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a computed constructed PI node argument. Use fn:count to avoid empty file. Should not use the declare base-uri .
   */
  @org.junit.Test
  public void fnBaseUri17() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare base-uri \"http://example.org\"; \n" +
      "         fn:base-uri(processing-instruction {\"PItarget\"} {\"PIcontent\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a directly constructed element node (via FLOWR expr). Use the declare base-uri .
   */
  @org.junit.Test
  public void fnBaseUri18() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.org\"; \n" +
      "        let $var := <anElement>With some contexnt</anElement> \n" +
      "        return fn:string(fn:base-uri($var))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://example.org")
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a directly constructed element node (via FLOWR expr). Use the xml-base attribute .
   */
  @org.junit.Test
  public void fnBaseUri19() {
    final XQuery query = new XQuery(
      "let $var := <anElement xml:base=\"http://www.examples.com\">With some content</anElement> \n" +
      "        return fn:string(fn:base-uri($var))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.examples.com")
    );
  }

  /**
   *  Evaluation of base-uri function with context item not a node .
   */
  @org.junit.Test
  public void fnBaseUri2() {
    final XQuery query = new XQuery(
      "(1 to 100)[fn:base-uri()]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a directly constructed element node (via FLOWR expr). Use the xml-base attribute and should ignore declared base uri property. .
   */
  @org.junit.Test
  public void fnBaseUri20() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.org\"; \n" +
      "        let $var := <anElement xml:base=\"http://www.examples.com\">With some content</anElement> \n" +
      "        return fn:string(fn:base-uri($var))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.examples.com")
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a directly constructed comment node (via FLOWR expr). Should ignore declared base uri property. .
   */
  @org.junit.Test
  public void fnBaseUri21() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.org\"; \n" +
      "        let $var := <!-- A Comment --> return fn:base-uri($var)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of base-uri function using the "." and no context item. .
   */
  @org.junit.Test
  public void fnBaseUri22() {
    final XQuery query = new XQuery(
      "declare namespace eg = \"http://example.org\"; \n" +
      "        declare function eg:noContextFunction() { fn:base-uri(.) }; \n" +
      "        eg:noContextFunction()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a directly constructed Element node with base-xml argument that needs escaping. Uses fn:string. .
   */
  @org.junit.Test
  public void fnBaseUri23() {
    final XQuery query = new XQuery(
      "fn:string(fn:base-uri(<anElement xml:base=\"http://example.com/examples\">Element content</anElement>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://example.com/examples")
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to empty sequence Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnBaseUri3() {
    final XQuery query = new XQuery(
      "fn:count(fn:base-uri(()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a directly constructed comment .
   */
  @org.junit.Test
  public void fnBaseUri4() {
    final XQuery query = new XQuery(
      "fn:base-uri(<!-- A comment -->)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a computed constructed comment .
   */
  @org.junit.Test
  public void fnBaseUri5() {
    final XQuery query = new XQuery(
      "fn:base-uri(comment {\"A Comment Node \"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a computed constructed Text node. .
   */
  @org.junit.Test
  public void fnBaseUri6() {
    final XQuery query = new XQuery(
      "fn:base-uri(text {\"A Text Node\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a computed constructed Element node with not base-xml argument. .
   */
  @org.junit.Test
  public void fnBaseUri7() {
    final XQuery query = new XQuery(
      "fn:count(fn:base-uri(element anElement {\"An Element Node\"}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("1")
      ||
        assertEq("0")
      )
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a directly constructed Element node with not base-xml argument. Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnBaseUri8() {
    final XQuery query = new XQuery(
      "fn:count(fn:base-uri(<anElement>Element content</anElement>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("1")
      ||
        assertEq("0")
      )
    );
  }

  /**
   *  Evaluation of base-uri function with argument set to a directly constructed Element node with base-xml argument. Use fn:string .
   */
  @org.junit.Test
  public void fnBaseUri9() {
    final XQuery query = new XQuery(
      "fn:string(fn:base-uri(<anElement xml:base=\"http://example.com/examples\">Element content</anElement>))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://example.com/examples")
    );
  }
}
