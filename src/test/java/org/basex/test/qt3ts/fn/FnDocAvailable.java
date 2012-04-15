package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the doc-available() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnDocAvailable extends QT3TestSet {

  /**
   *  ':/' is an invalid URI, no scheme. Under erratum FO.E26, this may either throw FODC0005, or return false .
   */
  @org.junit.Test
  public void k2SeqDocAvailableFunc1() {
    final XQuery query = new XQuery(
      "doc-available(':/')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(false)
      ||
        error("FODC0005")
      )
    );
  }

  /**
   *  Evaluation of ana fn:doc-available function with wrong arity. .
   */
  @org.junit.Test
  public void fnDocAvailable1() {
    final XQuery query = new XQuery(
      "fn:doc-available(\"http://example.com\",\"string 2\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  Evaluation of ana fn:doc-available function with wrong argument type. .
   */
  @org.junit.Test
  public void fnDocAvailable2() {
    final XQuery query = new XQuery(
      "fn:doc-available(xs:integer(2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Check that a document with duplicated xml:id attributes is flagged. .
   */
  @org.junit.Test
  public void fnDocAvailable3() {
    final XQuery query = new XQuery(
      "fn:doc-available($uri)",
      ctx);
    query.addDocument("id/XMLIdDuplicated.xml", file("fn/id/XMLIdDuplicated.xml"));
    query.bind("uri", new XQuery("'id/XMLIdDuplicated.xml'", ctx).value());

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Check that a document with an invalid xml:id attribute is flagged. .
   */
  @org.junit.Test
  public void fnDocAvailable4() {
    final XQuery query = new XQuery(
      "fn:doc-available($uri)",
      ctx);
    query.addDocument("id/InvalidXMLId.xml", file("fn/id/InvalidXMLId.xml"));
    query.bind("uri", new XQuery("'id/InvalidXMLId.xml'", ctx).value());

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        assertBoolean(false)
      )
    );
  }

  /**
   *  Evaluation of fn:doc-available function using document URI of a known document. .
   */
  @org.junit.Test
  public void fnDocAvailable5() {
    final XQuery query = new XQuery(
      "fn:doc-available(document-uri(/))",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of fn:doc-available function using a URI that does not exist. .
   */
  @org.junit.Test
  public void fnDocAvailable6() {
    final XQuery query = new XQuery(
      "fn:doc-available(\"file:///a/b/c/wefdobqciyvdsoihnfcpinads.xml\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of fn:doc-available function using an empty sequence. .
   */
  @org.junit.Test
  public void fnDocAvailable7() {
    final XQuery query = new XQuery(
      "fn:doc-available(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of fn:doc-available function using a URI that exists but is not XML. .
   */
  @org.junit.Test
  public void fnDocAvailable8() {
    final XQuery query = new XQuery(
      "fn:doc-available(\"../prod/ModuleImport/module1-lib.xq\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }
}
