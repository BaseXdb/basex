package org.basex.query.ast;

import static org.basex.util.Prop.*;
import static org.junit.Assert.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Abstract test class for properties on the Query Plan.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public abstract class QueryPlanTest extends AdvancedQueryTest {
  /**
   * Checks the query plan and the result.
   * @param qu query
   * @param res result or {@code null} for no comparison
   * @param pr queries on the query plan
   */
  protected static void check(final String qu, final String res, final String... pr) {
    try(final QueryProcessor qp = new QueryProcessor(qu, context)) {
      // parse and compile query plan
      qp.compile();
      // retrieve compiled query plan
      final FDoc plan = qp.plan();
      // compare results
      if(res != null) assertEquals(res, qp.execute().toString());

      for(final String p : pr) {
        if(new QueryProcessor(p, context).context(plan).value() != Bln.TRUE) {
          fail(NL + "- Query: " + qu + NL + "- Check: " + p + NL +
              "- Plan: " + plan.serialize());
        }
      }
    } catch(final QueryException | QueryIOException ex) {
      final AssertionError err = new AssertionError(Util.message(ex));
      err.initCause(ex);
      throw err;
    }
  }
}
