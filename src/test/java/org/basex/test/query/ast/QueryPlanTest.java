package org.basex.test.query.ast;

import static org.basex.core.Prop.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.test.*;

/**
 * Abstract test class for properties on the Query Plan.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public abstract class QueryPlanTest extends SandboxTest {
  /**
   * Checks the query plan and the result.
   * @param qu query
   * @param res result or {@code null} for no comparison
   * @param pr queries on the query plan
   */
  static final void check(final String qu, final String res,
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
      if(res != null)
        assertEquals("Query result:", res, qp.execute().toString());

      // check query plan
      context.openDB(plan);
      for(final String p : pr) {
        if(!new XQuery(p).execute(context).equals("true"))
          fail(p + ':' + NL + qp.ctx.root + NL + new XQuery("/").execute(context));
      }
    } catch(final Exception ex) {
      throw new Error(ex.getMessage(), ex);
    } finally {
      qp.close();
    }
  }
}
