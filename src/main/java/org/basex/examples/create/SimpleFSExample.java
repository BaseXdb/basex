package org.basex.examples.create;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Create;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.XQuery;

/**
 * This example demonstrates how to import a filesystem structure
 * into the database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class SimpleFSExample {
  /**
   * Main test method.
   * @param args command-line arguments
   * @throws BaseXException if a database command fails
   */
  public static void main(final String[] args) throws BaseXException {

    System.out.println("=== SimpleFSExample ===\n");

    // ------------------------------------------------------------------------
    // Create database context
    final Context context = new Context();

    // Input path and name of database
    final String path = ".";
    final String name = "fsexample";

    // ------------------------------------------------------------------------
    // Import a directory as database
    System.out.println("* Import: \"" + path + "\".");

    new Create(new SimpleFSParser(path), name).execute(context);

    // ------------------------------------------------------------------------
    // Perform query
    System.out.print("* Number of files: ");

    System.out.println(new XQuery("count(//file)").execute(context));

    // ------------------------------------------------------------------------
    // Drop database and close context
    System.out.println("* Drop database.");

    new DropDB(name).execute(context);

    context.close();
  }
}
