package org.basex.test.examples;

import org.basex.core.*;
import org.basex.core.proc.*;
import org.basex.io.*;

/**
 * This class demonstrates how new are created and deleted.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author BaseX Team
 */
public final class DBExample {
  /** Private constructor. */
  private DBExample() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // Creates a new database context, referencing the database.
    Context context = new Context();
    // Creates a standard output stream
    PrintOutput out = new PrintOutput(System.out);

    out.println("\n=== Create a database from a file:");

    // Sets an option: activates command info output
    new Set("Info", "on").execute(context, out);
    // Sets an option: chops whitespaces between text nodes (saves disk space)
    new Set("Chop", "on").execute(context, out);
    // Creates a database from the specified file.
    new CreateDB("input.xml", "Example1").execute(context, out);

    out.println("\n=== Create a database from an input string:");

    // XML string.
    String xml = "<xml>This is a test</xml>";
    // Creates a database for the specified input.
    new CreateDB(xml, "Example2").execute(context, out);

    out.println("\n=== Open an existing database and show database info:");

    // Opens an existing database
    new Open("Example1").execute(context, out);
    // Dumps information on the specified database context
    new InfoDB().execute(context, out);

    out.println("=== Drop databases:");

    // Removes the first database
    new DropDB("Example1").execute(context, out);
    // Removes the second database
    new DropDB("Example2").execute(context, out);

    // Closes the output stream
    out.close();
    // Closes the database context
    context.close();
  }
}

