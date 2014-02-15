package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the fn:parse-xml-fragment function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnParseXmlFragment extends QT3TestSet {

  /**
   * parse-xml-fragment test.
   */
  @org.junit.Test
  public void parseXmlFragment001() {
    final XQuery query = new XQuery(
      "\n" +
      "        \tparse-xml-fragment(unparsed-text(\"../docs/atomic.xml\"))/*\n" +
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
      assertType("element(*,xs:untyped)")
    );
  }

  /**
   * parse-xml-fragment test - two args not allowed.
   */
  @org.junit.Test
  public void parseXmlFragment002() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(unparsed-text(\"../docs/atomic.xml\"),'###/atomic.xml')",
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
   * parse-xml-fragment test - two args not allowed.
   */
  @org.junit.Test
  public void parseXmlFragment003() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(unparsed-text(\"../docs/atomic.xml\"),'file:/test/fots/../docs/atomic.xml')",
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
   * parse-xml-fragment test - invalid XML document.
   */
  @org.junit.Test
  public void parseXmlFragment004() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(\"<a>Test123\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0006")
    );
  }

  /**
   * parse-xml-fragment test - empty string is OK.
   */
  @org.junit.Test
  public void parseXmlFragment005() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(\"\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
   * parse-xml-fragment test - text only is OK.
   */
  @org.junit.Test
  public void parseXmlFragment006() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(\"vanessa\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "vanessa")
    );
  }

  /**
   * parse-xml-fragment test - multiple elements are OK.
   */
  @org.junit.Test
  public void parseXmlFragment007() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(\"<a/><b/><c/>\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/><b/><c/>", false)
    );
  }

  /**
   * parse-xml-fragment test - whitespace is preserved.
   */
  @org.junit.Test
  public void parseXmlFragment008() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(\"  \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "  ")
    );
  }

  /**
   * parse-xml-fragment test - whitespace is preserved.
   */
  @org.junit.Test
  public void parseXmlFragment009() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(\"<a> </a> <b> </b>\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a> </a> <b> </b>", false)
    );
  }

  /**
   * parse-xml-fragment test - comments and PIs are OK.
   */
  @org.junit.Test
  public void parseXmlFragment010() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(\"<a/><!--comment--><?PI?><b/>\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/><!--comment--><?PI?><b/>", false)
    );
  }

  /**
   * parse-xml-fragment test - built-in entity references are OK.
   */
  @org.junit.Test
  public void parseXmlFragment011() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(codepoints-to-string((38, 108, 116, 59)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "<")
    );
  }

  /**
   * parse-xml-fragment test - numeric character references are OK.
   */
  @org.junit.Test
  public void parseXmlFragment012() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(codepoints-to-string((38, 35, 51, 56, 59)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "&")
    );
  }

  /**
   * parse-xml-fragment test - text declaration is OK.
   */
  @org.junit.Test
  public void parseXmlFragment013() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(\"<?xml version='1.0' encoding='utf-8'?><a/>\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/>", false)
    );
  }

  /**
   * parse-xml-fragment test - text declaration on its own is OK.
   */
  @org.junit.Test
  public void parseXmlFragment014() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(\"<?xml version='1.0' encoding='utf-8'?>\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
   * parse-xml-fragment test - text declaration plus text on its own is OK.
   */
  @org.junit.Test
  public void parseXmlFragment015() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(\"<?xml version='1.0' encoding='utf-8'?>abc\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc")
    );
  }

  /**
   * parse-xml-fragment test - encoding attribute in a text declaration is mandatory.
   */
  @org.junit.Test
  public void parseXmlFragment016() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(\"<?xml version='1.0'?><a/>\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0006")
    );
  }

  /**
   * parse-xml-fragment test - standalone attribute in a text declaration is disallowed.
   */
  @org.junit.Test
  public void parseXmlFragment017() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(\"<?xml version='1.0' encoding='utf-8' standalone='yes'?><a/>\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0006")
    );
  }

  /**
   * parse-xml-fragment test - tags must be balanced.
   */
  @org.junit.Test
  public void parseXmlFragment018() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(\"<a>\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0006")
    );
  }

  /**
   * parse-xml-fragment test - namespaces must be declared.
   */
  @org.junit.Test
  public void parseXmlFragment019() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(\"<p:a/>\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0006")
    );
  }

  /**
   * parse-xml-fragment test - DOCTYPE is not allowed.
   */
  @org.junit.Test
  public void parseXmlFragment020() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(\"<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN'\n" +
      "                                           'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'><html/>\")\n" +
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
      error("FODC0006")
    );
  }

  /**
   * parse-xml-fragment test - line endings are normalized.
   */
  @org.junit.Test
  public void parseXmlFragment021() {
    final XQuery query = new XQuery(
      "string-to-codepoints(parse-xml-fragment(\"a\"||codepoints-to-string((13, 10))||\"b\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "97 10 98")
    );
  }

  /**
   * parse-xml-fragment test - result is parentless.
   */
  @org.junit.Test
  public void parseXmlFragment022() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(\"<a/>\")/..",
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
   * parse-xml-fragment test - result is a document node.
   */
  @org.junit.Test
  public void parseXmlFragment023() {
    final XQuery query = new XQuery(
      "parse-xml-fragment(\"<a/>\") instance of document-node()",
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
}
