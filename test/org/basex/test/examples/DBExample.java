package org.basex.test.examples;

import java.io.OutputStream;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.CreateIndex;
import org.basex.core.proc.InfoDB;
import org.basex.core.proc.Open;
import org.basex.core.proc.Optimize;

/**
 * This class demonstrates how new databases are created and dropped.
 * It then shows how to add indices and get some basic database information.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */

public final class DBExample {
  /** The current database Context. */
  Context ctx = new Context();
  /**
   * PrintOutput Context. Point the PrintOutput to whatever file you like to
   * store the serializing results in a file. You may as well point it to
   * System.out.
   */
  OutputStream out;

  /**
   * Constructor, initializing the database context and the output stream.
   */
  DBExample() {
    ctx = new Context();
    out =  System.out;
  }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   */
  public static void main(final String[] args) {
    new DBExample().run();

  }
  /**
   * Runs the examples.
   */
  private void run() {
    // ----------------------------------------------------------------------
    // Creates a new database in the current context.
    System.out.println("\n=== Create a database from a file.");
    try {
      createAndReopenDatabase("input.xml", "ExampleDB");
    } catch(BaseXException e) {
      System.err.println(e.getMessage());
    }

    // ----------------------------------------------------------------------
    // Creates some indices 
    try {
      modifyIndexes();
    } catch(BaseXException e) {
      System.err.println(e.getMessage());
    }
      
    // ----------------------------------------------------------------------
    // Optimizes the internal data structure and shows some information on the
    // database afterwards.
    System.out.println("\n=== Running optimize on the database...");
    System.out.println("\n===  ...information on the specified "
        + "database context.");
    try {
      optimizeDB();
      info();
    }catch(BaseXException e) {
      System.err.println(e.getMessage());
    }
    
    
    
  }

  /**
   * Creates a database.
   * @param input source file or XML String for database creation.
   * @param dbname name for the database.
   * @throws BaseXException on error.
   */
  void createAndReopenDatabase(final String input, final String dbname)
    throws BaseXException {

    new CreateDB(input, dbname).execute(ctx, out);
    new Close().execute(ctx, out);
    new Open(dbname).execute(ctx, out);
  }

  /**
   * Method that shows how to invoke the optimization of the index structure.
   * @throws BaseXException In case of failing.
   */
  private  void optimizeDB() throws BaseXException {
    new Optimize().execute(ctx, out);

  }

  /**
   * This method shows operations to create and drop indexes.
   * You generally do not want to drop an index from the database.
   * Remember to Optimize the data strucuture afterwards via
   *  {@link DBExample#optimizeDB()}
   * @throws BaseXException on error.
   */
  private  void modifyIndexes() throws BaseXException {
    // Index Creation & Maintenance
    new CreateIndex("fulltext").execute(ctx, out);
    // To drop an Index use: 
    // new DropIndex("fulltext").execute(ctx, out);
  }

  /**
   * Information on the currently open database.
   * @throws BaseXException whenever it fails.
   */
  private  void info() throws BaseXException {
    // Dumps information on the specified database context
    new InfoDB().execute(ctx, out);

  }
}
