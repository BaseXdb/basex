package org.basex.test.examples;

import org.basex.core.*;
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

    // create query instance
    QueryProcessor xquery = new XQueryProcessor(QUERY);
    // execute query; no initial context set is specified (null)
    Result result = xquery.query(null);
    // print output
    result.serialize(new XMLSerializer(out));

    // close output stream
    out.println();
    out.close();
  }
}
