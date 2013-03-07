package org.basex.test.query.ast;

import static org.basex.core.Prop.*;
import static org.junit.Assert.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.test.query.*;
import org.basex.util.*;

/**
 * Abstract test class for properties on the Query Plan.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public abstract class QueryPlanTest extends AdvancedQueryTest {
  /**
   * Checks the query plan and the result.
   * @param qu query
   * @param res result or {@code null} for no comparison
   * @param pr queries on the query plan
   */
  protected static final void check(final String qu, final String res,
      final String... pr) {

    final QueryProcessor qp = new QueryProcessor(qu, context);
    try {
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
    } catch(final Exception ex) {
      final AssertionError err = new AssertionError(Util.message(ex));
      err.initCause(ex);
      throw err;
    } finally {
      qp.close();
    }
  }
}
