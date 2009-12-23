package org.basex.test.examples;

import java.io.IOException;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.XQuery;
import org.basex.data.Result;
import org.basex.data.XMLSerializer;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

/**
 * Some Examples in XQuery Evaluation. This class contains several variants of
 * XQuery Processing in BaseX. For further information on BaseX Client-Side
 * abilities in XQuery Processing please see:
 * @see XQueryExample#main(String[])
 */
public final class QueryExample {
  /** The current database Context. */
  static final Context CONTEXT = new Context();

  /** The query to evaluate. */
  private static final String QUERY = "for $x in //body//li return $x";
  
  /** insert a node into the last li Element. */
  private static final String UPDATE = "insert node <b>I am new</b>" +
    " into /html/body//li[position()=last()]";
  
  /**
   * PrintOutput Context. Point the PrintOutput to whatever file you like to
   * store the serializing results in a file. You may as well point it to
   * System.out.
   */
  private PrintOutput out;

  /** Setup and point the serializer to System.out. */
  private XMLSerializer xmlSer;

  /**
   * Starts the QueryExample from the Commandline.
   * @param args not used.
   */
  public static void main(final String[] args) {
    QueryExample queryExample = new QueryExample();
    try {
      queryExample.run();
    } catch(IOException e) {
      e.printStackTrace();
    } catch(BaseXException e) {
      System.err.println(e.getMessage());
    }
  }
  
  /**
   * Sets up the example XMLSerializer instance.
   */
  private QueryExample() {
    try {
      out = new PrintOutput(System.out);
      xmlSer = new XMLSerializer(out);
    } catch(IOException e) {
      System.err.println("Could not initiate the XMLSerializer.");
    }
  }
  /**
   * Runs the Example Queries.
   * @throws IOException for XMLSerializer and PrintOutput errors.
   * @throws BaseXException if database creation fails for any reason.
   */
  private void run() throws IOException, BaseXException {
    // Creates a new database context, referencing the database.
    System.out.println("\n=== Create a database from a file.");
    // Creates a database from the specified file.
    new CreateDB("input.xml", "Example1").exec(CONTEXT, System.out);

    // -------------------------------------------------------------------------
    // Evaluate XQuery directly to System.out
    try {
      directOutputExample();
      // uncomment the following line to see error handling.
      // errorExample();
    } catch(BaseXException e) {
      System.err.println(e.getMessage());
    }

    // -------------------------------------------------------------------------
    // Process the result with an iterator:
    try {
      iterateExample();
    } catch(Exception e) {
      System.err.println(e.getMessage());
    }

    // -------------------------------------------------------------------------
    // Processing the whole result instance at once:
    try {
      resultInstance();
    } catch(QueryException e) {
      System.err.println(e.getMessage());
    }
    try {
      updateExample();
    }catch(BaseXException e) {
      System.err.println(e.getMessage());
    }
    // -------------------------------------------------------------------------
    // Close the serializer and the PrintOutput stream
    xmlSer.close();
    out.close();

    // -------------------------------------------------------------------------
    // Close and drop the Database.
    new Close().execute(CONTEXT);
    new DropDB("Example1");

  }

  /**
   * This method exexcutes an XQuery Process for the given database context. The
   * results are automatically serialized and printed to an arbitrary
   * OutputStream.
   * @throws BaseXException in case your query contains errors.
   */
  private void directOutputExample() throws BaseXException {
    System.out.println("\n=== II Evaluating queries.");
    System.out.println("===== XQuery Proc: direct output.");
    new XQuery(QUERY).exec(CONTEXT, System.out);

  }

  /**
   * This method throws an BaseXException.
   * @throws BaseXException in case your *TODO*
   */
  @SuppressWarnings("unused")
  private void errorExample() throws BaseXException {
    System.out.println("\n===== The following query contains an error:");
    new XQuery("for error s$x in . return $x").exec(CONTEXT, System.out);

  }

  /**
   * Shows how Results can be iterated and serialized one after another using
   * the {@link QueryProcessor} class. This is especially useful if you happen
   * to have very big results, as you will not have to process all resulting
   * nodes at once. Please note the use of {@link XMLSerializer} to generate
   * valid XML output.
   * 
   * @throws QueryException in case your query was wrong.
   * @throws IOException for {@link XMLSerializer}.
   */
  private void iterateExample() throws QueryException, IOException {
    System.out.println("\n\n===== XQuery Node Iteration and XML Serializing.");
    System.out.println("==== Iterator result:");

    // -------------------------------------------------------------------------
    // Create a QueryProcessor
    final QueryProcessor qp = new QueryProcessor(QUERY, CONTEXT);

    // -------------------------------------------------------------------------
    // Store the pointer to the result in an iterator:
    final Iter iter = qp.iter();
    Item item;

    // -------------------------------------------------------------------------
    // Iterate through all items and
    // serialze their contents to System.out
    while(null != (item = iter.next())) {
      item.serialize(xmlSer);
    }
  }

  /**
   * This method uses the QueryProcessor.
   * @throws QueryException on Query error.
   * @throws IOException on Serializer error.
   */
  private void resultInstance() throws QueryException, IOException {
    System.out.println("\n=== Serializing a complete result instance.");
    // Creates and executes a query
    QueryProcessor processor = new QueryProcessor(QUERY, CONTEXT);

    // -------------------------------------------------------------------------
    // Executes the query.
    Result result = processor.query();

    // Serializes the result
    result.serialize(xmlSer);

    // -------------------------------------------------------------------------
    // Closes the query processor
    processor.close();

  }

  /**
   * Use XQuery update to .... *TODO* [MSe]
   * @throws BaseXException in case contains errors.
   */
  private void updateExample() throws BaseXException {

    System.out.print("\n\n=== Updating the instance.");
    new XQuery(UPDATE).exec(CONTEXT, System.out);

    // -------------------------------------------------------------------------
    // Show the newly inserted node.
    System.out.println("\n=>> Update result:.");
    new XQuery(QUERY).exec(CONTEXT, System.out);
  }


  

}
