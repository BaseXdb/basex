package org.basex.test.examples;

import org.basex.core.*;
import org.basex.core.proc.*;

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
   */
  public static void main(final String[] args) {
    // Creates a new database context, referencing the database.
    Context context = new Context();

    System.out.println("\n=== Create a database from a file.");

    // Creates a database from the specified file.
    new CreateDB("input.xml", "Example1").exec(context, System.out);
    // Closes the database.
    new Close().exec(context, System.out);

    System.out.println("\n=== Create a database from an input string.");

    // XML string.
    String xml = "<xml>This is a test</xml>";
    // Creates a database for the specified input.
    new CreateDB(xml, "Example2").exec(context, System.out);
    // Closes the database.
    new Close().exec(context, System.out);

    System.out.println("\n=== Open a database and show database info:");

    // Opens an existing database
    new Open("Example1").exec(context, System.out);
    // Dumps information on the specified database context
    new InfoDB().exec(context, System.out);
    // Closes the database.
    new Close().exec(context, System.out);

    System.out.println("=== Drop databases.");

    // Removes the first database
    new DropDB("Example1").exec(context, System.out);
    // Removes the second database
    new DropDB("Example2").exec(context, System.out);

    // Closes the database context
    context.close();
  }
}
