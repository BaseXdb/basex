package org.basex.test.examples;

import org.basex.core.*;
import org.basex.core.proc.*;
import org.basex.io.*;

/**
 * This class shows how databases can be created.
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

    // Creates a standard output stream
    PrintOutput out = new PrintOutput(System.out);

    // Creates a new database context, referencing the database.
    Context context = new Context();

    out.println("=== First example: Creating a database from a file");

    // Sets an option: activates command info output
    new Set("Info", "on").execute(context, out);

    // Chops whitespaces between text nodes
    new Set("Chop", "on").execute(context, out);

    // Creates a database from the specified file.
    new CreateDB("input.xml", "DB1").execute(context, out);


    out.println("=== Second example: Creating a database from an input string");

    // XML string.
    String xml = "<xml>This is a test</xml>";

    // Creates a database for the specified input.
    new CreateDB(xml, "DB2").execute(context, out);


    out.println("=== Third example: Opens an existing database");

    // Opens an existing database
    new Open("DB1").execute(context, out);

    // Dumps information on the specified database context
    new InfoDB().execute(context, out);

    // Closes the database
    context.close();

    // Closes the output stream
    out.close();
  }
}

