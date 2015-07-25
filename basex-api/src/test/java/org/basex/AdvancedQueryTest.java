package org.basex;

import static org.junit.Assert.*;

import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;

/**
 * This class contains some methods for performing advanced query tests.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class AdvancedQueryTest extends SandboxTest {
  /**
   * Runs the specified query and normalizes newlines.
   * @param query query string
   * @return result
   */
  private static String query(final String query) {
    try(final QueryProcessor qp = new QueryProcessor(query, context)) {
      final ArrayOutput ao = new ArrayOutput();
      try(final Serializer ser = qp.getSerializer(ao)) {
        qp.value().serialize(ser);
      }
      return ao.toString().replaceAll("(\\r|\\n)\\s*", "");
    } catch(final Exception ex) {
      ex.printStackTrace();
      final AssertionError err = new AssertionError("Query failed:\n" + query);
      err.initCause(ex);
      throw err;
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
      fail("Wrong result:\n[Q] " + query + "\n[E] " + result + "\n[F] " + res);
  }
}
