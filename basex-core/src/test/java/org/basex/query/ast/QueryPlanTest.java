package org.basex.query.ast;

import static org.basex.util.Prop.*;
import static org.junit.Assert.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Abstract test class for properties on the Query Plan.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public abstract class QueryPlanTest extends AdvancedQueryTest {
  /**
   * Checks the query plan and the result.
   * @param query query
   * @param expected result or {@code null} for no comparison
   * @param checks queries on the query plan
   */
  protected static void check(final String query, final Object expected, final String... checks) {
    try(QueryProcessor qp = new QueryProcessor(query, context)) {
      // compile query
      qp.compile();
      // retrieve compiled query plan
      final FDoc plan = qp.plan();
      // compare results
      if(expected != null) {
        final String result = normNL(qp.value().serialize().toString());
        assertEquals("\nQuery: " + query + '\n', expected.toString(), result);
      }

      for(final String p : checks) {
        if(new QueryProcessor(p, context).context(plan).value() != Bln.TRUE) {
          fail(NL + "- Query: " + query + NL + "- Check: " + p + NL +
              "- Plan: " + plan.serialize());
        }
      }
    } catch(final Exception ex) {
      throw new AssertionError(query, ex);
    }
  }
}
