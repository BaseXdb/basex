package org.basex.examples.create;

import org.basex.core.*;
import org.basex.core.cmd.*;

/**
 * This example demonstrates how to import a file in the CSV format
 * into the database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CSVExample {
  /**
   * Main test method.
   * @param args command-line arguments
   * @throws BaseXException if a database command fails
   */
  public static void main(final String[] args) throws BaseXException {

    System.out.println("=== CSVExample ===");

    // ------------------------------------------------------------------------
    // Create database context
    final Context ctx = new Context();

    // Input file and name of database
    final String file = "src/main/resources/csv/example.csv";
    final String name = "csvexample";

    // ------------------------------------------------------------------------
    // Import the specified file
    System.out.println("\n* Import: \"" + file + "\".");

    new Set("parser", "csv").execute(ctx);
    new CreateDB(name, file).execute(ctx);

    // ------------------------------------------------------------------------
    // Perform query
    System.out.print("\n* Number of records: ");

    System.out.println(new XQuery("count(//record)").execute(ctx));

    // ------------------------------------------------------------------------
    // Drop database and close context
    System.out.println("\n* Drop database.");

    new DropDB(name).execute(ctx);
    ctx.close();
  }
}
