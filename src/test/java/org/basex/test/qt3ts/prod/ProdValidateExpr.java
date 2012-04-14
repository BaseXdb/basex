package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the ValidateExpr production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdValidateExpr extends QT3TestSet {

  /**
   *  Ensure the validate keyword is parsed correctly. .
   */
  @org.junit.Test
  public void k2ValidateExpression1() {
    final XQuery query = new XQuery(
      "validate gt validate",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Test For error condition XQDY0061 using a document node. .
   */
  @org.junit.Test
  public void validateexpr26() {
    final XQuery query = new XQuery(
      "\n" +
      "        validate { document { <a/>, <b/> } }\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XQDY0061")
      ||
        error("XQDY0027")
      )
    );
  }
}
