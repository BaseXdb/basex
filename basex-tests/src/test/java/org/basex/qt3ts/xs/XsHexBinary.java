package org.basex.qt3ts.xs;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for conversion to/from hexBinary.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class XsHexBinary extends QT3TestSet {

  /**
.
   */
  @org.junit.Test
  public void cbclHexbinary001() {
    final XQuery query = new XQuery(
      "count(xs:hexBinary(()))",
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
}
