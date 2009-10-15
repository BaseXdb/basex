package org.basex.test.examples;

import org.basex.core.*;
import org.basex.core.proc.*;
import org.basex.io.*;

/**
 * This class demonstrates how updates can be performed on a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author BaseX Team
 */
public final class UpdateExample {
  /** Private constructor. */
  private UpdateExample() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // Creates a new context
    Context context = new Context();
    // Creates a standard output stream
    PrintOutput out = new PrintOutput(System.out);

    out.println("\n=== Create a database:");

    // Sets an option: activates command info output
    new Set("info", true).exec(context, out);
    // Creates a new database instance; argument can be a file name or XML
    new CreateDB("<doc a='b'>first</doc>", "Example").exec(context, out);

    out.println("\n=== Insert a document:");

    // Inserts a document into the database; argument can be a file name or XML
    // Target: insert on root level
    new Insert("fragment", "/", "<doc>second</doc>").exec(context, out);

    out.println("\n=== Delete nodes");

    // Deletes all attributes in the database.
    new Delete("//@*").exec(context, out);

    out.println("\n=== Insert a node:");

    // Inserts an element fragment into the database
    // Position: 1 = as first child
    // Target: insert after all /doc elements...
    new Insert("fragment", "/doc", 1, "<sub/>").exec(context, out);

    out.println("\n=== Optimize the database:");

    // Updates all indexes and database statistics
    new Optimize().exec(context, out);

    out.println("\n=== Output the result:");

    // Serializes the database to show the result
    new XQuery("/").exec(context, out);

    out.println("\n=== Drop the database:");

    // Removes the second database
    new DropDB("Example").exec(context, out);

    // Closes the output stream
    out.close();
    // Closes the database context
    context.close();
  }
}
