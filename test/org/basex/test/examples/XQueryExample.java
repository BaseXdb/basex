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
  private static final String QUERY = "<xml>1 + 2 = { 1+2 }</xml>/text()";

  /** Private constructor. */
  private XQueryExample() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // Creates a database context
    Context context = new Context();

    // Creates a print output
    PrintOutput out = new PrintOutput(System.out);
    // Alternative: write results to disk
    // PrintOutput out = new PrintOutput("result.txt");

    out.println("\n=== First version: Using the database command");

    // Creates and executes a query
    new XQuery(QUERY).exec(context, out);

    out.println("\n=== Second version: Iterating through all results");

    // Creates a query instance
    QueryProcessor processor = new QueryProcessor(QUERY, context);
    // Returns a query iterator
    Iter iter = processor.iter();
    // Creates a result serializer
    XMLSerializer xml = new XMLSerializer(out);
    // Uses an iterator to serialize the result
    while(true) {
      Item item = iter.next();
      if(item == null) break;
      item.serialize(xml);
    }
    // Closes the serializer
    xml.close();
    // Closes the query processor
    processor.close();

    out.println("=== Third version: Processing a result instance");

    // Creates a query instance
    processor = new QueryProcessor(QUERY, context);
    // Executes the query.
    Result result = processor.query();
    // Creates a result serializer
    xml = new XMLSerializer(out);
    // Serializes the result
    result.serialize(xml);
    // Closes the serializer
    xml.close();
    // Closes the query processor
    processor.close();

    // Closes the output stream
    out.close();
    // Closes the database
    context.close();
  }
}
