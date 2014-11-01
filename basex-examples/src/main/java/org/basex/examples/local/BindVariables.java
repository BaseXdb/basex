package org.basex.examples.local;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;

/**
 * This example demonstrates how items can be bound to variables with
 * the XQuery processor.
 *
 * @author BaseX Team 2005-14, BSD License
 */
public final class BindVariables {
  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws QueryException if an error occurs while evaluating the query
   */
  public static void main(final String[] args) throws QueryException {
    // Database context.
    Context context = new Context();

    System.out.println("=== BindVariable ===");

    // Specify query to be executed
    String query =
      "declare variable $var1 as xs:string external;\n" +
      "declare variable $var2 external;\n" +
      "($var1, $var2)";

    // Show query
    System.out.println("\n* Query:");
    System.out.println(query);

    // Create a query processor
    try(QueryProcessor proc = new QueryProcessor(query, context)) {

      // Define the items to be bound
      String string = "Hello World!\n";
      String number = "123";

      // Bind the variables
      proc.bind("var1", string);
      proc.bind("var2", number, "xs:integer");

      // Execute the query
      Result result = proc.execute();

      System.out.println("\n* Result:");

      // ------------------------------------------------------------------------
      // Print result as string
      System.out.println(result);
    }

    // ------------------------------------------------------------------------
    // Close the database context
    context.close();
  }
}
