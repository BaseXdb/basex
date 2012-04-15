package org.basex.test.qt3ts.misc;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the StaticContext.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MiscStaticContext extends QT3TestSet {

  /**
   *  Evaluate error condition XPST0001 .
   */
  @org.junit.Test
  public void staticContext1() {
    final XQuery query = new XQuery(
      "declare namespace test = 'http://www.example.com'; \n" +
      "        <a/> instance of element(*, test:unknownType)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }
}
