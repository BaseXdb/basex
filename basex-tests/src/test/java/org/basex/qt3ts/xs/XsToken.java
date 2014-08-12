package org.basex.qt3ts.xs;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for conversion to/from xs:normalizedString.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class XsToken extends QT3TestSet {

  /**
.
   */
  @org.junit.Test
  public void cbclToken001() {
    final XQuery query = new XQuery(
      "xs:token(xs:token(\"test\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "test")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void cbclToken002() {
    final XQuery query = new XQuery(
      "string-length(xs:token('&#x9;'))",
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
.
   */
  @org.junit.Test
  public void cbclToken003() {
    final XQuery query = new XQuery(
      "xs:token(5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void cbclToken004() {
    final XQuery query = new XQuery(
      "\"&#x9;\" castable as xs:token",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void cbclToken005() {
    final XQuery query = new XQuery(
      "5 castable as xs:token",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void cbclToken006() {
    final XQuery query = new XQuery(
      "xs:token(\"test\") castable as xs:token",
      ctx);
    try {
      result = new QT3Result(query.value());
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
