package org.basex.examples.query;

import java.io.IOException;
import java.io.OutputStream;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.XQuery;
import org.basex.data.Result;
import org.basex.data.XMLSerializer;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

/**
 * This example demonstrates three variants how XQuery expressions can be
 * evaluated with BaseX.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class QueryExample {
  /** Database context. */
  static final Context CONTEXT = new Context();
  /** Output stream. */
  static final OutputStream OUT = System.out;

  /** Default constructor. */
  protected QueryExample() { }

  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws IOException if an error occurs while serializing the results
   * @throws QueryException if an error occurs while evaluating the query
   * @throws BaseXException if a database command fails
   */
  public static void main(final String[] args)
      throws IOException, QueryException, BaseXException {

    System.out.println("=== QueryExample ===");

    // ------------------------------------------------------------------------
    // Evaluate the specified XQuery
    final String query = "for $x in doc('etc/xml/input.xml')//li return $x";

    // ------------------------------------------------------------------------
    // Process the query by using the database command
    System.out.println("\n* Query by using the database command:");

    query(query);

    // ------------------------------------------------------------------------
    // Directly use the query processor
    System.out.println("\n\n* Query by directly using the query processor:");

    process(query);

    // ------------------------------------------------------------------------
    // Iterate through all query results
    System.out.println("\n\n* Query by iterating through all query results:");

    iterate(query);

    // Uncomment this line to see how erroneous queries are handled
    // iterate("for error s$x in . return $x");

    // ------------------------------------------------------------------------
    // Flush output.
    System.out.println();
  }

  /**
   * This method evaluates a query by using the database command.
   * The results are automatically serialized and printed to a specified
   * output stream.
   *
   * @param query query to be evaluated
   * @throws BaseXException if a database command fails
   */
  static void query(final String query) throws BaseXException {
    new XQuery(query).execute(CONTEXT, OUT);
  }

  /**
   * This method uses the {@link QueryProcessor} to evaluate a query.
   * The resulting items are passed on to an {@link XMLSerializer} instance.
   *
   * @param query query to be evaluated
   * @throws QueryException if an error occurs while evaluating the query
   * @throws IOException if an error occurs while serializing the results
   */
  static void process(final String query) throws QueryException, IOException {
    // ------------------------------------------------------------------------
    // Create a query processor
    final QueryProcessor processor = new QueryProcessor(query, CONTEXT);

    // ------------------------------------------------------------------------
    // Execute the query
    final Result result = processor.query();

    // ------------------------------------------------------------------------
    // Serialize all results to OUT, using the specified serializer
    result.serialize(new XMLSerializer(OUT));

    // ------------------------------------------------------------------------
    // Close the query processor
    processor.close();
  }

  /**
   * This method uses the {@link QueryProcessor} to evaluate a query.
   * The results are iterated one by one and passed on to an
   * {@link XMLSerializer} instance. This variant is especially
   * efficient if large result sets are expected.
   *
   * @param query query to be evaluated
   * @throws QueryException if an error occurs while evaluating the query
   * @throws IOException if an error occurs while serializing the results
   */
  static void iterate(final String query) throws QueryException, IOException {
    // ------------------------------------------------------------------------
    // Create a query processor
    final QueryProcessor processor = new QueryProcessor(query, CONTEXT);

    // ------------------------------------------------------------------------
    // Store the pointer to the result in an iterator:
    final Iter iter = processor.iter();

    // ------------------------------------------------------------------------
    // Create an XML serializer
    final XMLSerializer serializer = new XMLSerializer(OUT);

    // ------------------------------------------------------------------------
    // Iterate through all items and serialize contents
    Item item;
    while((item = iter.next()) != null) {
      item.serialize(serializer);
    }

    // ------------------------------------------------------------------------
    // Close the serializer
    serializer.close();

    // ------------------------------------------------------------------------
    // Close the query processor
    processor.close();
  }
}
