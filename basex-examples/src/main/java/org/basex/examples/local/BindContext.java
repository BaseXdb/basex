package org.basex.examples.local;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.*;

/**
 * This example demonstrates how items can be bound as context item of
 * the XQuery processor.
 *
 * @author BaseX Team 2005-21, BSD License
 */
public final class BindContext {
  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws QueryException if an error occurs while evaluating the query
   */
  public static void main(final String... args) throws QueryException {
    // Database context.
    Context context = new Context();

    System.out.println("=== BindContext ===");

    // Specify query to be executed
    String query = "declare context item external; .";

    // Show query
    System.out.println("\n* Query:");
    System.out.println(query);

    // Create a query processor
    try(QueryProcessor qp = new QueryProcessor(query, context)) {

      // Define the items to be bound
      String item = "Hello World!\n";

      // Bind the variables
      qp.context(item);

      // Execute the query
      Value result = qp.value();

      // Print result as string
      System.out.println("\n* Result:");
      System.out.println(result);
    }

    // Close the database context
    context.close();
  }
}
