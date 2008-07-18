package org.basex.test.examples;

import org.basex.core.*;
import org.basex.core.proc.*;
import org.basex.io.*;

/**
 * This class serves as an example for creating and processing database
 * instances.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
public final class DBExample {
  /** Input XML file. */
  static final String INPUT = "input.xml";
  /** Name of the resulting database. */
  static final String DBNAME = "input";
  /** Sample query. */
  static final String QUERY = "//li";
  /** Result file. */
  static final String RESULT = "result.txt";

  /** Private constructor. */
  private DBExample() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // Creates a new database context
    Context context = new Context();

    // Optionally sets a new database path
    //Proc.execute(context, Commands.SET, Set.DBPATH + " ...............");

    // Creates a database for the specified path/file.
    Proc.execute(context, Commands.CREATEDB, INPUT);

    // Creates a process for the XQuery command 
    Proc proc = Proc.get(context, Commands.XQUERY, QUERY);

    // Uses console output
    ConsoleOutput out = new ConsoleOutput(System.out);
    // Uses file output
    //PrintOutput out = new PrintOutput(RESULT);

    // Launches the process
    if(proc.execute()) {
      // Successful execution: print result
      proc.output(out);
    } else {
      // Execution failed: print information
      proc.info(out);
    }

    out.close();
  }
}
