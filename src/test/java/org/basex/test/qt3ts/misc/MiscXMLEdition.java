package org.basex.test.qt3ts.misc;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the XMLEdition.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MiscXMLEdition extends QT3TestSet {

  /**
   * CDATA sections, comments and PIs may occur in ANY content. in XML 1.0 3th edition and older .
   */
  @org.junit.Test
  public void xML103edMixedContent() {
    final XQuery query = new XQuery(
      "<foo> a <![CDATA[cdata section]]> in mixed content. a <!-- comment --> in mixed content. a <?processing instruction?> in mixed content. </foo>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<foo> a cdata section in mixed content. a <!-- comment --> in mixed content. a <?processing instruction?> in mixed content. </foo>", false)
    );
  }

  /**
   *  The character #x037F is excluded from the start of a Name in XML 1.0 4th edition and older .
   */
  @org.junit.Test
  public void xML104edExcludedChar1() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\" encoding \"utf-8\"; <\u037fnode/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  The character #x037F is allowed at the start of a Name in XML 1.0 5th edition and later .
   */
  @org.junit.Test
  public void xML104edExcludedChar1New() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\" encoding \"utf-8\"; <\u037fnode/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<\u037fnode/>", false)
    );
  }

  /**
   * The character #x0100 is excluded from the start of a Name in XML 1.0 4th edition and older.
   *         Note: the query is in a separate file because it cannot be represented in an XML document..
   */
  @org.junit.Test
  public void xML104edExcludedChar2() {
    final XQuery query = new XQuery(
      "(: Name: Excluded-char-2 :)\n" +
      "(: Written by: Nicolae Brinza :)\n" +
      "(: Description: The character #x0100 is excluded from the start of a Name :)\n" +
      "(:              in XML 1.0 4th edition and older                          :)\n" +
      "\n" +
      "xquery version \"1.0\" encoding \"utf-8\";\n" +
      "\n" +
      "<\u0001\u0000node/>\n" +
      "",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   * Written by: Tim Kraska  Contains a DEL, legal in XML 1.0, illegal in XML 1.1 .
   */
  @org.junit.Test
  public void xML105edIncludedChar1() {
    final XQuery query = new XQuery(
      "<foo>\u007f</foo>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<foo>&#x7f;</foo>", false)
    );
  }

  /**
   * Written by: Tim Kraska  Has a "long s" in a name, legal in XML 1.1, illegal in XML 1.0 ed 4 and earlier.
   */
  @org.junit.Test
  public void xML111edIncludedChar1() {
    final XQuery query = new XQuery(
      "<eggſ/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   * Written by: Tim Kraska  Has a "long s" in a name, legal in XML 1.1, illegal in XML 1.0 ed 4 and earlier.
   */
  @org.junit.Test
  public void xML111edIncludedChar1New() {
    final XQuery query = new XQuery(
      "<eggſ/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<eggſ/>", false)
    );
  }

  /**
   * Normalization of line endings in XQuery.
   */
  @org.junit.Test
  public void lineEndingQ001() {
    final XQuery query = new XQuery(
      "deep-equal(string-to-codepoints('\n" +
      "'), (10))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Normalization of line endings in XQuery.
   */
  @org.junit.Test
  public void lineEndingQ002() {
    final XQuery query = new XQuery(
      "deep-equal(string-to-codepoints('\r\n" +
      "'), (10))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Normalization of line endings in XQuery.
   */
  @org.junit.Test
  public void lineEndingQ003() {
    final XQuery query = new XQuery(
      "deep-equal(string-to-codepoints('\r \r\n" +
      " \n" +
      "\r'), (10, 32, 10, 32, 10, 10))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Normalization of line endings in XQuery with XML 1.0.
   */
  @org.junit.Test
  public void lineEndingQ007() {
    final XQuery query = new XQuery(
      "deep-equal(string-to-codepoints('\r\u0085'), (10, 133))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Normalization of line endings in XQuery with XML 1.1.
   */
  @org.junit.Test
  public void lineEndingQ008() {
    final XQuery query = new XQuery(
      "deep-equal(string-to-codepoints(' \u0085 '), (32, 133, 32))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Normalization of line endings in XQuery with XML 1.1.
   */
  @org.junit.Test
  public void lineEndingQ009() {
    final XQuery query = new XQuery(
      "deep-equal(string-to-codepoints(' \u2028 '), (32, 8232, 32))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }
}
