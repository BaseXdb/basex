package org.basex.qt3ts.xs;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for conversion to/from xs:normalizedString.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class XsNormalizedString extends QT3TestSet {

  /**
.
   */
  @org.junit.Test
  public void cbclNormalizedstring001() {
    final XQuery query = new XQuery(
      "xs:normalizedString(xs:normalizedString(\"test\"))",
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
  public void cbclNormalizedstring002() {
    final XQuery query = new XQuery(
      "xs:normalizedString('&#x9;')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, " ")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void cbclNormalizedstring003() {
    final XQuery query = new XQuery(
      "xs:normalizedString(5)",
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
  public void cbclNormalizedstring004() {
    final XQuery query = new XQuery(
      "\"&#x9;\" castable as xs:normalizedString",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
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
  public void cbclNormalizedstring005() {
    final XQuery query = new XQuery(
      "5 castable as xs:normalizedString",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
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
  public void cbclNormalizedstring006() {
    final XQuery query = new XQuery(
      "xs:normalizedString(\"test\") castable as xs:normalizedString",
      ctx);
    try {
      result = new QT3Result(query.value());
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
