package org.basex.examples.local;

import org.basex.core.*;
import org.basex.core.cmd.*;

/**
 * This class demonstrates database creation and dropping.
 * It then shows how to add indexes to the database and retrieve
 * some information on the database structures.
 *
 * @author BaseX Team 2005-15, BSD License
 */
public final class RunCommands {
  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws BaseXException if a database command fails
   */
  public static void main(final String[] args) throws BaseXException {
    // Database context.
    Context context = new Context();

    System.out.println("=== RunCommands ===");

    // Create a database from a local or remote XML document or XML String
    System.out.println("\n* Create a database.");

    new CreateDB("DBExample", "src/main/resources/xml/input.xml").execute(context);

    // Close and reopen the database
    System.out.println("\n* Close and reopen database.");

    new Close().execute(context);
    new Open("DBExample").execute(context);

    // Additionally create a full-text index
    System.out.println("\n* Create a full-text index.");

    new CreateIndex("fulltext").execute(context);

    // Show information on the currently opened database
    System.out.println("\n* Show database information:");

    System.out.print(new InfoDB().execute(context));

    // Drop indexes to save disk space
    System.out.println("\n* Drop indexes.");

    new DropIndex("text").execute(context);
    new DropIndex("attribute").execute(context);
    new DropIndex("fulltext").execute(context);

    // Drop the database
    System.out.println("\n* Drop the database.");

    new DropDB("DBExample").execute(context);

    // Show all existing databases
    System.out.println("\n* Show existing databases:");

    System.out.print(new List().execute(context));

    // Close the database context
    context.close();
  }
}
