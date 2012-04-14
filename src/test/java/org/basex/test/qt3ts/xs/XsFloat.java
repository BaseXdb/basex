package org.basex.test.qt3ts.xs;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests features of XSD 1.1 floats.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class XsFloat extends QT3TestSet {

  /**
   * Ensure +INF rejected in XSD 1.0.
   */
  @org.junit.Test
  public void xsFloat004() {
    final XQuery query = new XQuery(
      "exists(xs:float(\"+INF\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0001")
    );
  }
}
