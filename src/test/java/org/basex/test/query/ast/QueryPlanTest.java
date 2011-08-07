package org.basex.test.query.ast;

import java.io.IOError;
import java.io.IOException;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.XQuery;
import org.basex.data.Data;
import org.basex.io.serial.BuilderSerializer;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;

import static org.junit.Assert.*;
import static org.basex.util.Token.token;
import static org.basex.core.Prop.NL;

/**
 * Abstract test class for properties on the Query Plan.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public abstract class QueryPlanTest {
  /** Database context. */
  private static final Context CTX = new Context();

  /**
   * Checks the query plan and the result.
   * @param qu query
   * @param res result
   * @param pr queries on the query plan
   */
  protected final void check(final String qu, final String res,
      final String... pr) {

    final QueryProcessor qp = new QueryProcessor(qu, CTX);
    try {
      // parse compiled query plan
      qp.compile();
      final Data plan = CreateDB.xml(new Parser("") {
        @Override
        public void parse(final Builder build) throws IOException {
          build.startDoc(token("QueryPlan"));
          qp.plan(new BuilderSerializer(build));
          build.endDoc();
        }
      }, CTX);

      // compare results
      assertEquals("Query result:", res, qp.execute().toString());

      // check query plan
      CTX.openDB(plan);
      for(final String p : pr) {
        if(!new XQuery(p).execute(CTX).equals("true"))
          fail(p + ":" + NL + qp.ctx.root + NL
              + new XQuery("/").execute(CTX));
      }
    } catch(final QueryException e) {
      throw new Error(e.getMessage(), e);
    } catch(final BaseXException e) {
      throw new Error(e.getMessage(), e);
    } catch(final IOException e) {
      throw new IOError(e);
    } finally {
      try { qp.close(); } catch(final IOException e) { }
    }
  }
}
