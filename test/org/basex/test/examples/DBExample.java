package org.basex.test.examples;

import java.io.IOException;

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
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // Creates a new database context, referencing the database.


    System.out.println("\n=== Create a database from a file.");

    // Creates a database from the specified file.
   new CreateDB("input.xml", "Example1").exec(CONTEXT, System.out);
    // Do some database stuff
   
   MaintenanceExamples.run();
   
   // Runs some example Queries
   QueryExample.run();
    
    
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
   * This class contains several variants of XQuery Processing in BaseX.
   * For further information on BaseX Client-Side abilities in 
   * XQuery Processing please see:
   * @see XQueryExample#main(String[])
   */
   static final class QueryExample {
     /** XMLSerializer that generates valid XML Output. */
    static XMLSerializer xmlSer;

    /**
     * PrintOutput Context. Point the PrintOutput to whatever file
     * you like to store the serializing results in a file.
     * You may as well point it to System.out.
     */
    static PrintOutput out = new PrintOutput(System.out);

    /**
     * Runs the Example Queries and sets up the XMLSerializer.
     * @throws Exception *TODO*
     */
    public static void run() throws Exception {
      // point the serializer to System.out
      xmlSer = new XMLSerializer(out);
      // II Evaluate XQueries for the given context
      System.out.println("\n=== II Evaluating queries.");
      // Evaluate XQuery and output the result to System.out
      System.out.println("===== XQuery Proc: direct output.");
      directOutputExample();
      System.out.println("\n===== XQuery Node Iteration and XML Serializing.");

      iterateExample();
      xmlSer.close();
      out.close();

    }

    /**
     * This method exexcutes an XQuery Process for the given database context.
     * The results are automatically serialized and 
     * printed to an arbitrary OutputStream.
     * @throws BaseXException in case your *TODO*
     */
    private static void directOutputExample() throws BaseXException {
      new XQuery("for $x in .//body//li return $x").exec(CONTEXT, System.out);
      
    }

    /**
     * Shows how Results can be iterated and serialized one after another
     * using the {@link QueryProcessor} class.
     * This is especially useful if you happen to have very big results,
     * as you will not have to process all resulting nodes at once.
     * Please note the use of {@link XMLSerializer} to generate 
     * valid XML output.
     * 
     * @throws BaseXException in case something went wrong.
     * @throws QueryException in case your query was wrong. 
     */
    private static void iterateExample() 
      throws BaseXException, QueryException {
      QueryProcessor qp = new QueryProcessor("for $x in .//body//li return $x",
          CONTEXT);
      // Returns a query iterator
      Iter iter = qp.iter();
      Item item;
      try {
        while(null != (item = iter.next())) {
          // 
          item.serialize(xmlSer);
        }
      } catch(IOException e) {
        e.printStackTrace();
        return;
      }// Closes the serializer
      // Closes the query processor
    }
    
    /**
     * Use XQuery update to ....
     * *TODO* [MSe]
     */
    private static void updateExample(){
      // *TODO*
    }
    /** Private constructor to avoid class creation.*/
    private QueryExample() { }

  }

  /**
   * Examples on Database maintenance.
   * @author michael
   * Topics covered: <br />
   * Index creation & maintenance: 
   * {@link MaintenanceExamples#createIndices} 
   * <br /> <br />
   * Index creation & maintenance: 
   * {@link MaintenanceExamples#createIndices} 
   */
  static final class MaintenanceExamples {
    /**
     * Runs the exmaples.
     * @throws BaseXException on error.
     */
    static void run() throws BaseXException {
      createIndices();
      optimizeDB();
    }
    /**
     * Method that shows how to invoke the optimization of the index structure. 
     * @throws BaseXException *TODO*
     */
    private static void optimizeDB() throws BaseXException {
      new Optimize().exec(CONTEXT, System.out);
      
    }
    /**
     * This method shows operations to create and drop indices.
     * @throws BaseXException on error.
     */
    private static void createIndices() throws BaseXException{
      // I Index Creation & Maintenance
      new CreateIndex("fulltext").exec(CONTEXT, System.out);

    }
    
    /**
     * Information on the currently open database.
     * @throws BaseXException *TODO*
     */
    private static void info() throws BaseXException{
      System.out.println("\n=== III information on the specified "
          + "database context.");
      // Dumps information on the specified database context
      new InfoDB().exec(CONTEXT, System.out);

    }
    /**
     * This method shows how to create, drop and modify databases in BaseX.
     * @throws BaseXException ex *TODO*
     */
    private static void creationAndDropping() throws BaseXException {

      
      // Closes the database.
      new Close().exec(CONTEXT, System.out);
      // new DropDB("Example1").exec(context, System.out);

      // System.out.println("\n=== Create a database from an input string.");
      //
      // // XML string.
      // String xml = "<xml>This is a test</xml>";
      // // Creates a database for the specified input.
      // new CreateDB(xml, "Example2").exec(context, System.out);
      // // Closes the database.
      // new Close().exec(context, System.out);

      // System.out.println("\n=== Open a database and show database info:");
      //
      // // Opens an existing database
      // new Open("Example1").exec(context, System.out);
      // // Dumps information on the specified database context
      // new InfoDB().exec(context, System.out);
      // // Closes the database.
      // new Close().exec(context, System.out);

    }

    /** Private constructor. */
    private MaintenanceExamples() { }
  }
}
