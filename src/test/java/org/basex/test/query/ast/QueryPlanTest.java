package org.basex.test.query.ast;

import static org.basex.core.Prop.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.test.query.*;

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
      // parse compiled query plan
      qp.compile();
      final Data plan = CreateDB.mainMem(new Parser("", context.prop) {
        @Override
        public void parse(final Builder build) throws IOException {
          build.startDoc(QueryText.PLAN);
          qp.plan(new BuilderSerializer(build));
          build.endDoc();
        }
      }, context);

      // compare results
      if(res != null) {
        assertEquals(res, qp.execute().toString());
      }

      final Nodes in = new Nodes(0, plan);
      for(final String p : pr) {
        QueryProcessor query = new QueryProcessor(p, context).context(in);
        if(query.value() != Bln.TRUE) {
          query = new QueryProcessor("/*", context).context(in);
          fail(NL + "- Query: " + qu + NL + "- Check: " + p + NL +
              "- Plan: " + query.execute().toString());
        }
      }
    } catch(final Exception ex) {
      throw new Error(ex.getMessage(), ex);
    } finally {
      qp.close();
    }
  }
}
