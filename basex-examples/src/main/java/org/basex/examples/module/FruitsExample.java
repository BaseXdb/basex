package org.basex.examples.module;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;

/**
 * This example demonstrates how Java classes can be imported as XQuery modules.
 *
 * @author BaseX Team 2005-14, BSD License
 */
public final class FruitsExample {
  /**
   * Runs the example code.
   *
   * @param args (ignored) command-line arguments
   * @throws QueryException if an error occurs while evaluating the query
   */
  public static void main(final String[] args) throws QueryException {
    // Database context.
    Context context = new Context();

    System.out.println("=== FruitsExample ===");

    // ------------------------------------------------------------------------
    // Specify query to be executed
    String query =
        "import module namespace fruits = " +
        "'java:org.basex.examples.module.FruitsModule'; \n" +
        "element convenient {\n" +
        "  for $i in 1 to 4\n" +
        "  return fruits:convenient(xs:int($i))\n" +
        "},\n" +
        "element fast {\n" +
        "  for $i in 1 to 4\n" +
        "  return fruits:fast($i)\n" +
        "},\n" +
        "element user { fruits:user() }";

    System.out.println("\n* Query:");
    System.out.println(query);

    // ------------------------------------------------------------------------
    // Create a query processor
    QueryProcessor processor = new QueryProcessor(query, context);

    // ------------------------------------------------------------------------
    // Execute the query
    Result result = processor.execute();

    System.out.println("\n* Result:");

    // ------------------------------------------------------------------------
    // Print result as string
    System.out.println(result);

    // ------------------------------------------------------------------------
    // Close the query processor
    processor.close();

    // ------------------------------------------------------------------------
    // Close the database context
    context.close();
  }
}
