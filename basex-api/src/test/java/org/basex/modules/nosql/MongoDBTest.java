package org.basex.modules.nosql;

import static org.basex.query.util.Err.*;
import static org.junit.Assert.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.junit.*;

/**
 * This class tests the XQuery Geo functions prefixed with "geo".
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Prakash Thapa
 */
public final class MongoDBTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void connection() {
    run("let $url := 'mongodb://127.0.0.1:27017/enron'" +
  "let $x:= n:connection($url,{}) return ''", "''");
    error("let $url := ''"
+ "return n:connection($url)", FUNCMP.qname());
  }
  /**
   * Query.
   * @param query query
   * @param result result
   */
  private static void run(final String query, final String result) {
    query("import module namespace n ='http://basex.org/modules/nosql/MongoDB'; "
  + query, result);
  }

  /**
   * Checks if a query yields the specified error code.
   * @param query query string
   * @param error expected error
   */
  private static void error(final String query, final QNm error) {
    final String q = "import module namespace n ='http://basex.org/modules/MongoDB';";
    final QueryProcessor qp = new QueryProcessor(q, context);
    qp.sc.baseURI(".");
    try {
      final String res = qp.execute().toString().replaceAll("(\\r|\\n) *", "");
      fail("Query did not fail:\n" + query + "\n[E] " +
          error + "...\n[F] " + res);
    } catch(final QueryException ex) {
      if(!ex.qname().eq(error))
        fail("Wrong error code:\n[E] " + error + "\n[F] " + ex.qname());
    } finally {
      qp.close();
    }
  }
}
