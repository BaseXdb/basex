package org.basex.test.qt3ts.xs;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for conversion to/from xs:anyURI.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class XsAnyURI extends QT3TestSet {

  /**
   * Test xs:anyURI on empty sequence.
   */
  @org.junit.Test
  public void cbclAnyURI001() {
    final XQuery query = new XQuery(
      "xs:anyURI(())",
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
   * Test parsing of characters '+', '-', '.' in scheme.
   */
  @org.junit.Test
  public void cbclAnyURI002() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"odd-scheme+1.://www.example.org/\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "odd-scheme+1.://www.example.org/")
    );
  }

  /**
   * Test parsing of pct-encoded characters in reg-name.
   */
  @org.junit.Test
  public void cbclAnyURI003() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://%0Ad%E2%9C%90%F0%98%9A%A0/\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://%0Ad%E2%9C%90%F0%98%9A%A0/")
    );
  }

  /**
   * Test parsing of subdelim characters in reg-name.
   */
  @org.junit.Test
  public void cbclAnyURI004() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://!$&amp;'()*+,;=/\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://!$&'()*+,;=/")
    );
  }

  /**
   * Test parsing of pct-encoded characters in segment.
   */
  @org.junit.Test
  public void cbclAnyURI005() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.org/%0Ad%E2%9C%90%F0%98%9A%A0/\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.org/%0Ad%E2%9C%90%F0%98%9A%A0/")
    );
  }

  /**
   * Test parsing of subdelim characters in seqment.
   */
  @org.junit.Test
  public void cbclAnyURI006() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.org/!/$/&amp;/'/(/)/*/+/,/;/=/\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.org/!/$/&/'/(/)/*/+/,/;/=/")
    );
  }

  /**
   * Test parsing characters ':','@' in segment.
   */
  @org.junit.Test
  public void cbclAnyURI007() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.org/:/@/\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.org/:/@/")
    );
  }

  /**
   * Test parsing of pct-encoded characters in query.
   */
  @org.junit.Test
  public void cbclAnyURI008() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.org/?%0Ad%E2%9C%90%F0%98%9A%A0/\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.org/?%0Ad%E2%9C%90%F0%98%9A%A0/")
    );
  }

  /**
   * Test parsing of subdelim characters in query.
   */
  @org.junit.Test
  public void cbclAnyURI009() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.org/?!$&amp;'()*+,;=\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.org/?!$&'()*+,;=")
    );
  }

  /**
   * Test parsing characters ':','@' in query.
   */
  @org.junit.Test
  public void cbclAnyURI010() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.org/?:@\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.org/?:@")
    );
  }

  /**
   * Test parsing of pct-encoded characters in fragment.
   */
  @org.junit.Test
  public void cbclAnyURI011() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.org/#%0Ad%E2%9C%90%F0%98%9A%A0/\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.org/#%0Ad%E2%9C%90%F0%98%9A%A0/")
    );
  }

  /**
   * Test parsing of subdelim characters in fragment.
   */
  @org.junit.Test
  public void cbclAnyURI012() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.org/#!$&amp;'()*+,;=\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.org/#!$&'()*+,;=")
    );
  }

  /**
   * Test parsing characters ':','@' in fragment.
   */
  @org.junit.Test
  public void cbclAnyURI013() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.example.org/#:@\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://www.example.org/#:@")
    );
  }
}
