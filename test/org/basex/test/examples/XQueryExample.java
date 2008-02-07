package org.basex.test.examples;

import org.basex.core.Commands;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.Proc;
import org.basex.data.PrintSerializer;
import org.basex.data.Result;
import org.basex.io.ConsoleOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.xquery.XQueryProcessor;

/**
 * This class serves an example for executing XQuery requests.
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

    // read properties (database path, language, ...)
    Prop.read();
    // create a new database context
    Context context = new Context();
    // Create a BaseX process
    Proc proc = Proc.get(context, Commands.XQUERY, QUERY);
    
    // launch process
    if(proc.execute()) {
      // successful execution: print result
      proc.output(out);
    } else {
      // execution failed: print result
      proc.info(out);
    }
    out.flush();
    System.out.println();
    
    // SECOND EXAMPLE:
    System.out.println("Second example:");

    // Execute XQuery request
    try {
      // create query instance
      QueryProcessor xquery = new XQueryProcessor(QUERY);
      // execute query; no initial context set is specified (null)
      Result result = xquery.query(null);
      // print output
      result.serialize(new PrintSerializer(out));
      out.println();
    } catch(QueryException e) {
      // dump stack trace
      e.printStackTrace();
    }

    // close output stream
    out.close();
  }
}
