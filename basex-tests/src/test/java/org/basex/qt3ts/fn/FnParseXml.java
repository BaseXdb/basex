package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the fn:parse-xml function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnParseXml extends QT3TestSet {

  /**
   * parse-xml test.
   */
  @org.junit.Test
  public void parseXml001() {
    final XQuery query = new XQuery(
      "parse-xml(unparsed-text(\"../docs/atomic.xml\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("document-node(element(*,xs:untyped))")
    );
  }

  /**
   * parse-xml test - with invalid absolute URI.
   */
  @org.junit.Test
  public void parseXml002() {
    final XQuery query = new XQuery(
      "parse-xml(unparsed-text(\"../docs/atomic.xml\"),'###/atomic.xml')",
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
   * parse-xml test - with valid absolute URI.
   */
  @org.junit.Test
  public void parseXml003() {
    final XQuery query = new XQuery(
      "parse-xml(unparsed-text(\"../docs/atomic.xml\"),'file:/test/fots/../docs/atomic.xml')",
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
   * parse-xml test - invalid XML document.
   */
  @org.junit.Test
  public void parseXml004() {
    final XQuery query = new XQuery(
      "parse-xml(\"<a>Test123\")",
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
   * parse-xml test - with XML declaration.
   */
  @org.junit.Test
  public void parseXml005() {
    final XQuery query = new XQuery(
      "parse-xml(\"<?xml version='1.0' encoding='iso-8859-1'?><a>foo</a>\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>foo</a>", false)
    );
  }

  /**
   * parse-xml test - with local DTD.
   */
  @org.junit.Test
  public void parseXml006() {
    final XQuery query = new XQuery(
      "parse-xml(\"<?xml version='1.0' encoding='iso-8859-1'?><!DOCTYPE a [<!ELEMENT a (#PCDATA)>]><a>foo</a>\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>foo</a>", false)
    );
  }

  /**
   * parse-xml test - base URI of result.
   */
  @org.junit.Test
  public void parseXml007() {
    final XQuery query = new XQuery(
      "base-uri(parse-xml(\"<a>foo</a>\")) eq static-base-uri()",
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
   * parse-xml test - with external DTD.
   */
  @org.junit.Test
  public void parseXml008() {
    final XQuery query = new XQuery(
      "parse-xml(\"<!DOCTYPE a SYSTEM 'parse-xml/a.dtd'><a>foo</a>\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>foo</a>", false)
    );
  }

  /**
   * parse-xml test - with XML declaration and external DTD.
   */
  @org.junit.Test
  public void parseXml009() {
    final XQuery query = new XQuery(
      "parse-xml(\"<?xml version='1.0' encoding='iso-8859-1'?><!DOCTYPE a SYSTEM 'parse-xml/a.dtd'><a>foo</a>\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a>foo</a>", false)
    );
  }

  /**
   * parse-xml test - with external entity.
   */
  @org.junit.Test
  public void parseXml010() {
    final XQuery query = new XQuery(
      "parse-xml(\"<!DOCTYPE a [<!ELEMENT a (#PCDATA)><!ENTITY foo SYSTEM 'parse-xml/foo.entity'>]><a>\" ||\n" +
      "            codepoints-to-string(38) || \"foo;</a>\")\n" +
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
      assertSerialization("<a><bar>baz</bar></a>", false)
    );
  }

  /**
   * parse-xml test - document-uri() of result is absent.
   */
  @org.junit.Test
  public void parseXml011() {
    final XQuery query = new XQuery(
      "document-uri(parse-xml(\"<a>foo</a>\"))",
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
   * parse-xml test - determinism of result is implementation-defined.
   */
  @org.junit.Test
  public void parseXml012() {
    final XQuery query = new XQuery(
      "parse-xml(\"<a>foo</a>\") is parse-xml(\"<a>foo</a>\")",
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
        assertBoolean(false)
      )
    );
  }

  /**
   * parse-xml test - with local DTD - invalid against DTD.
   */
  @org.junit.Test
  public void parseXml013() {
    final XQuery query = new XQuery(
      "parse-xml(\"<!DOCTYPE a [<!ELEMENT a (#PCDATA)>]><a><b/></a>\")",
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
        assertSerialization("<a><b/></a>", false)
      ||
        error("FODC0006")
      )
    );
  }

  /**
   * parse-xml test - result is a document node.
   */
  @org.junit.Test
  public void parseXml014() {
    final XQuery query = new XQuery(
      "parse-xml(\"<a>foo</a>\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("document-node()")
    );
  }
}
