package org.basex.test.examples;

import org.basex.core.*;
import org.basex.core.proc.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.xpath.*;

/**
 * This class serves as an example for executing XPath requests.
 * [...]
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
public final class XPathExample {
  /** Input XML file. */
  private static final String XMLFILE = "input.xml";
  /** Name of the resulting database. */
  private static final String DBNAME = "input";
  /** Sample query. */
  private static final String QUERY = "//li";
  /** Result file. */
  private static final String RESULT = "result.txt";

  /** Private constructor. */
  private XPathExample() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // First example, creating a database and
    // writing the query result to RESULT:
    System.out.println("First example, writing result to " + RESULT + ":");

    // create a new database context
    Context context = new Context();
    // execute the specified command
    Proc.execute(context, Commands.CREATEDB, XMLFILE);
    // writing result as well-formed XML
    Proc.execute(context, Commands.SET, "xmloutput on");

    // create standard output stream
    PrintOutput file = new PrintOutput(RESULT);

    // create a process for the XPath command 
    Proc proc = Proc.get(context, Commands.XPATH, QUERY);

    // launch process
    if(proc.execute()) {
      // successful execution: print result
      proc.output(file);
    } else {
      // execution failed: print result
      proc.info(file);
    }
    // close output stream
    file.close();
    System.out.println();
    
    // Second example, creating a database and
    // writing the query result to the the standard output:
    System.out.println("Second example, writing result to standard output:");

    // Execute XPath request
    // create new database; "input" = database name, "input.xml" = source doc.
    Data data = Create.xml(new IO(XMLFILE), DBNAME);

    // create query instance
    QueryProcessor xpath = new XPathProcessor(QUERY);
    // create context set, referring to the root node (0)
    Nodes nodes = new Nodes(0, data);
    // execute query
    Result result = xpath.query(nodes);

    // print output to file
    ConsoleOutput console = new ConsoleOutput(System.out);
    result.serialize(new XMLSerializer(console));
    console.flush();
  }
}
