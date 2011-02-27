package org.basex.test.query;

import static org.junit.Assert.*;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.XQuery;
import org.basex.query.func.FunDef;
import org.basex.query.item.DBNode;
import org.basex.query.util.Err;
import org.basex.util.Util;

/**
 * This class contains some methods for performing advanced query tests.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class AdvancedQueryTest {
  /** Database context. */
  protected static final Context CTX = new Context();
  /** Function prefix. */
  protected final String pref;

  /**
   * Constructor.
   * @param pr prefix of tested functions.
   */
  public AdvancedQueryTest(final String pr) {
    pref = pr;
  }

  /**
   * Runs the specified query.
   * @param qu query
   * @return result
   * @throws BaseXException database exception
   */
  protected static String query(final String qu) throws BaseXException {
    return new XQuery(qu).execute(CTX).replaceAll("(\\r|\\n) *", "");
  }

  /**
   * Checks if a query yields the specified string.
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
   * @param error expected error
   */
  protected static void error(final String query, final Err error) {
    try {
      query(query);
      fail("[" + error + "] expected for query: " + query);
    } catch(final BaseXException ex) {
      assertContains(ex.getMessage(), error.code());
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
   * Checks the arguments of a function and returns the function name.
   * @param def function definition
   * @param args arguments
   * @return function name
   */
  protected String check(final FunDef def, final Class<?>... args) {
    final String desc = def.toString();
    final String name = pref + ":" + desc.replaceAll("\\(.*", "");
    final int max = desc.contains("()") ? 0 : desc.split(",").length;
    final int min = max + 1 - desc.split("\\?").length;
    if(max != args.length) Util.notexpected("Check #arguments: " + def);

    // test too few, too many, and wrong argument types
    for(int al = Math.max(min - 1, 0); al <= max + 1; al++) {
      final boolean in = al >= min && al <= max;
      final StringBuilder qu = new StringBuilder(name + "(");
      int any = 0;
      boolean db = false;
      for(int a = 0; a < al; a++) {
        if(a != 0) qu.append(", ");
        if(in) {
          // test arguments
          if(args[a] == String.class) {
            qu.append("1");
          } else if(args[a] == null) { // any type (skip test)
            qu.append("'X'");
            any++;
          } else {
            qu.append("'X'");
            db |= args[a] == DBNode.class;
          }
        } else {
          // test wrong number of arguments
          qu.append("'x'");
        }
      }
      // skip test if all types are arbitrary
      if((al != 0 || min > 0) && (any == 0 || any != al)) {
        error(qu + ")", db ? Err.NODBCTX : in ? Err.XPTYPE : Err.XPARGS);
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
