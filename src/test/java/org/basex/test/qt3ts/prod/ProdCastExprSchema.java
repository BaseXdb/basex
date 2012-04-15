package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the CastExpr production with user-defined types.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdCastExprSchema extends QT3TestSet {

  /**
   *  Evaluates casting an xs:QName type to another xs:QName type. .
   */
  @org.junit.Test
  public void qnameCast1() {
    final XQuery query = new XQuery(
      "xs:QName(\"value1\") cast as xs:QName",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "value1")
    );
  }
}
