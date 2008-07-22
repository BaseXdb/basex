package org.basex.test.examples;

import org.basex.core.Context;
import org.basex.core.proc.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.xquery.*;

/**
 * This class serves as an example for executing XQuery requests.
 * [...]
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
public final class XQueryExample {
  /** Sample query. */
  private static final String QUERY = "<xml>This is a test</xml>/text()";

  /** Private constructor. */
  private XQueryExample() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // FIRST EXAMPLE:
    System.out.println("First example:");

    // create standard output stream
    ConsoleOutput out = new ConsoleOutput(System.out);

    // Creates a new database context
    Context context = new Context();

    // Creates and executes a query
    new XQuery(QUERY).execute(context, out);
    
    // Closes the output stream
    out.println();
    out.close();

    
    // SECOND EXAMPLE, directly accessing the query processor:
    System.out.println("Second example:");

    // Creates a query instance
    QueryProcessor xquery = new XQueryProcessor(QUERY);

    // Executes the query; no initial context set is specified (null)
    Result result = xquery.query(null);

    // Serializes the result
    result.serialize(new XMLSerializer(out));

    // Closes the output stream
    out.println();
    out.close();
  }
}
