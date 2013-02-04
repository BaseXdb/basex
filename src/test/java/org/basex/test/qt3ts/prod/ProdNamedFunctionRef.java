package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for "named function reference" construct introduced in XPath 3.0.
 *    Created by Michael Kay by automatic conversion of the tests for function-lookup
 *    produced (mainly) by Tim Mills.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdNamedFunctionRef extends QT3TestSet {

  /**
   * Attempts to look up function fn:node-name..
   */
  @org.junit.Test
  public void functionLiteral001() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}node-name#0)",
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
   * Attempts to invoke function fn:node-name..
   */
  @org.junit.Test
  public void functionLiteral002() {
    final XQuery query = new XQuery(
      "/root/Q{http://www.w3.org/2005/xpath-functions}node-name#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "root")
    );
  }

  /**
   * Attempts to look up function fn:node-name..
   */
  @org.junit.Test
  public void functionLiteral003() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}node-name#1)",
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
   * Attempts to invoke function fn:node-name..
   */
  @org.junit.Test
  public void functionLiteral004() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}node-name#1(/root)",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "root")
    );
  }

  /**
   * Attempts to look up function fn:nilled..
   */
  @org.junit.Test
  public void functionLiteral005() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}nilled#0)",
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
   * Attempts to invoke function fn:nilled..
   */
  @org.junit.Test
  public void functionLiteral006() {
    final XQuery query = new XQuery(
      "/root/Q{http://www.w3.org/2005/xpath-functions}nilled#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "false")
    );
  }

  /**
   * Attempts to look up function fn:nilled..
   */
  @org.junit.Test
  public void functionLiteral007() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}nilled#1)",
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
   * Attempts to invoke function fn:nilled..
   */
  @org.junit.Test
  public void functionLiteral008() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}nilled#1(/root)",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "false")
    );
  }

  /**
   * Attempts to look up function fn:string..
   */
  @org.junit.Test
  public void functionLiteral009() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}string#0)",
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
   * Attempts to invoke function fn:string..
   */
  @org.junit.Test
  public void functionLiteral010() {
    final XQuery query = new XQuery(
      "/root/child/Q{http://www.w3.org/2005/xpath-functions}string#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:string..
   */
  @org.junit.Test
  public void functionLiteral011() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}string#1)",
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
   * Attempts to invoke function fn:string..
   */
  @org.junit.Test
  public void functionLiteral012() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}string#1(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:data..
   */
  @org.junit.Test
  public void functionLiteral013() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}data#0)",
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
   * Attempts to invoke function fn:data..
   */
  @org.junit.Test
  public void functionLiteral014() {
    final XQuery query = new XQuery(
      "/root/child/Q{http://www.w3.org/2005/xpath-functions}data#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:data..
   */
  @org.junit.Test
  public void functionLiteral015() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}data#1)",
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
   * Attempts to invoke function fn:data..
   */
  @org.junit.Test
  public void functionLiteral016() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}data#1(/root/child[1])",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:base-uri..
   */
  @org.junit.Test
  public void functionLiteral017() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}base-uri#0)",
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
   * Attempts to invoke function fn:base-uri..
   */
  @org.junit.Test
  public void functionLiteral018() {
    final XQuery query = new XQuery(
      "/root/Q{http://www.w3.org/2005/xpath-functions}base-uri#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.w3.org/fots/fn/function-lookup/function-lookup.xml")
    );
  }

  /**
   * Attempts to look up function fn:base-uri..
   */
  @org.junit.Test
  public void functionLiteral019() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}base-uri#1)",
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
   * Attempts to invoke function fn:base-uri..
   */
  @org.junit.Test
  public void functionLiteral020() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}base-uri#1(/)",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.w3.org/fots/fn/function-lookup/function-lookup.xml")
    );
  }

  /**
   * Attempts to look up function fn:document-uri..
   */
  @org.junit.Test
  public void functionLiteral021() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}document-uri#0)",
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
   * Attempts to invoke function fn:document-uri..
   */
  @org.junit.Test
  public void functionLiteral022() {
    final XQuery query = new XQuery(
      "/Q{http://www.w3.org/2005/xpath-functions}document-uri#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.w3.org/fots/fn/function-lookup/function-lookup.xml")
    );
  }

  /**
   * Attempts to look up function fn:document-uri..
   */
  @org.junit.Test
  public void functionLiteral023() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}document-uri#1)",
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
   * Attempts to invoke function fn:document-uri..
   */
  @org.junit.Test
  public void functionLiteral024() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}document-uri#1(/)",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.w3.org/fots/fn/function-lookup/function-lookup.xml")
    );
  }

  /**
   * Attempts to look up function fn:error..
   */
  @org.junit.Test
  public void functionLiteral025() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}error#0)",
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
   * Attempts to invoke function fn:error..
   */
  @org.junit.Test
  public void functionLiteral026() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}error#0()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   * Attempts to look up function fn:error..
   */
  @org.junit.Test
  public void functionLiteral027() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}error#1)",
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
   * Attempts to invoke function fn:error..
   */
  @org.junit.Test
  public void functionLiteral028() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}error#1(fn:QName('http://www.w3.org/2005/xqt-errors', 'foo:XXXX0000'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XXXX0000")
    );
  }

  /**
   * Attempts to look up function fn:error..
   */
  @org.junit.Test
  public void functionLiteral029() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}error#2)",
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
   * Attempts to invoke function fn:error..
   */
  @org.junit.Test
  public void functionLiteral030() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}error#2(fn:QName('http://www.w3.org/2005/xqt-errors', 'foo:XXXX0000'), 'string')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XXXX0000")
    );
  }

  /**
   * Attempts to look up function fn:error..
   */
  @org.junit.Test
  public void functionLiteral031() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}error#3)",
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
   * Attempts to invoke function fn:error..
   */
  @org.junit.Test
  public void functionLiteral032() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}error#3(fn:QName('http://www.w3.org/2005/xqt-errors', 'foo:XXXX0000'), 'string', (1, true()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XXXX0000")
    );
  }

  /**
   * Attempts to look up function fn:trace..
   */
  @org.junit.Test
  public void functionLiteral033() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}trace#2)",
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
   * Attempts to invoke function fn:trace..
   */
  @org.junit.Test
  public void functionLiteral034() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}trace#2(1, 'label')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:abs..
   */
  @org.junit.Test
  public void functionLiteral035() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}abs#1)",
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
   * Attempts to invoke function fn:abs..
   */
  @org.junit.Test
  public void functionLiteral036() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}abs#1(-1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:ceiling..
   */
  @org.junit.Test
  public void functionLiteral037() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}ceiling#1)",
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
   * Attempts to invoke function fn:ceiling..
   */
  @org.junit.Test
  public void functionLiteral038() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}ceiling#1(0.9)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:floor..
   */
  @org.junit.Test
  public void functionLiteral039() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}floor#1)",
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
   * Attempts to invoke function fn:floor..
   */
  @org.junit.Test
  public void functionLiteral040() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}floor#1(1.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:round..
   */
  @org.junit.Test
  public void functionLiteral041() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}round#1)",
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
   * Attempts to invoke function fn:round..
   */
  @org.junit.Test
  public void functionLiteral042() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}round#1(1.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:round..
   */
  @org.junit.Test
  public void functionLiteral043() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}round#2)",
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
   * Attempts to invoke function fn:round..
   */
  @org.junit.Test
  public void functionLiteral044() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}round#2(1.1, 0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:round-half-to-even..
   */
  @org.junit.Test
  public void functionLiteral045() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}round-half-to-even#1)",
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
   * Attempts to invoke function fn:round-half-to-even..
   */
  @org.junit.Test
  public void functionLiteral046() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}round-half-to-even#1(1.1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:round-half-to-even..
   */
  @org.junit.Test
  public void functionLiteral047() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}round-half-to-even#2)",
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
   * Attempts to invoke function fn:round-half-to-even..
   */
  @org.junit.Test
  public void functionLiteral048() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}round-half-to-even#2(1.1, 0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:number..
   */
  @org.junit.Test
  public void functionLiteral049() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}number#0)",
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
   * Attempts to invoke function fn:number..
   */
  @org.junit.Test
  public void functionLiteral050() {
    final XQuery query = new XQuery(
      "/root/child/Q{http://www.w3.org/2005/xpath-functions}number#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:number..
   */
  @org.junit.Test
  public void functionLiteral051() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}number#1)",
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
   * Attempts to invoke function fn:number..
   */
  @org.junit.Test
  public void functionLiteral052() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}number#1('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:format-integer..
   */
  @org.junit.Test
  public void functionLiteral053() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}format-integer#2)",
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
   * Attempts to invoke function fn:format-integer..
   */
  @org.junit.Test
  public void functionLiteral054() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}format-integer#2(1, '0')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:format-integer..
   */
  @org.junit.Test
  public void functionLiteral055() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}format-integer#3)",
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
   * Attempts to invoke function fn:format-integer..
   */
  @org.junit.Test
  public void functionLiteral056() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}format-integer#3(1, '0', 'en')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:format-number..
   */
  @org.junit.Test
  public void functionLiteral057() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}format-number#2)",
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
   * Attempts to invoke function fn:format-number..
   */
  @org.junit.Test
  public void functionLiteral058() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}format-number#2(1, '0')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:format-number..
   */
  @org.junit.Test
  public void functionLiteral059() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}format-number#3)",
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
   * Attempts to invoke function fn:format-number..
   */
  @org.junit.Test
  public void functionLiteral060() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}format-number#3(1, '0', ())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function math:pi..
   */
  @org.junit.Test
  public void functionLiteral061() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions/math}pi#0)",
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
   * Attempts to invoke function math:pi..
   */
  @org.junit.Test
  public void functionLiteral062() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions/math}pi#0()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3.141592653589793")
    );
  }

  /**
   * Attempts to look up function math:exp..
   */
  @org.junit.Test
  public void functionLiteral063() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions/math}exp#1)",
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
   * Attempts to invoke function math:exp..
   */
  @org.junit.Test
  public void functionLiteral064() {
    final XQuery query = new XQuery(
      "format-number(Q{http://www.w3.org/2005/xpath-functions/math}exp#1(1e0), '#.0000000')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2.7182818")
    );
  }

  /**
   * Attempts to look up function math:exp10..
   */
  @org.junit.Test
  public void functionLiteral065() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions/math}exp10#1)",
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
   * Attempts to invoke function math:exp10..
   */
  @org.junit.Test
  public void functionLiteral066() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions/math}exp10#1(1e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10")
    );
  }

  /**
   * Attempts to look up function math:log..
   */
  @org.junit.Test
  public void functionLiteral067() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions/math}log#1)",
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
   * Attempts to invoke function math:log..
   */
  @org.junit.Test
  public void functionLiteral068() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions/math}log#1(1e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Attempts to look up function math:log10..
   */
  @org.junit.Test
  public void functionLiteral069() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions/math}log10#1)",
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
   * Attempts to invoke function math:log10..
   */
  @org.junit.Test
  public void functionLiteral070() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions/math}log10#1(1e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Attempts to look up function math:pow..
   */
  @org.junit.Test
  public void functionLiteral071() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions/math}pow#2)",
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
   * Attempts to invoke function math:pow..
   */
  @org.junit.Test
  public void functionLiteral072() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions/math}pow#2(1e0, 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function math:sqrt..
   */
  @org.junit.Test
  public void functionLiteral073() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions/math}sqrt#1)",
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
   * Attempts to invoke function math:sqrt..
   */
  @org.junit.Test
  public void functionLiteral074() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions/math}sqrt#1(1e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function math:sin..
   */
  @org.junit.Test
  public void functionLiteral075() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions/math}sin#1)",
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
   * Attempts to invoke function math:sin..
   */
  @org.junit.Test
  public void functionLiteral076() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions/math}sin#1(1e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0.8414709848078965")
    );
  }

  /**
   * Attempts to look up function math:cos..
   */
  @org.junit.Test
  public void functionLiteral077() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions/math}cos#1)",
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
   * Attempts to invoke function math:cos..
   */
  @org.junit.Test
  public void functionLiteral078() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions/math}cos#1(1e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0.5403023058681398")
    );
  }

  /**
   * Attempts to look up function math:tan..
   */
  @org.junit.Test
  public void functionLiteral079() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions/math}tan#1)",
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
   * Attempts to invoke function math:tan..
   */
  @org.junit.Test
  public void functionLiteral080() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions/math}tan#1(1e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.5574077246549023")
    );
  }

  /**
   * Attempts to look up function math:asin..
   */
  @org.junit.Test
  public void functionLiteral081() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions/math}asin#1)",
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
   * Attempts to invoke function math:asin..
   */
  @org.junit.Test
  public void functionLiteral082() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions/math}asin#1(1e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.5707963267948966")
    );
  }

  /**
   * Attempts to look up function math:acos..
   */
  @org.junit.Test
  public void functionLiteral083() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions/math}acos#1)",
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
   * Attempts to invoke function math:acos..
   */
  @org.junit.Test
  public void functionLiteral084() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions/math}acos#1(1e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Attempts to look up function math:atan..
   */
  @org.junit.Test
  public void functionLiteral085() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions/math}atan#1)",
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
   * Attempts to invoke function math:atan..
   */
  @org.junit.Test
  public void functionLiteral086() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions/math}atan#1(1e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0.7853981633974483")
    );
  }

  /**
   * Attempts to look up function math:atan2..
   */
  @org.junit.Test
  public void functionLiteral087() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions/math}atan2#2)",
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
   * Attempts to invoke function math:atan2..
   */
  @org.junit.Test
  public void functionLiteral088() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions/math}atan2#2(1e0, 1e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0.7853981633974483")
    );
  }

  /**
   * Attempts to look up function fn:codepoints-to-string..
   */
  @org.junit.Test
  public void functionLiteral089() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}codepoints-to-string#1)",
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
   * Attempts to invoke function fn:codepoints-to-string..
   */
  @org.junit.Test
  public void functionLiteral090() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}codepoints-to-string#1((65, 66))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "AB")
    );
  }

  /**
   * Attempts to look up function fn:string-to-codepoints..
   */
  @org.junit.Test
  public void functionLiteral091() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}string-to-codepoints#1)",
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
   * Attempts to invoke function fn:string-to-codepoints..
   */
  @org.junit.Test
  public void functionLiteral092() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}string-to-codepoints#1('A')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "65")
    );
  }

  /**
   * Attempts to look up function fn:compare..
   */
  @org.junit.Test
  public void functionLiteral093() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}compare#2)",
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
   * Attempts to invoke function fn:compare..
   */
  @org.junit.Test
  public void functionLiteral094() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}compare#2('string', 'string')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Attempts to look up function fn:compare..
   */
  @org.junit.Test
  public void functionLiteral095() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}compare#3)",
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
   * Attempts to invoke function fn:compare..
   */
  @org.junit.Test
  public void functionLiteral096() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}compare#3('string', 'string', 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Attempts to look up function fn:codepoint-equal..
   */
  @org.junit.Test
  public void functionLiteral097() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}codepoint-equal#2)",
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
   * Attempts to invoke function fn:codepoint-equal..
   */
  @org.junit.Test
  public void functionLiteral098() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}codepoint-equal#2('string', 'string')",
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
   * Attempts to look up function fn:concat..
   */
  @org.junit.Test
  public void functionLiteral099() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}concat#3)",
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
   * Attempts to invoke function fn:concat..
   */
  @org.junit.Test
  public void functionLiteral100() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}concat#3('a', 'bc', 'def')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abcdef")
    );
  }

  /**
   * Attempts to look up function fn:string-join..
   */
  @org.junit.Test
  public void functionLiteral101() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}string-join#1)",
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
   * Attempts to invoke function fn:string-join..
   */
  @org.junit.Test
  public void functionLiteral102() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}string-join#1(('abc', 'def'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abcdef")
    );
  }

  /**
   * Attempts to look up function fn:string-join..
   */
  @org.junit.Test
  public void functionLiteral103() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}string-join#2)",
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
   * Attempts to invoke function fn:string-join..
   */
  @org.junit.Test
  public void functionLiteral104() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}string-join#2(('abc', 'def'), '-')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abc-def")
    );
  }

  /**
   * Attempts to look up function fn:substring..
   */
  @org.junit.Test
  public void functionLiteral105() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}substring#2)",
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
   * Attempts to invoke function fn:substring..
   */
  @org.junit.Test
  public void functionLiteral106() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}substring#2('string', 2e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "tring")
    );
  }

  /**
   * Attempts to look up function fn:substring..
   */
  @org.junit.Test
  public void functionLiteral107() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}substring#3)",
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
   * Attempts to invoke function fn:substring..
   */
  @org.junit.Test
  public void functionLiteral108() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}substring#3('string', 1e0, 1e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "s")
    );
  }

  /**
   * Attempts to look up function fn:string-length..
   */
  @org.junit.Test
  public void functionLiteral109() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}string-length#0)",
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
   * Attempts to invoke function fn:string-length..
   */
  @org.junit.Test
  public void functionLiteral110() {
    final XQuery query = new XQuery(
      "/root/child/Q{http://www.w3.org/2005/xpath-functions}string-length#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:string-length..
   */
  @org.junit.Test
  public void functionLiteral111() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}string-length#1)",
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
   * Attempts to invoke function fn:string-length..
   */
  @org.junit.Test
  public void functionLiteral112() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}string-length#1('string')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "6")
    );
  }

  /**
   * Attempts to look up function fn:normalize-space..
   */
  @org.junit.Test
  public void functionLiteral113() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}normalize-space#0)",
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
   * Attempts to invoke function fn:normalize-space..
   */
  @org.junit.Test
  public void functionLiteral114() {
    final XQuery query = new XQuery(
      "/root/child/Q{http://www.w3.org/2005/xpath-functions}normalize-space#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:normalize-space..
   */
  @org.junit.Test
  public void functionLiteral115() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}normalize-space#1)",
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
   * Attempts to invoke function fn:normalize-space..
   */
  @org.junit.Test
  public void functionLiteral116() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}normalize-space#1(' string ')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "string")
    );
  }

  /**
   * Attempts to look up function fn:normalize-unicode..
   */
  @org.junit.Test
  public void functionLiteral117() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}normalize-unicode#1)",
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
   * Attempts to invoke function fn:normalize-unicode..
   */
  @org.junit.Test
  public void functionLiteral118() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}normalize-unicode#1('string')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "string")
    );
  }

  /**
   * Attempts to look up function fn:normalize-unicode..
   */
  @org.junit.Test
  public void functionLiteral119() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}normalize-unicode#2)",
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
   * Attempts to invoke function fn:normalize-unicode..
   */
  @org.junit.Test
  public void functionLiteral120() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}normalize-unicode#2('string', 'NFC')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "string")
    );
  }

  /**
   * Attempts to look up function fn:upper-case..
   */
  @org.junit.Test
  public void functionLiteral121() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}upper-case#1)",
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
   * Attempts to invoke function fn:upper-case..
   */
  @org.junit.Test
  public void functionLiteral122() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}upper-case#1('string')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "STRING")
    );
  }

  /**
   * Attempts to look up function fn:lower-case..
   */
  @org.junit.Test
  public void functionLiteral123() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}lower-case#1)",
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
   * Attempts to invoke function fn:lower-case..
   */
  @org.junit.Test
  public void functionLiteral124() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}lower-case#1('STRING')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "string")
    );
  }

  /**
   * Attempts to look up function fn:translate..
   */
  @org.junit.Test
  public void functionLiteral125() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}translate#3)",
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
   * Attempts to invoke function fn:translate..
   */
  @org.junit.Test
  public void functionLiteral126() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}translate#3('string', 'i', 'o')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "strong")
    );
  }

  /**
   * Attempts to look up function fn:contains..
   */
  @org.junit.Test
  public void functionLiteral127() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}contains#2)",
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
   * Attempts to invoke function fn:contains..
   */
  @org.junit.Test
  public void functionLiteral128() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}contains#2('string', 'rin')",
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
   * Attempts to look up function fn:contains..
   */
  @org.junit.Test
  public void functionLiteral129() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}contains#3)",
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
   * Attempts to invoke function fn:contains..
   */
  @org.junit.Test
  public void functionLiteral130() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}contains#3('string', 'RIN', 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
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
   * Attempts to look up function fn:starts-with..
   */
  @org.junit.Test
  public void functionLiteral131() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}starts-with#2)",
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
   * Attempts to invoke function fn:starts-with..
   */
  @org.junit.Test
  public void functionLiteral132() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}starts-with#2('string', 'str')",
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
   * Attempts to look up function fn:starts-with..
   */
  @org.junit.Test
  public void functionLiteral133() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}starts-with#3)",
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
   * Attempts to invoke function fn:starts-with..
   */
  @org.junit.Test
  public void functionLiteral134() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}starts-with#3('string', 'ing', 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
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
   * Attempts to look up function fn:ends-with..
   */
  @org.junit.Test
  public void functionLiteral135() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}ends-with#2)",
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
   * Attempts to invoke function fn:ends-with..
   */
  @org.junit.Test
  public void functionLiteral136() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}ends-with#2('string', 'ing')",
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
   * Attempts to look up function fn:ends-with..
   */
  @org.junit.Test
  public void functionLiteral137() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}ends-with#3)",
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
   * Attempts to invoke function fn:ends-with..
   */
  @org.junit.Test
  public void functionLiteral138() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}ends-with#3('string', 'str', 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
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
   * Attempts to look up function fn:substring-before..
   */
  @org.junit.Test
  public void functionLiteral139() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}substring-before#2)",
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
   * Attempts to invoke function fn:substring-before..
   */
  @org.junit.Test
  public void functionLiteral140() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}substring-before#2('string', 'ing')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "str")
    );
  }

  /**
   * Attempts to look up function fn:substring-before..
   */
  @org.junit.Test
  public void functionLiteral141() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}substring-before#3)",
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
   * Attempts to invoke function fn:substring-before..
   */
  @org.junit.Test
  public void functionLiteral142() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}substring-before#3('string', 'ing', 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "str")
    );
  }

  /**
   * Attempts to look up function fn:substring-after..
   */
  @org.junit.Test
  public void functionLiteral143() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}substring-after#2)",
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
   * Attempts to invoke function fn:substring-after..
   */
  @org.junit.Test
  public void functionLiteral144() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}substring-after#2('string', 'str')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ing")
    );
  }

  /**
   * Attempts to look up function fn:substring-after..
   */
  @org.junit.Test
  public void functionLiteral145() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}substring-after#3)",
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
   * Attempts to invoke function fn:substring-after..
   */
  @org.junit.Test
  public void functionLiteral146() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}substring-after#3('string', 'str', 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ing")
    );
  }

  /**
   * Attempts to look up function fn:matches..
   */
  @org.junit.Test
  public void functionLiteral147() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}matches#2)",
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
   * Attempts to invoke function fn:matches..
   */
  @org.junit.Test
  public void functionLiteral148() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}matches#2('string', 'string')",
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
   * Attempts to look up function fn:matches..
   */
  @org.junit.Test
  public void functionLiteral149() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}matches#3)",
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
   * Attempts to invoke function fn:matches..
   */
  @org.junit.Test
  public void functionLiteral150() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}matches#3('string', 'STRING', 'i')",
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
   * Attempts to look up function fn:replace..
   */
  @org.junit.Test
  public void functionLiteral151() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}replace#3)",
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
   * Attempts to invoke function fn:replace..
   */
  @org.junit.Test
  public void functionLiteral152() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}replace#3('string', 'i', 'o')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "strong")
    );
  }

  /**
   * Attempts to look up function fn:replace..
   */
  @org.junit.Test
  public void functionLiteral153() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}replace#4)",
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
   * Attempts to invoke function fn:replace..
   */
  @org.junit.Test
  public void functionLiteral154() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}replace#4('string', 'I', 'o', 'i')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "strong")
    );
  }

  /**
   * Attempts to look up function fn:tokenize..
   */
  @org.junit.Test
  public void functionLiteral155() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}tokenize#2)",
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
   * Attempts to invoke function fn:tokenize..
   */
  @org.junit.Test
  public void functionLiteral156() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}tokenize#2('string', 'i')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "str ng")
    );
  }

  /**
   * Attempts to look up function fn:tokenize..
   */
  @org.junit.Test
  public void functionLiteral157() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}tokenize#3)",
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
   * Attempts to invoke function fn:tokenize..
   */
  @org.junit.Test
  public void functionLiteral158() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}tokenize#3('string', 'i', 'i')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "str ng")
    );
  }

  /**
   * Attempts to look up function fn:analyze-string..
   */
  @org.junit.Test
  public void functionLiteral159() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}analyze-string#2)",
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
   * Attempts to invoke function fn:analyze-string..
   */
  @org.junit.Test
  public void functionLiteral160() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}analyze-string#2('', 'abc')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"/>", true)
    );
  }

  /**
   * Attempts to look up function fn:analyze-string..
   */
  @org.junit.Test
  public void functionLiteral161() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}analyze-string#3)",
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
   * Attempts to invoke function fn:analyze-string..
   */
  @org.junit.Test
  public void functionLiteral162() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}analyze-string#3('', 'abc', 'i')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"/>", true)
    );
  }

  /**
   * Attempts to look up function fn:resolve-uri..
   */
  @org.junit.Test
  public void functionLiteral163() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}resolve-uri#1)",
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
   * Attempts to invoke function fn:resolve-uri..
   */
  @org.junit.Test
  public void functionLiteral164() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}resolve-uri#1('http://www.w3.org/2005/xpath-functions')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:anyURI")
    );
  }

  /**
   * Attempts to look up function fn:resolve-uri..
   */
  @org.junit.Test
  public void functionLiteral165() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}resolve-uri#2)",
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
   * Attempts to invoke function fn:resolve-uri..
   */
  @org.junit.Test
  public void functionLiteral166() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}resolve-uri#2('/2005/xpath-functions', 'http://www.w3.org/')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.w3.org/2005/xpath-functions")
    );
  }

  /**
   * Attempts to look up function fn:encode-for-uri..
   */
  @org.junit.Test
  public void functionLiteral167() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}encode-for-uri#1)",
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
   * Attempts to invoke function fn:encode-for-uri..
   */
  @org.junit.Test
  public void functionLiteral168() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}encode-for-uri#1(' ')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "%20")
    );
  }

  /**
   * Attempts to look up function fn:iri-to-uri..
   */
  @org.junit.Test
  public void functionLiteral169() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}iri-to-uri#1)",
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
   * Attempts to invoke function fn:iri-to-uri..
   */
  @org.junit.Test
  public void functionLiteral170() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}iri-to-uri#1('http://www.example.com/')",
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
   * Attempts to look up function fn:escape-html-uri..
   */
  @org.junit.Test
  public void functionLiteral171() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}escape-html-uri#1)",
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
   * Attempts to invoke function fn:escape-html-uri..
   */
  @org.junit.Test
  public void functionLiteral172() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}escape-html-uri#1('http://www.example.com/')",
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
   * Attempts to look up function fn:true..
   */
  @org.junit.Test
  public void functionLiteral173() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}true#0)",
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
   * Attempts to invoke function fn:true..
   */
  @org.junit.Test
  public void functionLiteral174() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}true#0()",
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
   * Attempts to look up function fn:false..
   */
  @org.junit.Test
  public void functionLiteral175() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}false#0)",
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
   * Attempts to invoke function fn:false..
   */
  @org.junit.Test
  public void functionLiteral176() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}false#0()",
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
   * Attempts to look up function fn:boolean..
   */
  @org.junit.Test
  public void functionLiteral177() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}boolean#1)",
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
   * Attempts to invoke function fn:boolean..
   */
  @org.junit.Test
  public void functionLiteral178() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}boolean#1(\"string\")",
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
   * Attempts to look up function fn:not..
   */
  @org.junit.Test
  public void functionLiteral179() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}not#1)",
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
   * Attempts to invoke function fn:not..
   */
  @org.junit.Test
  public void functionLiteral180() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}not#1(\"string\")",
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
   * Attempts to look up function fn:years-from-duration..
   */
  @org.junit.Test
  public void functionLiteral181() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}years-from-duration#1)",
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
   * Attempts to invoke function fn:years-from-duration..
   */
  @org.junit.Test
  public void functionLiteral182() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}years-from-duration#1(xs:yearMonthDuration(\"P20Y15M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "21")
    );
  }

  /**
   * Attempts to look up function fn:months-from-duration..
   */
  @org.junit.Test
  public void functionLiteral183() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}months-from-duration#1)",
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
   * Attempts to invoke function fn:months-from-duration..
   */
  @org.junit.Test
  public void functionLiteral184() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}months-from-duration#1(xs:yearMonthDuration(\"P20Y15M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   * Attempts to look up function fn:days-from-duration..
   */
  @org.junit.Test
  public void functionLiteral185() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}days-from-duration#1)",
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
   * Attempts to invoke function fn:days-from-duration..
   */
  @org.junit.Test
  public void functionLiteral186() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}days-from-duration#1(xs:dayTimeDuration(\"P3DT10H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   * Attempts to look up function fn:hours-from-duration..
   */
  @org.junit.Test
  public void functionLiteral187() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}hours-from-duration#1)",
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
   * Attempts to invoke function fn:hours-from-duration..
   */
  @org.junit.Test
  public void functionLiteral188() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}hours-from-duration#1(xs:dayTimeDuration(\"P3DT10H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10")
    );
  }

  /**
   * Attempts to look up function fn:minutes-from-duration..
   */
  @org.junit.Test
  public void functionLiteral189() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}minutes-from-duration#1)",
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
   * Attempts to invoke function fn:minutes-from-duration..
   */
  @org.junit.Test
  public void functionLiteral190() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}minutes-from-duration#1(xs:dayTimeDuration(\"P3DT10H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Attempts to look up function fn:seconds-from-duration..
   */
  @org.junit.Test
  public void functionLiteral191() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}seconds-from-duration#1)",
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
   * Attempts to invoke function fn:seconds-from-duration..
   */
  @org.junit.Test
  public void functionLiteral192() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}seconds-from-duration#1(xs:dayTimeDuration(\"P3DT10H12.5S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "12.5")
    );
  }

  /**
   * Attempts to look up function fn:dateTime..
   */
  @org.junit.Test
  public void functionLiteral193() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}dateTime#2)",
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
   * Attempts to invoke function fn:dateTime..
   */
  @org.junit.Test
  public void functionLiteral194() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}dateTime#2(xs:date('2012-01-01Z'), xs:time('00:00:00Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2012-01-01T00:00:00Z")
    );
  }

  /**
   * Attempts to look up function fn:year-from-dateTime..
   */
  @org.junit.Test
  public void functionLiteral195() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}year-from-dateTime#1)",
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
   * Attempts to invoke function fn:year-from-dateTime..
   */
  @org.junit.Test
  public void functionLiteral196() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}year-from-dateTime#1(xs:dateTime('2012-04-03T02:01:00Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2012")
    );
  }

  /**
   * Attempts to look up function fn:month-from-dateTime..
   */
  @org.junit.Test
  public void functionLiteral197() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}month-from-dateTime#1)",
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
   * Attempts to invoke function fn:month-from-dateTime..
   */
  @org.junit.Test
  public void functionLiteral198() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}month-from-dateTime#1(xs:dateTime('2012-04-03T02:01:00Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   * Attempts to look up function fn:day-from-dateTime..
   */
  @org.junit.Test
  public void functionLiteral199() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}day-from-dateTime#1)",
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
   * Attempts to invoke function fn:day-from-dateTime..
   */
  @org.junit.Test
  public void functionLiteral200() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}day-from-dateTime#1(xs:dateTime('2012-04-03T02:01:00Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   * Attempts to look up function fn:hours-from-dateTime..
   */
  @org.junit.Test
  public void functionLiteral201() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}hours-from-dateTime#1)",
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
   * Attempts to invoke function fn:hours-from-dateTime..
   */
  @org.junit.Test
  public void functionLiteral202() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}hours-from-dateTime#1(xs:dateTime('2012-04-03T02:01:00Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   * Attempts to look up function fn:minutes-from-dateTime..
   */
  @org.junit.Test
  public void functionLiteral203() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}minutes-from-dateTime#1)",
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
   * Attempts to invoke function fn:minutes-from-dateTime..
   */
  @org.junit.Test
  public void functionLiteral204() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}minutes-from-dateTime#1(xs:dateTime('2012-04-03T02:01:00Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:seconds-from-dateTime..
   */
  @org.junit.Test
  public void functionLiteral205() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}seconds-from-dateTime#1)",
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
   * Attempts to invoke function fn:seconds-from-dateTime..
   */
  @org.junit.Test
  public void functionLiteral206() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}seconds-from-dateTime#1(xs:dateTime('2012-04-03T02:01:00Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Attempts to look up function fn:timezone-from-dateTime..
   */
  @org.junit.Test
  public void functionLiteral207() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}timezone-from-dateTime#1)",
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
   * Attempts to invoke function fn:timezone-from-dateTime..
   */
  @org.junit.Test
  public void functionLiteral208() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}timezone-from-dateTime#1(xs:dateTime('2012-01-01T00:00:00Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   * Attempts to look up function fn:year-from-date..
   */
  @org.junit.Test
  public void functionLiteral209() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}year-from-date#1)",
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
   * Attempts to invoke function fn:year-from-date..
   */
  @org.junit.Test
  public void functionLiteral210() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}year-from-date#1(xs:date('2012-02-01Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2012")
    );
  }

  /**
   * Attempts to look up function fn:month-from-date..
   */
  @org.junit.Test
  public void functionLiteral211() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}month-from-date#1)",
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
   * Attempts to invoke function fn:month-from-date..
   */
  @org.junit.Test
  public void functionLiteral212() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}month-from-date#1(xs:date('2012-02-01Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   * Attempts to look up function fn:day-from-date..
   */
  @org.junit.Test
  public void functionLiteral213() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}day-from-date#1)",
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
   * Attempts to invoke function fn:day-from-date..
   */
  @org.junit.Test
  public void functionLiteral214() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}day-from-date#1(xs:date('2012-02-01Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:timezone-from-date..
   */
  @org.junit.Test
  public void functionLiteral215() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}timezone-from-date#1)",
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
   * Attempts to invoke function fn:timezone-from-date..
   */
  @org.junit.Test
  public void functionLiteral216() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}timezone-from-date#1(xs:date('2012-01-01Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   * Attempts to look up function fn:hours-from-time..
   */
  @org.junit.Test
  public void functionLiteral217() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}hours-from-time#1)",
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
   * Attempts to invoke function fn:hours-from-time..
   */
  @org.junit.Test
  public void functionLiteral218() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}hours-from-time#1(xs:time('02:01:00Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   * Attempts to look up function fn:minutes-from-time..
   */
  @org.junit.Test
  public void functionLiteral219() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}minutes-from-time#1)",
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
   * Attempts to invoke function fn:minutes-from-time..
   */
  @org.junit.Test
  public void functionLiteral220() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}minutes-from-time#1(xs:time('02:01:00Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:seconds-from-time..
   */
  @org.junit.Test
  public void functionLiteral221() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}seconds-from-time#1)",
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
   * Attempts to invoke function fn:seconds-from-time..
   */
  @org.junit.Test
  public void functionLiteral222() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}seconds-from-time#1(xs:time('02:01:00Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Attempts to look up function fn:timezone-from-time..
   */
  @org.junit.Test
  public void functionLiteral223() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}timezone-from-time#1)",
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
   * Attempts to invoke function fn:timezone-from-time..
   */
  @org.junit.Test
  public void functionLiteral224() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}timezone-from-time#1(xs:time('02:01:00Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   * Attempts to look up function fn:adjust-dateTime-to-timezone..
   */
  @org.junit.Test
  public void functionLiteral225() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}adjust-dateTime-to-timezone#1)",
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
   * Attempts to invoke function fn:adjust-dateTime-to-timezone..
   */
  @org.junit.Test
  public void functionLiteral226() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}adjust-dateTime-to-timezone#1(xs:dateTime('2012-01-01T00:00:00Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:dateTime")
    );
  }

  /**
   * Attempts to look up function fn:adjust-dateTime-to-timezone..
   */
  @org.junit.Test
  public void functionLiteral227() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}adjust-dateTime-to-timezone#2)",
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
   * Attempts to invoke function fn:adjust-dateTime-to-timezone..
   */
  @org.junit.Test
  public void functionLiteral228() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}adjust-dateTime-to-timezone#2(xs:dateTime(\"1970-01-01T00:00:00Z\"),xs:dayTimeDuration(\"-PT10H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1969-12-31T14:00:00-10:00")
    );
  }

  /**
   * Attempts to look up function fn:adjust-date-to-timezone..
   */
  @org.junit.Test
  public void functionLiteral229() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}adjust-date-to-timezone#1)",
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
   * Attempts to invoke function fn:adjust-date-to-timezone..
   */
  @org.junit.Test
  public void functionLiteral230() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}adjust-date-to-timezone#1(xs:date('2012-01-01Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:date")
    );
  }

  /**
   * Attempts to look up function fn:adjust-date-to-timezone..
   */
  @org.junit.Test
  public void functionLiteral231() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}adjust-date-to-timezone#2)",
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
   * Attempts to invoke function fn:adjust-date-to-timezone..
   */
  @org.junit.Test
  public void functionLiteral232() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}adjust-date-to-timezone#2(xs:date(\"1970-01-01Z\"),xs:dayTimeDuration(\"-PT10H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1969-12-31-10:00")
    );
  }

  /**
   * Attempts to look up function fn:adjust-time-to-timezone..
   */
  @org.junit.Test
  public void functionLiteral233() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}adjust-time-to-timezone#1)",
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
   * Attempts to invoke function fn:adjust-time-to-timezone..
   */
  @org.junit.Test
  public void functionLiteral234() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}adjust-time-to-timezone#1(xs:time('00:00:00Z'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:time")
    );
  }

  /**
   * Attempts to look up function fn:adjust-time-to-timezone..
   */
  @org.junit.Test
  public void functionLiteral235() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}adjust-time-to-timezone#2)",
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
   * Attempts to invoke function fn:adjust-time-to-timezone..
   */
  @org.junit.Test
  public void functionLiteral236() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}adjust-time-to-timezone#2(xs:time(\"00:00:00Z\"),xs:dayTimeDuration(\"-PT10H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "14:00:00-10:00")
    );
  }

  /**
   * Attempts to look up function fn:format-dateTime..
   */
  @org.junit.Test
  public void functionLiteral237() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}format-dateTime#2)",
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
   * Attempts to invoke function fn:format-dateTime..
   */
  @org.junit.Test
  public void functionLiteral238() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}format-dateTime#2(xs:dateTime('2012-01-01T00:00:00Z'), '[Y]')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:string")
    );
  }

  /**
   * Attempts to look up function fn:format-dateTime..
   */
  @org.junit.Test
  public void functionLiteral239() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}format-dateTime#5)",
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
   * Attempts to invoke function fn:format-dateTime..
   */
  @org.junit.Test
  public void functionLiteral240() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}format-dateTime#5(xs:dateTime('2012-01-01T00:00:00Z'), '[Y]', 'en', (), ())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2012")
    );
  }

  /**
   * Attempts to look up function fn:format-date..
   */
  @org.junit.Test
  public void functionLiteral241() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}format-date#2)",
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
   * Attempts to invoke function fn:format-date..
   */
  @org.junit.Test
  public void functionLiteral242() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}format-date#2(xs:date('2012-01-01Z'), '[Y]')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:string")
    );
  }

  /**
   * Attempts to look up function fn:format-date..
   */
  @org.junit.Test
  public void functionLiteral243() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}format-date#5)",
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
   * Attempts to invoke function fn:format-date..
   */
  @org.junit.Test
  public void functionLiteral244() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}format-date#5(xs:date('2012-01-01Z'), '[Y]', 'en', (), ())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2012")
    );
  }

  /**
   * Attempts to look up function fn:format-time..
   */
  @org.junit.Test
  public void functionLiteral245() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}format-time#2)",
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
   * Attempts to invoke function fn:format-time..
   */
  @org.junit.Test
  public void functionLiteral246() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}format-time#2(xs:time('00:00:00Z'), '[H01]')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:string")
    );
  }

  /**
   * Attempts to look up function fn:format-time..
   */
  @org.junit.Test
  public void functionLiteral247() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}format-time#5)",
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
   * Attempts to invoke function fn:format-time..
   */
  @org.junit.Test
  public void functionLiteral248() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}format-time#5(xs:time('00:00:00Z'), '[H01]', 'en', (), ())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "00")
    );
  }

  /**
   * Attempts to look up function fn:resolve-QName..
   */
  @org.junit.Test
  public void functionLiteral249() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}resolve-QName#2)",
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
   * Attempts to invoke function fn:resolve-QName..
   */
  @org.junit.Test
  public void functionLiteral250() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}resolve-QName#2('ns:local', /root/*[2])",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ns:local")
    );
  }

  /**
   * Attempts to look up function fn:QName..
   */
  @org.junit.Test
  public void functionLiteral251() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}QName#2)",
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
   * Attempts to invoke function fn:QName..
   */
  @org.junit.Test
  public void functionLiteral252() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}QName#2('http://www.example.org/', 'ns:local')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ns:local")
    );
  }

  /**
   * Attempts to look up function fn:prefix-from-QName..
   */
  @org.junit.Test
  public void functionLiteral253() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}prefix-from-QName#1)",
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
   * Attempts to invoke function fn:prefix-from-QName..
   */
  @org.junit.Test
  public void functionLiteral254() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}prefix-from-QName#1(fn:QName('http://www.example.org', 'foo:bar'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "foo")
    );
  }

  /**
   * Attempts to look up function fn:local-name-from-QName..
   */
  @org.junit.Test
  public void functionLiteral255() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}local-name-from-QName#1)",
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
   * Attempts to invoke function fn:local-name-from-QName..
   */
  @org.junit.Test
  public void functionLiteral256() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}local-name-from-QName#1(fn:QName('http://www.example.org', 'foo:bar'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "bar")
    );
  }

  /**
   * Attempts to look up function fn:namespace-uri-from-QName..
   */
  @org.junit.Test
  public void functionLiteral257() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}namespace-uri-from-QName#1)",
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
   * Attempts to invoke function fn:namespace-uri-from-QName..
   */
  @org.junit.Test
  public void functionLiteral258() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}namespace-uri-from-QName#1(fn:QName('http://www.example.org', 'foo:bar'))",
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
   * Attempts to look up function fn:namespace-uri-for-prefix..
   */
  @org.junit.Test
  public void functionLiteral259() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}namespace-uri-for-prefix#2)",
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
   * Attempts to invoke function fn:namespace-uri-for-prefix..
   */
  @org.junit.Test
  public void functionLiteral260() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}namespace-uri-for-prefix#2('ns', /root/*[2])",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.org/")
    );
  }

  /**
   * Attempts to look up function fn:in-scope-prefixes..
   */
  @org.junit.Test
  public void functionLiteral261() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}in-scope-prefixes#1)",
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
   * Attempts to invoke function fn:in-scope-prefixes..
   */
  @org.junit.Test
  public void functionLiteral262() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}in-scope-prefixes#1(/root)",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xml")
    );
  }

  /**
   * Attempts to look up function fn:name..
   */
  @org.junit.Test
  public void functionLiteral263() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}name#0)",
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
   * Attempts to invoke function fn:name..
   */
  @org.junit.Test
  public void functionLiteral264() {
    final XQuery query = new XQuery(
      "/root/Q{http://www.w3.org/2005/xpath-functions}name#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "root")
    );
  }

  /**
   * Attempts to look up function fn:name..
   */
  @org.junit.Test
  public void functionLiteral265() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}name#1)",
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
   * Attempts to invoke function fn:name..
   */
  @org.junit.Test
  public void functionLiteral266() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}name#1(/root)",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "root")
    );
  }

  /**
   * Attempts to look up function fn:local-name..
   */
  @org.junit.Test
  public void functionLiteral267() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}local-name#0)",
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
   * Attempts to invoke function fn:local-name..
   */
  @org.junit.Test
  public void functionLiteral268() {
    final XQuery query = new XQuery(
      "/root/Q{http://www.w3.org/2005/xpath-functions}local-name#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "root")
    );
  }

  /**
   * Attempts to look up function fn:local-name..
   */
  @org.junit.Test
  public void functionLiteral269() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}local-name#1)",
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
   * Attempts to invoke function fn:local-name..
   */
  @org.junit.Test
  public void functionLiteral270() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}local-name#1(/root)",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "root")
    );
  }

  /**
   * Attempts to look up function fn:namespace-uri..
   */
  @org.junit.Test
  public void functionLiteral271() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}namespace-uri#0)",
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
   * Attempts to invoke function fn:namespace-uri..
   */
  @org.junit.Test
  public void functionLiteral272() {
    final XQuery query = new XQuery(
      "/root/*[2]/Q{http://www.w3.org/2005/xpath-functions}namespace-uri#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.org/")
    );
  }

  /**
   * Attempts to look up function fn:namespace-uri..
   */
  @org.junit.Test
  public void functionLiteral273() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}namespace-uri#1)",
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
   * Attempts to invoke function fn:namespace-uri..
   */
  @org.junit.Test
  public void functionLiteral274() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}namespace-uri#1(/root/*[2])",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.org/")
    );
  }

  /**
   * Attempts to look up function fn:lang..
   */
  @org.junit.Test
  public void functionLiteral275() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}lang#1)",
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
   * Attempts to invoke function fn:lang..
   */
  @org.junit.Test
  public void functionLiteral276() {
    final XQuery query = new XQuery(
      "/root/Q{http://www.w3.org/2005/xpath-functions}lang#1('en')",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
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
   * Attempts to look up function fn:lang..
   */
  @org.junit.Test
  public void functionLiteral277() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}lang#2)",
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
   * Attempts to invoke function fn:lang..
   */
  @org.junit.Test
  public void functionLiteral278() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}lang#2('en', /root)",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
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
   * Attempts to look up function fn:root..
   */
  @org.junit.Test
  public void functionLiteral279() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}root#0)",
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
   * Attempts to invoke function fn:root..
   */
  @org.junit.Test
  public void functionLiteral280() {
    final XQuery query = new XQuery(
      "/root/Q{http://www.w3.org/2005/xpath-functions}root#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
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

  /**
   * Attempts to look up function fn:root..
   */
  @org.junit.Test
  public void functionLiteral281() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}root#1)",
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
   * Attempts to invoke function fn:root..
   */
  @org.junit.Test
  public void functionLiteral282() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}root#1(())",
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
   * Attempts to look up function fn:path..
   */
  @org.junit.Test
  public void functionLiteral283() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}path#0)",
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
   * Attempts to invoke function fn:path..
   */
  @org.junit.Test
  public void functionLiteral284() {
    final XQuery query = new XQuery(
      "/Q{http://www.w3.org/2005/xpath-functions}path#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "/")
    );
  }

  /**
   * Attempts to look up function fn:path..
   */
  @org.junit.Test
  public void functionLiteral285() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}path#1)",
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
   * Attempts to invoke function fn:path..
   */
  @org.junit.Test
  public void functionLiteral286() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}path#1(/)",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "/")
    );
  }

  /**
   * Attempts to look up function fn:has-children..
   */
  @org.junit.Test
  public void functionLiteral287() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}has-children#0)",
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
   * Attempts to invoke function fn:has-children..
   */
  @org.junit.Test
  public void functionLiteral288() {
    final XQuery query = new XQuery(
      "/Q{http://www.w3.org/2005/xpath-functions}has-children#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
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
   * Attempts to look up function fn:has-children..
   */
  @org.junit.Test
  public void functionLiteral289() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}has-children#1)",
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
   * Attempts to invoke function fn:has-children..
   */
  @org.junit.Test
  public void functionLiteral290() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}has-children#1(/)",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
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
   * Attempts to look up function fn:innermost..
   */
  @org.junit.Test
  public void functionLiteral291() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}innermost#1)",
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
   * Attempts to invoke function fn:innermost..
   */
  @org.junit.Test
  public void functionLiteral292() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}innermost#1(())",
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
   * Attempts to look up function fn:outermost..
   */
  @org.junit.Test
  public void functionLiteral293() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}outermost#1)",
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
   * Attempts to invoke function fn:outermost..
   */
  @org.junit.Test
  public void functionLiteral294() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}outermost#1(())",
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
   * Attempts to look up function fn:empty..
   */
  @org.junit.Test
  public void functionLiteral295() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}empty#1)",
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
   * Attempts to invoke function fn:empty..
   */
  @org.junit.Test
  public void functionLiteral296() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}empty#1((1, true()))",
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
   * Attempts to look up function fn:exists..
   */
  @org.junit.Test
  public void functionLiteral297() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}exists#1)",
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
   * Attempts to invoke function fn:exists..
   */
  @org.junit.Test
  public void functionLiteral298() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}exists#1((1, true()))",
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
   * Attempts to look up function fn:head..
   */
  @org.junit.Test
  public void functionLiteral299() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}head#1)",
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
   * Attempts to invoke function fn:head..
   */
  @org.junit.Test
  public void functionLiteral300() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}head#1((1, true()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:tail..
   */
  @org.junit.Test
  public void functionLiteral301() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}tail#1)",
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
   * Attempts to invoke function fn:tail..
   */
  @org.junit.Test
  public void functionLiteral302() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}tail#1((1, true()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true")
    );
  }

  /**
   * Attempts to look up function fn:insert-before..
   */
  @org.junit.Test
  public void functionLiteral303() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}insert-before#3)",
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
   * Attempts to invoke function fn:insert-before..
   */
  @org.junit.Test
  public void functionLiteral304() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}insert-before#3((1, 2, 3), 2, ('a', 'b', 'c'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 a b c 2 3")
    );
  }

  /**
   * Attempts to look up function fn:remove..
   */
  @org.junit.Test
  public void functionLiteral305() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}remove#2)",
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
   * Attempts to invoke function fn:remove..
   */
  @org.junit.Test
  public void functionLiteral306() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}remove#2(('a', 'b', 'c'), 2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a c")
    );
  }

  /**
   * Attempts to look up function fn:reverse..
   */
  @org.junit.Test
  public void functionLiteral307() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}reverse#1)",
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
   * Attempts to invoke function fn:reverse..
   */
  @org.junit.Test
  public void functionLiteral308() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}reverse#1(1 to 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3 2 1")
    );
  }

  /**
   * Attempts to look up function fn:subsequence..
   */
  @org.junit.Test
  public void functionLiteral309() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}subsequence#2)",
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
   * Attempts to invoke function fn:subsequence..
   */
  @org.junit.Test
  public void functionLiteral310() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}subsequence#2((1, true()), 2e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true")
    );
  }

  /**
   * Attempts to look up function fn:subsequence..
   */
  @org.junit.Test
  public void functionLiteral311() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}subsequence#3)",
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
   * Attempts to invoke function fn:subsequence..
   */
  @org.junit.Test
  public void functionLiteral312() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}subsequence#3((1, true()), 1e0, 1e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:unordered..
   */
  @org.junit.Test
  public void functionLiteral313() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}unordered#1)",
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
   * Attempts to invoke function fn:unordered..
   */
  @org.junit.Test
  public void functionLiteral314() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}unordered#1(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:distinct-values..
   */
  @org.junit.Test
  public void functionLiteral315() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}distinct-values#1)",
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
   * Attempts to invoke function fn:distinct-values..
   */
  @org.junit.Test
  public void functionLiteral316() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}distinct-values#1((1, 1, 1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:distinct-values..
   */
  @org.junit.Test
  public void functionLiteral317() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}distinct-values#2)",
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
   * Attempts to invoke function fn:distinct-values..
   */
  @org.junit.Test
  public void functionLiteral318() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}distinct-values#2((1, 1, 1), 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:index-of..
   */
  @org.junit.Test
  public void functionLiteral319() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}index-of#2)",
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
   * Attempts to invoke function fn:index-of..
   */
  @org.junit.Test
  public void functionLiteral320() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}index-of#2((1, 'string'), 'string')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   * Attempts to look up function fn:index-of..
   */
  @org.junit.Test
  public void functionLiteral321() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}index-of#3)",
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
   * Attempts to invoke function fn:index-of..
   */
  @org.junit.Test
  public void functionLiteral322() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}index-of#3((1, 'string'), 'string', 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   * Attempts to look up function fn:deep-equal..
   */
  @org.junit.Test
  public void functionLiteral323() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}deep-equal#2)",
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
   * Attempts to invoke function fn:deep-equal..
   */
  @org.junit.Test
  public void functionLiteral324() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}deep-equal#2((1, true()), (1, true()))",
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
   * Attempts to look up function fn:deep-equal..
   */
  @org.junit.Test
  public void functionLiteral325() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}deep-equal#3)",
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
   * Attempts to invoke function fn:deep-equal..
   */
  @org.junit.Test
  public void functionLiteral326() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}deep-equal#3((1, true()), (1, true()), 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
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
   * Attempts to look up function fn:zero-or-one..
   */
  @org.junit.Test
  public void functionLiteral327() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}zero-or-one#1)",
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
   * Attempts to invoke function fn:zero-or-one..
   */
  @org.junit.Test
  public void functionLiteral328() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}zero-or-one#1(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:one-or-more..
   */
  @org.junit.Test
  public void functionLiteral329() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}one-or-more#1)",
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
   * Attempts to invoke function fn:one-or-more..
   */
  @org.junit.Test
  public void functionLiteral330() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}one-or-more#1(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:exactly-one..
   */
  @org.junit.Test
  public void functionLiteral331() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}exactly-one#1)",
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
   * Attempts to invoke function fn:exactly-one..
   */
  @org.junit.Test
  public void functionLiteral332() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}exactly-one#1(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:count..
   */
  @org.junit.Test
  public void functionLiteral333() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}count#1)",
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
   * Attempts to invoke function fn:count..
   */
  @org.junit.Test
  public void functionLiteral334() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}count#1((1, true()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   * Attempts to look up function fn:avg..
   */
  @org.junit.Test
  public void functionLiteral335() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}avg#1)",
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
   * Attempts to invoke function fn:avg..
   */
  @org.junit.Test
  public void functionLiteral336() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}avg#1((1, 3))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   * Attempts to look up function fn:max..
   */
  @org.junit.Test
  public void functionLiteral337() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}max#1)",
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
   * Attempts to invoke function fn:max..
   */
  @org.junit.Test
  public void functionLiteral338() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}max#1((1, 3))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   * Attempts to look up function fn:max..
   */
  @org.junit.Test
  public void functionLiteral339() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}max#2)",
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
   * Attempts to invoke function fn:max..
   */
  @org.junit.Test
  public void functionLiteral340() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}max#2((1, 3), 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   * Attempts to look up function fn:min..
   */
  @org.junit.Test
  public void functionLiteral341() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}min#1)",
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
   * Attempts to invoke function fn:min..
   */
  @org.junit.Test
  public void functionLiteral342() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}min#1((1, 3))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:min..
   */
  @org.junit.Test
  public void functionLiteral343() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}min#2)",
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
   * Attempts to invoke function fn:min..
   */
  @org.junit.Test
  public void functionLiteral344() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}min#2((1, 3), 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:sum..
   */
  @org.junit.Test
  public void functionLiteral345() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}sum#1)",
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
   * Attempts to invoke function fn:sum..
   */
  @org.junit.Test
  public void functionLiteral346() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}sum#1((1, 2))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   * Attempts to look up function fn:sum..
   */
  @org.junit.Test
  public void functionLiteral347() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}sum#2)",
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
   * Attempts to invoke function fn:sum..
   */
  @org.junit.Test
  public void functionLiteral348() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}sum#2((1, 2), 0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   * Attempts to look up function fn:id..
   */
  @org.junit.Test
  public void functionLiteral349() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}id#1)",
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
   * Attempts to invoke function fn:id..
   */
  @org.junit.Test
  public void functionLiteral350() {
    final XQuery query = new XQuery(
      "/Q{http://www.w3.org/2005/xpath-functions}id#1(('id1', 'id2'))",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
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
   * Attempts to look up function fn:id..
   */
  @org.junit.Test
  public void functionLiteral351() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}id#2)",
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
   * Attempts to invoke function fn:id..
   */
  @org.junit.Test
  public void functionLiteral352() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}id#2(('id1', 'id2'), /)",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
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
   * Attempts to look up function fn:element-with-id..
   */
  @org.junit.Test
  public void functionLiteral353() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}element-with-id#1)",
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
   * Attempts to invoke function fn:element-with-id..
   */
  @org.junit.Test
  public void functionLiteral354() {
    final XQuery query = new XQuery(
      "/Q{http://www.w3.org/2005/xpath-functions}element-with-id#1(('id1', 'id2'))",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
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
   * Attempts to look up function fn:element-with-id..
   */
  @org.junit.Test
  public void functionLiteral355() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}element-with-id#2)",
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
   * Attempts to invoke function fn:element-with-id..
   */
  @org.junit.Test
  public void functionLiteral356() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}element-with-id#2(('id1', 'id2'), /)",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
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
   * Attempts to look up function fn:idref..
   */
  @org.junit.Test
  public void functionLiteral357() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}idref#1)",
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
   * Attempts to invoke function fn:idref..
   */
  @org.junit.Test
  public void functionLiteral358() {
    final XQuery query = new XQuery(
      "/Q{http://www.w3.org/2005/xpath-functions}idref#1(('id1', 'id2'))",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
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
   * Attempts to look up function fn:idref..
   */
  @org.junit.Test
  public void functionLiteral359() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}idref#2)",
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
   * Attempts to invoke function fn:idref..
   */
  @org.junit.Test
  public void functionLiteral360() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}idref#2(('id1', 'id2'), /)",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
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
   * Attempts to look up function fn:generate-id..
   */
  @org.junit.Test
  public void functionLiteral361() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}generate-id#0)",
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
   * Attempts to invoke function fn:generate-id..
   */
  @org.junit.Test
  public void functionLiteral362() {
    final XQuery query = new XQuery(
      "/Q{http://www.w3.org/2005/xpath-functions}generate-id#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:string")
    );
  }

  /**
   * Attempts to look up function fn:generate-id..
   */
  @org.junit.Test
  public void functionLiteral363() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}generate-id#1)",
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
   * Attempts to invoke function fn:generate-id..
   */
  @org.junit.Test
  public void functionLiteral364() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}generate-id#1(())",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertType("xs:string")
      &&
        assertEq("\"\"")
      )
    );
  }

  /**
   * Attempts to look up function fn:doc..
   */
  @org.junit.Test
  public void functionLiteral365() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}doc#1)",
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
   * Attempts to invoke function fn:doc..
   */
  @org.junit.Test
  public void functionLiteral366() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}doc#1('http://www.w3.org/fots/fn/function-lookup/function-lookup.xml')",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
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

  /**
   * Attempts to look up function fn:doc-available..
   */
  @org.junit.Test
  public void functionLiteral367() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}doc-available#1)",
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
   * Attempts to invoke function fn:doc-available..
   */
  @org.junit.Test
  public void functionLiteral368() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}doc-available#1('http://www.example.org/unknown-document')",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
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
   * Attempts to look up function fn:collection..
   */
  @org.junit.Test
  public void functionLiteral369() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}collection#0)",
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
   * Attempts to invoke function fn:collection..
   */
  @org.junit.Test
  public void functionLiteral370() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}collection#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("node()+")
    );
  }

  /**
   * Attempts to look up function fn:collection..
   */
  @org.junit.Test
  public void functionLiteral371() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}collection#1)",
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
   * Attempts to invoke function fn:collection..
   */
  @org.junit.Test
  public void functionLiteral372() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}collection#1(())",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("node()+")
    );
  }

  /**
   * Attempts to look up function fn:uri-collection..
   */
  @org.junit.Test
  public void functionLiteral373() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}uri-collection#0)",
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
   * Attempts to invoke function fn:uri-collection..
   */
  @org.junit.Test
  public void functionLiteral374() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}uri-collection#0()",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:anyURI+")
    );
  }

  /**
   * Attempts to look up function fn:uri-collection..
   */
  @org.junit.Test
  public void functionLiteral375() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}uri-collection#1)",
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
   * Attempts to invoke function fn:uri-collection..
   */
  @org.junit.Test
  public void functionLiteral376() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}uri-collection#1(())",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:anyURI+")
    );
  }

  /**
   * Attempts to look up function fn:unparsed-text..
   */
  @org.junit.Test
  public void functionLiteral377() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}unparsed-text#1)",
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
   * Attempts to invoke function fn:unparsed-text..
   */
  @org.junit.Test
  public void functionLiteral378() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}unparsed-text#1('http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt')",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:string")
    );
  }

  /**
   * Attempts to look up function fn:unparsed-text..
   */
  @org.junit.Test
  public void functionLiteral379() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}unparsed-text#2)",
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
   * Attempts to invoke function fn:unparsed-text..
   */
  @org.junit.Test
  public void functionLiteral380() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}unparsed-text#2('http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt', 'utf-8')",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:string")
    );
  }

  /**
   * Attempts to look up function fn:unparsed-text-lines..
   */
  @org.junit.Test
  public void functionLiteral381() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}unparsed-text-lines#1)",
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
   * Attempts to invoke function fn:unparsed-text-lines..
   */
  @org.junit.Test
  public void functionLiteral382() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}unparsed-text-lines#1('http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt')",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:string+")
    );
  }

  /**
   * Attempts to look up function fn:unparsed-text-lines..
   */
  @org.junit.Test
  public void functionLiteral383() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}unparsed-text-lines#2)",
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
   * Attempts to invoke function fn:unparsed-text-lines..
   */
  @org.junit.Test
  public void functionLiteral384() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}unparsed-text-lines#2('http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt', 'utf-8')",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:string+")
    );
  }

  /**
   * Attempts to look up function fn:unparsed-text-available..
   */
  @org.junit.Test
  public void functionLiteral385() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}unparsed-text-available#1)",
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
   * Attempts to invoke function fn:unparsed-text-available..
   */
  @org.junit.Test
  public void functionLiteral386() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}unparsed-text-available#1('http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt')",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
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
   * Attempts to look up function fn:unparsed-text-available..
   */
  @org.junit.Test
  public void functionLiteral387() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}unparsed-text-available#2)",
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
   * Attempts to invoke function fn:unparsed-text-available..
   */
  @org.junit.Test
  public void functionLiteral388() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}unparsed-text-available#2('http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt', 'utf-8')",
      ctx);
    try {
      query.context(node(file("fn/function-lookup/function-lookup.xml")));
      // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt
      query.addCollection("", new String[] { file("fn/function-lookup/collection-1.xml"), file("fn/function-lookup/collection-2.xml") });
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
   * Attempts to look up function fn:environment-variable..
   */
  @org.junit.Test
  public void functionLiteral389() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}environment-variable#1)",
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
   * Attempts to invoke function fn:environment-variable..
   */
  @org.junit.Test
  public void functionLiteral390() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}environment-variable#1('should-not-exist')",
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
   * Attempts to look up function fn:available-environment-variables..
   */
  @org.junit.Test
  public void functionLiteral391() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}available-environment-variables#0)",
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
   * Attempts to invoke function fn:available-environment-variables..
   */
  @org.junit.Test
  public void functionLiteral392() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}available-environment-variables#0()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:string*")
    );
  }

  /**
   * Attempts to look up function fn:parse-xml..
   */
  @org.junit.Test
  public void functionLiteral393() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}parse-xml#1)",
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
   * Attempts to invoke function fn:parse-xml..
   */
  @org.junit.Test
  public void functionLiteral394() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}parse-xml#1('<doc />')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("node()")
    );
  }

  /**
   * Attempts to look up function fn:parse-xml-fragment..
   */
  @org.junit.Test
  public void functionLiteral395() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}parse-xml-fragment#1)",
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
   * Attempts to invoke function fn:parse-xml-fragment..
   */
  @org.junit.Test
  public void functionLiteral396() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}parse-xml-fragment#1('<doc />')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("node()")
    );
  }

  /**
   * Attempts to look up function fn:serialize..
   */
  @org.junit.Test
  public void functionLiteral397() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}serialize#1)",
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
   * Attempts to invoke function fn:serialize..
   */
  @org.junit.Test
  public void functionLiteral398() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}serialize#1((1, true()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 true")
    );
  }

  /**
   * Attempts to look up function fn:serialize..
   */
  @org.junit.Test
  public void functionLiteral399() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}serialize#2)",
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
   * Attempts to invoke function fn:serialize..
   */
  @org.junit.Test
  public void functionLiteral400() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}serialize#2((1, false()), ())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 false")
    );
  }

  /**
   * Attempts to look up function fn:position..
   */
  @org.junit.Test
  public void functionLiteral401() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}position#0)",
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
   * Attempts to invoke function fn:position..
   */
  @org.junit.Test
  public void functionLiteral402() {
    final XQuery query = new XQuery(
      "(2, 4, 6)!Q{http://www.w3.org/2005/xpath-functions}position#0()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3")
    );
  }

  /**
   * Attempts to look up function fn:last..
   */
  @org.junit.Test
  public void functionLiteral403() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}last#0)",
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
   * Attempts to invoke function fn:last..
   */
  @org.junit.Test
  public void functionLiteral404() {
    final XQuery query = new XQuery(
      "(2, 4, 6)!Q{http://www.w3.org/2005/xpath-functions}last#0()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3 3 3")
    );
  }

  /**
   * Attempts to look up function fn:current-dateTime..
   */
  @org.junit.Test
  public void functionLiteral405() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}current-dateTime#0)",
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
   * Attempts to invoke function fn:current-dateTime..
   */
  @org.junit.Test
  public void functionLiteral406() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}current-dateTime#0()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:dateTime")
    );
  }

  /**
   * Attempts to look up function fn:current-date..
   */
  @org.junit.Test
  public void functionLiteral407() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}current-date#0)",
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
   * Attempts to invoke function fn:current-date..
   */
  @org.junit.Test
  public void functionLiteral408() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}current-date#0()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:date")
    );
  }

  /**
   * Attempts to look up function fn:current-time..
   */
  @org.junit.Test
  public void functionLiteral409() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}current-time#0)",
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
   * Attempts to invoke function fn:current-time..
   */
  @org.junit.Test
  public void functionLiteral410() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}current-time#0()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:time")
    );
  }

  /**
   * Attempts to look up function fn:implicit-timezone..
   */
  @org.junit.Test
  public void functionLiteral411() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}implicit-timezone#0)",
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
   * Attempts to invoke function fn:implicit-timezone..
   */
  @org.junit.Test
  public void functionLiteral412() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}implicit-timezone#0()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:dayTimeDuration")
    );
  }

  /**
   * Attempts to look up function fn:default-collation..
   */
  @org.junit.Test
  public void functionLiteral413() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}default-collation#0)",
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
   * Attempts to invoke function fn:default-collation..
   */
  @org.junit.Test
  public void functionLiteral414() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}default-collation#0()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.w3.org/2005/xpath-functions/collation/codepoint")
    );
  }

  /**
   * Attempts to look up function fn:static-base-uri..
   */
  @org.junit.Test
  public void functionLiteral415() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}static-base-uri#0)",
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
   * Attempts to invoke function fn:static-base-uri.  Note that this actually returns a property of the dynamic context!.
   */
  @org.junit.Test
  public void functionLiteral416() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}static-base-uri#0()",
      ctx);
    try {
      query.baseURI("http://www.example.com");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:anyURI?")
    );
  }

  /**
   * Attempts to look up function fn:function-lookup..
   */
  @org.junit.Test
  public void functionLiteral417() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}function-lookup#2)",
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
   * Attempts to invoke function fn:function-lookup..
   */
  @org.junit.Test
  public void functionLiteral418() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}function-lookup#2(fn:QName('http://www.example.org', 'foo:bar'), 1)",
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
   * Attempts to look up function fn:function-name..
   */
  @org.junit.Test
  public void functionLiteral419() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}function-name#1)",
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
   * Attempts to invoke function fn:function-name..
   */
  @org.junit.Test
  public void functionLiteral420() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}function-name#1(fn:abs#1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("fn:QName('http://www.w3.org/2005/xpath-functions', 'fn:abs')")
    );
  }

  /**
   * Attempts to look up function fn:function-arity..
   */
  @org.junit.Test
  public void functionLiteral421() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}function-arity#1)",
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
   * Attempts to invoke function fn:function-arity..
   */
  @org.junit.Test
  public void functionLiteral422() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}function-arity#1(fn:abs#1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:map..
   */
  @org.junit.Test
  public void functionLiteral423() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}map#2)",
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
   * Attempts to invoke function fn:map..
   */
  @org.junit.Test
  public void functionLiteral424() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}map#2(xs:int#1, (\"23\", \"29\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("(23, 29)")
    );
  }

  /**
   * Attempts to look up function fn:filter..
   */
  @org.junit.Test
  public void functionLiteral425() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}filter#2)",
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
   * Attempts to invoke function fn:filter..
   */
  @org.junit.Test
  public void functionLiteral426() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}filter#2(function($a) {$a mod 2 = 0}, 1 to 10)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("(2, 4, 6, 8, 10)")
    );
  }

  /**
   * Attempts to look up function fn:fold-left..
   */
  @org.junit.Test
  public void functionLiteral427() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}fold-left#3)",
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
   * Attempts to invoke function fn:fold-left..
   */
  @org.junit.Test
  public void functionLiteral428() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}fold-left#3(fn:concat(?, \".\", ?), \"\", 1 to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, ".1.2.3.4.5")
    );
  }

  /**
   * Attempts to look up function fn:fold-right..
   */
  @org.junit.Test
  public void functionLiteral429() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}fold-right#3)",
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
   * Attempts to invoke function fn:fold-right..
   */
  @org.junit.Test
  public void functionLiteral430() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}fold-right#3(fn:concat(?, \".\", ?), \"\", 1 to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.2.3.4.5.")
    );
  }

  /**
   * Attempts to look up function fn:map-pairs..
   */
  @org.junit.Test
  public void functionLiteral431() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2005/xpath-functions}map-pairs#3)",
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
   * Attempts to invoke function fn:map-pairs..
   */
  @org.junit.Test
  public void functionLiteral432() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2005/xpath-functions}map-pairs#3(concat#2, (\"a\", \"b\", \"c\"), (\"x\", \"y\", \"z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("(\"ax\", \"by\", \"cz\")")
    );
  }

  /**
   * Attempts to look up function xs:untypedAtomic..
   */
  @org.junit.Test
  public void functionLiteral433() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}untypedAtomic#1)",
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
   * Attempts to invoke function xs:untypedAtomic..
   */
  @org.junit.Test
  public void functionLiteral434() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}untypedAtomic#1('string')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "string")
    );
  }

  /**
   * Attempts to look up function xs:dateTime..
   */
  @org.junit.Test
  public void functionLiteral435() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}dateTime#1)",
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
   * Attempts to invoke function xs:dateTime..
   */
  @org.junit.Test
  public void functionLiteral436() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}dateTime#1('1970-01-02T04:05:06Z')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1970-01-02T04:05:06Z")
    );
  }

  /**
   * Attempts to look up function xs:date..
   */
  @org.junit.Test
  public void functionLiteral437() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}date#1)",
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
   * Attempts to invoke function xs:date..
   */
  @org.junit.Test
  public void functionLiteral438() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}date#1('1970-01-02Z')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1970-01-02Z")
    );
  }

  /**
   * Attempts to look up function xs:time..
   */
  @org.junit.Test
  public void functionLiteral439() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}time#1)",
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
   * Attempts to invoke function xs:time..
   */
  @org.junit.Test
  public void functionLiteral440() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}time#1('01:02:03Z')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "01:02:03Z")
    );
  }

  /**
   * Attempts to look up function xs:duration..
   */
  @org.junit.Test
  public void functionLiteral441() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}duration#1)",
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
   * Attempts to invoke function xs:duration..
   */
  @org.junit.Test
  public void functionLiteral442() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}duration#1('P5Y2M10DT15H')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P5Y2M10DT15H")
    );
  }

  /**
   * Attempts to look up function xs:yearMonthDuration..
   */
  @org.junit.Test
  public void functionLiteral443() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}yearMonthDuration#1)",
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
   * Attempts to invoke function xs:yearMonthDuration..
   */
  @org.junit.Test
  public void functionLiteral444() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}yearMonthDuration#1('P1Y')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1Y")
    );
  }

  /**
   * Attempts to look up function xs:dayTimeDuration..
   */
  @org.junit.Test
  public void functionLiteral445() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}dayTimeDuration#1)",
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
   * Attempts to invoke function xs:dayTimeDuration..
   */
  @org.junit.Test
  public void functionLiteral446() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}dayTimeDuration#1('PT15H')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT15H")
    );
  }

  /**
   * Attempts to look up function xs:float..
   */
  @org.junit.Test
  public void functionLiteral447() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}float#1)",
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
   * Attempts to invoke function xs:float..
   */
  @org.junit.Test
  public void functionLiteral448() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}float#1('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:double..
   */
  @org.junit.Test
  public void functionLiteral449() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}double#1)",
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
   * Attempts to invoke function xs:double..
   */
  @org.junit.Test
  public void functionLiteral450() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}double#1('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:decimal..
   */
  @org.junit.Test
  public void functionLiteral451() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}decimal#1)",
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
   * Attempts to invoke function xs:decimal..
   */
  @org.junit.Test
  public void functionLiteral452() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}decimal#1('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:integer..
   */
  @org.junit.Test
  public void functionLiteral453() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}integer#1)",
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
   * Attempts to invoke function xs:integer..
   */
  @org.junit.Test
  public void functionLiteral454() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}integer#1('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:nonPositiveInteger..
   */
  @org.junit.Test
  public void functionLiteral455() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}nonPositiveInteger#1)",
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
   * Attempts to invoke function xs:nonPositiveInteger..
   */
  @org.junit.Test
  public void functionLiteral456() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}nonPositiveInteger#1('-1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1")
    );
  }

  /**
   * Attempts to look up function xs:negativeInteger..
   */
  @org.junit.Test
  public void functionLiteral457() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}negativeInteger#1)",
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
   * Attempts to invoke function xs:negativeInteger..
   */
  @org.junit.Test
  public void functionLiteral458() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}negativeInteger#1('-1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1")
    );
  }

  /**
   * Attempts to look up function xs:long..
   */
  @org.junit.Test
  public void functionLiteral459() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}long#1)",
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
   * Attempts to invoke function xs:long..
   */
  @org.junit.Test
  public void functionLiteral460() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}long#1('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:int..
   */
  @org.junit.Test
  public void functionLiteral461() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}int#1)",
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
   * Attempts to invoke function xs:int..
   */
  @org.junit.Test
  public void functionLiteral462() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}int#1('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:short..
   */
  @org.junit.Test
  public void functionLiteral463() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}short#1)",
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
   * Attempts to invoke function xs:short..
   */
  @org.junit.Test
  public void functionLiteral464() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}short#1('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:byte..
   */
  @org.junit.Test
  public void functionLiteral465() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}byte#1)",
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
   * Attempts to invoke function xs:byte..
   */
  @org.junit.Test
  public void functionLiteral466() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}byte#1('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:nonNegativeInteger..
   */
  @org.junit.Test
  public void functionLiteral467() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}nonNegativeInteger#1)",
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
   * Attempts to invoke function xs:nonNegativeInteger..
   */
  @org.junit.Test
  public void functionLiteral468() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}nonNegativeInteger#1('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:unsignedLong..
   */
  @org.junit.Test
  public void functionLiteral469() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}unsignedLong#1)",
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
   * Attempts to invoke function xs:unsignedLong..
   */
  @org.junit.Test
  public void functionLiteral470() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}unsignedLong#1('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:unsignedInt..
   */
  @org.junit.Test
  public void functionLiteral471() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}unsignedInt#1)",
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
   * Attempts to invoke function xs:unsignedInt..
   */
  @org.junit.Test
  public void functionLiteral472() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}unsignedInt#1('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:unsignedShort..
   */
  @org.junit.Test
  public void functionLiteral473() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}unsignedShort#1)",
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
   * Attempts to invoke function xs:unsignedShort..
   */
  @org.junit.Test
  public void functionLiteral474() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}unsignedShort#1('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:unsignedByte..
   */
  @org.junit.Test
  public void functionLiteral475() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}unsignedByte#1)",
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
   * Attempts to invoke function xs:unsignedByte..
   */
  @org.junit.Test
  public void functionLiteral476() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}unsignedByte#1('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:nonNegativeInteger..
   */
  @org.junit.Test
  public void functionLiteral477() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}nonNegativeInteger#1)",
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
   * Attempts to invoke function xs:nonNegativeInteger..
   */
  @org.junit.Test
  public void functionLiteral478() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}nonNegativeInteger#1('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:positiveInteger..
   */
  @org.junit.Test
  public void functionLiteral479() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}positiveInteger#1)",
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
   * Attempts to invoke function xs:positiveInteger..
   */
  @org.junit.Test
  public void functionLiteral480() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}positiveInteger#1('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:gYearMonth..
   */
  @org.junit.Test
  public void functionLiteral481() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}gYearMonth#1)",
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
   * Attempts to invoke function xs:gYearMonth..
   */
  @org.junit.Test
  public void functionLiteral482() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}gYearMonth#1('2001-10Z')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2001-10Z")
    );
  }

  /**
   * Attempts to look up function xs:gYear..
   */
  @org.junit.Test
  public void functionLiteral483() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}gYear#1)",
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
   * Attempts to invoke function xs:gYear..
   */
  @org.junit.Test
  public void functionLiteral484() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}gYear#1('2012Z')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2012Z")
    );
  }

  /**
   * Attempts to look up function xs:gMonthDay..
   */
  @org.junit.Test
  public void functionLiteral485() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}gMonthDay#1)",
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
   * Attempts to invoke function xs:gMonthDay..
   */
  @org.junit.Test
  public void functionLiteral486() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}gMonthDay#1('--11-01Z')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "--11-01Z")
    );
  }

  /**
   * Attempts to look up function xs:gDay..
   */
  @org.junit.Test
  public void functionLiteral487() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}gDay#1)",
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
   * Attempts to invoke function xs:gDay..
   */
  @org.junit.Test
  public void functionLiteral488() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}gDay#1('---01Z')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "---01Z")
    );
  }

  /**
   * Attempts to look up function xs:gMonth..
   */
  @org.junit.Test
  public void functionLiteral489() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}gMonth#1)",
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
   * Attempts to invoke function xs:gMonth..
   */
  @org.junit.Test
  public void functionLiteral490() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}gMonth#1('--11Z')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "--11Z")
    );
  }

  /**
   * Attempts to look up function xs:string..
   */
  @org.junit.Test
  public void functionLiteral491() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}string#1)",
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
   * Attempts to invoke function xs:string..
   */
  @org.junit.Test
  public void functionLiteral492() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}string#1('string')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "string")
    );
  }

  /**
   * Attempts to look up function xs:normalizeString..
   */
  @org.junit.Test
  public void functionLiteral493() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}normalizedString#1)",
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
   * Attempts to invoke function xs:normalizeString..
   */
  @org.junit.Test
  public void functionLiteral494() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}normalizedString#1('normalized\n" +
      "string')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "normalized string")
    );
  }

  /**
   * Attempts to look up function xs:token..
   */
  @org.junit.Test
  public void functionLiteral495() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}token#1)",
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
   * Attempts to invoke function xs:token..
   */
  @org.junit.Test
  public void functionLiteral496() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}token#1('token')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "token")
    );
  }

  /**
   * Attempts to look up function xs:language..
   */
  @org.junit.Test
  public void functionLiteral497() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}language#1)",
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
   * Attempts to invoke function xs:language..
   */
  @org.junit.Test
  public void functionLiteral498() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}language#1('en')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "en")
    );
  }

  /**
   * Attempts to look up function xs:NMTOKEN..
   */
  @org.junit.Test
  public void functionLiteral499() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}NMTOKEN#1)",
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
   * Attempts to invoke function xs:NMTOKEN..
   */
  @org.junit.Test
  public void functionLiteral500() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}NMTOKEN#1('NMTOKEN')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NMTOKEN")
    );
  }

  /**
   * Attempts to look up function xs:Name..
   */
  @org.junit.Test
  public void functionLiteral501() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}Name#1)",
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
   * Attempts to invoke function xs:Name..
   */
  @org.junit.Test
  public void functionLiteral502() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}Name#1('Name')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Name")
    );
  }

  /**
   * Attempts to look up function xs:NCName..
   */
  @org.junit.Test
  public void functionLiteral503() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}NCName#1)",
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
   * Attempts to invoke function xs:NCName..
   */
  @org.junit.Test
  public void functionLiteral504() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}NCName#1('NCName')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NCName")
    );
  }

  /**
   * Attempts to look up function xs:ID..
   */
  @org.junit.Test
  public void functionLiteral505() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}ID#1)",
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
   * Attempts to invoke function xs:ID..
   */
  @org.junit.Test
  public void functionLiteral506() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}ID#1('ID')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ID")
    );
  }

  /**
   * Attempts to look up function xs:IDREF..
   */
  @org.junit.Test
  public void functionLiteral507() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}IDREF#1)",
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
   * Attempts to invoke function xs:IDREF..
   */
  @org.junit.Test
  public void functionLiteral508() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}IDREF#1('IDREF')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "IDREF")
    );
  }

  /**
   * Attempts to look up function xs:ENTITY..
   */
  @org.junit.Test
  public void functionLiteral509() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}ENTITY#1)",
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
   * Attempts to invoke function xs:ENTITY..
   */
  @org.junit.Test
  public void functionLiteral510() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}ENTITY#1('ENTITY')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ENTITY")
    );
  }

  /**
   * Attempts to look up function xs:boolean..
   */
  @org.junit.Test
  public void functionLiteral511() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}boolean#1)",
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
   * Attempts to invoke function xs:boolean..
   */
  @org.junit.Test
  public void functionLiteral512() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}boolean#1('1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true")
    );
  }

  /**
   * Attempts to look up function xs:base64Binary..
   */
  @org.junit.Test
  public void functionLiteral513() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}base64Binary#1)",
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
   * Attempts to invoke function xs:base64Binary..
   */
  @org.junit.Test
  public void functionLiteral514() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}base64Binary#1('D74D35D35D35')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "D74D35D35D35")
    );
  }

  /**
   * Attempts to look up function xs:hexBinary..
   */
  @org.junit.Test
  public void functionLiteral515() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}hexBinary#1)",
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
   * Attempts to invoke function xs:hexBinary..
   */
  @org.junit.Test
  public void functionLiteral516() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}hexBinary#1('0fb7')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0FB7")
    );
  }

  /**
   * Attempts to look up function xs:anyURI..
   */
  @org.junit.Test
  public void functionLiteral517() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}anyURI#1)",
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
   * Attempts to invoke function xs:anyURI..
   */
  @org.junit.Test
  public void functionLiteral518() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}anyURI#1('http://www.example.org/')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.org/")
    );
  }

  /**
   * Attempts to look up function xs:QName..
   */
  @org.junit.Test
  public void functionLiteral519() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}QName#1)",
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
   * Attempts to invoke function xs:QName..
   */
  @org.junit.Test
  public void functionLiteral520() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}QName#1('fn:QName')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "fn:QName")
    );
  }

  /**
   * Attempts to look up function xs:IDREFS..
   */
  @org.junit.Test
  public void functionLiteral523() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}IDREFS#1)",
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
   * Attempts to invoke function xs:IDREFS..
   */
  @org.junit.Test
  public void functionLiteral524() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}IDREFS#1('ID1 ID2 ID3')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ID1 ID2 ID3")
    );
  }

  /**
   * Attempts to look up function xs:NMTOKENS..
   */
  @org.junit.Test
  public void functionLiteral525() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}NMTOKENS#1)",
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
   * Attempts to invoke function xs:NMTOKENS..
   */
  @org.junit.Test
  public void functionLiteral526() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}NMTOKENS#1('NMTOKEN1 NMTOKEN2 NMTOKEN3')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NMTOKEN1 NMTOKEN2 NMTOKEN3")
    );
  }

  /**
   * Attempts to look up function xs:ENTITES..
   */
  @org.junit.Test
  public void functionLiteral527() {
    final XQuery query = new XQuery(
      "exists(Q{http://www.w3.org/2001/XMLSchema}ENTITIES#1)",
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
   * Attempts to invoke function xs:ENTITIES..
   */
  @org.junit.Test
  public void functionLiteral528() {
    final XQuery query = new XQuery(
      "Q{http://www.w3.org/2001/XMLSchema}ENTITIES#1('ENTITY1 ENTITY2 ENTITY3')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ENTITY1 ENTITY2 ENTITY3")
    );
  }

  /**
   * Check that reserved function name attribute is handled correctly. .
   */
  @org.junit.Test
  public void namedFunctionRefReservedFunctionNames001() {
    final XQuery query = new XQuery(
      "\n" +
      "\tfn:exists(attribute#0)\n" +
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
      error("XPST0003")
    );
  }

  /**
   * Check that reserved function name comment is handled correctly. .
   */
  @org.junit.Test
  public void namedFunctionRefReservedFunctionNames002() {
    final XQuery query = new XQuery(
      "\n" +
      "\tfn:exists(comment#0)\n" +
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
      error("XPST0003")
    );
  }

  /**
   * Check that reserved function name document-node is handled correctly. .
   */
  @org.junit.Test
  public void namedFunctionRefReservedFunctionNames003() {
    final XQuery query = new XQuery(
      "\n" +
      "\tfn:exists(document-node#0)\n" +
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
      error("XPST0003")
    );
  }

  /**
   * Check that reserved function name element is handled correctly. .
   */
  @org.junit.Test
  public void namedFunctionRefReservedFunctionNames004() {
    final XQuery query = new XQuery(
      "\n" +
      "\tfn:exists(element#0)\n" +
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
      error("XPST0003")
    );
  }

  /**
   * Check that reserved function name empty-sequence is handled correctly. .
   */
  @org.junit.Test
  public void namedFunctionRefReservedFunctionNames005() {
    final XQuery query = new XQuery(
      "\n" +
      "\tfn:exists(empty-sequence#0)\n" +
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
      error("XPST0003")
    );
  }

  /**
   * Check that reserved function name function is handled correctly. .
   */
  @org.junit.Test
  public void namedFunctionRefReservedFunctionNames006() {
    final XQuery query = new XQuery(
      "\n" +
      "\tfn:exists(function#0)\n" +
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
      error("XPST0003")
    );
  }

  /**
   * Check that reserved function name if is handled correctly. .
   */
  @org.junit.Test
  public void namedFunctionRefReservedFunctionNames007() {
    final XQuery query = new XQuery(
      "\n" +
      "\tfn:exists(if#0)\n" +
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
      error("XPST0003")
    );
  }

  /**
   * Check that reserved function name item is handled correctly. .
   */
  @org.junit.Test
  public void namedFunctionRefReservedFunctionNames008() {
    final XQuery query = new XQuery(
      "\n" +
      "\tfn:exists(item#0)\n" +
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
      error("XPST0003")
    );
  }

  /**
   * Check that reserved function name namespace-node is handled correctly. .
   */
  @org.junit.Test
  public void namedFunctionRefReservedFunctionNames009() {
    final XQuery query = new XQuery(
      "\n" +
      "\tfn:exists(namespace-node#0)\n" +
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
      error("XPST0003")
    );
  }

  /**
   * Check that reserved function name node is handled correctly. .
   */
  @org.junit.Test
  public void namedFunctionRefReservedFunctionNames010() {
    final XQuery query = new XQuery(
      "\n" +
      "\tfn:exists(node#0)\n" +
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
      error("XPST0003")
    );
  }

  /**
   * Check that reserved function name processing-instruction is handled correctly. .
   */
  @org.junit.Test
  public void namedFunctionRefReservedFunctionNames011() {
    final XQuery query = new XQuery(
      "\n" +
      "\tfn:exists(processing-instruction#0)\n" +
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
      error("XPST0003")
    );
  }

  /**
   * Check that reserved function name schema-attribute is handled correctly. .
   */
  @org.junit.Test
  public void namedFunctionRefReservedFunctionNames012() {
    final XQuery query = new XQuery(
      "\n" +
      "\tfn:exists(schema-attribute#0)\n" +
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
      error("XPST0003")
    );
  }

  /**
   * Check that reserved function name schema-element is handled correctly. .
   */
  @org.junit.Test
  public void namedFunctionRefReservedFunctionNames013() {
    final XQuery query = new XQuery(
      "\n" +
      "\tfn:exists(schema-element#0)\n" +
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
      error("XPST0003")
    );
  }

  /**
   * Check that reserved function name switch is handled correctly. .
   */
  @org.junit.Test
  public void namedFunctionRefReservedFunctionNames014() {
    final XQuery query = new XQuery(
      "\n" +
      "\tfn:exists(switch#0)\n" +
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
      error("XPST0003")
    );
  }

  /**
   * Check that reserved function name text is handled correctly. .
   */
  @org.junit.Test
  public void namedFunctionRefReservedFunctionNames015() {
    final XQuery query = new XQuery(
      "\n" +
      "\tfn:exists(text#0)\n" +
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
      error("XPST0003")
    );
  }

  /**
   * Check that reserved function name typeswitch is handled correctly. .
   */
  @org.junit.Test
  public void namedFunctionRefReservedFunctionNames016() {
    final XQuery query = new XQuery(
      "\n" +
      "\tfn:exists(typeswitch#0)\n" +
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
      error("XPST0003")
    );
  }
}
