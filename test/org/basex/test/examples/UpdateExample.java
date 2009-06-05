package org.basex.test.examples;

import org.basex.core.*;
import org.basex.core.proc.*;
import org.basex.io.*;

/**
 * This class demonstrates how updates can be performed on an existing
 * database instance.
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

    // Creates a standard output stream
    ConsoleOutput out = new ConsoleOutput(System.out);

    // Creates a new context
    Context context = new Context();


    out.println("\n=== Create a new database");

    // Sets an option: activates command info output
    new Set("Info", "on").execute(context, out);

    // Creates a new database instance; argument can be a file name or XML
    new CreateDB("<doc a='b'>first</doc>", "Database").execute(context, out);


    out.println("\n=== Insert a document");

    // Inserts a document into the database; argument can be a file name or XML
    // Position: 0 (ignored for documents)
    // Target: insert on root level
    new Insert("fragment", "<doc>second</doc>", "0", "/").execute(context, out);


    out.println("\n=== Delete nodes");

    // Deletes all attributes in the database.
    new Delete("//@*").execute(context, out);


    out.println("\n=== Insert a node");

    // Inserts an element fragment into the database
    // Position: 1 = as first child
    // Target: insert after all /doc elements...
    new Insert("fragment", "<sub/>", "1", "/doc").execute(context, out);


    out.println("\n=== Optimize the database");

    // Updates all indexes and database statistics
    new Optimize().execute(context, out);


    out.println("\n=== Output the result");

    // Serializes the database to show the result
    new XQuery("/").execute(context, out);

    // Closes the database
    context.close();

    // Closes the output stream
    out.close();
  }
}
