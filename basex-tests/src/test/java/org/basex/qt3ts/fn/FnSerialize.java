package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the fn:serialize() function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnSerialize extends QT3TestSet {

  /**
   * serialize test: New suppress-indentation parameter.
   */
  @org.junit.Test
  public void serializeXml008() {
    final XQuery query = new XQuery(
      "\n" +
      "          let $params := \n" +
      "              <output:serialization-parameters\n" +
      "                   xmlns:output=\"http://www.w3.org/2010/xslt-xquery-serialization\">\n" +
      "                <output:method value=\"xml\"/>   \n" +
      "                <output:indent value=\"yes\"/>\n" +
      "                <output:suppress-indentation value=\"p\"/>\n" +
      "              </output:serialization-parameters>\n" +
      "          return serialize(., $params)\n" +
      "        ",
      ctx);
    try {
      query.context(node(file("fn/serialize/serialize-008-src.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
   * Serialize a function item.
   */
  @org.junit.Test
  public void serializeXml010() {
    final XQuery query = new XQuery(
      "serialize(name#1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("SENR0001")
    );
  }
}
