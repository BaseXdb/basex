package org.basex.test.examples;

import org.basex.core.Context;
import org.basex.core.proc.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

/**
 * This class presents three alternatives to process XQuery requests with BaseX.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author BaseX Team
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
    // Creates a standard output stream
    ConsoleOutput out = new ConsoleOutput(System.out);

    out.println("=== First example ===");

    // Creates a new database context
    Context context = new Context();

    // Creates and executes a query
    new XQuery(QUERY).execute(context, out);

    out.println();
    out.println("=== Second example ====");

    // Creates a result serializer
    XMLSerializer xml = new XMLSerializer(out);

    // Creates a query instance
    QueryProcessor proc1 = new QueryProcessor(QUERY);

    // Executes the query.
    Result result = proc1.query();

    // Serializes the result
    result.serialize(xml);

    out.println();
    out.println("=== Third example ===");

    // Creates a query instance
    QueryProcessor proc2 = new QueryProcessor(QUERY);

    // Returns a query iterator
    Iter iter = proc2.iter();

    // Iterates and serializes the result
    for(final Item item : iter) {
      item.serialize(xml);
    }

    // Closes the output stream
    out.println();
    out.close();
  }
}
