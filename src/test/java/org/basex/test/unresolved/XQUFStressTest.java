package org.basex.test.unresolved;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.XQuery;
import org.basex.util.Util;

/**
 * Performs bulk updates with BaseX standalone version.
 *
 * @author BaseX Team 2005-11, BSD License
 */
public final class XQUFStressTest {
  /** Verbose flag. */
  private static final boolean VERBOSE = false;

  /** Current context. */
  Context ctx = new Context();
  /** Query string. */
  final String query = "delete nodes //n";
  /** Number of runs. */
  private static final int RUNS = 100;
  /** Number of node updates. */
  private static final int NRNODES = 100;
  /** Basic database name for each test. */
  private static final String DBNAME = "TESTXQUF";

  /**
   * Performs a delete test.
   */
  private void delete() {
    final String op = "DELETE";
    p(op);
    final String db = DBNAME + op;

    try {
      new CreateDB(db, "<doc/>").execute(ctx);
      new XQuery("for $i in 1 to " + NRNODES
          + " return insert node <node/> into /doc").
          execute(ctx);
      p("DB created.");
      p("querying ...");
      new XQuery("delete nodes //node").execute(ctx);
      p("finished.");
      new DropDB(db).execute(ctx);
    } catch(final BaseXException ex) {
      ex.printStackTrace();
    }
    p("delete finished.\n");
  }

  /**
   * Tests the insertAfter statement.
   */
  private void insertBefore() {
    final String op = "INSERTBEFORE";
    p(op);
    final String sec = "<section><page/></section>";
    final String doc = "<doc/>";
    final String db = DBNAME + op;

    try {
      // fill DB
      new CreateDB(db, doc).
      execute(ctx);
      new XQuery("for $i in 1 to " + NRNODES + " return insert node " + sec +
          "into /doc").execute(ctx);
      p("DB created.");

      // actual query
      p("querying ...");
      new XQuery("for $page in //page " +
          "let $par := $page/parent::node()" +
        "return (delete node $page, insert node $page before $par)").
          execute(ctx);
      p("finished.");
      new DropDB(db).execute(ctx);
    } catch(final BaseXException ex) {
      ex.printStackTrace();
    }
    p("insert finished.\n");
  }

  /**
   * Helper.
   * @param s String to be printed
   */
  private static void p(final String s) {
    if(VERBOSE) Util.outln(s);
  }

  /**
   * Main.
   * @param args args
   */
  public static void main(final String[] args) {
    for(int i = 0; i < RUNS; i++)
      new XQUFStressTest().insertBefore();
    for(int i = 0; i < RUNS; i++)
      new XQUFStressTest().delete();
  }
}
