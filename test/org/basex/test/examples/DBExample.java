package org.basex.test.examples;

import org.basex.core.*;
import org.basex.core.Commands.CmdIndex;
import org.basex.core.proc.*;

/**
 * This class demonstrates how new are created and deleted.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author BaseX Team
 */
@SuppressWarnings("unused")
public final class DBExample {
  /** The current database Context. */
  private static Context context;

  /** Private constructor. */
  private DBExample() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws BaseXException exception
   */
  public static void main(final String[] args) throws BaseXException {
    // Creates a new database context, referencing the database.
    context = new Context();

    System.out.println("\n=== Create a database from a file.");

    // Creates a database from the specified file.
   new CreateDB("input.xml", "Example1").exec(context, System.out);
    // Do some database stuff
   
   indexExample();

   queryExample();
   System.out.println("\n=== III information on the specified " +
       "database context.");
   // Dumps information on the specified database context
   new InfoDB().exec(context, System.out);
 
    // Closes the database.
    new Close().exec(context, System.out);
//    new DropDB("Example1").exec(context, System.out);

//    System.out.println("\n=== Create a database from an input string.");
//
//    // XML string.
//    String xml = "<xml>This is a test</xml>";
//    // Creates a database for the specified input.
//    new CreateDB(xml, "Example2").exec(context, System.out);
//    // Closes the database.
//    new Close().exec(context, System.out);

//    System.out.println("\n=== Open a database and show database info:");
//
//    // Opens an existing database
//    new Open("Example1").exec(context, System.out);
//    // Dumps information on the specified database context
//    new InfoDB().exec(context, System.out);
//    // Closes the database.
//    new Close().exec(context, System.out);
    
    
//    // You may also create Databases from a collection of files.
//
//    System.out.println("=== Drop databases.");
//
//    // Removes the first database
//    new DropDB("Example1").exec(context, System.out);
//    // Removes the second database
//    new DropDB("Example2").exec(context, System.out);
//
//    // Closes the database context
//    context.close();
  }

  /**
   * Some Examples in XQuery Evaluation.
   * @throws BaseXException 
   * For further information on BaseX Client-Side abilities on 
   * XQuery Processing please see:
   * @see XQueryExample#main(String[])
   */
  private static void queryExample() throws BaseXException {
    // II Evaluate XQueries for the given context
    System.out.println("\n=== II Evaluating queries.");

      new XQuery("for $x in .//body//li return $x").exec(context, System.out);
    
  }

  /**
   * Some Examples on index creation.
   * @throws BaseXException on error.
   */
  private static void indexExample() throws BaseXException {
    // I Index Creation & Maintenance
    new CreateIndex("fulltext").exec(context, System.out);
    
    // Optimize the index structure 
    new Optimize().exec(context, System.out);
  }
}
