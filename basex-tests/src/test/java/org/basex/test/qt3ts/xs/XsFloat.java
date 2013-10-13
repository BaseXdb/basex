package org.basex.test.qt3ts.xs;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests features of XSD 1.1 floats.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class XsFloat extends QT3TestSet {

  /**
.
   */
  @org.junit.Test
  public void cbclFloat001() {
    final XQuery query = new XQuery(
      "count(xs:float(()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void cbclFloat002() {
    final XQuery query = new XQuery(
      "xs:float(xs:double('-INF')),xs:float(xs:double('INF'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-INF INF")
    );
  }

  /**
   * Ensure +INF rejected in XSD 1.0.
   */
  @org.junit.Test
  public void xsFloat004() {
    final XQuery query = new XQuery(
      "exists(xs:float(\"+INF\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }
}
