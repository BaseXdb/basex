package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the doc-available() function.
 *
 * @author BaseX Team 2005-13, BSD License
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
        error("FODC0005")
      )
    );
  }

  /**
   *  test fn:doc-available on () .
   */
  @org.junit.Test
  public void cbclDocAvailable001() {
    final XQuery query = new XQuery(
      "fn:doc-available( () )",
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
   *  test fn:doc-available on invalid input .
   */
  @org.junit.Test
  public void cbclDocAvailable002() {
    final XQuery query = new XQuery(
      "fn:doc-available( '%gg' )",
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
        error("FODC0005")
      ||
        assertBoolean(false)
      )
    );
  }

  /**
   *  test fn:doc-available on a URI which is not that of a document .
   */
  @org.junit.Test
  public void cbclDocAvailable003() {
    final XQuery query = new XQuery(
      "fn:doc-available( 'collection1' )",
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
   *  Test doc-available with a URL .
   */
  @org.junit.Test
  public void cbclDocAvailable004() {
    final XQuery query = new XQuery(
      "doc-available(\"%gg\")",
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
        error("FODC0005")
      ||
        assertBoolean(false)
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
   *  Evaluation of ana fn:doc-available function with wrong argument type. .
   */
  @org.junit.Test
  public void fnDocAvailable2() {
    final XQuery query = new XQuery(
      "fn:doc-available(xs:integer(2))",
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
   *  Check that a document with duplicated xml:id attributes is flagged. .
   */
  @org.junit.Test
  public void fnDocAvailable3() {
    final XQuery query = new XQuery(
      "fn:doc-available($uri)",
      ctx);
    try {
      query.addDocument("id/XMLIdDuplicated.xml", file("fn/id/XMLIdDuplicated.xml"));
      query.bind("uri", new XQuery("'id/XMLIdDuplicated.xml'", ctx).value());
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
   *  Check that a document with an invalid xml:id attribute is flagged. .
   */
  @org.junit.Test
  public void fnDocAvailable4() {
    final XQuery query = new XQuery(
      "fn:doc-available($uri)",
      ctx);
    try {
      query.addDocument("id/InvalidXMLId.xml", file("fn/id/InvalidXMLId.xml"));
      query.bind("uri", new XQuery("'id/InvalidXMLId.xml'", ctx).value());
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
   *  Evaluation of fn:doc-available function using document URI of a known document. .
   */
  @org.junit.Test
  public void fnDocAvailable5() {
    final XQuery query = new XQuery(
      "fn:doc-available(document-uri(/))",
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
   *  Evaluation of fn:doc-available function using an empty sequence. .
   */
  @org.junit.Test
  public void fnDocAvailable7() {
    final XQuery query = new XQuery(
      "fn:doc-available(())",
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
   *  Evaluation of fn:doc-available function using a URI that exists but is not XML. .
   */
  @org.junit.Test
  public void fnDocAvailable8() {
    final XQuery query = new XQuery(
      "fn:doc-available(\"../prod/ModuleImport/module1-lib.xq\")",
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
}
