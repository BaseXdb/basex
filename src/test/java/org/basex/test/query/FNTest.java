package org.basex.test.query;

import static org.junit.Assert.*;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.XQuery;
import org.basex.query.item.DBNode;
import org.basex.util.Util;

/**
 * This class contains some methods for testing project specific functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class FNTest {
  /** Database context. */
  protected static final Context CTX = new Context();

  /** Hidden constructor. */
  protected FNTest() { }
  
  /**
   * Runs the specified query.
   * @param qu query
   * @return result
   * @throws BaseXException database exception
   */
  protected static String query(final String qu) throws BaseXException {
    return new XQuery(qu).execute(CTX);
  }

  /**
   * Checks if a query yields the specified error code.
   * @param query query to be run
   * @param result query result
   * @throws BaseXException database exception
   */
  protected static void query(final String query, final String result)
      throws BaseXException {
    assertEquals(result, query(query));
  }

  /**
   * Checks if a query yields the specified error code.
   * @param query query to be run
   * @param result query result
   * @throws BaseXException database exception
   */
  protected static void contains(final String query, final String result)
      throws BaseXException {
    assertContains(query(query), result);
  }

  /**
   * Checks if a query yields the specified error code.
   * @param query query to be run
   * @param error expected error code
   */
  protected static void error(final String query, final String error) {
    try {
      query(query);
      fail("[" + error + "] expected for query: " + query);
    } catch(final BaseXException ex) {
      assertContains(ex.getMessage(), error);
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
   * Checks the arguments of a function.
   * @param name function name
   * @param args arguments
   */
  protected static void args(final String name, final Class<?>... args) {
    // test too few, too many, and wrong argument types
    for(int al = Math.max(0, args.length - 1); al <= args.length + 1; al++) {
      final String error = al == args.length ? "XPTY0004" : "XPST0017";
      final StringBuilder qu = new StringBuilder(name + "(");
      // test wrong argument types
      boolean test = al != args.length || al != 0;
      for(int a = 0; a < al; a++) {
        if(a != 0) qu.append(',');
        if(al == args.length) {
          // insert wrong type
          if(args[a] == String.class) { // string
            qu.append("123");
          } else if(args[a] == Integer.class) { // integer
            qu.append("'string'");
          } else if(args[a] == DBNode.class) { // integer
            qu.append("'string'");
          } else if(args[a] == null) { // any type (skip test)
            test = false;
          } else {
            Util.notexpected("Type not supported: " + args[a]);
          }
        } else if(al >= 0) {
          // insert dummy argument
          qu.append("'x'");
        }
      }
      if(test) error(qu + ")", error);
    }
  }
}
