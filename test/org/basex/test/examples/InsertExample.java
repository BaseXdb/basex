package org.basex.test.examples;

import org.basex.core.*;
import org.basex.core.Commands.CmdUpdate;
import org.basex.core.proc.*;
import org.basex.io.*;

/**
 * This class serves as an example for inserting XML documents into
 * existing databases.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author BaseX Team
 */
public final class InsertExample {
  /** Private constructor. */
  private InsertExample() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // Creates a standard output stream
    ConsoleOutput out = new ConsoleOutput(System.out);

    // Creates a new database context
    Context context = new Context();
    // Creates the specified database with the specified name
    new CreateDB("input.xml", "input").execute(context, out);

    // Inserts another document into the currently opened database
    // Type of insertion: fragment
    // Document to insert: input.xml
    // Position to insert: 0 = end of specified XQuery target
    // Target nodes: insert after /html ...
    new Insert(CmdUpdate.FRAGMENT, "input.xml", "0", "/html").
      execute(context, out);

    // Serializes the database to show the result
    new XQuery("/").execute(context, out);

    // Closes the output stream
    out.close();
  }
}
