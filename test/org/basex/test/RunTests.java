package org.basex.test;

import org.basex.core.Context;
import org.basex.core.proc.CreateDB;
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
  /**
   * Main method
   * @param args (ignored) command-line arguments
   * @throws Exception exceptions
   */
  public static void main(final String[] args) throws Exception {
    // create input.xml database for XMLDB examples
    final Context ctx = new Context();
    new CreateDB("input.xml").execute(ctx);
    ctx.close();

    System.out.println("============= XQJ Tests =============");
    XQJQuery.main(args);
    AllTests.main(args);
    System.out.println();

    System.out.println("============= XMLDB Tests =============");
    org.basex.test.xmldb.AllTests.main(args);
    XMLDBQuery.main(args);
    XMLDBCreate.main(args);
    XMLDBInsert.main(args);
    XMLDBQuery.main(args);
    System.out.println();

    System.out.println("=============  BaseX Examples =============");
    DBExample.main(args);
    UpdateExample.main(args);
    XQueryExample.main(args);
    System.out.println();

    System.out.println("=============  XQuery Tests =============");
    QueryTest.main(args);
    System.out.println();
    System.out.println("=============>  Done.");
    System.out.println();

    System.out.println("Additionally run the remaining JUnit tests.");
  }
}
