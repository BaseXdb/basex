package org.basex.examples.query;

import java.io.IOException;
import org.basex.core.Context;
import org.basex.data.Result;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;

/**
 * This example demonstrates how items can be bound to variables with
 * the XQuery processor.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class QueryBindExample {
  /** Private constructor. */
  private QueryBindExample() { }

  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws IOException if an error occurs while serializing the results
   * @throws QueryException if an error occurs while evaluating the query
   */
  public static void main(final String[] args)
      throws IOException, QueryException {

    /** Database context. */
    Context context = new Context();

    System.out.println("=== QueryBindExample ===");

    // ------------------------------------------------------------------------
    // Evaluate the specified XQuery
    String query = "declare variable $var external; $var";

    // ------------------------------------------------------------------------
    // Create a query processor
    QueryProcessor processor = new QueryProcessor(query, context);

    // ------------------------------------------------------------------------
    // Create the item to be bound
    String string = "Hello World!\n";

    // ------------------------------------------------------------------------
    // Binds a variable to the global context
    processor.bind("var", string);

    // ------------------------------------------------------------------------
    // Execute the query
    Result result = processor.execute();

    System.out.println("\n* Execute query:");

    // ------------------------------------------------------------------------
    // Serialize all results to OUT, using the specified serializer
    result.serialize(processor.getSerializer(System.out));

    // ------------------------------------------------------------------------
    // Close the query processor
    processor.close();

    // ------------------------------------------------------------------------
    // Close the database context
    context.close();
  }
}
