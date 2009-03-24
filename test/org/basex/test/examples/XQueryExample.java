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

    out.println("=== First example: Creating a result instance");

    // Creates a result serializer
    XMLSerializer serializer = new XMLSerializer(out);

    // Creates a query instance
    QueryProcessor processor = new QueryProcessor(QUERY);

    // Executes the query.
    Result result = processor.query();

    // Serializes the result
    result.serialize(serializer);

    // Closes the query processor
    processor.close();

    
    out.println("\n=== Second example: Iterating through all results");

    // Creates a query instance
    processor = new QueryProcessor(QUERY);

    // Returns a query iterator
    Iter iterator = processor.iter();

    // Uses an iterator to serialize the result
    for(Item item : iterator) item.serialize(serializer);

    // Closes the query processor and the output stream
    processor.close();

    
    out.println("\n=== Third example: Using the BaseX command");

    // Creates a database context
    Context context = new Context();

    // Creates and executes a query
    new XQuery(QUERY).execute(context, out);
    
    // Closes the database
    context.close();

    // Closes the output stream
    out.close();
  }
}

