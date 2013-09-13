package org.basex.test;

import static org.junit.Assert.*;

import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;

/**
 * This class contains some methods for performing advanced query tests.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class AdvancedQueryTest extends SandboxTest {
  /**
   * Runs the specified query and normalizes newlines.
   * @param query query string
   * @return result
   */
  protected static String query(final String query) {
    final QueryProcessor qp = new QueryProcessor(query, context);
    qp.ctx.sc.baseURI(".");
    try {
      final ArrayOutput ao = new ArrayOutput();
      final Serializer ser = qp.getSerializer(ao);
      qp.execute().serialize(ser);
      ser.close();
      return ao.toString().replaceAll("(\\r|\\n)+ *", "");
    } catch(final Exception ex) {
      final AssertionError err = new AssertionError("Query failed:\n" + query);
      err.initCause(ex);
      throw err;
    } finally {
      qp.close();
    }
  }

  /**
   * Checks if a query yields the specified string.
   * @param query query string
   * @param result query result
   */
  protected static void query(final String query, final Object result) {
    final String res = query(query);
    final String exp = result.toString();
    if(!res.equals(exp))
      fail("Wrong result:\n[Q] " + query + "\n[E] \u00bb" + result +
          "\u00ab\n[F] \u00bb" + res + '\u00ab');
  }
}
