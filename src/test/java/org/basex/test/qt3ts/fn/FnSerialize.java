package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the fn:serialize() function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnSerialize extends QT3TestSet {

  /**
   * serialize test.
   */
  @org.junit.Test
  public void serializeXml001() {
    final XQuery query = new XQuery(
      "serialize(doc('../docs/atomic.xml'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("contains($result,'atomic')")
    );
  }

  /**
   * serialize test - with invalid attribute node.
   */
  @org.junit.Test
  public void serializeXml002() {
    final XQuery query = new XQuery(
      "serialize((doc('../docs/atomic.xml')//@*)[1])",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("SENR0001")
    );
  }

  /**
   * serialize test - with list of params as nodes.
   */
  @org.junit.Test
  public void serializeXml003() {
    final XQuery query = new XQuery(
      "serialize(doc('../docs/atomic.xml'),\n" +
      "            doc('serialize/serialize-003.xml')/params/*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("contains($result,'atomic')")
    );
  }

  /**
   * serialize test - with list of properties, but no Method set.
   */
  @org.junit.Test
  public void serializeXml004() {
    final XQuery query = new XQuery(
      "serialize(doc('../docs/atomic.xml'),\n" +
      "            doc('serialize/serialize-004.xml')/params/*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("contains($result,'atomic')")
    );
  }

  /**
   * serialize test: Error - specified the use-character-map property.
   */
  @org.junit.Test
  public void serializeXml005() {
    final XQuery query = new XQuery(
      "serialize(doc('../docs/atomic.xml'),\n" +
      "            doc('serialize/serialize-005.xml')/params/*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("SEPM0016")
    );
  }

  /**
   * serialize test: specified the cdata-section-element property, space separated
   *             QNames.
   */
  @org.junit.Test
  public void serializeXml006() {
    final XQuery query = new XQuery(
      "serialize(doc('../docs/atomic.xml'),\n" +
      "            doc('serialize/serialize-006.xml')/params/*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("contains($result,'atomic')")
    );
  }

  /**
   * serialize test: list of properties, but one is an unrecognized name (no error,
   *             it is ignored).
   */
  @org.junit.Test
  public void serializeXml007() {
    final XQuery query = new XQuery(
      "serialize(doc('../docs/atomic.xml'),\n" +
      "            doc('serialize/serialize-007.xml')/params/*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("contains($result,'atomic')")
    );
  }

  /**
   * serialize test: New suppress-indentation parameter.
   */
  @org.junit.Test
  public void serializeXml008() {
    final XQuery query = new XQuery(
      "serialize(doc('serialize/serialize-008-src.xml'),\n" +
      "            doc('serialize/serialize-008-params.xml')/params/*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("matches($result,'\\n\\s+<title>')")
      &&
        assertQuery("matches($result,'\\n\\s+<p>')")
      &&
        assertQuery("not(matches($result,'\\n\\s+<code>'))")
      )
    );
  }

  /**
   * serialize test: Error - bad value for indent parameter.
   */
  @org.junit.Test
  public void serializeXml009() {
    final XQuery query = new XQuery(
      "serialize(doc('../docs/atomic.xml'),\n" +
      "            doc('serialize/serialize-009.xml')/params/*)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("SEPM0016")
    );
  }
}
