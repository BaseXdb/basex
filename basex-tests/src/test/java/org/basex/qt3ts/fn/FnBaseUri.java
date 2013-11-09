package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the base-uri() function.
 *
 * @author BaseX Team 2005-13, BSD License
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   *  Invoke on a comment node. .
   */
  @org.junit.Test
  public void k2BaseURIFunc1() {
    final XQuery query = new XQuery(
      "empty(document-uri(<!-- comment -->))",
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
   *  Ensure computed processing-instructions pick up the correct xml:base value. .
   */
  @org.junit.Test
  public void k2BaseURIFunc10() {
    final XQuery query = new XQuery(
      "let $i := <e xml:base=\"http://www.example.com/\">{processing-instruction target {\"data\"}}</e> return base-uri($i/processing-instruction()[1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   *  Ensure computed comments don't pick up the base-uri from the static context. .
   */
  @org.junit.Test
  public void k2BaseURIFunc14() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; empty(base-uri(<!-- comment -->))",
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
   *  Ensure computed processing-instructions don't pick up the base-uri from the static context. .
   */
  @org.junit.Test
  public void k2BaseURIFunc15() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; empty(base-uri(processing-instruction target {\"data\"}))",
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
   *  Ensure processing-instructions don't pick up the base-uri from the static context. .
   */
  @org.junit.Test
  public void k2BaseURIFunc16() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; empty(base-uri(<?target data?>))",
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
   *  Ensure computed attributes don't pick up the base-uri from the static context. .
   */
  @org.junit.Test
  public void k2BaseURIFunc17() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; empty(base-uri(attribute name {\"data\"}))",
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
   *  Ensure computed attributes pick up the base-uri from parent. .
   */
  @org.junit.Test
  public void k2BaseURIFunc18() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com\"; let $i := <e attr=\"foo\"></e> return base-uri($i/@attr)",
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
   *  Ensure computed attributes pick up the resolved base-uri of the parent. .
   */
  @org.junit.Test
  public void k2BaseURIFunc19() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com/\"; let $i := <e xml:base = \"foo/../xml\" attr=\"foo\"> </e> return base-uri($i/@attr)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   *  Ensure computed attributes pick up the resolved base-uri of the parent(#2). .
   */
  @org.junit.Test
  public void k2BaseURIFunc20() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com/\"; let $i := <e xml:base = \"foo/../xml\" attr=\"foo\"> </e> return base-uri($i/@xml:base)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   *  Ensure that the base URI is empty for computed PI constructors. .
   */
  @org.junit.Test
  public void k2BaseURIFunc23() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.com/baseURI\"; empty(base-uri(processing-instruction target {\"data\"}))",
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
   *  Ensure that the base URI is empty for direct PI constructors, with no base-uri declaration. .
   */
  @org.junit.Test
  public void k2BaseURIFunc24() {
    final XQuery query = new XQuery(
      "empty(base-uri(<?target data?>))",
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
   *  Ensure that the base URI is empty for computed PI constructors, with no base-uri declaration. .
   */
  @org.junit.Test
  public void k2BaseURIFunc25() {
    final XQuery query = new XQuery(
      "empty(base-uri(processing-instruction target {\"data\"}))",
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
   *  Ensure that the base URI is empty for computed attribute constructors, with no base-uri declaration. .
   */
  @org.junit.Test
  public void k2BaseURIFunc26() {
    final XQuery query = new XQuery(
      "empty(base-uri(attribute name {\"value\"}))",
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
   *  Ensure that the base URI is set for computed documents. .
   */
  @org.junit.Test
  public void k2BaseURIFunc27() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.com/BASEURI\"; base-uri(document {()})",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   *  Invoke on a processing instruction node. .
   */
  @org.junit.Test
  public void k2BaseURIFunc3() {
    final XQuery query = new XQuery(
      "empty(document-uri(<?target data?>))",
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
   *  Use an empty xml:base attribute. .
   */
  @org.junit.Test
  public void k2BaseURIFunc30() {
    final XQuery query = new XQuery(
      "fn:base-uri(<anElement xml:base=\"http://example.com/examples\"><b xml:base=\"\"/>Element content</anElement>/b)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   *  Invoke on a text node. .
   */
  @org.junit.Test
  public void k2BaseURIFunc5() {
    final XQuery query = new XQuery(
      "empty(document-uri(text {123}))",
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
   *  Invoke on a single element node. .
   */
  @org.junit.Test
  public void k2BaseURIFunc6() {
    final XQuery query = new XQuery(
      "empty(document-uri(<elem/>))",
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
   *  Invoke on a single attribute node. .
   */
  @org.junit.Test
  public void k2BaseURIFunc7() {
    final XQuery query = new XQuery(
      "empty(document-uri(<elem attr=\"f\"/>/@attr))",
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
   *  Invoke on a single document node. .
   */
  @org.junit.Test
  public void k2BaseURIFunc8() {
    final XQuery query = new XQuery(
      "empty(document-uri(document {1}))",
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
   *  Ensure processing-instructions pick up the correct xml:base value. .
   */
  @org.junit.Test
  public void k2BaseURIFunc9() {
    final XQuery query = new XQuery(
      "let $i := <e xml:base=\"http://www.example.com/\"><?target data?></e> \n" +
      "        return base-uri($i/processing-instruction()[1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.com/")
    );
  }

  /**
   * The base-uri property of the copied node, and of each of its descendants, 
   *       is set to be the same as that of its new parent, unless it (the child node) has an xml:base attribute, 
   *       in which case its base-uri property is set to the value of that attribute, resolved (if it is relative) 
   *       against the base-uri property of its new parent node. 
   *       .
   */
  @org.junit.Test
  public void cbclBaseUri001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tlet $d := document { <root xml:base=\"http://www.w3.org/\"><implicit-base><child /></implicit-base><explicit-base xml:base=\"http://www.w3.org/TR/xquery\"><child /></explicit-base></root> } \n" +
      "      \treturn let $y := <copy xml:base=\"http://www.example.org\"> { $d/root/explicit-base } </copy> return fn:base-uri(($y/explicit-base)[1])\n" +
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
      assertStringValue(false, "http://www.w3.org/TR/xquery")
    );
  }

  /**
   * 
   *       The base-uri property of the copied node, and of each of its descendants, is set to be the same as 
   *       that of its new parent, unless it (the child node) has an xml:base attribute, in which case its 
   *       base-uri property is set to the value of that attribute, resolved (if it is relative) against 
   *       the base-uri property of its new parent node. 
   * 	  .
   */
  @org.junit.Test
  public void cbclBaseUri002() {
    final XQuery query = new XQuery(
      "\n" +
      "      let $d := document { <root xml:base=\"http://www.w3.org/\"> <implicit-base><child /></implicit-base> <explicit-base xml:base=\"http://www.w3.org/TR/xquery\"><child /></explicit-base> </root> } \n" +
      "      return let $y := <copy xml:base=\"http://www.example.org\"> { $d/root/explicit-base } </copy> \n" +
      "      return fn:base-uri(($y/explicit-base/child)[1])\n" +
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
      assertStringValue(false, "http://www.w3.org/TR/xquery")
    );
  }

  /**
   * 
   *       The base-uri property of the copied node, and of each of its descendants, is set to be the 
   *       same as that of its new parent, unless it (the child node) has an xml:base attribute, 
   *       in which case its base-uri property is set to the value of that attribute, resolved 
   *       (if it is relative) against the base-uri property of its new parent node. 
   *       .
   */
  @org.junit.Test
  public void cbclBaseUri003() {
    final XQuery query = new XQuery(
      "\n" +
      "      let $d := document { <root xml:base=\"http://www.w3.org/\"> <implicit-base><child /></implicit-base> <explicit-base xml:base=\"http://www.w3.org/TR/xquery\"><child /></explicit-base> </root> } \n" +
      "      return let $y := <copy xml:base=\"http://www.example.org\"> { $d/root/implicit-base } </copy> \n" +
      "      return fn:base-uri(($y/implicit-base)[1])\n" +
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
      assertStringValue(false, "http://www.example.org")
    );
  }

  /**
   * 
   *       The base-uri property of the copied node, and of each of its descendants, is set to be the 
   *       same as that of its new parent, unless it (the child node) has an xml:base attribute, 
   *       in which case its base-uri property is set to the value of that attribute, resolved 
   *       (if it is relative) against the base-uri property of its new parent node. 
   * 	  .
   */
  @org.junit.Test
  public void cbclBaseUri004() {
    final XQuery query = new XQuery(
      "\n" +
      "      let $d := document { <root xml:base=\"http://www.w3.org/\"> <implicit-base><child /></implicit-base> <explicit-base xml:base=\"http://www.w3.org/TR/xquery\"><child /></explicit-base> </root> } \n" +
      "      return let $y := <copy xml:base=\"http://www.example.org\"> { $d/root/implicit-base } </copy> \n" +
      "      return fn:base-uri(($y/implicit-base/child)[1])\n" +
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
      assertStringValue(false, "http://www.example.org")
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   *  Evaluation of base-uri function with argument set to a computed constructed Document node with no base-xml argument. Use fn:count .
   */
  @org.junit.Test
  public void fnBaseUri11() {
    final XQuery query = new XQuery(
      "fn:count(fn:base-uri(document {<aDocument>some content</aDocument>}))",
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   *  Evaluation of base-uri function with argument set to a directly constructed element node (via FLOWR expr). Use the xml-base attribute and should ignore declared base uri property. .
   */
  @org.junit.Test
  public void fnBaseUri20() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.org\"; \n" +
      "        let $var := <anElement xml:base=\"http://www.examples.com\">With some content</anElement> \n" +
      "        return fn:string(fn:base-uri($var))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.com/examples")
    );
  }

  /**
   *  Evaluation of base-uri#0 function with context item set to a directly constructed comment .
   */
  @org.junit.Test
  public void fnBaseUri24() {
    final XQuery query = new XQuery(
      "(<!-- A comment -->)/base-uri()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of base-uri#0 function with context item set to a computed constructed comment .
   */
  @org.junit.Test
  public void fnBaseUri25() {
    final XQuery query = new XQuery(
      "(comment {\"A Comment Node \"})/fn:base-uri()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of base-uri#0 function with context item set to a computed constructed Text node. .
   */
  @org.junit.Test
  public void fnBaseUri26() {
    final XQuery query = new XQuery(
      "(text {\"A Text Node\"})/fn:base-uri()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of base-uri#0 function with context item set to a computed constructed Element node with not base-xml argument. .
   */
  @org.junit.Test
  public void fnBaseUri27() {
    final XQuery query = new XQuery(
      "fn:count((element anElement {\"An Element Node\"})/base-uri())",
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
        assertEq("1")
      ||
        assertEq("0")
      )
    );
  }

  /**
   *  Evaluation of base-uri#0 function with context item set to a directly constructed Element node with not base-xml argument. Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnBaseUri28() {
    final XQuery query = new XQuery(
      "fn:count((<anElement>Element content</anElement>)/fn:base-uri())",
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
        assertEq("1")
      ||
        assertEq("0")
      )
    );
  }

  /**
   *  Evaluation of base-uri#0 function with context item set to a directly constructed Element node with base-xml argument. Use fn:string .
   */
  @org.junit.Test
  public void fnBaseUri29() {
    final XQuery query = new XQuery(
      "fn:string((<anElement xml:base=\"http://example.com/examples\">Element content</anElement>)/fn:base-uri())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   *  Evaluation of base-uri#0 function with context item set to a directly constructed Element node with base-xml argument (no escaping). Use fn:string .
   */
  @org.junit.Test
  public void fnBaseUri30() {
    final XQuery query = new XQuery(
      "fn:string((<anElement xml:base=\"http://www.example.com\">Element content</anElement>)/base-uri())",
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
   *  Evaluation of base-uri#0 function with context item set to a computed constructed Document node with no base-xml argument. Use fn:count .
   */
  @org.junit.Test
  public void fnBaseUri31() {
    final XQuery query = new XQuery(
      "fn:count((document {<aDocument>some content</aDocument>})/base-uri())",
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
        assertEq("1")
      )
    );
  }

  /**
   *  Evaluation of base-uri#0 function with context item set to a computed constructed Document node argument. Uses declared base uri property Use fn:string .
   */
  @org.junit.Test
  public void fnBaseUri32() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.org\"; \n" +
      "            fn:string((document {<aDocument>some content</aDocument>})/base-uri())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.org")
    );
  }

  /**
   *  Evaluation of base-uri#0 function with context item set to a directly constructed element node argument. Should not declared base uri property Use fn:string .
   */
  @org.junit.Test
  public void fnBaseUri33() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.org\"; fn:string((<anElement>some content</anElement>)/fn:base-uri())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.org")
    );
  }

  /**
   *  Evaluation of base-uri#0 function with context item set to a computed constructed attribute node argument. .
   */
  @org.junit.Test
  public void fnBaseUri34() {
    final XQuery query = new XQuery(
      "(attribute anAttribute{\"attribute value\"})/fn:base-uri()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of base-uri#0 function with context item set to a directly constructed PI node argument. .
   */
  @org.junit.Test
  public void fnBaseUri35() {
    final XQuery query = new XQuery(
      "(<?format role=\"output\" ?>)/fn:base-uri()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of base-uri#0 function with context item set to a computed constructed PI node argument. Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnBaseUri36() {
    final XQuery query = new XQuery(
      "(processing-instruction {\"PItarget\"} {\"PIcontent\"})/base-uri()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of base-uri#0 function with context item set to a computed constructed PI node argument. Use fn:count to avoid empty file. Should not use the declare base-uri .
   */
  @org.junit.Test
  public void fnBaseUri37() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare base-uri \"http://example.org\"; \n" +
      "         (processing-instruction {\"PItarget\"} {\"PIcontent\"})/base-uri()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of base-uri#0 function with context item set to a directly constructed element node (via FLOWR expr). Use the declare base-uri .
   */
  @org.junit.Test
  public void fnBaseUri38() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.org\"; \n" +
      "        let $var := <anElement>With some contexnt</anElement> \n" +
      "        return fn:string(($var)/base-uri())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.org")
    );
  }

  /**
   *  Evaluation of base-uri#0 function with context item set to a directly constructed element node (via FLOWR expr). Use the xml-base attribute .
   */
  @org.junit.Test
  public void fnBaseUri39() {
    final XQuery query = new XQuery(
      "let $var := <anElement xml:base=\"http://www.examples.com\">With some content</anElement> \n" +
      "        return fn:string(($var)/base-uri())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.examples.com")
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of base-uri#0 function with context item set to a directly constructed element node (via FLOWR expr). Use the xml-base attribute and should ignore declared base uri property. .
   */
  @org.junit.Test
  public void fnBaseUri40() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.org\"; \n" +
      "        let $var := <anElement xml:base=\"http://www.examples.com\">With some content</anElement> \n" +
      "        return fn:string(($var)/base-uri())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.examples.com")
    );
  }

  /**
   *  Evaluation of base-uri#0 function with context item set to a directly constructed comment node (via FLOWR expr). Should ignore declared base uri property. .
   */
  @org.junit.Test
  public void fnBaseUri41() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.org\"; \n" +
      "        let $var := <!-- A Comment --> return ($var)/base-uri()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of base-uri#0 function using undefined context item. .
   */
  @org.junit.Test
  public void fnBaseUri42() {
    final XQuery query = new XQuery(
      "declare namespace eg = \"http://example.org\"; \n" +
      "        declare function eg:noContextFunction() { fn:base-uri() }; \n" +
      "        eg:noContextFunction()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Evaluation of base-uri#0 function with context item set to a directly constructed Element node with base-xml argument that needs escaping. Uses fn:string. .
   */
  @org.junit.Test
  public void fnBaseUri43() {
    final XQuery query = new XQuery(
      "fn:string((<anElement xml:base=\"http://example.com/examples\">Element content</anElement>)/fn:base-uri())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.com/examples")
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.com/examples")
    );
  }
}
