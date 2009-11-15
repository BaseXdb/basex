package org.basex.test;

import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.test.examples.DBExample;
import org.basex.test.examples.UpdateExample;
import org.basex.test.examples.XMLDBCreate;
import org.basex.test.examples.XMLDBInsert;
import org.basex.test.examples.XMLDBQuery;
import org.basex.test.examples.XQJQuery;
import org.basex.test.examples.XQueryExample;
import org.basex.test.query.QueryTest;
import org.basex.test.xqj.AllTests;

/**
 * Runs all Non-JUnit tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
    new CreateDB("input.xml").execute(ctx);
    new Close().execute(ctx);

    Main.outln("============= XQJ Tests =============");
    XQJQuery.main(args);
    AllTests.main(args);
    Main.outln();

    Main.outln("============= XMLDB Tests =============");
    org.basex.test.xmldb.AllTests.main(args);
    XMLDBQuery.main(args);
    XMLDBCreate.main(args);
    XMLDBInsert.main(args);
    XMLDBQuery.main(args);
    new DropDB(XMLDBCreate.COLL).execute(ctx);
    Main.outln();

    Main.outln("=============  BaseX Examples =============");
    DBExample.main(args);
    UpdateExample.main(args);
    XQueryExample.main(args);
    Main.outln();

    Main.outln("=============  XQuery Tests =============");
    QueryTest.main(args);
    Main.outln();
    Main.outln("=============>  Done.");
    Main.outln();

    Main.outln("Additionally run the remaining JUnit tests.");
    ctx.close();
  }
}
