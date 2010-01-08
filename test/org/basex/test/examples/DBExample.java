package org.basex.test.examples;

import java.io.IOException;
import java.io.OutputStream;
import org.basex.core.*;
import org.basex.core.Commands.CmdIndex;
import org.basex.core.proc.*;
import org.basex.data.XMLSerializer;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

/**
 * This class demonstrates how new databases are created, deleted and queried.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
@SuppressWarnings("unused")
public final class DBExample {
  /** The current database Context. */
  static final Context CONTEXT = new Context();

  /** Private constructor. */
  private DBExample() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception E
   */
  public static void main(final String[] args) throws Exception {
    // Creates a new database context, referencing the database.
    System.out.println("\n=== Create a database from a file.");

    // Creates a database from the specified file.
    new CreateDB("input.xml", "Example1").execute(CONTEXT, System.out);

    // Collection Examples
   // CollectionExamples.run();

    // Do some database stuff
    MaintenanceExamples.run();

    // Runs some example Queries
    QueryExample.main(args);
  }

  /**
   * This class shows you how to work with XQuery Update.
   */
  static final class UpdateExamples {}

  /**
   * Examples on Database maintenance.
   * @author BaseXTeam Topics covered: <br />
   *         Index creation & maintenance:
   *         {@link MaintenanceExamples#createIndexes} <br />
   * <br />
   *         Index creation & maintenance:
   *         {@link MaintenanceExamples#createIndexes}
   */
  static final class MaintenanceExamples {
    /** Private constructor. */
    private MaintenanceExamples() { }

    /**
     * PrintOutput Context. Point the PrintOutput to whatever file you like to
     * store the serializing results in a file. You may as well point it to
     * System.out.
     */
    static OutputStream out = System.out;

    /**
     * Runs the examples.
     * @throws Exception on error.
     */
    static void run() throws Exception {
      createDatabase();
      createIndexes();
      optimizeDB();
      // close the PrintOutput.
      out.close();
    }

    /**
     * This method creates a new Database. It then closes the databases and
     * reopens it.
     * @throws BaseXException in case the database could not be opened.
     */
    private static void createDatabase() throws BaseXException {
      // // You may also create Databases from a collection of files.
      System.out.println("=== Create Database.");

      // Creates a database
      new CreateDB("input.xml", "Example1").execute(CONTEXT, out);
      // Closes the database.
      System.out.println("=== Close Database.");

      new Close().execute(CONTEXT, out);
      System.out.println("=== Reopen Database.");

      new Open("Example1").execute(CONTEXT, out);
    }

    /**
     * Method that shows how to invoke the optimization of the index structure.
     * @throws BaseXException In case of failing.
     */
    private static void optimizeDB() throws BaseXException {
      new Optimize().execute(CONTEXT, out);

    }

    /**
     * This method shows operations to create and drop indexes.
     * @throws BaseXException on error.
     */
    private static void createIndexes() throws BaseXException {
      // I Index Creation & Maintenance
      new CreateIndex("fulltext").execute(CONTEXT, out);

    }

    /**
     * Information on the currently open database.
     * @throws BaseXException whenever it fails.
     */
    private static void info() throws BaseXException {
      System.out.println("\n=== III information on the specified "
          + "database context.");
      // Dumps information on the specified database context
      new InfoDB().execute(CONTEXT, System.out);

    }

    /**
     * This method shows how to create, drop and modify databases in BaseX.
     * @throws BaseXException when it fails.
     */
    private static void creationAndDropping() throws BaseXException {
      // Closes the database.
      new Close().execute(CONTEXT);
      // new DropDB("Example1").execute(context);

      // System.out.println("\n=== Create a database from an input string.");
      //
      // // XML string.
      // String xml = "<xml>This is a test</xml>";
      // // Creates a database for the specified input.
      // new CreateDB(xml, "Example2").execute(context);
      // // Closes the database.
      // new Close().execute(context);

      // System.out.println("\n=== Open a database and show database info:");
      //
      // // Opens an existing database
      // new Open("Example1").execute(context);
      // // Dumps information on the specified database context
      // new InfoDB().execute(context, out);
      // // Closes the database.
      // new Close().execute(context);

    }
  }
}
