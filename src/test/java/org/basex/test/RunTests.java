package org.basex.test;

import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.proc.DropDB;
import org.basex.test.query.QueryTest;
import org.basex.test.xqj.AllTests;

/**
 * Runs all Non-JUnit tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class RunTests {
  /** Private constructor. */
  private RunTests() { }

  /**
   * Main method.
   * @param args (ignored) command-line arguments
   * @throws Exception exceptions
   */
  public static void main(final String[] args) throws Exception {
    // create input.xml database for XQJ/XMLDB examples
    final Context ctx = new Context();

    Main.outln("============= XQJ Tests =============");
    AllTests.main(args);

    Main.outln("============= XMLDB Tests =============");
    org.basex.test.xmldb.AllTests.main(args);

    /*Main.outln("============= XQJ Examples =============");
    XQJQuery.main(args);
    Main.outln();

    Main.outln("============= XMLDB Examples =============");
    XMLDBQuery.main(args);
    XMLDBCreate.main(args);
    XMLDBInsert.main(args);
    XMLDBQuery.main(args);
    Main.outln();
    */

    Main.outln("=============  XQuery Tests =============");
    QueryTest.main(args);
    Main.outln();

    Main.outln("=============>  Done.");
    Main.outln();

    Main.outln("Additionally run the remaining JUnit tests.");
    new DropDB("input").exec(ctx);
    ctx.close();
  }
}
