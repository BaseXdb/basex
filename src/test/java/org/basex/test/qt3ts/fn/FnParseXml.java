package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the fn:parse-xml function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0007")
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertType("document-node(element(*,xs:untyped))")
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

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0006")
    );
  }
}
