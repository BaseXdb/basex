package org.basex.test.query.advanced;

import static org.junit.Assert.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.func.Function;
import org.basex.query.item.AtomType;
import org.basex.query.item.SeqType;
import org.basex.query.util.Err;

/**
 * This class contains some methods for performing advanced query tests.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
abstract class AdvancedQueryTest {
  /** Database context. */
  protected static final Context CONTEXT = new Context();

  /**
   * Runs the specified query.
   * @param qu query
   * @return result
   * @throws QueryException database exception
   */
  protected static String query(final String qu) throws QueryException {
    final QueryProcessor qp = new QueryProcessor(qu, CONTEXT);
    try {
      return qp.execute().toString().replaceAll("(\\r|\\n) *", "");
    } finally {
      try { qp.close(); } catch(final IOException e) { }
    }
  }

  /**
   * Checks if a query yields the specified string.
   * @param query query to be run
   * @param result query result
   * @throws QueryException database exception
   */
  protected static void query(final String query, final String result)
      throws QueryException {
    assertEquals(result, query(query));
  }

  /**
   * Checks if a query yields the specified error code.
   * @param query query to be run
   * @param result query result
   * @throws QueryException database exception
   */
  protected static void contains(final String query, final String result)
      throws QueryException {
    assertContains(query(query), result);
  }

  /**
   * Checks if a query yields the specified error code.
   * @param query query to be run
   * @param error expected error
   */
  protected static void error(final String query, final Err... error) {
    try {
      query(query);
      fail("[" + error[0] + "] expected for query: " + query);
    } catch(final QueryException ex) {
      check(ex, error);
    }
  }

  /**
   * Checks if an exception yields one of the specified error codes.
   * @param ex exception
   * @param error expected errors
   */
  protected static void check(final QueryException ex, final Err... error) {
    final String msg = ex.getMessage();
    boolean found = false;
    for(final Err e : error) found |= msg.contains(e.code());
    if(!found) {
      fail("'" + error[0].code() + "' not contained in '" + msg + "'.");
    }
  }

  /**
   * Checks if a string is contained in another string.
   * @param str string
   * @param sub sub string
   */
  private static void assertContains(final String str, final String sub) {
    if(!str.contains(sub)) {
      fail("'" + sub + "' not contained in '" + str + "'.");
    }
  }

  /**
   * Checks if the specified function correctly handles its argument types,
   * and returns the function name.
   * @param def function definition
   * types are supported.
   * @return function name
   */
  protected String check(final Function def) {
    final String desc = def.toString();
    final String name = desc.replaceAll("\\(.*", "");

    // test too few, too many, and wrong argument types
    for(int al = Math.max(def.min - 1, 0); al <= def.max + 1; al++) {
      final boolean in = al >= def.min && al <= def.max;
      final StringBuilder qu = new StringBuilder(name + "(");
      int any = 0;
      for(int a = 0; a < al; a++) {
        if(a != 0) qu.append(", ");
        if(in) {
          // test arguments
          if(def.args[a].type == AtomType.STR) {
            qu.append("1");
          } else { // any type (skip test)
            qu.append("'X'");
            if(SeqType.STR.instance(def.args[a])) any++;
          }
        } else {
          // test wrong number of arguments
          qu.append("'x'");
        }
      }
      // skip test if all types are arbitrary
      if((def.min > 0 || al != 0) && (any == 0 || any != al)) {
        final String query = qu.append(")").toString();
        if(in) error(query, Err.XPTYPE, Err.NODBCTX);
        else error(query, Err.XPARGS);
      }
    }
    return name;
  }

  /**
   * Returns serialization parameters.
   * @param arg serialization arguments
   * @return parameter string
   */
  protected static String serialParams(final String arg) {
    return "<serialization-parameters " +
      "xmlns='http://www.w3.org/2010/xslt-xquery-serialization'>" + arg +
      "</serialization-parameters>";
  }
}
