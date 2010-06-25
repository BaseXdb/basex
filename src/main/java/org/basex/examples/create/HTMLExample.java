package org.basex.examples.create;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;

/**
 * This example demonstrates how to import a file in the HTML format
 * into the database. The specified input file will be converted to XML
 * if TagSoup is found in the classpath.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class HTMLExample {
  /** Private constructor. */
  private HTMLExample() { }

  /**
   * Main test method.
   * @param args command-line arguments
   * @throws BaseXException if a database command fails
   */
  public static void main(final String[] args) throws BaseXException {

    System.out.println("=== HTMLExample ===");

    // ------------------------------------------------------------------------
    // Create database context
    final Context ctx = new Context();

    // input file and name of database
    final String file = "http://www.google.com/index.html";
    final String name = "htmlexample";

    // ------------------------------------------------------------------------
    // Import the specified file
    System.out.println("\n* Import '" + file + "'.");

    new Set(Prop.PARSER, "html").execute(ctx);
    new CreateDB(name, file).execute(ctx);

    // ------------------------------------------------------------------------
    // Perform query
    System.out.println("\n* <a/> elements:");

    new XQuery("//*:a").execute(ctx, System.out);

    // ------------------------------------------------------------------------
    // Drop database and close context
    System.out.println("\n\n* Drop database.");

    new DropDB(name).execute(ctx);
    ctx.close();
  }
}
