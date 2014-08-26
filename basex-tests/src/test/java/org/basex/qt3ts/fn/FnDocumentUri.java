package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the document-uri() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnDocumentUri extends QT3TestSet {

  /**
   *  A test whose essence is: `document-uri((), "wrong param")`. .
   */
  @org.junit.Test
  public void kDocumentURIFunc1() {
    final XQuery query = new XQuery(
      "document-uri((), \"wrong param\")",
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
   *  A test whose essence is: `document-uri()`. .
   */
  @org.junit.Test
  public void kDocumentURIFunc2() {
    final XQuery query = new XQuery(
      "document-uri()",
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
   *  A test whose essence is: `document-uri()`. .
   */
  @org.junit.Test
  public void kDocumentURIFunc2a() {
    final XQuery query = new XQuery(
      "ends-with(document-uri(),\"works-mod.xml\")",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
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
   *  A test whose essence is: `document-uri()`. .
   */
  @org.junit.Test
  public void kDocumentURIFunc2b() {
    final XQuery query = new XQuery(
      "document-uri()",
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
   *  A test whose essence is: `empty(document-uri(()))`. .
   */
  @org.junit.Test
  public void kDocumentURIFunc3() {
    final XQuery query = new XQuery(
      "empty(document-uri(()))",
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
  public void k2DocumentURIFunc1() {
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
   *  Invoke on a tree document node with fn:root(). .
   */
  @org.junit.Test
  public void k2DocumentURIFunc10() {
    final XQuery query = new XQuery(
      "let $i := document { <e> <a/> <a/> <a/> <b/> <b/> <a/> <a/> </e> } return empty(document-uri(root(($i/a/b)[1])))",
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
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Ensure that the return value of document-uri() is of correct type. .
   */
  @org.junit.Test
  public void k2DocumentURIFunc11() {
    final XQuery query = new XQuery(
      "for $i in (1, document-uri(.), 3) return typeswitch($i) case xs:anyURI return \"xs:anyURI\" case xs:integer return \"int\" default return \"FAILURE\"",
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
      assertStringValue(false, "int xs:anyURI int")
    );
  }

  /**
   *  Invoke on an attribute node. .
   */
  @org.junit.Test
  public void k2DocumentURIFunc2() {
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
   *  Invoke on a processing instruction node. .
   */
  @org.junit.Test
  public void k2DocumentURIFunc3() {
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
   *  Invoke on a processing instruction node. .
   */
  @org.junit.Test
  public void k2DocumentURIFunc4() {
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
  public void k2DocumentURIFunc5() {
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
  public void k2DocumentURIFunc6() {
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
  public void k2DocumentURIFunc7() {
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
  public void k2DocumentURIFunc8() {
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
   *  Invoke on a tree document node. .
   */
  @org.junit.Test
  public void k2DocumentURIFunc9() {
    final XQuery query = new XQuery(
      "let $i := document { <e> <a/> <a/> <a/> <b/> <b/> <a/> <a/> </e> } return empty(document-uri($i))",
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
   * fn:document-uri with zero arguments given context via a predicate - predicate should be () and hense false in all cases except the document root.
   */
  @org.junit.Test
  public void fnDocumentUri0Ok() {
    final XQuery query = new XQuery(
      "empty(//works[fn:document-uri()])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
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
   *  Evaluation of fn:document-uri with incorrect arity. .
   */
  @org.junit.Test
  public void fnDocumentUri1() {
    final XQuery query = new XQuery(
      "fn:document-uri(<element1>contenty</element1>,\"Argument 2\")",
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
   *  Evaluation of fn:document-uri with argument set to a directly constructed element node. Use the fn:count to avoid the empty file. .
   */
  @org.junit.Test
  public void fnDocumentUri10() {
    final XQuery query = new XQuery(
      "fn:count(fn:document-uri(<anElement>element content</anElement>))",
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
   *  Evaluation of fn:document-uri with argument set to a directly constructed document node. Use the fn:count to avoid the empty file. .
   */
  @org.junit.Test
  public void fnDocumentUri11() {
    final XQuery query = new XQuery(
      "fn:count(fn:document-uri(document {<anElement>element content</anElement>}))",
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
   *  Evaluation of fn:document-uri with argument set to document node from xml file. .
   */
  @org.junit.Test
  public void fnDocumentUri12() {
    final XQuery query = new XQuery(
      "fn:contains(fn:document-uri(fn:doc($uri)),$uri) or (fn:document-uri(fn:doc($uri)) = \"\")",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(true)
      ||
        assertBoolean(false)
      )
    );
  }

  /**
   *  Evaluation of fn:document-uri with argument set to element node from xml file. Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnDocumentUri13() {
    final XQuery query = new XQuery(
      "fn:count(fn:document-uri(/works[1]/employee[1]))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
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
   *  Evaluation of fn:document-uri with argument set to an attribute node from xml file. Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnDocumentUri14() {
    final XQuery query = new XQuery(
      "fn:count(fn:document-uri(/works[1]/employee[1]/@name))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
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
   *  Evaluation of fn:document-uri used as argument to an fn:string-length function. .
   */
  @org.junit.Test
  public void fnDocumentUri15() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:string(fn:contains(fn:document-uri(fn:doc($uri)),$uri)))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("4")
      ||
        assertEq("5")
      )
    );
  }

  /**
   *  Evaluation of fn:document-uri used as argument to an fn:upper-case function. .
   */
  @org.junit.Test
  public void fnDocumentUri16() {
    final XQuery query = new XQuery(
      "fn:upper-case(fn:string(fn:contains(fn:document-uri(fn:doc($uri)),$uri)))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "TRUE")
      ||
        assertStringValue(false, "FALSE")
      )
    );
  }

  /**
   *  Evaluation of fn:document-uri used as argument to an fn:lower-case function. .
   */
  @org.junit.Test
  public void fnDocumentUri17() {
    final XQuery query = new XQuery(
      "fn:lower-case(fn:string(fn:contains(fn:document-uri(fn:doc($uri)),$uri)))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "true")
      ||
        assertStringValue(false, "false")
      )
    );
  }

  /**
   *  Evaluation of fn:document-uri used as argument to an fn:concat function. .
   */
  @org.junit.Test
  public void fnDocumentUri18() {
    final XQuery query = new XQuery(
      "fn:concat(fn:string(fn:contains(fn:document-uri(fn:doc($uri)),$uri)),\" A String\")",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "true A String")
      ||
        assertStringValue(false, "false A String")
      )
    );
  }

  /**
   * Evaluation of fn:document-uri used as argument to an fn:string-join function..
   */
  @org.junit.Test
  public void fnDocumentUri19() {
    final XQuery query = new XQuery(
      "fn:string-join((fn:string(fn:contains(fn:document-uri(fn:doc($uri)),$uri)),\" A String\"),\"\")",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "true A String")
      ||
        assertStringValue(false, "false A String")
      )
    );
  }

  /**
   *  Evaluation of fn:document-uri with argument set to empty sequence. Use the fn:count to avoid the empty file. .
   */
  @org.junit.Test
  public void fnDocumentUri2() {
    final XQuery query = new XQuery(
      "fn:count(fn:document-uri(()))",
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
   *  Evaluation of fn:document-uri used as argument to an fn:substring-before function. .
   */
  @org.junit.Test
  public void fnDocumentUri20() {
    final XQuery query = new XQuery(
      "fn:substring-before(fn:string(fn:contains(fn:document-uri(/),\"works-mod\")),\"e\")",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "tru")
    );
  }

  /**
   *  Evaluation of fn:document-uri used as argument to an fn:substring-after function. Use string. .
   */
  @org.junit.Test
  public void fnDocumentUri21() {
    final XQuery query = new XQuery(
      "fn:substring-after(fn:string(fn:contains(fn:document-uri(/),\"works-mod\")),\"t\")",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "rue")
    );
  }

  /**
   *  Evaluation of fn:document-uri#0 with context item set to a computed attribute node. .
   */
  @org.junit.Test
  public void fnDocumentUri22() {
    final XQuery query = new XQuery(
      "(attribute anAttribute {\"an attribute node\"})/document-uri()",
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
   *  Evaluation of fn:document-uri#0 with context item set to a computed PI node. .
   */
  @org.junit.Test
  public void fnDocumentUri23() {
    final XQuery query = new XQuery(
      "(processing-instruction {\"PITarget\"} {\"PIContent\"})/document-uri()",
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
   *  Evaluation of fn:document-uri#0 with context item set to a directly constructed PI node. .
   */
  @org.junit.Test
  public void fnDocumentUri24() {
    final XQuery query = new XQuery(
      "(<?audio-output beep?>)/document-uri()",
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
   *  Evaluation of fn:document-uri#0 with context item set to a directly constructed comment node. .
   */
  @org.junit.Test
  public void fnDocumentUri25() {
    final XQuery query = new XQuery(
      "(<!-- A comment node -->)/document-uri()",
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
   *  Evaluation of fn:document-uri#0 with context item set to a directly constructed element node. .
   */
  @org.junit.Test
  public void fnDocumentUri26() {
    final XQuery query = new XQuery(
      "(<anElement>element content</anElement>)/document-uri()",
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
   *  Evaluation of fn:document-uri#0 with context item set to a directly constructed document node. .
   */
  @org.junit.Test
  public void fnDocumentUri27() {
    final XQuery query = new XQuery(
      "(document {<anElement>element content</anElement>})/document-uri()",
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
   *  Evaluation of fn:document-uri#0 with context item set to document node from xml file. .
   */
  @org.junit.Test
  public void fnDocumentUri28() {
    final XQuery query = new XQuery(
      "fn:contains((fn:doc($uri))/document-uri(),$uri) or ((fn:doc($uri))/document-uri() = \"\")",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(true)
      ||
        assertBoolean(false)
      )
    );
  }

  /**
   *  Evaluation of fn:document-uri#0 with context item set to element node from xml file. Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnDocumentUri29() {
    final XQuery query = new XQuery(
      "fn:count((/works[1]/employee[1])/document-uri())",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
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
   *  Evaluation of fn:document-uri with argument set to a computed element node. Use the fn:count to avoid the empty file. .
   */
  @org.junit.Test
  public void fnDocumentUri3() {
    final XQuery query = new XQuery(
      "fn:count(fn:document-uri(element anElement {\"some content\"}))",
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
   *  Evaluation of fn:document-uri#0 with context item set to an attribute node from xml file. Use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnDocumentUri30() {
    final XQuery query = new XQuery(
      "fn:count((/works[1]/employee[1]/@name)/document-uri())",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
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
   *  Evaluation of fn:document-uri#0 used as argument to an fn:string-length function. .
   */
  @org.junit.Test
  public void fnDocumentUri31() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:string(fn:contains((fn:doc($uri))/document-uri(),$uri)))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("4")
      ||
        assertEq("5")
      )
    );
  }

  /**
   *  Evaluation of fn:document-uri#0 used as argument to an fn:upper-case function. .
   */
  @org.junit.Test
  public void fnDocumentUri32() {
    final XQuery query = new XQuery(
      "fn:upper-case(fn:string(fn:contains((fn:doc($uri))/document-uri(),$uri)))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "TRUE")
      ||
        assertStringValue(false, "FALSE")
      )
    );
  }

  /**
   *  Evaluation of fn:document-uri#0 used as argument to an fn:lower-case function. .
   */
  @org.junit.Test
  public void fnDocumentUri33() {
    final XQuery query = new XQuery(
      "fn:lower-case(fn:string(fn:contains((fn:doc($uri))/document-uri(),$uri)))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "true")
      ||
        assertStringValue(false, "false")
      )
    );
  }

  /**
   *  Evaluation of fn:document-uri#0 used as argument to an fn:concat function. .
   */
  @org.junit.Test
  public void fnDocumentUri34() {
    final XQuery query = new XQuery(
      "fn:concat(fn:string(fn:contains((fn:doc($uri))/document-uri(),$uri)),\" A String\")",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "true A String")
      ||
        assertStringValue(false, "false A String")
      )
    );
  }

  /**
   * Evaluation of fn:document-uri#0 used as argument to an fn:string-join function..
   */
  @org.junit.Test
  public void fnDocumentUri35() {
    final XQuery query = new XQuery(
      "fn:string-join((fn:string(fn:contains((fn:doc($uri))/document-uri(),$uri)),\" A String\"),\"\")",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.bind("uri", new XQuery("'http://www.w3.org/fots/docs/works-mod.xml'", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "true A String")
      ||
        assertStringValue(false, "false A String")
      )
    );
  }

  /**
   *  Evaluation of fn:document-uri#0 used as argument to an fn:substring-before function. .
   */
  @org.junit.Test
  public void fnDocumentUri36() {
    final XQuery query = new XQuery(
      "fn:substring-before(fn:string(fn:contains((/)/document-uri(),\"works-mod\")),\"e\")",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "tru")
    );
  }

  /**
   *  Evaluation of fn:document-uri#0 used as argument to an fn:substring-after function. Use string. .
   */
  @org.junit.Test
  public void fnDocumentUri37() {
    final XQuery query = new XQuery(
      "fn:substring-after(fn:string(fn:contains((/)/document-uri(),\"works-mod\")),\"t\")",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "rue")
    );
  }

  /**
   *  Evaluation of fn:document-uri with argument set to a computed comment node. Use the fn:count to avoid the empty file. .
   */
  @org.junit.Test
  public void fnDocumentUri4() {
    final XQuery query = new XQuery(
      "fn:count(fn:document-uri(comment {\"a comment node\"}))",
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
   *  Evaluation of fn:document-uri with argument set to a computed text node. Use the fn:count to avoid the empty file. .
   */
  @org.junit.Test
  public void fnDocumentUri5() {
    final XQuery query = new XQuery(
      "fn:count(fn:document-uri(text {\"a text node\"}))",
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
   *  Evaluation of fn:document-uri with argument set to a computed attribute node. Use the fn:count to avoid the empty file. .
   */
  @org.junit.Test
  public void fnDocumentUri6() {
    final XQuery query = new XQuery(
      "fn:count(fn:document-uri(attribute anAttribute {\"an attribute node\"}))",
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
   *  Evaluation of fn:document-uri with argument set to a computed PI node. Use the fn:count to avoid the empty file. .
   */
  @org.junit.Test
  public void fnDocumentUri7() {
    final XQuery query = new XQuery(
      "fn:count(fn:document-uri(processing-instruction {\"PITarget\"} {\"PIContent\"}))",
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
   *  Evaluation of fn:document-uri with argument set to a directly constructed PI node. Use the fn:count to avoid the empty file. .
   */
  @org.junit.Test
  public void fnDocumentUri8() {
    final XQuery query = new XQuery(
      "fn:count(fn:document-uri(<?audio-output beep?>))",
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
   *  Evaluation of fn:document-uri with argument set to a directly constructed comment node. Use the fn:count to avoid the empty file. .
   */
  @org.junit.Test
  public void fnDocumentUri9() {
    final XQuery query = new XQuery(
      "fn:count(fn:document-uri(<!-- A comment node -->))",
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
}
